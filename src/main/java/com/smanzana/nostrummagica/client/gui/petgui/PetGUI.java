package com.smanzana.nostrummagica.client.gui.petgui;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.AutoGuiContainer;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.entity.IRerollablePet;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.PetGUIControlMessage;
import com.smanzana.nostrummagica.network.messages.PetGUISyncMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiInventory;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A nice wrapped up pet gui.
 * 
 * Doesn't do a lot on its own. Instead, things can build it up using pet sheets
 * 
 * @author Skyler
 */
public class PetGUI {
	
	// Need a static registry of open containers for dispatching messages on the server
	private static Map<Integer, PetContainer<?>> containers = new HashMap<>();
	
	private static int lastKey = 0;
	
	private static int register(PetContainer<?> container) {
		if (container.player.world.isRemote) {
			throw new IllegalArgumentException("Can't register on the client!");
		}
		
		int id = lastKey++;
		containers.put(id, container);
		return id;
	}
	
	private static void revoke(int id) {
		containers.remove(id);
	}
	
	public static void updateServerContainer(int id, CompoundNBT nbt) {
		PetContainer<?> container = containers.get(id);
		if (container != null) {
			container.handle(nbt);
		}
	}
	
	private static PetContainer<?> clientContainer = null;
	
	public static void updateClientContainer(CompoundNBT nbt) {
		if (clientContainer != null) {
			clientContainer.handle(nbt);
		}
	}

	public static class PetContainer<T extends IEntityPet> extends Container {

		private PlayerEntity player;
		
		private T pet;
		
		private LivingEntity livingPet;
		
		private int currentSheet;
		
		protected List<IPetGUISheet<T>> sheetsAllInternal;
		
		protected int id;
		
		private int guiOffsetX;
		private int guiOffsetY;
		
		@SafeVarargs
		public PetContainer(T pet, PlayerEntity player, IPetGUISheet<T> ... sheets) {
			this.pet = pet;
			this.livingPet= (LivingEntity) pet;
			this.player = player;
			this.currentSheet = 0;
			this.sheetsAllInternal = Lists.newArrayList(sheets);
			
			if (!livingPet.world.isRemote) {
				this.id = PetGUI.register(this);				
			} else {
				PetGUI.clientContainer = this;
			}
		}
		
		public void overrideID(int id) {
			if (!this.player.world.isRemote) {
				throw new IllegalArgumentException("Can't reset id on the server!");
			}
//			revoke(id);
//			registerAt(this, id);
			this.id = id;
		}
		
		public void setGUIOffets(int x, int y) {
			this.guiOffsetX = x;
			this.guiOffsetY = y;
		}
		
		@Override
		public boolean canInteractWith(PlayerEntity playerIn) {
			if (pet == null) {
				// Pet hasn't been synced yet
				return false;
			}
			return playerIn.equals(pet.getOwner());
		}

		// Caution: This assumes only one player has these open!
		@Override
		public void onContainerClosed(PlayerEntity playerIn) {
			if (this.getCurrentSheet() != null) {
				this.getCurrentSheet().hideSheet(pet, player, this);
			}
			revoke(this.id);
		}
		
		@Override
		public @Nonnull ItemStack transferStackInSlot(PlayerEntity playerIn, int fromSlot) {
			return ItemStack.EMPTY;
		}
		
		/**
		 * Returns a list of sheets this container has.
		 * This is a collection that's filtered down to what should be shown
		 * @return
		 */
		protected List<IPetGUISheet<T>> getSheets() {
			final PetContainer<T> container = this;
			return sheetsAllInternal.parallelStream().filter((sheet) -> {
				return sheet.shouldShow(container.pet, container);
			}).collect(Collectors.toList());
		}
		
		public IPetGUISheet<T> getCurrentSheet() {
			return getSheets().get(currentSheet);
		}
		
