package com.smanzana.nostrummagica.util;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.world.dimension.NostrumDimensions;

import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;

public class DimensionUtils {
	
	public static final ResourceKey<Level> GetDimension(Entity ent) {
		return ent.getCommandSenderWorld().dimension();
	}
	
	public static ResourceKey<Level> GetDimension(Level world) {
		return world.dimension();
	}

	public static final boolean InDimension(Entity ent, ResourceKey<Level> dimension) {
		return DimEquals(GetDimension(ent), dimension);
	}
	
	public static final boolean DimEquals(ResourceKey<Level> dim1, ResourceKey<Level> dim2) {
		return dim1.equals(dim2);
	}

	public static final boolean InDimension(Entity ent, Level world) {
		return InDimension(ent, GetDimension(world));
	}
	
	public static final boolean IsOverworld(ResourceKey<Level> dim) {
		return DimEquals(dim, Level.OVERWORLD);
	}
	
	public static final boolean IsOverworld(Level world) {
		return IsOverworld(GetDimension(world));
	}
	
	public static final boolean IsNether(ResourceKey<Level> dim) {
		return DimEquals(dim, Level.NETHER);
	}
	
	public static final boolean IsNether(Level world) {
		return IsNether(GetDimension(world));
	}
	
	public static final boolean IsEnd(ResourceKey<Level> dim) {
		return DimEquals(dim, Level.END);
	}
	
	public static final boolean IsEnd(Level world) {
		return IsEnd(GetDimension(world));
	}
	
	public static final boolean IsSorceryDim(ResourceKey<Level> dim) {
		return DimEquals(dim, NostrumDimensions.GetSorceryDimension());
	}
	
	public static final boolean IsSorceryDim(Level world) {
		return IsSorceryDim(GetDimension(world));
	}

	public static final ResourceKey<Level> GetDimKey(ResourceLocation loc) {
		return ResourceKey.create(Registry.DIMENSION_REGISTRY, loc);
	}
	
	public static final ResourceKey<Level> GetDimKey(String locString) {
		return GetDimKey(new ResourceLocation(locString));
	}
	
	public static final ResourceKey<Level> GetDimKeySafe(String locString) {
		if (locString == null || locString.isEmpty()) {
			return Level.OVERWORLD;
		}
		return GetDimKey(new ResourceLocation(locString));
	}

	public static boolean SameDimension(@Nonnull Entity a, @Nonnull Entity b) {
		return SameDimension(a.getCommandSenderWorld(), b.getCommandSenderWorld());
	}
	
	public static boolean SameDimension(@Nonnull Level a, @Nonnull Level b) {
		return DimEquals(GetDimension(a), GetDimension(b));
	}
}
