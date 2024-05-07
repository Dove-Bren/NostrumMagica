package com.smanzana.nostrummagica.utils;

import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ContainerUtil {
	
	@SuppressWarnings("unchecked")
	@OnlyIn(Dist.CLIENT)
	public static final <T extends TileEntity> @Nullable T GetPackedTE(@Nonnull PacketBuffer buffer) {
		BlockPos pos = buffer.readBlockPos();
		final Minecraft mc = Minecraft.getInstance();
		TileEntity teRaw = mc.world.getTileEntity(pos);
		
		if (teRaw != null) {
			return (T) teRaw;
		}
		return null;
	}
	
	public static final <T extends TileEntity> void PackTE(@Nonnull PacketBuffer buffer, @Nonnull T tileEntity) {
		buffer.writeBlockPos(tileEntity.getPos());
	}
	
	public static interface IPackedContainerProvider extends INamedContainerProvider {
		public Consumer<PacketBuffer> getData();
	}

	public static IPackedContainerProvider MakeProvider(String name, IContainerProvider provider, Consumer<PacketBuffer> dataFunc) {
		return new IPackedContainerProvider() {

			@Override
			public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
				return provider.createMenu(windowId, playerInv, player);
			}

			@Override
			public ITextComponent getDisplayName() {
				return new TranslationTextComponent("container." + NostrumMagica.MODID + "." + name + ".name");
			}

			@Override
			public Consumer<PacketBuffer> getData() {
				return dataFunc;
			}
		};
	}
	
	public static interface AutoContainerFields extends IIntArray {
		
	}
	
	public static interface IAutoContainerInventory extends IInventory, AutoContainerFields {
		
		public int getFieldCount();
		
		public int getField(int index);
		
		public void setField(int index, int val);
		
		default int size() { return this.getFieldCount(); }
		
		default int get(int index) { return this.getField(index); }
		
		default void set(int index, int val) { this.setField(index, val); }
	}
	
	public static class NoisySlot extends Slot {
		
		protected final @Nonnull Consumer<NoisySlot> listener;
		
		public NoisySlot(IInventory inventoryIn, int index, int xPosition, int yPosition, @Nonnull Consumer<NoisySlot> listener) {
			super(inventoryIn, index, xPosition, yPosition);
			this.listener = listener;
		}
		
		@Override
		public void onSlotChanged() {
			super.onSlotChanged();
			listener.accept(this);
		}
		
		@Override
		public boolean isItemValid(ItemStack stack) {
			return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
		}
	}
}
