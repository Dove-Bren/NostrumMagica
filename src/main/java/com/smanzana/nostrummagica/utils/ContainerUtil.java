package com.smanzana.nostrummagica.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
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
}