		public void setSheet(int index) {
			if (this.currentSheet < this.getSheetCount()) {
				// If we changed the number of sheets, we may have an invalid one to close. So just don't close it.
				this.getCurrentSheet().hideSheet(pet, player, this);
			}
			this.currentSheet = Math.min(Math.max(0, index), getSheets().size() - 1);
			this.getCurrentSheet().showSheet(pet, player, this, GUI_SHEET_WIDTH, GUI_SHEET_HEIGHT, guiOffsetX, guiOffsetY);
		}
		
		public int getSheetIndex() {
			return this.currentSheet;
		}
		
		public void clearSlots() {
			this.inventorySlots.clear();
			this.inventoryItemStacks.clear();
		}
		
		public void dropContainerInventory(IInventory inv) {
			this.clearContainer(player, player.world, inv);
		}
		
		public void addSheetSlot(Slot slot) {
			this.addSlotToContainer(slot);
		}
		
		public int getContainerID() {
			return this.id;
		}
		
		public int getSheetCount() {
			return this.getSheets().size();
		}
		
		protected boolean supportsReroll() {
			return pet != null && pet instanceof IRerollablePet;
		}
		
		// Handle a message sent from the client.
		// Could be a button click to change sheets, some other control message,
		// or a message for updating a sheet's contents.
		protected void handle(CompoundNBT nbt) {
			PetContainerMessageType type = NetworkHelper.GetType(nbt);
			
			if (type == null) {
				return;
			}
			
			switch (type) {
			case SET_SHEET:
				int index = NetworkHelper.GetSendSheetIndex(nbt);
				this.setSheet(index);
				break;
			case SHEET_DATA:
				this.getCurrentSheet().handleMessage(NetworkHelper.GetSendSheetData(nbt));
				break;
			case REROLL:
				if (pet != null
					&& supportsReroll()
					&& pet.getOwner() instanceof PlayerEntity
					&& ((PlayerEntity) pet.getOwner()).isCreative()) {
					// Reset container sheet. The client will send this as well later.
					this.setSheet(0);
					((IRerollablePet) pet).rerollStats();
				}
				break;
			}
		}
		
		// Sheets can call on their handle to the container to sync with the server.
		// This call doesn't check if it's on the server. It'll just 'send' it. Know what you're doing!
		public void sendSheetMessageToServer(CompoundNBT data) {
			NetworkHelper.ClientSendSheetData(id, data);
		}
		
		public void sendSheetMessageToClient(CompoundNBT data) {
			NetworkHelper.ServerSendSheetData((ServerPlayerEntity) this.player, data);
		}
		
	}
	
	public static int GUI_SHEET_WIDTH = 246;
	public static int GUI_SHEET_HEIGHT = 191;
	public static int GUI_TEX_WIDTH = 256;
	public static int GUI_TEX_HEIGHT = 256;
	public static int GUI_TEX_CELL_HOFFSET = 0;
	public static int GUI_TEX_CELL_VOFFSET = 202;
	public static int GUI_TEX_TOGGLE_HOFFSET = 0;
	public static int GUI_TEX_TOGGLE_VOFFSET = 220;
	public static int GUI_TEX_TOGGLE_LENGTH = 10;
	
	
	@OnlyIn(Dist.CLIENT)
	public static class PetGUIContainer<T extends IEntityPet> extends AutoGuiContainer {
		
		public static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/tamed_pet_gui.png");
		
		private static int GUI_LENGTH_PREVIEW = 48;
		private static int GUI_INFO_HOFFSET = 12;
		private static int GUI_INFO_VOFFSET = GUI_LENGTH_PREVIEW + 10;
		private static int GUI_SHEET_NHOFFSET = 10;
		private static int GUI_SHEET_MARGIN = 5;
		private static int GUI_SHEET_BUTTON_WIDTH = 50;
		private static int GUI_SHEET_BUTTON_HEIGHT = 20;
		private static int GUI_SHEET_BUTTON_VOFFSET = 5;
		private static int GUI_SHEET_VOFFSET = GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT + GUI_SHEET_BUTTON_VOFFSET;
		
