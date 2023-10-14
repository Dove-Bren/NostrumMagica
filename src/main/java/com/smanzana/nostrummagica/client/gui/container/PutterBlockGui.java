package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tiles.PutterBlockTileEntity;
import com.smanzana.nostrummagica.utils.ContainerUtil;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.utils.Inventories;
import com.smanzana.nostrummagica.utils.RenderFuncs;

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

public class PutterBlockGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/putter.png");

	private static final int GUI_WIDTH = 176;
	private static final int GUI_HEIGHT = 166;
	private static final int PLAYER_INV_HOFFSET = 8;
	private static final int PLAYER_INV_VOFFSET = 84;
	private static final int PUTTER_INV_HOFFSET = 62;
	private static final int PUTTER_INV_VOFFSET = 17;
	
	public static class PutterBlockContainer extends Container {
		
		public static final String ID = "putter";
		
		protected final PutterBlockTileEntity putter;
		
		public PutterBlockContainer(int windowId, PlayerInventory playerInv, PutterBlockTileEntity putter) {
			super(NostrumContainers.Putter, windowId);
			
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
			
			// Construct putter inventory
			IInventory putterInv = putter.getInventory();
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 3; x++) {
					this.addSlot(new Slot(putterInv, x + y * 3, PUTTER_INV_HOFFSET + (x * 18), PUTTER_INV_VOFFSET + (y * 18)));
				}
			}
			
			this.putter = putter;
		}
		
		@OnlyIn(Dist.CLIENT)
		public static final PutterBlockContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buffer) {
			return new PutterBlockContainer(windowId, playerInv, ContainerUtil.GetPackedTE(buffer));
		}
		
		public static final IPackedContainerProvider Make(PutterBlockTileEntity putter) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new PutterBlockContainer(windowId, playerInv, putter);
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, putter);
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
		public boolean canInteractWith(PlayerEntity playerIn) {
			return true;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class PutterBlockGuiContainer extends AutoGuiContainer<PutterBlockContainer> {

		//private PutterBlockContainer container;
		
		public PutterBlockGuiContainer(PutterBlockContainer container, PlayerInventory playerInv, ITextComponent name) {
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