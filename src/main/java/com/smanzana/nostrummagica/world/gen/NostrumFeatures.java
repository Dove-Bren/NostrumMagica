package com.smanzana.nostrummagica.world.gen;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.blockplacer.SimpleBlockPlacer;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.Features;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumFeatures {

	
	@SubscribeEvent
	public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
		// Register base features
		// No custom base features to register
		// final IForgeRegistry<Feature<?>> registry = event.getRegistry();
		// registry.register(new NostrumFlowerGenerator(NoFeatureConfig::deserialize).setRegistryName(FLOWERGEN_ID));
		
		// Register configured features
		registerConfiguredFeature(NostrumMagica.Loc("nostrum_flowers_crystabloom"), Feature.FLOWER.withConfiguration(
				(new BlockClusterFeatureConfig.Builder(new SimpleBlockStateProvider(NostrumBlocks.crystabloom.getDefaultState()), SimpleBlockPlacer.PLACER))
					.tries(32).build()
				).withPlacement(Features.Placements.VEGETATION_PLACEMENT).withPlacement(Features.Placements.HEIGHTMAP_PLACEMENT));
		
		registerConfiguredFeature(NostrumMagica.Loc("nostrum_flowers_mightnightiris"), Feature.FLOWER.withConfiguration(
				(new BlockClusterFeatureConfig.Builder(new SimpleBlockStateProvider(NostrumBlocks.midnightIris.getDefaultState()), SimpleBlockPlacer.PLACER))
					.tries(48).build()
				).withPlacement(Features.Placements.VEGETATION_PLACEMENT).withPlacement(Features.Placements.HEIGHTMAP_PLACEMENT));
		
		//registry.register(new NostrumFlowerGenerator(NostrumFlowerGenerator.NostrumFlowerConfig::deserialize).setRegistryName(FLOWERGEN_ID));
		
	}
	
	private static void registerConfiguredFeature(ResourceLocation id, ConfiguredFeature<?, ?> feature) {
		Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, id, feature);
	}
	
}
