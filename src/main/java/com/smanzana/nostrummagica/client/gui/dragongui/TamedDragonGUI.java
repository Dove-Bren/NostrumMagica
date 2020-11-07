package com.smanzana.nostrummagica.client.gui.dragongui;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.dragon.EntityDragon;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.TamedDragonGUIControlMessage;
import com.smanzana.nostrummagica.network.messages.TamedDragonGUISyncMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A nice wrapped up dragon gui.
 * 
 * Doesn't do a lot on its own. Instead, things can build it up using dragon sheets
 * 
 * @author Skyler
 */
public class TamedDragonGUI {
	
	// Need a static registry of open containers for dispatching messages on the server
	private static Map<Integer, DragonContainer> containers = new HashMap<>();
	
	private static int lastKey = 0;
	
	private static int register(DragonContainer container) {
		if (container.player.worldObj.isRemote) {
			throw new IllegalArgumentException("Can't register on the client!");
		}
		
		int id = lastKey++;
		containers.put(id, container);
		return id;
	}
	
	private static void revoke(int id) {
		containers.remove(id);
	}
	
	public static void updateServerContainer(int id, NBTTagCompound nbt) {
		DragonContainer container = containers.get(id);
		if (container != null) {
			container.handle(nbt);
		}
	}
	
	private static DragonContainer clientContainer = null;
	
	public static void updateClientContainer(NBTTagCompound nbt) {
		if (clientContainer != null) {
			clientContainer.handle(nbt);
		}
	}

	public static class DragonContainer extends Container {

		private EntityPlayer player;
		
		private ITameDragon dragon;
		
		private EntityLivingBase livingDragon;
		
		private int currentSheet;
		
		protected List<IDragonGUISheet> sheetsAllInternal;
		
		protected int id;
		
		private int guiOffsetX;
		private int guiOffsetY;
		
		public DragonContainer(ITameDragon dragon, EntityPlayer player, IDragonGUISheet ... sheets) {
			this.dragon = dragon;
			this.livingDragon = (EntityLivingBase) dragon;
			this.player = player;
			this.currentSheet = 0;
			this.sheetsAllInternal = Lists.newArrayList(sheets);
			
			if (!((EntityDragon) dragon).worldObj.isRemote) {
				this.id = TamedDragonGUI.register(this);				
			} else {
				TamedDragonGUI.clientContainer = this;
			}
		}
		
