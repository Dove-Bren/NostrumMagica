package com.smanzana.nostrummagica.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class DimensionUtils {
	
	public static final RegistryKey<World> GetDimension(Entity ent) {
		return ent.getEntityWorld().getDimensionKey();
	}

	public static final boolean InDimension(Entity ent, RegistryKey<World> dimension) {
		return DimEquals(GetDimension(ent), dimension);
	}
	
	public static final boolean DimEquals(RegistryKey<World> dim1, RegistryKey<World> dim2) {
		return dim1.equals(dim2);
	}

	public static final boolean InDimension(Entity ent, World world) {
		return InDimension(ent, world.getDimensionKey());
	}
	
	public static final boolean IsOverworld(RegistryKey<World> dim) {
		return DimEquals(dim, World.OVERWORLD);
	}
	
	public static final boolean IsOverworld(World world) {
		return IsOverworld(world.getDimensionKey());
	}
	
	public static final boolean IsNether(RegistryKey<World> dim) {
		return DimEquals(dim, World.THE_NETHER);
	}
	
	public static final boolean IsNether(World world) {
		return IsOverworld(world.getDimensionKey());
	}
	
	public static final boolean IsEnd(RegistryKey<World> dim) {
		return DimEquals(dim, World.THE_END);
	}
	
	public static final boolean IsEnd(World world) {
		return IsOverworld(world.getDimensionKey());
	}

	public static final RegistryKey<World> GetDimKey(ResourceLocation loc) {
		return RegistryKey.getOrCreateKey(Registry.WORLD_KEY, loc);
	}
	
	public static final RegistryKey<World> GetDimKey(String locString) {
		return GetDimKey(new ResourceLocation(locString));
	}
	
}
