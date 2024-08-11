package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.IPositionHolderItem;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.equipment.SilverMirrorItem;
import com.smanzana.nostrummagica.tile.ParadoxMirrorTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

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
	
	public static class MirrorContainer extends Container {
		
		public static final String ID = "silver_mirror_container";
		
		protected @Nonnull ItemStack mirrorStack;
		protected Inventory inventory;
		protected final int mirrorPos;
		protected final boolean isServer;
		
		public MirrorContainer(int windowId, PlayerInventory playerInv, @Nonnull ItemStack mirrorStack, int mirrorPos) {
			super(NostrumContainers.SilverMirror, windowId);
			this.mirrorStack = mirrorStack;
			this.inventory = new Inventory(1);
			this.mirrorPos = mirrorPos;
			this.isServer = !playerInv.player.world.isRemote();

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
					playerInv.player.world.getServer().enqueue(new TickDelayedTask(playerInv.player.world.getServer().getTickCounter(), MirrorContainer.this::onInventoryChange));
				}
			});
		}
		
		public static final MirrorContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buffer) {
			final int slot = buffer.readVarInt();
			ItemStack stack = playerInv.getStackInSlot(slot);
			if (stack.isEmpty() || !(stack.getItem() instanceof SilverMirrorItem)) {
				stack = new ItemStack(NostrumItems.silverMirror);
			}
			return new MirrorContainer(windowId, playerInv, stack, slot);
		}
		
		public static final IPackedContainerProvider Make(int slot) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				ItemStack stack = playerInv.getStackInSlot(slot);
				if (stack.isEmpty() || !(stack.getItem() instanceof SilverMirrorItem)) {
					stack = new ItemStack(NostrumItems.silverMirror);
				}
				return new MirrorContainer(windowId, playerInv, stack, slot);
			}, (buffer) -> {
				buffer.writeVarInt(slot);
			});
		}
		
		protected void onInventoryChange() {
			ItemStack stack = inventory.getStackInSlot(0);
			if (!stack.isEmpty()) {
				if (attemptSend(stack)) {
					inventory.setInventorySlotContents(0, ItemStack.EMPTY);
					inventory.markDirty();
				}
			}
		}
		
		protected boolean attemptSend(ItemStack stack) {
			if (this.isServer) {
				final RegistryKey<World> dimension = IPositionHolderItem.getDimension(this.mirrorStack);
				final BlockPos pos = IPositionHolderItem.getBlockPosition(this.mirrorStack);
				
				if (dimension != null && pos != null) {
					final World world = ServerLifecycleHooks.getCurrentServer().getWorld(dimension);
					if (world != null && NostrumMagica.isBlockLoaded(world, pos)) {
						TileEntity te = world.getTileEntity(pos);
						if (te != null && te instanceof ParadoxMirrorTileEntity) {
							((ParadoxMirrorTileEntity) te).receiveAndSpawnItem(stack, pos.add(0, 2, 0));
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
			if (slotId == mirrorPos) {
				return ItemStack.EMPTY; // don't touch the mirror slot
			}
			
			ItemStack itemstack = ItemStack.EMPTY;
			PlayerInventory inventoryplayer = player.inventory;

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
							if (itemstack9.getItem() == itemstack12.getItem() && ItemStack.areItemStackTagsEqual(itemstack9, itemstack12)) {
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
						} else if (itemstack9.getItem() == itemstack12.getItem() && itemstack12.getMaxStackSize() > 1 && ItemStack.areItemStackTagsEqual(itemstack9, itemstack12)) {
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
		
		@Override
		public void onContainerClosed(PlayerEntity playerIn) {
			super.onContainerClosed(playerIn);
			this.clearContainer(playerIn, playerIn.world, inventory);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class MirrorGui extends AutoGuiContainer<MirrorContainer> {

		public MirrorGui(MirrorContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			this.xSize = GUI_WIDTH;
			this.ySize = GUI_HEIGHT;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			mc.getTextureManager().bindTexture(TEXT);
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin,0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			// no labels
			//super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
		}
			
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
			return super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}
}