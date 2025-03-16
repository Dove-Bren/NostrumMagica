package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.DungeonLauncherTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.Inventories;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LauncherBlockGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/putter.png");

	private static final int GUI_WIDTH = 176;
	private static final int GUI_HEIGHT = 166;
	private static final int PLAYER_INV_HOFFSET = 8;
	private static final int PLAYER_INV_VOFFSET = 84;
	private static final int LAUNCHER_INV_HOFFSET = 62;
	private static final int LAUNCHER_INV_VOFFSET = 17;
	
	public static class LauncherBlockContainer extends AbstractContainerMenu {
		
		public static final String ID = "launcher";
		
		protected final DungeonLauncherTileEntity launcher;
		
		public LauncherBlockContainer(int windowId, Inventory playerInv, DungeonLauncherTileEntity launcher) {
			super(NostrumContainers.Launcher, windowId);
			
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
			
			// Construct launcher inventory
			Container launcherInv = launcher.getInventory();
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 3; x++) {
					this.addSlot(new Slot(launcherInv, x + y * 3, LAUNCHER_INV_HOFFSET + (x * 18), LAUNCHER_INV_VOFFSET + (y * 18)));
				}
			}
			
			this.launcher = launcher;
		}
		
		public static final LauncherBlockContainer FromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buffer) {
			return new LauncherBlockContainer(windowId, playerInv, ContainerUtil.GetPackedTE(buffer));
		}
		
		public static final IPackedContainerProvider Make(DungeonLauncherTileEntity launcher) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new LauncherBlockContainer(windowId, playerInv, launcher);
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, launcher);
			});
		}
		
		@Override
		@Nonnull
		public ItemStack quickMoveStack(Player playerIn, int index) {
			Slot slot = (Slot)this.slots.get(index);
			ItemStack prev = ItemStack.EMPTY;

			if (slot != null && slot.hasItem()) {
				//IInventory from = slot.inventory;
				Container to;
				
				if (slot.container == launcher.getInventory()) {
					to = playerIn.inventory;
				} else {
					to = launcher.getInventory();
				}
				
				ItemStack stack = slot.getItem();
				prev = stack.copy();

				stack = Inventories.addItem(to, stack);
				if (!stack.isEmpty() && stack.getCount() == 0) {
					stack = ItemStack.EMPTY;
				}
				
				if (!stack.isEmpty() && stack.getCount() == prev.getCount()) {
					return ItemStack.EMPTY;
				};
				
				slot.set(stack);
				slot.onTake(playerIn, stack);
			}

			return prev;
		}
		
		@Override
		public boolean canDragTo(Slot slotIn) {
			return true;
		}
		
		@Override
		public boolean stillValid(Player playerIn) {
			return true;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class LauncherBlockGuiContainer extends AutoGuiContainer<LauncherBlockContainer> {

		//private LauncherBlockContainer container;
		
		public LauncherBlockGuiContainer(LauncherBlockContainer container, Inventory playerInv, Component name) {
			super(container, playerInv, name);
			//this.container = container;
			
			this.imageWidth = GUI_WIDTH;
			this.imageHeight = GUI_HEIGHT;
		}
		
		@Override
		public void init() {
			super.init();
		}
		
		@Override
		protected void renderBg(PoseStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			int horizontalMargin = (width - imageWidth) / 2;
			int verticalMargin = (height - imageHeight) / 2;
			
			mc.getTextureManager().bind(TEXT);
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin,0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
		}
		
		@Override
		protected void renderLabels(PoseStack matrixStackIn, int mouseX, int mouseY) {
			super.renderLabels(matrixStackIn, mouseX, mouseY);
		}
		
	}
}