		//private static int GUI_OPEN_ANIM_TIME = 20 * 1;
		
		private PetContainer<T> container;
		private final PetGUIStatAdapter<T> adapter;
		
		//private int openTicks;
		
		public PetGUIContainer(PetContainer<T> container, PetGUIStatAdapter<T> adapter) {
			super(container);
			this.container = container;
			this.adapter = adapter;
			//this.openTicks = 0;
		}
		
		@Override
		public void init() {
			this.xSize = this.width;
			this.ySize = this.height;
			super.init();
			
			final int GUI_SHEET_HOFFSET = this.width - (GUI_SHEET_WIDTH + GUI_SHEET_NHOFFSET);
			this.container.setGUIOffets(GUI_SHEET_HOFFSET, GUI_SHEET_VOFFSET);
		}
		
//		@Override
//		public void tick() {
//			super.updateScreen();
//			
//			this.openTicks++;
//		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			GlStateManager.color4f(1.0F,  1.0F, 1.0F, 1.0F);
			
			final int GUI_SHEET_HOFFSET = this.width - (GUI_SHEET_WIDTH + GUI_SHEET_NHOFFSET);
			final int GUI_SHEET_BUTTON_HOFFSET = GUI_SHEET_HOFFSET;
			
			if (this.container.pet == null) {
				this.drawCenteredString(font, "Waiting for server...", this.width / 2, this.height / 2, 0XFFAAAAAA);
				return;
			}
			
			// Draw top-left preview
			{
				RenderFuncs.drawRect(0, 0, GUI_LENGTH_PREVIEW, GUI_LENGTH_PREVIEW, 0xFF283D2A);
				
				int xPosition = GUI_LENGTH_PREVIEW / 2;
				int yPosition = GUI_LENGTH_PREVIEW / 2;
				RenderHelper.disableStandardItemLighting();
				GlStateManager.color4f(1f, 1f, 1f, 1f);
				GuiInventory.drawEntityOnScreen(
						xPosition,
						(int) (GUI_LENGTH_PREVIEW * .75f),
						(int) (GUI_LENGTH_PREVIEW * .2),
						(float) (xPosition) - mouseX,
						(float) (-yPosition) - mouseY,
						(LivingEntity) container.pet);
			}
			
			// Move everything forward ahead of the drawn entity
			// Can't just move entity back cause there's a GRAY plane drawn at just below 0 Z
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0, 0, 51);
			
			// Black background (not overlapping preview)
			{
				RenderFuncs.drawRect(0, GUI_LENGTH_PREVIEW, width, height, 0xFF000000);
				RenderFuncs.drawRect(GUI_LENGTH_PREVIEW, 0, width, GUI_LENGTH_PREVIEW, 0xFF000000);
			}
			
			// Draw stats and stuff
			{
				//RenderFuncs.drawRect(GUI_INFO_HOFFSET, GUI_INFO_VOFFSET, GUI_SHEET_HOFFSET - 10, height - 10, 0xFF00FFFF);
				
				final int w = (GUI_SHEET_HOFFSET - GUI_SHEET_MARGIN) - (GUI_INFO_HOFFSET * 2);
				int x = GUI_INFO_HOFFSET;
				int y = GUI_INFO_VOFFSET;
				//final int w = 125;
				final int h = 14;
				final int centerX = GUI_SHEET_HOFFSET / 2;
				
				// Health
				{
					this.drawCenteredString(this.font, ChatFormatting.BOLD + adapter.getHealthLabel(container.pet), centerX, y, 0xFFFFFFFF);
					y += font.FONT_HEIGHT + 5;
					RenderFuncs.drawRect(x, y, x + w, y + h, 0xFFD0D0D0);
					RenderFuncs.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF201010);
					
					int prog = (int) ((float) (w - 2) * (adapter.getHealth(container.pet) / adapter.getMaxHealth(container.pet)));
					RenderFuncs.drawRect(x + 1, y + 1, x + 1 + prog, y + h - 1, 0xFFA02020);
					
					this.drawCenteredString(font,
							String.format("%d / %d", (int) adapter.getHealth(container.pet), (int) adapter.getMaxHealth(container.pet)),
							centerX,
							y + (h / 2) - (font.FONT_HEIGHT / 2),
							0xFFC0C0C0);
					
					y += h + 10;
				}
				
