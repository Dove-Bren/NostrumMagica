package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.ActiveHopperTileEntity;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.client.gui.Gui;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActiveHopperGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/active_hopper.png");

	private static final int GUI_WIDTH = 176;
	private static final int GUI_HEIGHT = 166;
	private static final int PLAYER_INV_HOFFSET = 8;
	private static final int PLAYER_INV_VOFFSET = 51;
	private static final int PUTTER_INV_HOFFSET = 80;
	private static final int PUTTER_INV_VOFFSET = 20;
	
	public static class ActiveHopperContainer extends Container {
		
		protected final ActiveHopperTileEntity hopper;
		
		public ActiveHopperContainer(IInventory playerInv, ActiveHopperTileEntity hopper) {
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
			
			// Construct hopper inventory
			this.addSlotToContainer(new Slot(hopper, 0, PUTTER_INV_HOFFSET, PUTTER_INV_VOFFSET));
			
			this.hopper = hopper;
		}
		
		@Override
		@Nonnull
		public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
			Slot slot = (Slot)this.inventorySlots.get(index);
			ItemStack prev = ItemStack.EMPTY;

			if (slot != null && slot.getHasStack()) {
				//IInventory from = slot.inventory;
				IInventory to;
				
				if (slot.inventory == hopper) {
					to = playerIn.inventory;
				} else {
					to = hopper;
				}
				
				ItemStack stack = slot.getStack();
				prev = stack.copy();

				stack = Inventories.addItem(to, stack);
				if (!stack.isEmpty() && stack.getCount() == 0) {
					stack = ItemStack.EMPTY;
				}
				
				if (!stack.isEmpty() && stack.getCount() == prev.getCount()) {
					return ItemStack.EMPTY;
				};
				
				slot.putStack(stack);
				slot.onTake(playerIn, stack);
			}

			return prev;
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return true;
		}
		
		@Override
		public boolean canInteractWith(PlayerEntity playerIn) {
			return true;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ActiveHopperGuiContainer extends AutoGuiContainer {

		public ActiveHopperGuiContainer(ActiveHopperContainer container) {
			super(container);
			
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
			
			GlStateManager.color4f(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTexture(horizontalMargin, verticalMargin, 0,0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		}
		
	}
}