package com.smanzana.nostrummagica.util;

import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ContainerUtil {
	
	@SuppressWarnings("unchecked")
	@OnlyIn(Dist.CLIENT)
	@Nullable
	public static final <T extends BlockEntity> T GetPackedTE(@Nonnull FriendlyByteBuf buffer) {
		BlockPos pos = buffer.readBlockPos();
		final Minecraft mc = Minecraft.getInstance();
		BlockEntity teRaw = mc.level.getBlockEntity(pos);
		
		if (teRaw != null) {
			return (T) teRaw;
		}
		return null;
	}
	
	public static final <T extends BlockEntity> void PackTE(@Nonnull FriendlyByteBuf buffer, @Nonnull T tileEntity) {
		buffer.writeBlockPos(tileEntity.getBlockPos());
	}
	
	public static interface IPackedContainerProvider extends MenuProvider {
		public Consumer<FriendlyByteBuf> getData();
	}

	public static IPackedContainerProvider MakeProvider(String name, MenuConstructor provider, Consumer<FriendlyByteBuf> dataFunc) {
		return new IPackedContainerProvider() {

			@Override
			public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
				return provider.createMenu(windowId, playerInv, player);
			}

			@Override
			public Component getDisplayName() {
				return new TranslatableComponent("container." + NostrumMagica.MODID + "." + name + ".name");
			}

			@Override
			public Consumer<FriendlyByteBuf> getData() {
				return dataFunc;
			}
		};
	}
	
	public static interface AutoContainerFields extends ContainerData {
		
	}
	
	public static interface IAutoContainerInventory extends Container, AutoContainerFields {
		
		public int getFieldCount();
		
		public int getField(int index);
		
		public void setField(int index, int val);
		
		default int getCount() { return this.getFieldCount(); }
		
		default int get(int index) { return this.getField(index); }
		
		default void set(int index, int val) { this.setField(index, val); }
	}
	
	public static class NoisySlot extends Slot {
		
		protected final @Nonnull Consumer<NoisySlot> listener;
		
		public NoisySlot(Container inventoryIn, int index, int xPosition, int yPosition, @Nonnull Consumer<NoisySlot> listener) {
			super(inventoryIn, index, xPosition, yPosition);
			this.listener = listener;
		}
		
		@Override
		public void setChanged() {
			super.setChanged();
			listener.accept(this);
		}
		
		@Override
		public boolean mayPlace(ItemStack stack) {
			return this.container.canPlaceItem(this.getSlotIndex(), stack);
		}
	}
}