		public void overrideID(int id) {
			if (!this.player.worldObj.isRemote) {
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
		public boolean canInteractWith(EntityPlayer playerIn) {
			if (dragon == null) {
				// Dragon hasn't been synced yet
				return false;
			}
			return playerIn.equals(dragon.getOwner());
		}

		// Caution: This assumes only one player has these open!
		@Override
		public void onContainerClosed(EntityPlayer playerIn) {
			revoke(this.id);
		}
		
		@Override
		public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {
			return null;
		}
		
		/**
		 * Returns a list of sheets this container has.
		 * This is a collection that's filtered down to what should be shown
		 * @return
		 */
		protected List<IDragonGUISheet> getSheets() {
			final DragonContainer container = this;
			return sheetsAllInternal.parallelStream().filter((sheet) -> {
				return sheet.shouldShow(container.dragon, container);
			}).collect(Collectors.toList());
		}
		
		public IDragonGUISheet getCurrentSheet() {
			return getSheets().get(currentSheet);
		}
		
		public void setSheet(int index) {
			if (this.currentSheet < this.getSheetCount()) {
				// If we changed the number of sheets, we may have an invalid one to close. So just don't close it.
				this.getCurrentSheet().hideSheet(dragon, player, this);
			}
			this.currentSheet = Math.min(Math.max(0, index), getSheets().size() - 1);
			this.getCurrentSheet().showSheet(dragon, player, this, GUI_SHEET_WIDTH, GUI_SHEET_HEIGHT, guiOffsetX, guiOffsetY);
		}
		
		public int getSheetIndex() {
			return this.currentSheet;
		}
		
		public void clearSlots() {
			this.inventorySlots.clear();
			this.inventoryItemStacks.clear();
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
		
		// Handle a message sent from the client.
		// Could be a button click to change sheets, some other control message,
		// or a message for updating a sheet's contents.
		protected void handle(NBTTagCompound nbt) {
			DragonContainerMessageType type = NetworkHelper.GetType(nbt);
			
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
				if (dragon != null && dragon.getOwner() instanceof EntityPlayer && ((EntityPlayer) dragon.getOwner()).isCreative()) {
					// Reset container sheet. The client will send this as well later.
					this.setSheet(0);
					dragon.rollStats();
				}
				break;
			}
		}
		
		// Sheets can call on their handle to the container to sync with the server.
		// This call doesn't check if it's on the server. It'll just 'send' it. Know what you're doing!
		public void sendSheetMessageToServer(NBTTagCompound data) {
			NetworkHelper.ClientSendSheetData(id, data);
		}
		
		public void sendSheetMessageToClient(NBTTagCompound data) {
			NetworkHelper.ServerSendSheetData((EntityPlayerMP) this.player, data);
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
	
	
	@SideOnly(Side.CLIENT)
	public static class DragonGUI extends GuiContainer {
		
		public static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/tamed_dragon_gui.png");
		
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
		
		private DragonContainer container;
		
		//private int openTicks;
		
		public DragonGUI(DragonContainer container) {
			super(container);
			this.container = container;
			//this.openTicks = 0;
		}
		
		@Override
		public void initGui() {
			this.xSize = this.width;
			this.ySize = this.height;
			super.initGui();
			
			final int GUI_SHEET_HOFFSET = this.width - (GUI_SHEET_WIDTH + GUI_SHEET_NHOFFSET);
			this.container.setGUIOffets(GUI_SHEET_HOFFSET, GUI_SHEET_VOFFSET);
		}
		
//		@Override
//		public void updateScreen() {
//			super.updateScreen();
//			
//			this.openTicks++;
//		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
			
			final int GUI_SHEET_HOFFSET = this.width - (GUI_SHEET_WIDTH + GUI_SHEET_NHOFFSET);
			final int GUI_SHEET_BUTTON_HOFFSET = GUI_SHEET_HOFFSET;
			
			if (this.container.dragon == null) {
				this.drawCenteredString(fontRendererObj, "Waiting for server...", this.width / 2, this.height / 2, 0XFFAAAAAA);
				return;
			}
			
			// Draw top-left preview
			{
				Gui.drawRect(0, 0, GUI_LENGTH_PREVIEW, GUI_LENGTH_PREVIEW, 0xFF283D2A);
				
				int xPosition = GUI_LENGTH_PREVIEW / 2;
				int yPosition = GUI_LENGTH_PREVIEW / 2;
				RenderHelper.disableStandardItemLighting();
				GuiInventory.drawEntityOnScreen(
						xPosition,
						(int) (GUI_LENGTH_PREVIEW * .75f),
						(int) (GUI_LENGTH_PREVIEW * .2),
						(float) (xPosition) - mouseX,
						(float) (-yPosition) - mouseY,
						(EntityLivingBase) container.dragon);
			}
			
			// Move everything forward ahead of the drawn entity
			// Can't just move entity back cause there's a GRAY plane drawn at just below 0 Z
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 51);
			
			// Black background (not overlapping preview)
			{
				Gui.drawRect(0, GUI_LENGTH_PREVIEW, width, height, 0xFF000000);
				Gui.drawRect(GUI_LENGTH_PREVIEW, 0, width, GUI_LENGTH_PREVIEW, 0xFF000000);
			}
			
			// Draw stats and stuff
			{
				//Gui.drawRect(GUI_INFO_HOFFSET, GUI_INFO_VOFFSET, GUI_SHEET_HOFFSET - 10, height - 10, 0xFF00FFFF);
				
				final int w = (GUI_SHEET_HOFFSET - GUI_SHEET_MARGIN) - (GUI_INFO_HOFFSET * 2);
				int x = GUI_INFO_HOFFSET;
				int y = GUI_INFO_VOFFSET;
				//final int w = 125;
				final int h = 14;
				final int centerX = GUI_SHEET_HOFFSET / 2;
				
				// Health
				{
					this.drawCenteredString(this.fontRendererObj, ChatFormatting.BOLD + "Health", centerX, y, 0xFFFFFFFF);
					y += fontRendererObj.FONT_HEIGHT + 5;
					Gui.drawRect(x, y, x + w, y + h, 0xFFD0D0D0);
					Gui.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF201010);
					
					int prog = (int) ((float) (w - 2) * (container.livingDragon.getHealth() / container.livingDragon.getMaxHealth()));
					Gui.drawRect(x + 1, y + 1, x + 1 + prog, y + h - 1, 0xFFA02020);
					
					this.drawCenteredString(fontRendererObj,
							String.format("%d / %d", (int) container.livingDragon.getHealth(), (int) container.livingDragon.getMaxHealth()),
							centerX,
							y + (h / 2) - (fontRendererObj.FONT_HEIGHT / 2),
							0xFFC0C0C0);
					
					y += h + 10;
				}
				
				// Mana
				if (container.dragon.getMaxMana() > 0) {
					this.drawCenteredString(this.fontRendererObj, ChatFormatting.BOLD + "Mana", centerX, y, 0xFFFFFFFF);
					y += fontRendererObj.FONT_HEIGHT + 5;
					Gui.drawRect(x, y, x + w, y + h, 0xFFD0D0D0);
					Gui.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF101020);
					
					int prog = (int) ((float) (w - 2) * ((float) container.dragon.getMana() / (float) container.dragon.getMaxMana()));
					Gui.drawRect(x + 1, y + 1, x + 1 + prog, y + h - 1, 0xFF2020A0);
					
					this.drawCenteredString(fontRendererObj,
							String.format("%d / %d", (int) container.dragon.getMana(), (int) container.dragon.getMaxMana()),
							centerX,
							y + (h / 2) - (fontRendererObj.FONT_HEIGHT / 2),
							0xFFC0C0C0);
					
					y += h + 10;
				}
				
				// Bond
				// TODO make optional?
				{
					float bond = container.dragon.getBond();
					this.drawCenteredString(this.fontRendererObj, ChatFormatting.BOLD + "Bond", centerX, y, 0xFFFFFFFF);
					y += fontRendererObj.FONT_HEIGHT + 5;
					Gui.drawRect(x, y, x + w, y + h, 0xFFD0D0D0);
					Gui.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF201020);
					
					int prog = (int) ((float) (w - 2) * bond);
					Gui.drawRect(x + 1, y + 1, x + 1 + prog, y + h - 1, 0xFFA020A0);
					
					this.drawCenteredString(fontRendererObj,
							String.format("%.2f%%", bond * 100f),
							centerX,
							y + (h / 2) - (fontRendererObj.FONT_HEIGHT / 2),
							bond == 1f ? 0xFFC0FFC0 : 0xFFC0C0C0);
					
					y += h + 10;
				}
				
				// XP
				if (container.dragon.getXP() > -1) {
					this.drawCenteredString(this.fontRendererObj, ChatFormatting.BOLD + "XP", centerX, y, 0xFFFFFFFF);
					y += fontRendererObj.FONT_HEIGHT + 5;
					Gui.drawRect(x, y, x + w, y + h, 0xFFD0D0D0);
					Gui.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF102010);
					
					int prog = (int) ((float) (w - 2) * ((float) container.dragon.getXP() / (float) container.dragon.getMaxXP()));
					Gui.drawRect(x + 1, y + 1, x + 1 + prog, y + h - 1, 0xFF20A020);
					
					this.drawCenteredString(fontRendererObj,
							String.format("%d / %d", (int) container.dragon.getXP(), (int) container.dragon.getMaxXP()),
							centerX,
							y + (h / 2) - (fontRendererObj.FONT_HEIGHT / 2),
							0xFFC0C0C0);
					
					y += h + 10;
				}
			}
			