				// Secondary
				if (adapter.supportsSecondaryAmt(container.pet) && adapter.getMaxSecondaryAmt(container.pet) > 0) {
					this.drawCenteredString(this.font, ChatFormatting.BOLD + adapter.getSecondaryLabel(container.pet), centerX, y, 0xFFFFFFFF);
					y += font.FONT_HEIGHT + 5;
					RenderFuncs.drawRect(x, y, x + w, y + h, 0xFFD0D0D0);
					RenderFuncs.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF101020);
					
					int prog = (int) ((float) (w - 2) * (adapter.getSecondaryAmt(container.pet) / adapter.getMaxSecondaryAmt(container.pet)));
					RenderFuncs.drawRect(x + 1, y + 1, x + 1 + prog, y + h - 1, 0xFF2020A0);
					
					this.drawCenteredString(font,
							String.format("%d / %d", (int) adapter.getSecondaryAmt(container.pet), (int) adapter.getMaxSecondaryAmt(container.pet)),
							centerX,
							y + (h / 2) - (font.FONT_HEIGHT / 2),
							0xFFC0C0C0);
					
					y += h + 10;
				}
				
				// Tertiary
				if (adapter.supportsTertiaryAmt(container.pet) && adapter.getMaxTertiaryAmt(container.pet) > 0) {
					final float cur = adapter.getTertiaryAmt(container.pet);
					final float max = adapter.getMaxTertiaryAmt(container.pet);
					
					this.drawCenteredString(this.font, ChatFormatting.BOLD + adapter.getTertiaryLabel(container.pet), centerX, y, 0xFFFFFFFF);
					y += font.FONT_HEIGHT + 5;
					RenderFuncs.drawRect(x, y, x + w, y + h, 0xFFD0D0D0);
					RenderFuncs.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF201020);
					
					int prog = (int) ((float) (w - 2) * (cur/max));
					RenderFuncs.drawRect(x + 1, y + 1, x + 1 + prog, y + h - 1, 0xFFA020A0);
					
					this.drawCenteredString(font,
							String.format("%.2f%%", (cur/max) * 100f),
							centerX,
							y + (h / 2) - (font.FONT_HEIGHT / 2),
							cur >= max ? 0xFFC0FFC0 : 0xFFC0C0C0);
					
