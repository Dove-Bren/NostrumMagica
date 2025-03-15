package com.smanzana.nostrummagica.util;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.world.dimension.NostrumDimensions;

import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class DimensionUtils {
	
	public static final RegistryKey<World> GetDimension(Entity ent) {
		return ent.getCommandSenderWorld().dimension();
	}
	
	public static RegistryKey<World> GetDimension(World world) {
		return world.dimension();
	}

	public static final boolean InDimension(Entity ent, RegistryKey<World> dimension) {
		return DimEquals(GetDimension(ent), dimension);
	}
	
	public static final boolean DimEquals(RegistryKey<World> dim1, RegistryKey<World> dim2) {
		return dim1.equals(dim2);
	}

	public static final boolean InDimension(Entity ent, World world) {
		return InDimension(ent, GetDimension(world));
	}
	
	public static final boolean IsOverworld(RegistryKey<World> dim) {
		return DimEquals(dim, World.OVERWORLD);
	}
	
	public static final boolean IsOverworld(World world) {
		return IsOverworld(GetDimension(world));
	}
	
	public static final boolean IsNether(RegistryKey<World> dim) {
		return DimEquals(dim, World.NETHER);
	}
	
	public static final boolean IsNether(World world) {
		return IsNether(GetDimension(world));
	}
	
	public static final boolean IsEnd(RegistryKey<World> dim) {
		return DimEquals(dim, World.END);
	}
	
	public static final boolean IsEnd(World world) {
		return IsEnd(GetDimension(world));
	}
	
	public static final boolean IsSorceryDim(RegistryKey<World> dim) {
		return DimEquals(dim, NostrumDimensions.GetSorceryDimension());
	}
	
	public static final boolean IsSorceryDim(World world) {
		return IsSorceryDim(GetDimension(world));
	}

	public static final RegistryKey<World> GetDimKey(ResourceLocation loc) {
		return RegistryKey.create(Registry.DIMENSION_REGISTRY, loc);
	}
	
	public static final RegistryKey<World> GetDimKey(String locString) {
		return GetDimKey(new ResourceLocation(locString));
	}
	
	public static final RegistryKey<World> GetDimKeySafe(String locString) {
		if (locString == null || locString.isEmpty()) {
			return World.OVERWORLD;
		}
		return GetDimKey(new ResourceLocation(locString));
	}

	public static boolean SameDimension(@Nonnull Entity a, @Nonnull Entity b) {
		return SameDimension(a.getCommandSenderWorld(), b.getCommandSenderWorld());
	}
	
	public static boolean SameDimension(@Nonnull World a, @Nonnull World b) {
		return DimEquals(GetDimension(a), GetDimension(b));
	}
}
