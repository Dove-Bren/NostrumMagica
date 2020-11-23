package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.aetheria.blocks.WispBlock.WispBlockTileEntity;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		
		// Kept just to report to server which TE is doing crafting
		protected BlockPos pos;
		protected EntityPlayer player;
		
		// Actual container variables as well as a couple for keeping track
		// of crafting state
		protected WispBlockTileEntity table;
		protected Slot scrollSlot;
		protected Slot reagentSlot;
		
		public WispBlockContainer(EntityPlayer player, IInventory playerInv, WispBlockTileEntity table, BlockPos pos) {
			super(table);
			this.player = player;
			this.pos = pos;
			this.table = table;
			this.scrollSlot = new Slot(table, 0, SCROLL_SLOT_INPUT_HOFFSET, SCROLL_SLOT_INPUT_VOFFSET) {
				
				@Override
				public boolean isItemValid(@Nullable ItemStack stack) {
					return stack == null ||
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
//				public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
//					//table.onTakeItem(playerIn);
//					super.onPickupFromSlot(playerIn, stack);
//				}
			};
			
			this.addSlotToContainer(scrollSlot);
			
			this.reagentSlot = new Slot(table, 1, REAGENT_SLOT_INPUT_HOFFSET, REAGENT_SLOT_INPUT_VOFFSET) {
				
				@Override
				public boolean isItemValid(@Nullable ItemStack stack) {
					return stack == null || stack.getItem() instanceof ReagentItem;
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
//				public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
//					//table.onTakeItem(playerIn);
//					super.onPickupFromSlot(playerIn, stack);
//				}
				
			};
			
			this.addSlotToContainer(reagentSlot);
			
			// Construct player inventory
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					this.addSlotToContainer(new Slot(playerInv, x + y * 9 + 9, PLAYER_INV_HOFFSET + (x * 18), PLAYER_INV_VOFFSET + (y * 18)));
				}
			}
			// Construct player hotbar
			for (int x = 0; x < 9; x++) {
				this.addSlotToContainer(new Slot(playerInv, x, PLAYER_INV_HOFFSET + x * 18, 58 + (PLAYER_INV_VOFFSET)));
			}
			
		}
		
		@Override
		public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {
			ItemStack prev = null;	
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			
			if (slot != null && slot.getHasStack()) {
				ItemStack cur = slot.getStack();
				prev = cur.copy();
				
				if (slot == this.scrollSlot) {
					// Trying to take our scroll
					if (playerIn.inventory.addItemStackToInventory(cur)) {
						scrollSlot.putStack(null);
						scrollSlot.onPickupFromSlot(playerIn, cur);
					} else {
						prev = null;
					}
				} else if (slot == this.reagentSlot) {
					// Trying to take our reagent
					if (playerIn.inventory.addItemStackToInventory(cur)) {
						reagentSlot.putStack(null);
						reagentSlot.onPickupFromSlot(playerIn, cur);
					} else {
						prev = null;
					}
				} else {
					// Trying to add an item
					if (!scrollSlot.getHasStack()
							&& scrollSlot.isItemValid(cur)) {
						ItemStack stack = cur.splitStack(1);
						scrollSlot.putStack(stack);
					} else if (!reagentSlot.getHasStack()
							&& reagentSlot.isItemValid(cur)) {
						ItemStack stack = cur.splitStack(cur.getMaxStackSize());
						reagentSlot.putStack(stack);
					} else {
						prev = null;
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
		public boolean canInteractWith(EntityPlayer playerIn) {
			return true;
		}
		
		public void setScroll(ItemStack item) {
			scrollSlot.putStack(item);
		}
		
		public void setReagent(ItemStack item) {
			reagentSlot.putStack(item);
		}

	}
	
	@SideOnly(Side.CLIENT)
	public static class WispBlockGuiContainer extends GuiContainer {

		private WispBlockContainer container;
		
		public WispBlockGuiContainer(WispBlockContainer container) {
			super(container);
			this.container = container;
			
			this.xSize = GUI_WIDTH;
			this.ySize = GUI_HEIGHT;
		}
		
		@Override
		public void initGui() {
			super.initGui();
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			ItemStack scroll = container.table.getScroll();
			int color = 0xFFFFFFFF;
			
			if (scroll != null) {
				Spell spell = SpellScroll.getSpell(scroll);
				if (spell != null) {
					color = spell.getPrimaryElement().getColor();
				}
			}
			float R = (float) ((color & 0x00FF0000) >> 16) / 256f;
			float G = (float) ((color & 0x0000FF00) >> 8) / 256f;
			float B = (float) ((color & 0x000000FF) >> 0) / 256f;
			
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(TEXT);
			
			Gui.drawModalRectWithCustomSizedTexture(horizontalMargin, verticalMargin, 0,0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
			
			float fuel = container.table.getPartialReagent();
			if (fuel > 0f) {
				int x = (int) (fuel * PROGRESS_WIDTH);
				GlStateManager.color(R, G, B, 1f);
				Gui.drawModalRectWithCustomSizedTexture(
						horizontalMargin + PROGRESS_GUI_HOFFSET,
						verticalMargin + PROGRESS_GUI_VOFFSET,
						0, GUI_HEIGHT, x, PROGRESS_HEIGHT, 256, 256);
				GlStateManager.color(1f, 1f, 1f, 1f);
			}
			
			int max = container.table.getMaxWisps();
			int filled = container.table.getWispCount();
			for (int i = 0; i < max; i++) {
				final int centerx = horizontalMargin + (GUI_WIDTH / 2);
				final int xspace = 20;
				final int leftx = centerx - ((xspace / 2) * (max - 1));
				final int x = leftx + (xspace * i) - (WISP_SOCKET_LENGTH / 2);
				final int y = verticalMargin + PROGRESS_GUI_VOFFSET + 7;
				Gui.drawModalRectWithCustomSizedTexture(x, y,
						WISP_SOCKET_HOFFSET,
						WISP_SOCKET_VOFFSET,
						WISP_SOCKET_LENGTH,
						WISP_SOCKET_LENGTH,
						256, 256);
				if (i < filled) {
					GlStateManager.color(R, G, B, 1f);
					Gui.drawModalRectWithCustomSizedTexture(x + 2, y + 2,
							WISP_SOCKET_HOFFSET,
							WISP_SOCKET_VOFFSET + WISP_SOCKET_LENGTH,
							5,
							5,
							256, 256);
					GlStateManager.color(1f, 1f, 1f, 1f); 
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
				this.drawHoveringText(Lists.newArrayList(((int) (container.table.getPartialReagent() * 100.0)) + "%"), mouseX - horizontalMargin, mouseY - verticalMargin);
			}
		}
		
	}
}