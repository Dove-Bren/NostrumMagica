package com.smanzana.nostrummagica.client.gui.container;

import java.io.IOException;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentBag.ReagentInventory;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ReagentBagToggleMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ReagentBagGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/reagent_bag.png");
	
	private static final int GUI_WIDTH = 176;
	private static final int GUI_HEIGHT = 175;
	private static final int PLAYER_INV_HOFFSET = 8;
	private static final int PLAYER_INV_VOFFSET = 93;
	private static final int BAG_INV_HOFFSET = 63;
	private static final int BAG_INV_VOFFSET = 9;
	private static final int BUTTON_WIDTH = 24;
	private static final int BUTTON_HOFFSET = 122;
	private static final int BUTTON_VOFFSET = 4;
	private static final int BUTTON_TEXT_VOFFSET = 175;
	
	public static class BagContainer extends Container {
		
		protected ReagentBag bag;
		protected ItemStack stack;
		protected IInventory inventory;
		protected int bagPos;
		
		private int bagIDStart;
		
		public BagContainer(IInventory playerInv, ReagentBag bag, ItemStack stack, int bagPos) {
			this.stack = stack;
			this.inventory = bag.asInventory(stack);
			this.bag = bag;
			this.bagPos = bagPos;
			
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
			
			this.bagIDStart = this.inventorySlots.size();
			
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					
					this.addSlotToContainer(new Slot(inventory, i * 3 + j, BAG_INV_HOFFSET + j * 18, BAG_INV_VOFFSET + i * 18) {
						public boolean isItemValid(@Nullable ItemStack stack) {
					        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
					    }
					});
				}
			}
		}
		
		@Override
		public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {
			ItemStack prev = null;	
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			IInventory inv = slot.inventory;
			
			if (slot.getHasStack()) {
				ItemStack stack = slot.getStack();
				if (inv == inventory) {
					// shift-click in bag
					if (playerIn.inventory.addItemStackToInventory(stack.copy())) {
						slot.putStack(null);
					}
				} else {
					// shift-click in player inventory
					ReagentInventory realinv = (ReagentInventory) inventory;
					ItemStack leftover = realinv.addItem(stack);
					slot.putStack(leftover != null && leftover.stackSize <= 0 ? null : leftover);
//					if (inventory.isItemValidForSlot(0, stack)) {
//						for (int i = 0; i < inventory.getSizeInventory(); i++) {
//							ItemStack inSlot = inventory.getStackInSlot(i);
//							if (inSlot == null) {
//								inventory.setInventorySlotContents(i, stack);
//								stack = null;
//								break;
//							} else if (inSlot.getItem() == stack.getItem()
//									&& inSlot.getMetadata() == stack.getMetadata()
//									&& inSlot.stackSize < inventory.getInventoryStackLimit()) {
//								int space = inventory.getInventoryStackLimit() - inSlot.stackSize;
//								if (space >= stack.stackSize) {
//									inSlot.stackSize += stack.stackSize;
//									inventory.setInventorySlotContents(i, inSlot);
//									stack = null;
//									break;
//								} else {
//									inSlot.stackSize = inventory.getInventoryStackLimit();
//									inventory.setInventorySlotContents(i, inSlot);
//									stack.stackSize -= space;
//								}
//							}
//						}
//						
//						if (stack == null || stack.stackSize <= 0) {
//							slot.putStack(null);
//						} else {
//							slot.putStack(stack);
//						}
//						
//					}
				}
			}
			
			if (slot != null && slot.getHasStack()) {
				ItemStack cur = slot.getStack();
				prev = cur.copy();
				
				
				/** If we want additional behavior put it here **/
				/**if (fromSlot == 0) {
					// This is going FROM Brazier to player
					if (!this.mergeItemStack(cur, 9, 45, true))
						return null;
					else
						// From Player TO Brazier
						if (!this.mergeItemStack(cur, 0, 0, false)) {
							return null;
						}
				}**/
				
				if (cur.stackSize == 0) {
					slot.putStack((ItemStack) null);
				} else {
					slot.onSlotChanged();
				}
				
				if (cur.stackSize == prev.stackSize) {
					return null;
				}
				slot.onPickupFromSlot(playerIn, cur);
			}
			
			return prev;
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn.inventory != this.inventory; // It's NOT bag inventory
		}
		
		@Override
		public boolean canInteractWith(EntityPlayer playerIn) {
			return true;
		}
		
		@Override
		public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
			if (slotId < bagIDStart) {
				if (slotId == bagPos) {
					return null;
				}
			}
			
			return super.slotClick(slotId, dragType, clickTypeIn, player);
		}

	}
	
	@SideOnly(Side.CLIENT)
	public static class BagGui extends GuiContainer {

		private BagContainer bag;
		
		public BagGui(BagContainer bag) {
			super(bag);
			this.bag = bag;
			this.xSize = GUI_WIDTH;
			this.ySize = GUI_HEIGHT;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(TEXT);
			
			Gui.drawModalRectWithCustomSizedTexture(horizontalMargin, verticalMargin, 0,0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
			
			int guiU = 0;
			if (ReagentBag.isVacuumEnabled(bag.stack)) {
				guiU += BUTTON_WIDTH;
			}
			
			Gui.drawModalRectWithCustomSizedTexture(horizontalMargin + BUTTON_HOFFSET,
					verticalMargin + BUTTON_VOFFSET,
					guiU,
					BUTTON_TEXT_VOFFSET,
					BUTTON_WIDTH, BUTTON_WIDTH,
					256, 256);
			
			int left = (width - xSize) / 2;
			int top = (height - ySize) / 2;
			
			left += BUTTON_HOFFSET;
			top += BUTTON_VOFFSET;
			
			if (mouseX >= left && mouseX <= left + BUTTON_WIDTH && 
					mouseY >= top && mouseY <= top + BUTTON_WIDTH) {
				GuiUtils.drawHoveringText(Lists.newArrayList(ReagentBag.isVacuumEnabled(bag.stack) ? "Disable Vacuum" : "Enable Vacuum"),
						mouseX, mouseY, width, height, 200, this.fontRendererObj);
			}
		}
			
		@Override
		protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
			int left = (width - xSize) / 2;
			int top = (height - ySize) / 2;
			
			left += BUTTON_HOFFSET;
			top += BUTTON_VOFFSET;
			
			if (mouseX >= left && mouseX <= left + BUTTON_WIDTH && 
					mouseY >= top && mouseY <= top + BUTTON_WIDTH) {
					ReagentBag.toggleVacuumEnabled(bag.stack);
					
					// Tell server what happened
					boolean val = ReagentBag.isVacuumEnabled(bag.stack);
					NetworkHandler.getSyncChannel().sendToServer(
			    			new ReagentBagToggleMessage(bag.bagPos != 40, val));
					NostrumMagicaSounds.UI_TICK.play(NostrumMagica.proxy.getPlayer());
			} else {
				super.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
	}
	
	
}