package com.smanzana.nostrummagica.world.dimension;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class NostrumDimensions {

	//public static RegistryKey<World> EmptyDimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, NostrumMagica.Loc(NostrumEmptyDimension.DIMENSION_ID));
	
	public static void init() {
		Registry.register(Registry.CHUNK_GENERATOR, NostrumMagica.Loc(EmptyChunkGen.ID), EmptyChunkGen.CODEC);

		NostrumSorceryDimension.RegisterListener();
	}
	
	private static RegistryKey<World> DimensionCache = null;
	private static String DimensionCacheInput = null;
	public static final RegistryKey<World> GetSorceryDimension() {
		final String name = ModConfig.config.getSorceryDimension();
		if (!name.equals(DimensionCacheInput)) {
			DimensionCacheInput = name;
			DimensionCache = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(name));
		}
		return DimensionCache;
	}
	
}
