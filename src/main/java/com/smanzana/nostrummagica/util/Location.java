package com.smanzana.nostrummagica.util;

import java.util.Objects;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class Location {

	private BlockPos pos;
	private RegistryKey<World> dimension;
	
	public Location(BlockPos pos, RegistryKey<World> dimension) {
		this.pos = pos;
		this.dimension = dimension;
	}
	
	public Location(World world, BlockPos pos) {
		this(pos, world.getDimensionKey());
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public RegistryKey<World> getDimension() {
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
	
	public CompoundNBT toNBT() {
		return toNBT(new CompoundNBT());
	}
	
	public CompoundNBT toNBT(CompoundNBT tag) {
		tag.putString(NBT_DIM, dimension.getLocation().toString());
		tag.put(NBT_POS, NBTUtil.writeBlockPos(pos));
		return tag;
	}
	
	public static Location FromNBT(CompoundNBT tag) {
		return new Location(
				NBTUtil.readBlockPos(tag.getCompound(NBT_POS)),
				RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(tag.getString(NBT_DIM)))
				);
	}
	
}