			if (container.getSheets().size() > 0) {
				int x = GUI_SHEET_BUTTON_HOFFSET;
				
				for (IDragonGUISheet sheet : container.getSheets()) {
					Gui.drawRect(x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0xFFFFFFFF);
					Gui.drawRect(x + 1, GUI_SHEET_BUTTON_VOFFSET + 1, x + GUI_SHEET_BUTTON_WIDTH - 1, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT - 1, 0xFF202020);
					
					if (sheet == container.getCurrentSheet()) {
						Gui.drawRect(x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0x40FFFFFF);
					}
					
					if (mouseX >= x && mouseX <= x + GUI_SHEET_BUTTON_WIDTH && mouseY >= GUI_SHEET_BUTTON_VOFFSET && mouseY <= GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT) {
						Gui.drawRect(x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0x40FFFFFF);
					}
					
					String text = sheet.getButtonText();
					int strLen = fontRendererObj.getStringWidth(text);
					int strHeight = fontRendererObj.FONT_HEIGHT;
					fontRendererObj.drawString(text, x + (GUI_SHEET_BUTTON_WIDTH / 2) - (strLen / 2), GUI_SHEET_BUTTON_VOFFSET + (GUI_SHEET_BUTTON_HEIGHT / 2) - (strHeight / 2), 0xFFFFFFFF);
					x += GUI_SHEET_BUTTON_WIDTH;
				}
				
				if (NostrumMagica.proxy.getPlayer().isCreative()) {
					Gui.drawRect(x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0xFFFFDDFF);
					Gui.drawRect(x + 1, GUI_SHEET_BUTTON_VOFFSET + 1, x + GUI_SHEET_BUTTON_WIDTH - 1, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT - 1, 0xFF702070);
					
					if (mouseX >= x && mouseX <= x + GUI_SHEET_BUTTON_WIDTH && mouseY >= GUI_SHEET_BUTTON_VOFFSET && mouseY <= GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT) {
						Gui.drawRect(x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0x40FFFFFF);
					}
					
					String text = "Reroll";
					int strLen = fontRendererObj.getStringWidth(text);
					int strHeight = fontRendererObj.FONT_HEIGHT;
					fontRendererObj.drawString(text, x + (GUI_SHEET_BUTTON_WIDTH / 2) - (strLen / 2), GUI_SHEET_BUTTON_VOFFSET + (GUI_SHEET_BUTTON_HEIGHT / 2) - (strHeight / 2), 0xFFFFFFFF);
					x += GUI_SHEET_BUTTON_WIDTH;
				}
			}
			
