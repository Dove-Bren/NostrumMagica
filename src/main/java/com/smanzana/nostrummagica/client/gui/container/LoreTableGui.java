package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.tile.LoreTableTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.client.Minecraft;
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

public class LoreTableGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/lore_table.png");

	private static final int GUI_WIDTH = 176;
	private static final int GUI_HEIGHT = 175;
	private static final int PLAYER_INV_HOFFSET = 8;
	private static final int PLAYER_INV_VOFFSET = 93;
	private static final int SLOT_INPUT_HOFFSET = 80;
	private static final int SLOT_INPUT_VOFFSET = 34;
	
	private static final int PROGRESS_WIDTH = 35;
	private static final int PROGRESS_HEIGHT = 38;
	private static final int PROGRESS_GUI_HOFFSET = 71;
	private static final int PROGRESS_GUI_VOFFSET = 22;
	
	private static final int SHINE_LENGTH = 16;
	
	public static class LoreTableContainer extends AbstractContainerMenu {
		
		public static final String ID = "lore_table";
		
		// Kept just to report to server which TE is doing crafting
		protected Player player;
		
		// Actual container variables as well as a couple for keeping track
		// of crafting state
		protected LoreTableTileEntity table;
		protected Slot inputSlot;
		
		public LoreTableContainer(int windowId, Player player, Container playerInv, LoreTableTileEntity table) {
			super(NostrumContainers.LoreTable, windowId);
			this.player = player;
			this.table = table;
			this.inputSlot = new Slot(null, 0, SLOT_INPUT_HOFFSET, SLOT_INPUT_VOFFSET) {
				
				@Override
				public boolean mayPlace(@Nonnull ItemStack stack) {
					return stack.isEmpty() || stack.getItem() instanceof ILoreTagged;
				}
				
				@Override
				public void set(@Nonnull ItemStack stack) {
					// Swapping items does this instead of a take
					if (!table.getItem().isEmpty()) {
						table.onTakeItem(player);
					}
					
					table.setItem(stack);
					this.setChanged();
				}
				
				@Override
				public ItemStack getItem() {
					return table.getItem();
				}
				
				@Override
				public void setChanged() {
					table.setChanged();
				}
				
				public int getMaxStackSize() {
					return 1;
				}
				
				public ItemStack remove(int amount) {
					ItemStack item = table.getItem();
					if (!item.isEmpty()) {
						if (table.setItem(ItemStack.EMPTY))
							return item.copy();
					}
					
					return ItemStack.EMPTY;
				}
				
				public boolean isSameInventory(Slot other) {
					return false;
				}
				
				public ItemStack onTake(Player playerIn, ItemStack stack) {
					table.onTakeItem(playerIn);
					return super.onTake(playerIn, stack);
				}
				
			};
			
			this.addSlot(inputSlot);
			
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
			
		}
		
		public static final LoreTableContainer FromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
			return new LoreTableContainer(windowId, playerInv.player, playerInv, ContainerUtil.GetPackedTE(buf));
		}
		
		public static IPackedContainerProvider Make(LoreTableTileEntity table) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new LoreTableContainer(windowId, playerInv.player, playerInv, table);
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, table);
			});
		}
		
		@Override
		public ItemStack quickMoveStack(Player playerIn, int fromSlot) {
			@Nonnull ItemStack prev = ItemStack.EMPTY;	
			Slot slot = (Slot) this.slots.get(fromSlot);
			
			if (slot != null && slot.hasItem()) {
				ItemStack cur = slot.getItem();
				prev = cur.copy();
				
				if (slot == this.inputSlot) {
					// Trying to take our item
					if (playerIn.inventory.add(cur)) {
						inputSlot.set(ItemStack.EMPTY);
						inputSlot.onTake(playerIn, cur);
					} else {
						prev = ItemStack.EMPTY;
					}
				} else {
					// Trying to add an item
					if (!inputSlot.hasItem()
							&& inputSlot.mayPlace(cur)) {
						ItemStack stack = cur.split(1);
						inputSlot.set(stack);
					} else {
						prev = ItemStack.EMPTY;
					}
				}
				
			}
			
			return prev;
		}
		
		@Override
		public boolean canDragTo(Slot slotIn) {
			return slotIn != inputSlot;
		}
		
		@Override
		public boolean stillValid(Player playerIn) {
			return true;
		}
		
		public void setInput(ItemStack item) {
			inputSlot.set(item);
		}

	}
	
	@OnlyIn(Dist.CLIENT)
	public static class LoreTableGuiContainer extends AutoGuiContainer<LoreTableContainer> {

		private LoreTableContainer container;
		
		public LoreTableGuiContainer(LoreTableContainer container, Inventory playerInv, Component name) {
			super(container, playerInv, name);
			this.container = container;
			
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
			
			Minecraft.getInstance().getTextureManager().bind(TEXT);
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin,0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
			
			float progress = container.table.getProgress();
			if (progress > 0f) {
				int y = (int) ((1f - progress) * PROGRESS_HEIGHT);
				RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(
						matrixStackIn,
						horizontalMargin + PROGRESS_GUI_HOFFSET,
						verticalMargin + PROGRESS_GUI_VOFFSET + y, 0, GUI_HEIGHT + y, PROGRESS_WIDTH, PROGRESS_HEIGHT - y, 256, 256);
			}
			
			
		}
		
		@Override
		protected void renderLabels(PoseStack matrixStackIn, int mouseX, int mouseY) {
			if (container.table.hasLore()) {
				int u, v;
				v = GUI_HEIGHT;
				u = PROGRESS_WIDTH;
				long time = System.currentTimeMillis();
				u += ((time % 3000) / 1000) * SHINE_LENGTH;
				float alpha = 1f - .5f * ((float) (time % 1000) / 1000f);
				
				Minecraft.getInstance().getTextureManager().bind(TEXT);
				
				RenderSystem.enableBlend();
				RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(
						matrixStackIn,
						SLOT_INPUT_HOFFSET,
						SLOT_INPUT_VOFFSET - 20, u,
						v, SHINE_LENGTH, SHINE_LENGTH, 256, 256,
						0, 1, 1, alpha);
			}
			
		}
		
	}
}