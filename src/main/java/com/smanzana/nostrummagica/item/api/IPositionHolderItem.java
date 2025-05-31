package com.smanzana.nostrummagica.item.api;

import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IPositionHolderItem {

	public static final String NBT_DIMENSION = "dimension";
	public static final String NBT_X = "x";
	public static final String NBT_Y = "y";
	public static final String NBT_Z = "z";
	
	public static BlockPos getBlockPosition(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof IPositionHolderItem))
			return null;
		
		CompoundTag nbt = stack.getTag();
		if (nbt == null)
			return null;
		
		if (!nbt.contains(NBT_X)
				|| !nbt.contains(NBT_Y)
				|| !nbt.contains(NBT_Z))
			return null;
		
		return new BlockPos(
				nbt.getInt(NBT_X),
				nbt.getInt(NBT_Y),
				nbt.getInt(NBT_Z)
				);
	}
	
	/**
	 * Returns 0 on error
	 * @param stack
	 * @return
	 */
	public static ResourceKey<Level> getDimension(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof IPositionHolderItem))
			return Level.OVERWORLD;
		
		CompoundTag nbt = stack.getTag();
		if (nbt == null)
			return Level.OVERWORLD;
		
		return DimensionUtils.GetDimKey(nbt.getString(NBT_DIMENSION));
	}
	
	public static void setPosition(ItemStack stack, ResourceKey<Level> dimension, BlockPos pos) {
		if (stack.isEmpty() || !(stack.getItem() instanceof IPositionHolderItem))
			return;
		
		if (pos == null)
			return;
		
		CompoundTag tag;
		if (!stack.hasTag())
			tag = new CompoundTag();
		else
			tag = stack.getTag();
		
		tag.putString(NBT_DIMENSION, dimension.location().toString());
		tag.putInt(NBT_X, pos.getX());
		tag.putInt(NBT_Y, pos.getY());
		tag.putInt(NBT_Z, pos.getZ());
		
		stack.setTag(tag);
	}
	
	public static void clearPosition(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof IPositionHolderItem))
			return;
		
		CompoundTag tag;
		if (!stack.hasTag())
			return;
		
		tag = stack.getTag();
		tag.remove(NBT_DIMENSION);
		tag.remove(NBT_X);
		tag.remove(NBT_Y);
		tag.remove(NBT_Z);
		
		stack.setTag(tag);
	}
	
}
