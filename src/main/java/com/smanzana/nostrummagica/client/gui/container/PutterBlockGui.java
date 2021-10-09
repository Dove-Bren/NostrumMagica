package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.PutterBlockTileEntity;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PutterBlockGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/putter.png");

	private static final int GUI_WIDTH = 176;
	private static final int GUI_HEIGHT = 166;
	private static final int PLAYER_INV_HOFFSET = 8;
	private static final int PLAYER_INV_VOFFSET = 84;
	private static final int PUTTER_INV_HOFFSET = 62;
	private static final int PUTTER_INV_VOFFSET = 17;
	
	public static class PutterBlockContainer extends Container {
		
		protected final PutterBlockTileEntity putter;
		
		public PutterBlockContainer(IInventory playerInv, PutterBlockTileEntity putter) {
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
			
			// Construct putter inventory
			IInventory putterInv = putter.getInventory();
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 3; x++) {
					this.addSlotToContainer(new Slot(putterInv, x + y * 3, PUTTER_INV_HOFFSET + (x * 18), PUTTER_INV_VOFFSET + (y * 18)));
				}
			}
			
			this.putter = putter;
		}
		
		@Override
		@Nonnull
		public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
			Slot slot = (Slot)this.inventorySlots.get(index);
			ItemStack prev = ItemStack.EMPTY;

			if (slot != null && slot.getHasStack()) {
				//IInventory from = slot.inventory;
				IInventory to;
				
				if (slot.inventory == putter.getInventory()) {
					to = playerIn.inventory;
				} else {
					to = putter.getInventory();
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
		public boolean canInteractWith(EntityPlayer playerIn) {
			return true;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class PutterBlockGuiContainer extends GuiContainer {

		//private PutterBlockContainer container;
		
		public PutterBlockGuiContainer(PutterBlockContainer container) {
			super(container);
			//this.container = container;
			
			this.xSize = GUI_WIDTH;
			this.ySize = GUI_HEIGHT;
		}
		
		@Override
		public void initGui() {
			super.initGui();
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawDefaultBackground();
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(TEXT);
			
			Gui.drawModalRectWithCustomSizedTexture(horizontalMargin, verticalMargin, 0,0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		}
		
	}
}