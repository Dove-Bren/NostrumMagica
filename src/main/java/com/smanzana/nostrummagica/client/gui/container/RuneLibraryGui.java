package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.RuneLibraryTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.Inventories;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RuneLibraryGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/rune_library.png");

	private static final int GUI_WIDTH = 176;
	private static final int GUI_HEIGHT = 166;
	private static final int PLAYER_INV_HOFFSET = 8;
	private static final int PLAYER_INV_VOFFSET = 84;
	private static final int RUNELIBRARY_INV_HOFFSET = 8;
	private static final int RUNELIBRARY_INV_VOFFSET = 17;
	
	public static class RuneLibraryContainer extends Container {
		
		public static final String ID = "rune_library";
		
		protected final RuneLibraryTileEntity library;
		
		public RuneLibraryContainer(int windowId, PlayerInventory playerInv, RuneLibraryTileEntity library) {
			super(NostrumContainers.RuneLibrary, windowId);
			
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
			
			// Construct library inventory
			IInventory libraryInv = library.getInventory();
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					this.addSlot(new Slot(libraryInv, x + y * 9, RUNELIBRARY_INV_HOFFSET + (x * 18), RUNELIBRARY_INV_VOFFSET + (y * 18)) {
						@Override
						public boolean isItemValid(ItemStack stack) {
							return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
						}
					});
				}
			}
			
			this.library = library;
		}
		
		public static final RuneLibraryContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buffer) {
			return new RuneLibraryContainer(windowId, playerInv, ContainerUtil.GetPackedTE(buffer));
		}
		
		public static final IPackedContainerProvider Make(RuneLibraryTileEntity library) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new RuneLibraryContainer(windowId, playerInv, library);
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, library);
			});
		}
		
		@Override
		@Nonnull
		public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
			Slot slot = (Slot)this.inventorySlots.get(index);
			ItemStack prev = ItemStack.EMPTY;

			if (slot != null && slot.getHasStack()) {
				//IInventory from = slot.inventory;
				IInventory to;
				
				if (slot.inventory == library.getInventory()) {
					to = playerIn.inventory;
				} else {
					to = library.getInventory();
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
	public static class Gui extends AutoGuiContainer<RuneLibraryContainer> {

		public Gui(RuneLibraryContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			//this.container = container;
			
			this.xSize = GUI_WIDTH;
			this.ySize = GUI_HEIGHT;
		}
		
		@Override
		public void init() {
			super.init();
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
			super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
		}
		
	}
}