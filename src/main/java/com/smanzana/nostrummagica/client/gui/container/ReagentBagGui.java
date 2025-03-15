package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.equipment.ReagentBag;
import com.smanzana.nostrummagica.item.equipment.ReagentBag.ReagentInventory;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.ReagentBagToggleMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;

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
		
		public static final String ID = "reagent_bag";
		
		protected ReagentBag bag;
		protected @Nonnull ItemStack stack;
		protected ReagentInventory inventory;
		protected int bagPos;
		
		private int bagIDStart;
		
		public BagContainer(int windowId, PlayerInventory playerInv, ReagentBag bag, @Nonnull ItemStack stack, int bagPos) {
			super(NostrumContainers.ReagentBag, windowId);
			this.stack = stack;
			this.inventory = bag.asInventory(stack);
			this.bag = bag;
			this.bagPos = bagPos;

			// Construct player hotbar
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(playerInv, x, PLAYER_INV_HOFFSET + x * 18, 58 + (PLAYER_INV_VOFFSET)));
			}
			// Construct player inventory
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					this.addSlot(new Slot(playerInv, x + y * 9 + 9, PLAYER_INV_HOFFSET + (x * 18), PLAYER_INV_VOFFSET + (y * 18)));
				}
			}
			
			this.bagIDStart = this.slots.size();
			
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					
					this.addSlot(new Slot(inventory, i * 3 + j, BAG_INV_HOFFSET + j * 18, BAG_INV_VOFFSET + i * 18) {
						public boolean mayPlace(@Nonnull ItemStack stack) {
					        return this.container.canPlaceItem(this.getSlotIndex(), stack);
					    }
					});
				}
			}
		}
		
		public static final BagContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buffer) {
			final int slot = buffer.readVarInt();
			ItemStack stack = playerInv.getItem(slot);
			if (stack.isEmpty() || !(stack.getItem() instanceof ReagentBag)) {
				stack = new ItemStack(NostrumItems.reagentBag);
			}
			return new BagContainer(windowId, playerInv, (ReagentBag) stack.getItem(), stack, slot);
		}
		
		public static final IPackedContainerProvider Make(int slot) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				ItemStack stack = playerInv.getItem(slot);
				if (stack.isEmpty() || !(stack.getItem() instanceof ReagentBag)) {
					stack = new ItemStack(NostrumItems.reagentBag);
				}
				return new BagContainer(windowId, playerInv, (ReagentBag) stack.getItem(), stack, slot);
			}, (buffer) -> {
				buffer.writeVarInt(slot);
			});
		}
		
		@Override
		public @Nonnull ItemStack quickMoveStack(PlayerEntity playerIn, int fromSlot) {
			ItemStack prev = ItemStack.EMPTY;	
			Slot slot = (Slot) this.slots.get(fromSlot);
			IInventory inv = slot.container;
			
			if (slot.hasItem()) {
				ItemStack stack = slot.getItem();
				if (inv == inventory) {
					// shift-click in bag
					if (playerIn.inventory.add(stack.copy())) {
						slot.set(ItemStack.EMPTY);
					}
				} else {
					// shift-click in player inventory
					ItemStack leftover = inventory.addItem(stack);
					slot.set(leftover);
				}
			}
			
			if (slot.hasItem()) {
				ItemStack cur = slot.getItem();
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
					slot.set(ItemStack.EMPTY);
				} else {
					slot.setChanged();
				}
				
				if (cur.getCount() == prev.getCount()) {
					return ItemStack.EMPTY;
				}
				slot.onTake(playerIn, cur);
			}
			
			return prev;
		}
		
		@Override
		public boolean canDragTo(Slot slotIn) {
			return slotIn.container != this.inventory; // It's NOT bag inventory
		}
		
		@Override
		public boolean stillValid(PlayerEntity playerIn) {
			return true;
		}
		
		@Override
		public @Nonnull ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (slotId < bagIDStart) {
				if (slotId == bagPos) {
					return ItemStack.EMPTY;
				}
			}
			
			ItemStack itemstack = ItemStack.EMPTY;
			PlayerInventory inventoryplayer = player.inventory;

			if (clickTypeIn == ClickType.PICKUP && (dragType == 0 || dragType == 1)
					&& slotId >= 0 && !inventoryplayer.getCarried().isEmpty()) {

				Slot slot7 = (Slot)this.slots.get(slotId);

				if (slot7 != null) {
					ItemStack itemstack9 = slot7.getItem();
					ItemStack itemstack12 = inventoryplayer.getCarried();

					if (!itemstack9.isEmpty()) {
						itemstack = itemstack9.copy();
					}

					if (itemstack9.isEmpty()) {
						if (!itemstack12.isEmpty() && slot7.mayPlace(itemstack12)) {
							int l2 = dragType == 0 ? itemstack12.getCount() : 1;

							if (l2 > slot7.getMaxStackSize(itemstack12)) {
								l2 = slot7.getMaxStackSize(itemstack12);
							}

							slot7.set(itemstack12.split(l2));

							if (itemstack12.isEmpty()) {
								inventoryplayer.setCarried(ItemStack.EMPTY);
							}
						}
					} else if (slot7.mayPickup(player)) {
						if (itemstack12.isEmpty()) {
							if (!itemstack9.isEmpty()) {
								int k2 = dragType == 0 ? itemstack9.getCount() : (itemstack9.getCount() + 1) / 2;
								inventoryplayer.setCarried(slot7.remove(k2));

								if (itemstack9.isEmpty()) {
									slot7.set(ItemStack.EMPTY);
								}

								slot7.onTake(player, inventoryplayer.getCarried());
							} else {
								slot7.set(ItemStack.EMPTY);
								inventoryplayer.setCarried(ItemStack.EMPTY);
							}
						} else if (slot7.mayPlace(itemstack12)) {
							if (itemstack9.getItem() == itemstack12.getItem() && ItemStack.tagMatches(itemstack9, itemstack12)) {
								int j2 = dragType == 0 ? itemstack12.getCount() : 1;

								if (j2 > slot7.getMaxStackSize(itemstack12) - itemstack9.getCount()) {
									j2 = slot7.getMaxStackSize(itemstack12) - itemstack9.getCount();
								}

								//if (j2 > itemstack12.getMaxStackSize() - itemstack9.getCount()) {
								//	j2 = itemstack12.getMaxStackSize() - itemstack9.getCount();
								//}

								itemstack12.split(j2);

								if (itemstack12.isEmpty()) {
									inventoryplayer.setCarried(ItemStack.EMPTY);
								}

								itemstack9.grow(j2);
							} else if (itemstack12.getCount() <= slot7.getMaxStackSize(itemstack12)) {
								slot7.set(itemstack12);
								inventoryplayer.setCarried(itemstack9);
							}
						} else if (itemstack9.getItem() == itemstack12.getItem() && itemstack12.getMaxStackSize() > 1 && ItemStack.tagMatches(itemstack9, itemstack12)) {
							int i2 = itemstack9.getCount();

							if (i2 > 0 && i2 + itemstack12.getCount() <= itemstack12.getMaxStackSize()) {
								itemstack12.grow(i2);
								itemstack9 = slot7.remove(i2);

								if (itemstack9.isEmpty()) {
									slot7.set(ItemStack.EMPTY);
								}

								slot7.onTake(player, inventoryplayer.getCarried());
							}
						}
					}

					slot7.setChanged();
				}
	            
				this.broadcastChanges();
				return itemstack;
			} else {
				return super.clicked(slotId, dragType, clickTypeIn, player);
			}
	        
		}
		
		public static boolean canAddItemToSlot(Slot slotIn, ItemStack stack, boolean stackSizeMatters) {
			boolean flag = slotIn == null || !slotIn.hasItem();

			if (slotIn != null && slotIn.hasItem() && !stack.isEmpty() && stack.sameItem(slotIn.getItem()) && ItemStack.tagMatches(slotIn.getItem(), stack)){
				flag |= slotIn.getItem().getCount() + (stackSizeMatters ? 0 : stack.getCount()) <= slotIn.getMaxStackSize();
			}

			return flag;
		}

	}
	
	@OnlyIn(Dist.CLIENT)
	public static class BagGui extends AutoGuiContainer<BagContainer> {

		private BagContainer bag;
		
		public BagGui(BagContainer bag, PlayerInventory playerInv, ITextComponent name) {
			super(bag, playerInv, name);
			this.bag = bag;
			this.imageWidth = GUI_WIDTH;
			this.imageHeight = GUI_HEIGHT;
		}
		
		@Override
		protected void renderBg(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			int horizontalMargin = (width - imageWidth) / 2;
			int verticalMargin = (height - imageHeight) / 2;
			
			mc.getTextureManager().bind(TEXT);
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin,0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
			
			int guiU = 0;
			if (ReagentBag.isVacuumEnabled(bag.stack)) {
				guiU += BUTTON_WIDTH;
			}
			
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn,
					horizontalMargin + BUTTON_HOFFSET,
					verticalMargin + BUTTON_VOFFSET,
					guiU,
					BUTTON_TEXT_VOFFSET, BUTTON_WIDTH,
					BUTTON_WIDTH, 256, 256);
			
			int left = (width - imageWidth) / 2;
			int top = (height - imageHeight) / 2;
			
			left += BUTTON_HOFFSET;
			top += BUTTON_VOFFSET;
			
			if (mouseX >= left && mouseX <= left + BUTTON_WIDTH && 
					mouseY >= top && mouseY <= top + BUTTON_WIDTH) {
				GuiUtils.drawHoveringText(matrixStackIn, Lists.newArrayList(new StringTextComponent(ReagentBag.isVacuumEnabled(bag.stack) ? "Disable Vacuum" : "Enable Vacuum")),
						mouseX, mouseY, width, height, 200, this.font);
			}
		}
		
		@Override
		protected void renderLabels(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			// no labels
			//super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
		}
			
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
			int left = (width - imageWidth) / 2;
			int top = (height - imageHeight) / 2;
			
			left += BUTTON_HOFFSET;
			top += BUTTON_VOFFSET;
			
			if (mouseX >= left && mouseX <= left + BUTTON_WIDTH && 
					mouseY >= top && mouseY <= top + BUTTON_WIDTH) {
					ReagentBag.toggleVacuumEnabled(bag.stack);
					
					// Tell server what happened
					boolean val = ReagentBag.isVacuumEnabled(bag.stack);
					NetworkHandler.sendToServer(
			    			new ReagentBagToggleMessage(bag.bagPos != 40, val));
					NostrumMagicaSounds.UI_TICK.play(NostrumMagica.instance.proxy.getPlayer());
					
					return true;
			} else {
				return super.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
	}
	
	
}