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
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumFeatures {

	public static final ConfiguredFeature<?, ?> CONFFEATURE_FLOWER_CRYSTABLOOM;
	public static final ConfiguredFeature<?, ?> CONFFEATURE_FLOWER_MIDNIGHTIRIS;
	public static final ConfiguredFeature<?, ?> CONFFEATURE_ORE_MANI;
	public static final ConfiguredFeature<?, ?> CONFFEATURE_ORE_ESSORE;
	
	static {
		CONFFEATURE_FLOWER_CRYSTABLOOM = Feature.FLOWER.withConfiguration(
				(new BlockClusterFeatureConfig.Builder(new SimpleBlockStateProvider(NostrumBlocks.crystabloom.getDefaultState()), SimpleBlockPlacer.PLACER))
				.tries(32).build()
			).withPlacement(Features.Placements.VEGETATION_PLACEMENT).withPlacement(Features.Placements.HEIGHTMAP_PLACEMENT);
		
		CONFFEATURE_FLOWER_MIDNIGHTIRIS = Feature.FLOWER.withConfiguration(
				(new BlockClusterFeatureConfig.Builder(new SimpleBlockStateProvider(NostrumBlocks.midnightIris.getDefaultState()), SimpleBlockPlacer.PLACER))
				.tries(48).build()
			).withPlacement(Features.Placements.VEGETATION_PLACEMENT).withPlacement(Features.Placements.HEIGHTMAP_PLACEMENT);
		
		CONFFEATURE_ORE_MANI = Feature.ORE.withConfiguration(
				new OreFeatureConfig(OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, NostrumBlocks.maniOre.getDefaultState(), 9))
				.range(128).square().func_242731_b(15);
		
		CONFFEATURE_ORE_ESSORE = Feature.ORE.withConfiguration(
				new OreFeatureConfig(OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, NostrumBlocks.essenceOre.getDefaultState(), 4))
				.range(60).square().func_242731_b(8);
		
		//new OreFeatureConfig(OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, Features.States.COAL_ORE, 17)).range(128).square().func_242731_b(20));
	}
	
	@SubscribeEvent
	public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
		// Register base features
		// No custom base features to register
		// final IForgeRegistry<Feature<?>> registry = event.getRegistry();
		// registry.register(new NostrumFlowerGenerator(NoFeatureConfig::deserialize).setRegistryName(FLOWERGEN_ID));
		
		// Register configured features
		registerConfiguredFeature(NostrumMagica.Loc("nostrum_flowers_crystabloom"), CONFFEATURE_FLOWER_CRYSTABLOOM);
		registerConfiguredFeature(NostrumMagica.Loc("nostrum_flowers_mightnightiris"), CONFFEATURE_FLOWER_MIDNIGHTIRIS);
		registerConfiguredFeature(NostrumMagica.Loc("nostrum_ore_mani"), CONFFEATURE_ORE_MANI);
		registerConfiguredFeature(NostrumMagica.Loc("nostrum_ore_essore"), CONFFEATURE_ORE_ESSORE);
	}
	
	private static void registerConfiguredFeature(ResourceLocation id, ConfiguredFeature<?, ?> feature) {
		Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, id, feature);
	}
	
}
