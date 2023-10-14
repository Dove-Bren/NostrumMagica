package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.aetheria.blocks.WispBlockTileEntity;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.utils.ContainerUtil;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WispBlockGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/wisp_block.png");

	private static final int GUI_WIDTH = 176;
	private static final int GUI_HEIGHT = 175;
	private static final int PLAYER_INV_HOFFSET = 8;
	private static final int PLAYER_INV_VOFFSET = 93;
	
	private static final int SCROLL_SLOT_INPUT_HOFFSET = 80;
	private static final int SCROLL_SLOT_INPUT_VOFFSET = 25;
	
	private static final int REAGENT_SLOT_INPUT_HOFFSET = 80;
	private static final int REAGENT_SLOT_INPUT_VOFFSET = 54;
	
	private static final int PROGRESS_WIDTH = 128;
	private static final int PROGRESS_HEIGHT = 3;
	private static final int PROGRESS_GUI_HOFFSET = 24;
	private static final int PROGRESS_GUI_VOFFSET = 75;
	
	private static final int WISP_SOCKET_LENGTH = 9;
	private static final int WISP_SOCKET_HOFFSET = 176;
	private static final int WISP_SOCKET_VOFFSET = 0;
	
	public static class WispBlockContainer extends AutoContainer {
		
		public static final String ID = "wisp_block";
		
		// Kept just to report to server which TE is doing crafting
		protected BlockPos pos;
		protected PlayerEntity player;
		
		// Actual container variables as well as a couple for keeping track
		// of crafting state
		protected WispBlockTileEntity table;
		protected Slot scrollSlot;
		protected Slot reagentSlot;
		
		public WispBlockContainer(int windowId, PlayerInventory playerInv, WispBlockTileEntity table) {
			super(NostrumContainers.WispBlock, windowId, table);
			this.player = playerInv.player;
			this.pos = table.getPos();
			this.table = table;
			this.scrollSlot = new Slot(table, 0, SCROLL_SLOT_INPUT_HOFFSET, SCROLL_SLOT_INPUT_VOFFSET) {
				
				@Override
				public boolean isItemValid(@Nonnull ItemStack stack) {
					return stack.isEmpty() ||
							(stack.getItem() instanceof SpellScroll && SpellScroll.getSpell(stack) != null);
				}
//				
//				@Override
//				public void putStack(@Nullable ItemStack stack) {
//					table.setScroll(stack);
//					this.onSlotChanged();
//				}
//				
//				@Override
//				public ItemStack getStack() {
//					return table.getScroll();
//				}
//				
//				@Override
//				public void onSlotChanged() {
//					table.markDirty();
//				}
//				
//				public int getSlotStackLimit() {
//					return 1;
//				}
//				
//				public ItemStack decrStackSize(int amount) {
//					ItemStack item = table.getScroll();
//					if (item != null) {
//						if (table.setScroll(null))
//							return item.copy();
//					}
//					
//					return null;
//				}
//				
//				public boolean isHere(IInventory inv, int slotIn) {
//					return false;
//				}
//				
//				public boolean isSameInventory(Slot other) {
//					return false;
//				}
//				
//				public void onPickupFromSlot(PlayerEntity playerIn, ItemStack stack) {
//					//table.onTakeItem(playerIn);
//					super.onPickupFromSlot(playerIn, stack);
//				}
			};
			
			this.addSlot(scrollSlot);
			
			this.reagentSlot = new Slot(table, 1, REAGENT_SLOT_INPUT_HOFFSET, REAGENT_SLOT_INPUT_VOFFSET) {
				
				@Override
				public boolean isItemValid(@Nonnull ItemStack stack) {
					return stack.isEmpty() || stack.getItem() instanceof ReagentItem;
				}
//				
//				@Override
//				public void putStack(@Nullable ItemStack stack) {
//					table.setReagent(stack);
//					this.onSlotChanged();
//				}
//				
//				@Override
//				public ItemStack getStack() {
//					return table.getReagent();
//				}
//				
//				@Override
//				public void onSlotChanged() {
//					table.markDirty();
//				}
//				
//				public int getSlotStackLimit() {
//					return 64;
//				}
//				
//				public ItemStack decrStackSize(int amount) {
//					ItemStack item = table.getReagent();
//					if (item != null) {
//						if (table.setReagent(null))
//							return item.copy();
//					}
//					
//					return null;
//				}
//				
//				public boolean isHere(IInventory inv, int slotIn) {
//					return false;
//				}
//				
//				public boolean isSameInventory(Slot other) {
//					return false;
//				}
//				
//				public void onPickupFromSlot(PlayerEntity playerIn, ItemStack stack) {
//					//table.onTakeItem(playerIn);
//					super.onPickupFromSlot(playerIn, stack);
//				}
				
			};
			
			this.addSlot(reagentSlot);
			
			// Construct player inventory
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					this.addSlot(new Slot(playerInv, x + y * 9 + 9, PLAYER_INV_HOFFSET + (x * 18), PLAYER_INV_VOFFSET + (y * 18)));
				}
			}
			// Construct player hotbar
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(playerInv, x, PLAYER_INV_HOFFSET + x * 18, 58 + (PLAYER_INV_VOFFSET)));
			}
			
		}
		
		@OnlyIn(Dist.CLIENT)
		public static final WispBlockContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buffer) {
			return new WispBlockContainer(windowId, playerInv, ContainerUtil.GetPackedTE(buffer));
		}
		
		@Override
		public ItemStack transferStackInSlot(PlayerEntity playerIn, int fromSlot) {
			ItemStack prev = ItemStack.EMPTY;	
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			
			if (slot != null && slot.getHasStack()) {
				ItemStack cur = slot.getStack();
				prev = cur.copy();
				
				if (slot == this.scrollSlot) {
					// Trying to take our scroll
					if (playerIn.inventory.addItemStackToInventory(cur)) {
						scrollSlot.putStack(ItemStack.EMPTY);
						scrollSlot.onTake(playerIn, cur);
					} else {
						prev = ItemStack.EMPTY;
					}
				} else if (slot == this.reagentSlot) {
					// Trying to take our reagent
					if (playerIn.inventory.addItemStackToInventory(cur)) {
						reagentSlot.putStack(ItemStack.EMPTY);
						reagentSlot.onTake(playerIn, cur);
					} else {
						prev = ItemStack.EMPTY;
					}
				} else {
					// Trying to add an item
					if (!scrollSlot.getHasStack()
							&& scrollSlot.isItemValid(cur)) {
						ItemStack stack = cur.split(1);
						scrollSlot.putStack(stack);
					} else if (!reagentSlot.getHasStack()
							&& reagentSlot.isItemValid(cur)) {
						ItemStack stack = cur.split(cur.getMaxStackSize());
						reagentSlot.putStack(stack);
					} else {
						prev = ItemStack.EMPTY;
					}
				}
				
			}
			
			return prev;
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn != scrollSlot && slotIn != reagentSlot;
		}
		
		@Override
		public boolean canInteractWith(PlayerEntity playerIn) {
			return true;
		}
		
		public void setScroll(ItemStack item) {
			scrollSlot.putStack(item);
		}
		
		public void setReagent(ItemStack item) {
			reagentSlot.putStack(item);
		}

	}
	
	@OnlyIn(Dist.CLIENT)
	public static class WispBlockGuiContainer extends AutoGuiContainer<WispBlockContainer> {

		private WispBlockContainer container;
		
		public WispBlockGuiContainer(WispBlockContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			this.container = container;
			
			this.xSize = GUI_WIDTH;
			this.ySize = GUI_HEIGHT;
		}
		
		@Override
		public void init() {
			super.init();
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			@Nonnull ItemStack scroll = container.table.getScroll();
			int color = 0xFFFFFFFF;
			
			if (!scroll.isEmpty()) {
				Spell spell = SpellScroll.getSpell(scroll);
				if (spell != null) {
					color = spell.getPrimaryElement().getColor();
				}
			}
			float R = (float) ((color & 0x00FF0000) >> 16) / 256f;
			float G = (float) ((color & 0x0000FF00) >> 8) / 256f;
			float B = (float) ((color & 0x000000FF) >> 0) / 256f;
			
			GlStateManager.color4f(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTexture(horizontalMargin, verticalMargin, 0,0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
			
			float fuel = container.table.getPartialReagent();
			if (fuel > 0f) {
				int x = (int) (fuel * PROGRESS_WIDTH);
				GlStateManager.color4f(R, G, B, 1f);
				RenderFuncs.drawModalRectWithCustomSizedTexture(
						horizontalMargin + PROGRESS_GUI_HOFFSET,
						verticalMargin + PROGRESS_GUI_VOFFSET,
						0, GUI_HEIGHT, x, PROGRESS_HEIGHT, 256, 256);
				GlStateManager.color4f(1f, 1f, 1f, 1f);
			}
			
			int max = container.table.getMaxWisps();
			int filled = container.table.getWispCount();
			for (int i = 0; i < max; i++) {
				final int centerx = horizontalMargin + (GUI_WIDTH / 2);
				final int xspace = 20;
				final int leftx = centerx - ((xspace / 2) * (max - 1));
				final int x = leftx + (xspace * i) - (WISP_SOCKET_LENGTH / 2);
				final int y = verticalMargin + PROGRESS_GUI_VOFFSET + 7;
				RenderFuncs.drawModalRectWithCustomSizedTexture(x, y,
						WISP_SOCKET_HOFFSET,
						WISP_SOCKET_VOFFSET,
						WISP_SOCKET_LENGTH,
						WISP_SOCKET_LENGTH,
						256, 256);
				if (i < filled) {
					GlStateManager.color4f(R, G, B, 1f);
					RenderFuncs.drawModalRectWithCustomSizedTexture(x + 2, y + 2,
							WISP_SOCKET_HOFFSET,
							WISP_SOCKET_VOFFSET + WISP_SOCKET_LENGTH,
							5,
							5,
							256, 256);
					GlStateManager.color4f(1f, 1f, 1f, 1f); 
				}
			}
			
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			/*
			 * horizontalMargin + PROGRESS_GUI_HOFFSET,
						verticalMargin + PROGRESS_GUI_VOFFSET,
						0, GUI_HEIGHT, x, PROGRESS_HEIGHT, 256, 256);
			 */
			if (mouseX >= horizontalMargin + PROGRESS_GUI_HOFFSET
					&& mouseX <= horizontalMargin + PROGRESS_GUI_HOFFSET + PROGRESS_WIDTH
					&& mouseY >= verticalMargin + PROGRESS_GUI_VOFFSET
					&& mouseY <= verticalMargin + PROGRESS_GUI_VOFFSET + PROGRESS_HEIGHT) {
				this.renderTooltip(Lists.newArrayList(((int) (container.table.getPartialReagent() * 100.0)) + "%"), mouseX - horizontalMargin, mouseY - verticalMargin);
			}
		}
		
	}
}