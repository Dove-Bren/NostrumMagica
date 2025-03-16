package com.smanzana.nostrummagica.world.dimension;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;

public class NostrumDimensions {

	//public static RegistryKey<World> EmptyDimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, NostrumMagica.Loc(NostrumEmptyDimension.DIMENSION_ID));
	
	public static void init() {
		Registry.register(Registry.CHUNK_GENERATOR, NostrumMagica.Loc(EmptyChunkGen.ID), EmptyChunkGen.CODEC);

		NostrumSorceryDimension.RegisterListener();
	}
	
	private static ResourceKey<Level> DimensionCache = null;
	private static String DimensionCacheInput = null;
	public static final ResourceKey<Level> GetSorceryDimension() {
		final String name = ModConfig.config.getSorceryDimension();
		if (!name.equals(DimensionCacheInput)) {
			DimensionCacheInput = name;
			DimensionCache = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(name));
		}
		return DimensionCache;
	}
	
}
