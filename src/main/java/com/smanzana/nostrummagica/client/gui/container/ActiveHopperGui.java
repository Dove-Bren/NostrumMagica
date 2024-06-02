package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.ActiveHopperTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.Inventories;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.client.Minecraft;
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

public class ActiveHopperGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/active_hopper.png");

	private static final int GUI_WIDTH = 176;
	private static final int GUI_HEIGHT = 166;
	private static final int PLAYER_INV_HOFFSET = 8;
	private static final int PLAYER_INV_VOFFSET = 51;
	private static final int PUTTER_INV_HOFFSET = 80;
	private static final int PUTTER_INV_VOFFSET = 20;
	
	public static class ActiveHopperContainer extends Container {
		
		public static final String ID = "active_hopper";
		
		protected final ActiveHopperTileEntity hopper;
		
		public ActiveHopperContainer(int windowId, IInventory playerInv, ActiveHopperTileEntity hopper) {
			super(NostrumContainers.ActiveHopper, windowId);

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
			
			// Construct hopper inventory
			this.addSlot(new Slot(hopper, 0, PUTTER_INV_HOFFSET, PUTTER_INV_VOFFSET));
			
			this.hopper = hopper;
		}
		
		public static ActiveHopperContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
			return new ActiveHopperContainer(windowId, playerInv, ContainerUtil.GetPackedTE(buf));
		}
		
		public static IPackedContainerProvider Make(ActiveHopperTileEntity hopper) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new ActiveHopperContainer(windowId, playerInv, hopper);
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, hopper);
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
	public static class ActiveHopperGuiContainer extends AutoGuiContainer<ActiveHopperContainer> {

		public ActiveHopperGuiContainer(ActiveHopperContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			
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
			
			Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin,0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
		}
		
	}
}