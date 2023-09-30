package com.smanzana.nostrummagica.client.gui.container;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentBag.ReagentInventory;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ReagentBagToggleMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.gui.Gui;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
		protected @Nonnull ItemStack stack;
		protected ReagentInventory inventory;
		protected int bagPos;
		
		private int bagIDStart;
		
		public BagContainer(IInventory playerInv, ReagentBag bag, @Nonnull ItemStack stack, int bagPos) {
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
						public boolean isItemValid(@Nonnull ItemStack stack) {
					        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
					    }
					});
				}
			}
		}
		
		@Override
		public @Nonnull ItemStack transferStackInSlot(PlayerEntity playerIn, int fromSlot) {
			ItemStack prev = ItemStack.EMPTY;	
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			IInventory inv = slot.inventory;
			
			if (slot.getHasStack()) {
				ItemStack stack = slot.getStack();
				if (inv == inventory) {
					// shift-click in bag
					if (playerIn.inventory.addItemStackToInventory(stack.copy())) {
						slot.putStack(ItemStack.EMPTY);
					}
				} else {
					// shift-click in player inventory
					ItemStack leftover = inventory.addItem(stack);
					slot.putStack(leftover);
				}
			}
			
			if (slot.getHasStack()) {
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
				
				if (cur.isEmpty()) {
					slot.putStack(ItemStack.EMPTY);
				} else {
					slot.onSlotChanged();
				}
				
				if (cur.getCount() == prev.getCount()) {
					return ItemStack.EMPTY;
				}
				slot.onTake(playerIn, cur);
			}
			
			return prev;
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn.inventory != this.inventory; // It's NOT bag inventory
		}
		
		@Override
		public boolean canInteractWith(PlayerEntity playerIn) {
			return true;
		}
		
		@Override
		public @Nonnull ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (slotId < bagIDStart) {
				if (slotId == bagPos) {
					return ItemStack.EMPTY;
				}
			}
			
			ItemStack itemstack = ItemStack.EMPTY;
			InventoryPlayer inventoryplayer = player.inventory;

			if (clickTypeIn == ClickType.PICKUP && (dragType == 0 || dragType == 1)
					&& slotId >= 0 && !inventoryplayer.getItemStack().isEmpty()) {

				Slot slot7 = (Slot)this.inventorySlots.get(slotId);

				if (slot7 != null) {
					ItemStack itemstack9 = slot7.getStack();
					ItemStack itemstack12 = inventoryplayer.getItemStack();

					if (!itemstack9.isEmpty()) {
						itemstack = itemstack9.copy();
					}

					if (itemstack9.isEmpty()) {
						if (!itemstack12.isEmpty() && slot7.isItemValid(itemstack12)) {
							int l2 = dragType == 0 ? itemstack12.getCount() : 1;

							if (l2 > slot7.getItemStackLimit(itemstack12)) {
								l2 = slot7.getItemStackLimit(itemstack12);
							}

							slot7.putStack(itemstack12.split(l2));

							if (itemstack12.isEmpty()) {
								inventoryplayer.setItemStack(ItemStack.EMPTY);
							}
						}
					} else if (slot7.canTakeStack(player)) {
						if (itemstack12.isEmpty()) {
							if (!itemstack9.isEmpty()) {
								int k2 = dragType == 0 ? itemstack9.getCount() : (itemstack9.getCount() + 1) / 2;
								inventoryplayer.setItemStack(slot7.decrStackSize(k2));

								if (itemstack9.isEmpty()) {
									slot7.putStack(ItemStack.EMPTY);
								}

								slot7.onTake(player, inventoryplayer.getItemStack());
							} else {
								slot7.putStack(ItemStack.EMPTY);
								inventoryplayer.setItemStack(ItemStack.EMPTY);
							}
						} else if (slot7.isItemValid(itemstack12)) {
							if (itemstack9.getItem() == itemstack12.getItem() && itemstack9.getMetadata() == itemstack12.getMetadata() && ItemStack.areItemStackTagsEqual(itemstack9, itemstack12)) {
								int j2 = dragType == 0 ? itemstack12.getCount() : 1;

								if (j2 > slot7.getItemStackLimit(itemstack12) - itemstack9.getCount()) {
									j2 = slot7.getItemStackLimit(itemstack12) - itemstack9.getCount();
								}

								//if (j2 > itemstack12.getMaxStackSize() - itemstack9.getCount()) {
								//	j2 = itemstack12.getMaxStackSize() - itemstack9.getCount();
								//}

								itemstack12.split(j2);

								if (itemstack12.isEmpty()) {
									inventoryplayer.setItemStack(ItemStack.EMPTY);
								}

								itemstack9.grow(j2);
							} else if (itemstack12.getCount() <= slot7.getItemStackLimit(itemstack12)) {
								slot7.putStack(itemstack12);
								inventoryplayer.setItemStack(itemstack9);
							}
						} else if (itemstack9.getItem() == itemstack12.getItem() && itemstack12.getMaxStackSize() > 1 && (!itemstack9.getHasSubtypes() || itemstack9.getMetadata() == itemstack12.getMetadata()) && ItemStack.areItemStackTagsEqual(itemstack9, itemstack12)) {
							int i2 = itemstack9.getCount();

							if (i2 > 0 && i2 + itemstack12.getCount() <= itemstack12.getMaxStackSize()) {
								itemstack12.grow(i2);
								itemstack9 = slot7.decrStackSize(i2);

								if (itemstack9.isEmpty()) {
									slot7.putStack(ItemStack.EMPTY);
								}

								slot7.onTake(player, inventoryplayer.getItemStack());
							}
						}
					}

					slot7.onSlotChanged();
				}
	            
				this.detectAndSendChanges();
				return itemstack;
			} else {
				return super.slotClick(slotId, dragType, clickTypeIn, player);
			}
	        
		}
		
		public static boolean canAddItemToSlot(Slot slotIn, ItemStack stack, boolean stackSizeMatters) {
			boolean flag = slotIn == null || !slotIn.getHasStack();

			if (slotIn != null && slotIn.getHasStack() && !stack.isEmpty() && stack.isItemEqual(slotIn.getStack()) && ItemStack.areItemStackTagsEqual(slotIn.getStack(), stack)){
				flag |= slotIn.getStack().getCount() + (stackSizeMatters ? 0 : stack.getCount()) <= slotIn.getSlotStackLimit();
			}

			return flag;
		}

	}
	
	@OnlyIn(Dist.CLIENT)
	public static class BagGui extends AutoGuiContainer {

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
			
			GlStateManager.color4f(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTexture(horizontalMargin, verticalMargin, 0,0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
			
			int guiU = 0;
			if (ReagentBag.isVacuumEnabled(bag.stack)) {
				guiU += BUTTON_WIDTH;
			}
			
			RenderFuncs.drawModalRectWithCustomSizedTexture(horizontalMargin + BUTTON_HOFFSET,
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
						mouseX, mouseY, width, height, 200, this.font);
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
					NostrumMagicaSounds.UI_TICK.play(NostrumMagica.instance.proxy.getPlayer());
			} else {
				super.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
	}
	
	
}