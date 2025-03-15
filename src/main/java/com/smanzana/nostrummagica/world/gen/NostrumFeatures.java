package com.smanzana.nostrummagica.world.gen;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;

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
	
	private static final String ID_FLOWER_CRYSTABLOOM = "nostrum_flowers_crystabloom";
	private static final String ID_FLOWER_MIDNIGHTIRIS = "nostrum_flowers_mightnightiris";
	private static final String ID_ORE_MANI = "nostrum_ore_mani";
	private static final String ID_ORE_ESSORE = "nostrum_ore_essore";

	public static ConfiguredFeature<?, ?> CONFFEATURE_FLOWER_CRYSTABLOOM;
	public static ConfiguredFeature<?, ?> CONFFEATURE_FLOWER_MIDNIGHTIRIS;
	public static ConfiguredFeature<?, ?> CONFFEATURE_ORE_MANI;
	public static ConfiguredFeature<?, ?> CONFFEATURE_ORE_ESSORE;
	
	@SubscribeEvent
	public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
		// Register base features
		// No custom base features to register
		// final IForgeRegistry<Feature<?>> registry = event.getRegistry();
		// registry.register(new NostrumFlowerGenerator(NoFeatureConfig::deserialize).setRegistryName(FLOWERGEN_ID));
		
		// Register configured features
		CONFFEATURE_FLOWER_CRYSTABLOOM = registerConfiguredFeature(NostrumMagica.Loc(ID_FLOWER_CRYSTABLOOM), Feature.FLOWER.configured(
				(new BlockClusterFeatureConfig.Builder(new SimpleBlockStateProvider(NostrumBlocks.crystabloom.defaultBlockState()), SimpleBlockPlacer.INSTANCE))
				.tries(32).build()
			).decorated(Features.Placements.ADD_32).decorated(Features.Placements.HEIGHTMAP_SQUARE));
		
		CONFFEATURE_FLOWER_MIDNIGHTIRIS = registerConfiguredFeature(NostrumMagica.Loc(ID_FLOWER_MIDNIGHTIRIS), Feature.FLOWER.configured(
				(new BlockClusterFeatureConfig.Builder(new SimpleBlockStateProvider(NostrumBlocks.midnightIris.defaultBlockState()), SimpleBlockPlacer.INSTANCE))
				.tries(48).build()
			).decorated(Features.Placements.ADD_32).decorated(Features.Placements.HEIGHTMAP_SQUARE));
		
		CONFFEATURE_ORE_MANI = registerConfiguredFeature(NostrumMagica.Loc(ID_ORE_MANI), Feature.ORE.configured(
				new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, NostrumBlocks.maniOre.defaultBlockState(), 9))
				.range(128).squared().count(15));
		
		CONFFEATURE_ORE_ESSORE = registerConfiguredFeature(NostrumMagica.Loc(ID_ORE_ESSORE), Feature.ORE.configured(
				new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, NostrumBlocks.essenceOre.defaultBlockState(), 4))
				.range(60).squared().count(8));
	}
	
	private static ConfiguredFeature<?, ?> registerConfiguredFeature(ResourceLocation id, ConfiguredFeature<?, ?> feature) {
		Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, id, feature);
		return feature;
	}
	
}
