package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.api.IPositionHolderItem;
import com.smanzana.nostrummagica.item.equipment.SilverMirrorItem;
import com.smanzana.nostrummagica.tile.ParadoxMirrorTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;

public class SilverMirrorGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/silver_mirror_gui.png");
	
	private static final int GUI_WIDTH = 176;
	private static final int GUI_HEIGHT = 175;
	private static final int GUI_MIRROR_INV_HOFFSET = 81;
	private static final int GUI_MIRROR_INV_VOFFSET = 27;
	private static final int GUI_PLAYER_INV_HOFFSET = 8;
	private static final int GUI_PLAYER_INV_VOFFSET = 93;
	private static final int GUI_HOTBAR_INV_HOFFSET = 8;
	private static final int GUI_HOTBAR_INV_VOFFSET = 151;
	
	public static class MirrorContainer extends AbstractContainerMenu {
		
		public static final String ID = "silver_mirror_container";
		
		protected @Nonnull ItemStack mirrorStack;
		protected SimpleContainer inventory;
		protected final int mirrorPos;
		protected final boolean isServer;
		
		public MirrorContainer(int windowId, Inventory playerInv, @Nonnull ItemStack mirrorStack, int mirrorPos) {
			super(NostrumContainers.SilverMirror, windowId);
			this.mirrorStack = mirrorStack;
			this.inventory = new SimpleContainer(1);
			this.mirrorPos = mirrorPos;
			this.isServer = !playerInv.player.level.isClientSide();

			// Construct player hotbar
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(playerInv, x, GUI_HOTBAR_INV_HOFFSET + x * 18, (GUI_HOTBAR_INV_VOFFSET)));
			}
			// Construct player inventory
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					this.addSlot(new Slot(playerInv, x + y * 9 + 9, GUI_PLAYER_INV_HOFFSET + (x * 18), GUI_PLAYER_INV_VOFFSET + (y * 18)));
				}
			}
			
			this.addSlot(new Slot(inventory, 0, GUI_MIRROR_INV_HOFFSET, GUI_MIRROR_INV_VOFFSET));
			
			inventory.addListener((inv) -> {
				if (isServer) {
					playerInv.player.level.getServer().tell(new TickTask(playerInv.player.level.getServer().getTickCount(), MirrorContainer.this::onInventoryChange));
				}
			});
		}
		
		public static final MirrorContainer FromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buffer) {
			final int slot = buffer.readVarInt();
			ItemStack stack = playerInv.getItem(slot);
			if (stack.isEmpty() || !(stack.getItem() instanceof SilverMirrorItem)) {
				stack = new ItemStack(NostrumItems.silverMirror);
			}
			return new MirrorContainer(windowId, playerInv, stack, slot);
		}
		
		public static final IPackedContainerProvider Make(int slot) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				ItemStack stack = playerInv.getItem(slot);
				if (stack.isEmpty() || !(stack.getItem() instanceof SilverMirrorItem)) {
					stack = new ItemStack(NostrumItems.silverMirror);
				}
				return new MirrorContainer(windowId, playerInv, stack, slot);
			}, (buffer) -> {
				buffer.writeVarInt(slot);
			});
		}
		
		protected void onInventoryChange() {
			ItemStack stack = inventory.getItem(0);
			if (!stack.isEmpty()) {
				if (attemptSend(stack)) {
					inventory.setItem(0, ItemStack.EMPTY);
					inventory.setChanged();
				}
			}
		}
		
		protected boolean attemptSend(ItemStack stack) {
			if (this.isServer) {
				final ResourceKey<Level> dimension = IPositionHolderItem.getDimension(this.mirrorStack);
				final BlockPos pos = IPositionHolderItem.getBlockPosition(this.mirrorStack);
				
				if (dimension != null && pos != null) {
					final Level world = ServerLifecycleHooks.getCurrentServer().getLevel(dimension);
					if (world != null && NostrumMagica.isBlockLoaded(world, pos)) {
						BlockEntity te = world.getBlockEntity(pos);
						if (te != null && te instanceof ParadoxMirrorTileEntity) {
							((ParadoxMirrorTileEntity) te).receiveAndSpawnItem(stack, pos.offset(0, 2, 0));
							return true;
						}
					}
				}
				
				return false;
			}
			
			// Just guess on the client :(
			return false;
		}
		
		@Override
		public @Nonnull ItemStack quickMoveStack(Player playerIn, int fromSlot) {
			ItemStack prev = ItemStack.EMPTY;	
			Slot slot = (Slot) this.slots.get(fromSlot);
			Container inv = slot.container;
			
			if (slot.hasItem()) {
				ItemStack stack = slot.getItem();
				if (inv == inventory) {
					// shift-click in bag
					if (playerIn.getInventory().add(stack.copy())) {
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
		public boolean stillValid(Player playerIn) {
			return true;
		}
		
		@Override
		public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
			if (slotId == mirrorPos) {
				return;
			}
			
			if (clickTypeIn == ClickType.PICKUP && (dragType == 0 || dragType == 1)
					&& slotId >= 0 && !getCarried().isEmpty()) {

				Slot slot7 = (Slot)this.slots.get(slotId);

				if (slot7 != null) {
					ItemStack itemstack9 = slot7.getItem();
					ItemStack itemstack12 = getCarried();

					if (itemstack9.isEmpty()) {
						if (!itemstack12.isEmpty() && slot7.mayPlace(itemstack12)) {
							int l2 = dragType == 0 ? itemstack12.getCount() : 1;

							if (l2 > slot7.getMaxStackSize(itemstack12)) {
								l2 = slot7.getMaxStackSize(itemstack12);
							}

							slot7.set(itemstack12.split(l2));

							if (itemstack12.isEmpty()) {
								setCarried(ItemStack.EMPTY);
							}
						}
					} else if (slot7.mayPickup(player)) {
						if (itemstack12.isEmpty()) {
							if (!itemstack9.isEmpty()) {
								int k2 = dragType == 0 ? itemstack9.getCount() : (itemstack9.getCount() + 1) / 2;
								setCarried(slot7.remove(k2));

								if (itemstack9.isEmpty()) {
									slot7.set(ItemStack.EMPTY);
								}

								slot7.onTake(player, getCarried());
							} else {
								slot7.set(ItemStack.EMPTY);
								setCarried(ItemStack.EMPTY);
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
									setCarried(ItemStack.EMPTY);
								}

								itemstack9.grow(j2);
							} else if (itemstack12.getCount() <= slot7.getMaxStackSize(itemstack12)) {
								slot7.set(itemstack12);
								setCarried(itemstack9);
							}
						} else if (itemstack9.getItem() == itemstack12.getItem() && itemstack12.getMaxStackSize() > 1 && ItemStack.tagMatches(itemstack9, itemstack12)) {
							int i2 = itemstack9.getCount();

							if (i2 > 0 && i2 + itemstack12.getCount() <= itemstack12.getMaxStackSize()) {
								itemstack12.grow(i2);
								itemstack9 = slot7.remove(i2);

								if (itemstack9.isEmpty()) {
									slot7.set(ItemStack.EMPTY);
								}

								slot7.onTake(player, getCarried());
							}
						}
					}

					slot7.setChanged();
				}
	            
				this.broadcastChanges();
				return; // used to return itemstack
			} else {
				super.clicked(slotId, dragType, clickTypeIn, player);
			}
	        
		}
		
		@Override
		public void removed(Player playerIn) {
			super.removed(playerIn);
			this.clearContainer(playerIn, inventory);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class MirrorGui extends AutoGuiContainer<MirrorContainer> {

		public MirrorGui(MirrorContainer container, Inventory playerInv, Component name) {
			super(container, playerInv, name);
			this.imageWidth = GUI_WIDTH;
			this.imageHeight = GUI_HEIGHT;
		}
		
		@Override
		protected void renderBg(PoseStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			int horizontalMargin = (width - imageWidth) / 2;
			int verticalMargin = (height - imageHeight) / 2;
			
			RenderSystem.setShaderTexture(0, TEXT);
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin,0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
		}
		
		@Override
		protected void renderLabels(PoseStack matrixStackIn, int mouseX, int mouseY) {
			// no labels
			//super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
		}
			
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
			return super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}
}