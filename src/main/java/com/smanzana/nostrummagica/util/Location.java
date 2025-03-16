package com.smanzana.nostrummagica.util;

import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;

public class Location {

	private BlockPos pos;
	private ResourceKey<Level> dimension;
	
	public Location(BlockPos pos, ResourceKey<Level> dimension) {
		this.pos = pos;
		this.dimension = dimension;
	}
	
	public Location(Level world, BlockPos pos) {
		this(pos, world.dimension());
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public ResourceKey<Level> getDimension() {
		return dimension;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Location && ((Location) o).hashCode() == hashCode();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(pos, dimension);
	}
	
	private static final String NBT_DIM = "dim";
	private static final String NBT_POS = "pos";
	
	public CompoundTag toNBT() {
		return toNBT(new CompoundTag());
	}
	
	public CompoundTag toNBT(CompoundTag tag) {
		tag.putString(NBT_DIM, dimension.location().toString());
		tag.put(NBT_POS, NbtUtils.writeBlockPos(pos));
		return tag;
	}
	
	public static Location FromNBT(CompoundTag tag) {
		return new Location(
				NbtUtils.readBlockPos(tag.getCompound(NBT_POS)),
				ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString(NBT_DIM)))
				);
	}
	
}