			mc.getTextureManager().bindTexture(TEXT);
			
			// Draw sheet
			IDragonGUISheet sheet = container.getCurrentSheet();
			if (sheet != null) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(GUI_SHEET_HOFFSET, GUI_SHEET_VOFFSET, 0);
				
				GlStateManager.enableAlpha();
				GlStateManager.enableBlend();
				GlStateManager.color(1f, 1f, 1f, 1f);
				drawModalRectWithCustomSizedTexture(-GUI_SHEET_MARGIN, -GUI_SHEET_MARGIN, 0, 0, GUI_SHEET_WIDTH + (GUI_SHEET_MARGIN * 2), GUI_SHEET_HEIGHT + (GUI_SHEET_MARGIN * 2), GUI_TEX_WIDTH, GUI_TEX_HEIGHT);
				
				sheet.draw(Minecraft.getMinecraft(), partialTicks, GUI_SHEET_WIDTH, GUI_SHEET_HEIGHT,
						mouseX - GUI_SHEET_HOFFSET, mouseY - GUI_SHEET_VOFFSET);
				GlStateManager.popMatrix();
			}
			
			GlStateManager.popMatrix();
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);
			
			final int GUI_SHEET_HOFFSET = this.width - (GUI_SHEET_WIDTH + GUI_SHEET_NHOFFSET);
			
			IDragonGUISheet sheet = container.getCurrentSheet();
			if (sheet != null) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(GUI_SHEET_HOFFSET, GUI_SHEET_VOFFSET, 0);
				
				GlStateManager.enableAlpha();
				GlStateManager.enableBlend();
				
				sheet.overlay(Minecraft.getMinecraft(), 0f, GUI_SHEET_WIDTH, GUI_SHEET_HEIGHT,
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
			if (NostrumMagica.proxy.getPlayer().inventory.getItemStack() == null) {
			
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
					} else if (buttonIdx == container.getSheets().size() && NostrumMagica.proxy.getPlayer().isCreative()) {
						NetworkHelper.ClientSendReroll(container.id);
						// Reset sheet index in case reroll removed a tab
						this.container.setSheet(0);
						NetworkHelper.ClientSendSheet(container.id, this.container.currentSheet);
					}
				}
				
				// Clicking on the sheet?
				if (mouseX >= GUI_SHEET_HOFFSET && mouseX <= GUI_SHEET_HOFFSET + GUI_SHEET_WIDTH
						&& mouseY >= GUI_SHEET_VOFFSET && mouseY <= GUI_SHEET_VOFFSET + GUI_SHEET_HEIGHT) {
					IDragonGUISheet sheet = container.getCurrentSheet();
					if (sheet != null) {
						sheet.mouseClicked(mouseX - GUI_SHEET_HOFFSET, mouseY - GUI_SHEET_VOFFSET, mouseButton);
					}
					
				}
			}
			
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}
	

	
	private static enum DragonContainerMessageType {
		
		SET_SHEET("_SETSHEET"),
		
		SHEET_DATA("_SHEET_DATA"),
		
		REROLL("_REROLL");
		
		private final String nbtKey;
		
		private DragonContainerMessageType(String key) {
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
		
		private static void clientSendInternal(int id, NBTTagCompound nbt) {
			TamedDragonGUIControlMessage message = new TamedDragonGUIControlMessage(id, nbt);
			
			NetworkHandler.getSyncChannel().sendToServer(message);
		}
		
		private static void serverSendInternal(EntityPlayerMP player, NBTTagCompound nbt) {
			TamedDragonGUISyncMessage message = new TamedDragonGUISyncMessage(nbt);
			
			NetworkHandler.getSyncChannel().sendTo(message, player);
		}
		
		private static NBTTagCompound base(DragonContainerMessageType type) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString(NBT_TYPE, type.getKey());
			return nbt;
		}
		
		public static void ClientSendSheet(int id, int sheet) {
			NBTTagCompound nbt = base(DragonContainerMessageType.SET_SHEET);
			nbt.setInteger(NBT_INDEX, sheet);
			
			clientSendInternal(id, nbt);
		}
		
		public static void ClientSendSheetData(int id, NBTTagCompound data) {
			NBTTagCompound nbt = base(DragonContainerMessageType.SHEET_DATA);
			nbt.setTag(NBT_USERDATA, data);
			
			clientSendInternal(id, nbt);
		}
		
		public static void ClientSendReroll(int id) {
			NBTTagCompound nbt = base(DragonContainerMessageType.REROLL);
			
			clientSendInternal(id, nbt);
		}
		
		public static void ServerSendSheetData(EntityPlayerMP player, NBTTagCompound data) {
			NBTTagCompound nbt = base(DragonContainerMessageType.SHEET_DATA);
			nbt.setTag(NBT_USERDATA, data);
			
			serverSendInternal(player, nbt);
		}
		
		
		public static DragonContainerMessageType GetType(NBTTagCompound nbt) {
			String str = nbt.getString(NBT_TYPE);
			if (str == null || str.isEmpty()) {
				return null;
			}
			
			for (DragonContainerMessageType type : DragonContainerMessageType.values()) {
				if (type.getKey().equalsIgnoreCase(str)) {
					return type;
				}
			}
			
			return null;
		}
		
		public static int GetSendSheetIndex(NBTTagCompound nbt) {
			return nbt.getInteger(NBT_INDEX);
		}
		
		public static NBTTagCompound GetSendSheetData(NBTTagCompound nbt) {
			return nbt.getCompoundTag(NBT_USERDATA);
		}
		
	}
	
}