//					if (container.pet.isSoulBound()) {
//						this.drawCenteredString(font,
//								"Soulbound",
//								centerX,
//								y + (h / 2) - (font.FONT_HEIGHT / 2),
//								0xFF40FF40);
//					} else {
//						this.drawCenteredString(font,
//								String.format("%.2f%%", bond * 100f),
//								centerX,
//								y + (h / 2) - (font.FONT_HEIGHT / 2),
//								bond == 1f ? 0xFFC0FFC0 : 0xFFC0C0C0);
//					}
					
					
					y += h + 10;
				}
				
				// XP
				if (adapter.supportsQuaternaryAmt(container.pet) && adapter.getMaxQuaternaryAmt(container.pet) > 0) {
					final float cur = adapter.getQuaternaryAmt(container.pet);
					final float max = adapter.getMaxQuaternaryAmt(container.pet);
					this.drawCenteredString(this.font, ChatFormatting.BOLD + adapter.getQuaternaryLabel(container.pet), centerX, y, 0xFFFFFFFF);
					y += font.FONT_HEIGHT + 5;
					RenderFuncs.drawRect(x, y, x + w, y + h, 0xFFD0D0D0);
					RenderFuncs.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF102010);
					
					int prog = (int) ((float) (w - 2) * (cur / max));
					RenderFuncs.drawRect(x + 1, y + 1, x + 1 + prog, y + h - 1, 0xFF20A020);
					
					this.drawCenteredString(font,
							String.format("%d / %d", (int) cur, (int) max),
							centerX,
							y + (h / 2) - (font.FONT_HEIGHT / 2),
							0xFFC0C0C0);
					
					y += h + 10;
				}
			}
			
			if (container.getSheets().size() > 0) {
				int x = GUI_SHEET_BUTTON_HOFFSET;
				
				for (IPetGUISheet<T> sheet : container.getSheets()) {
					RenderFuncs.drawRect(x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0xFFFFFFFF);
					RenderFuncs.drawRect(x + 1, GUI_SHEET_BUTTON_VOFFSET + 1, x + GUI_SHEET_BUTTON_WIDTH - 1, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT - 1, 0xFF202020);
					
					if (sheet == container.getCurrentSheet()) {
						RenderFuncs.drawRect(x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0x40FFFFFF);
					}
					
					if (mouseX >= x && mouseX <= x + GUI_SHEET_BUTTON_WIDTH && mouseY >= GUI_SHEET_BUTTON_VOFFSET && mouseY <= GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT) {
						RenderFuncs.drawRect(x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0x40FFFFFF);
					}
					
					String text = sheet.getButtonText();
					int strLen = font.getStringWidth(text);
					int strHeight = font.FONT_HEIGHT;
					font.drawString(text, x + (GUI_SHEET_BUTTON_WIDTH / 2) - (strLen / 2), GUI_SHEET_BUTTON_VOFFSET + (GUI_SHEET_BUTTON_HEIGHT / 2) - (strHeight / 2), 0xFFFFFFFF);
					x += GUI_SHEET_BUTTON_WIDTH;
				}
				
				if (container.supportsReroll() && NostrumMagica.proxy.getPlayer().isCreative()) {
					RenderFuncs.drawRect(x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0xFFFFDDFF);
					RenderFuncs.drawRect(x + 1, GUI_SHEET_BUTTON_VOFFSET + 1, x + GUI_SHEET_BUTTON_WIDTH - 1, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT - 1, 0xFF702070);
					
					if (mouseX >= x && mouseX <= x + GUI_SHEET_BUTTON_WIDTH && mouseY >= GUI_SHEET_BUTTON_VOFFSET && mouseY <= GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT) {
						RenderFuncs.drawRect(x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0x40FFFFFF);
					}
					
					String text = "Reroll";
					int strLen = font.getStringWidth(text);
					int strHeight = font.FONT_HEIGHT;
					font.drawString(text, x + (GUI_SHEET_BUTTON_WIDTH / 2) - (strLen / 2), GUI_SHEET_BUTTON_VOFFSET + (GUI_SHEET_BUTTON_HEIGHT / 2) - (strHeight / 2), 0xFFFFFFFF);
					x += GUI_SHEET_BUTTON_WIDTH;
				}
			}
			
			mc.getTextureManager().bindTexture(TEXT);
			
			// Draw sheet
			IPetGUISheet<T> sheet = container.getCurrentSheet();
			if (sheet != null) {
				GlStateManager.pushMatrix();
				GlStateManager.translatef(GUI_SHEET_HOFFSET, GUI_SHEET_VOFFSET, 0);
				
				GlStateManager.enableAlphaTest();
				GlStateManager.enableBlend();
				GlStateManager.color4f(1f, 1f, 1f, 1f);
				drawModalRectWithCustomSizedTexture(-GUI_SHEET_MARGIN, -GUI_SHEET_MARGIN, 0, 0, GUI_SHEET_WIDTH + (GUI_SHEET_MARGIN * 2), GUI_SHEET_HEIGHT + (GUI_SHEET_MARGIN * 2), GUI_TEX_WIDTH, GUI_TEX_HEIGHT);
				
				sheet.draw(Minecraft.getInstance(), partialTicks, GUI_SHEET_WIDTH, GUI_SHEET_HEIGHT,
						mouseX - GUI_SHEET_HOFFSET, mouseY - GUI_SHEET_VOFFSET);
				GlStateManager.popMatrix();
			}
			
			GlStateManager.popMatrix();
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);
			
			final int GUI_SHEET_HOFFSET = this.width - (GUI_SHEET_WIDTH + GUI_SHEET_NHOFFSET);
			
			IPetGUISheet<T> sheet = container.getCurrentSheet();
			if (sheet != null) {
				GlStateManager.pushMatrix();
				GlStateManager.translatef(GUI_SHEET_HOFFSET, GUI_SHEET_VOFFSET, 0);
				
				GlStateManager.enableAlphaTest();
				GlStateManager.enableBlend();
				
				sheet.overlay(Minecraft.getInstance(), 0f, GUI_SHEET_WIDTH, GUI_SHEET_HEIGHT,
						mouseX - GUI_SHEET_HOFFSET, mouseY - GUI_SHEET_VOFFSET);
				GlStateManager.popMatrix();
			}
		}
		
		@Override
		protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
			
			if (!container.canInteractWith(NostrumMagica.proxy.getPlayer())) {
				return;
			}
			
			// Only allow custom clicking s tuff if there isn't an item being held
			if (NostrumMagica.proxy.getPlayer().inventory.getItemStack().isEmpty()) {
			
				final int GUI_SHEET_HOFFSET = this.width - (GUI_SHEET_WIDTH + GUI_SHEET_NHOFFSET);
				final int GUI_SHEET_BUTTON_HOFFSET = GUI_SHEET_HOFFSET;
				
				// Sheet button?
				if (mouseX >= GUI_SHEET_BUTTON_HOFFSET && mouseY >= GUI_SHEET_BUTTON_VOFFSET
						&& mouseY <= GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT) {
					int buttonIdx = (mouseX - GUI_SHEET_BUTTON_HOFFSET) / GUI_SHEET_BUTTON_WIDTH;
					if (buttonIdx < container.getSheets().size()) {
						// Clicked a button!
						NostrumMagicaSounds.UI_TICK.play(NostrumMagica.proxy.getPlayer());
						this.container.setSheet(buttonIdx);
						NetworkHelper.ClientSendSheet(container.id, buttonIdx);
						return;
					} else if (container.supportsReroll()
							&& buttonIdx == container.getSheets().size()
							&& NostrumMagica.proxy.getPlayer().isCreative()) {
						NetworkHelper.ClientSendReroll(container.id);
						// Reset sheet index in case reroll removed a tab
						this.container.setSheet(0);
						NetworkHelper.ClientSendSheet(container.id, this.container.currentSheet);
					}
				}
				
				// Clicking on the sheet?
				if (mouseX >= GUI_SHEET_HOFFSET && mouseX <= GUI_SHEET_HOFFSET + GUI_SHEET_WIDTH
						&& mouseY >= GUI_SHEET_VOFFSET && mouseY <= GUI_SHEET_VOFFSET + GUI_SHEET_HEIGHT) {
					IPetGUISheet<T> sheet = container.getCurrentSheet();
					if (sheet != null) {
						sheet.mouseClicked(mouseX - GUI_SHEET_HOFFSET, mouseY - GUI_SHEET_VOFFSET, mouseButton);
					}
					
				}
			}
			
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}
	
	public static interface PetGUIStatAdapter<T> {
		
		// Health/first bar
		default public float getHealth(T pet) { return ((LivingEntity) pet).getHealth(); }
		default public float getMaxHealth(T pet) { return ((LivingEntity) pet).getMaxHealth(); }
		default public String getHealthLabel(T pet) { return "Health"; }
		
		// Second bar (mana?)
		default public boolean supportsSecondaryAmt(T pet) { return true; }
		public float getSecondaryAmt(T pet);
		public float getMaxSecondaryAmt(T pet);
		public String getSecondaryLabel(T pet);
		
		// Third bar (bond?)
		default public boolean supportsTertiaryAmt(T pet) { return true; };
		public float getTertiaryAmt(T pet);
		public float getMaxTertiaryAmt(T pet);
		public String getTertiaryLabel(T pet);
		
		// Fourth bar (xp?)
		default public boolean supportsQuaternaryAmt(T pet) { return true; };
		public float getQuaternaryAmt(T pet);
		public float getMaxQuaternaryAmt(T pet);
		public String getQuaternaryLabel(T pet);
	}
	
	private static enum PetContainerMessageType {
		
		SET_SHEET("_SETSHEET"),
		
		SHEET_DATA("_SHEET_DATA"),
		
		REROLL("_REROLL");
		
		private final String nbtKey;
		
		private PetContainerMessageType(String key) {
			this.nbtKey = key;
		}
		
		public String getKey() {
			return this.nbtKey;
		}
	}
	
	private static final String NBT_TYPE = "TYPE";
	private static final String NBT_INDEX = "INDEX";
	private static final String NBT_USERDATA = "DATA";
	
	private static final class NetworkHelper {
		
		private static void clientSendInternal(int id, CompoundNBT nbt) {
			PetGUIControlMessage message = new PetGUIControlMessage(id, nbt);
			
			NetworkHandler.getSyncChannel().sendToServer(message);
		}
		
		private static void serverSendInternal(ServerPlayerEntity player, CompoundNBT nbt) {
			PetGUISyncMessage message = new PetGUISyncMessage(nbt);
			
			NetworkHandler.getSyncChannel().sendTo(message, player);
		}
		
		private static CompoundNBT base(PetContainerMessageType type) {
			CompoundNBT nbt = new CompoundNBT();
			nbt.putString(NBT_TYPE, type.getKey());
			return nbt;
		}
		
		public static void ClientSendSheet(int id, int sheet) {
			CompoundNBT nbt = base(PetContainerMessageType.SET_SHEET);
			nbt.putInt(NBT_INDEX, sheet);
			
			clientSendInternal(id, nbt);
		}
		
		public static void ClientSendSheetData(int id, CompoundNBT data) {
			CompoundNBT nbt = base(PetContainerMessageType.SHEET_DATA);
			nbt.put(NBT_USERDATA, data);
			
			clientSendInternal(id, nbt);
		}
		
		public static void ClientSendReroll(int id) {
			CompoundNBT nbt = base(PetContainerMessageType.REROLL);
			
			clientSendInternal(id, nbt);
		}
		
		public static void ServerSendSheetData(ServerPlayerEntity player, CompoundNBT data) {
			CompoundNBT nbt = base(PetContainerMessageType.SHEET_DATA);
			nbt.put(NBT_USERDATA, data);
			
			serverSendInternal(player, nbt);
		}
		
		
		public static PetContainerMessageType GetType(CompoundNBT nbt) {
			String str = nbt.getString(NBT_TYPE);
			if (str == null || str.isEmpty()) {
				return null;
			}
			
			for (PetContainerMessageType type : PetContainerMessageType.values()) {
				if (type.getKey().equalsIgnoreCase(str)) {
					return type;
				}
			}
			
			return null;
		}
		
		public static int GetSendSheetIndex(CompoundNBT nbt) {
			return nbt.getInt(NBT_INDEX);
		}
		
		public static CompoundNBT GetSendSheetData(CompoundNBT nbt) {
			return nbt.getCompound(NBT_USERDATA);
		}
		
	}
	
}
