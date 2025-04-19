package com.smanzana.nostrummagica.world.gen;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
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

	protected static Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> CONFFEATURE_FLOWER_CRYSTABLOOM;
	protected static Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> CONFFEATURE_FLOWER_MIDNIGHTIRIS;
	protected static Holder<ConfiguredFeature<OreConfiguration, ?>> CONFFEATURE_ORE_MANI;
	protected static Holder<ConfiguredFeature<OreConfiguration, ?>> CONFFEATURE_ORE_ESSORE;
	
	public static Holder<PlacedFeature> PLACED_FLOWER_CRYSTABLOOM;
	public static Holder<PlacedFeature> PLACED_FLOWER_MIDNIGHTIRIS;
	public static Holder<PlacedFeature> PLACED_ORE_MANI;
	public static Holder<PlacedFeature> PLACED_ORE_ESSORE;
	
	@SubscribeEvent
	public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
		// Register base features
		// No custom base features to register
		// final IForgeRegistry<Feature<?>> registry = event.getRegistry();
		// registry.register(new NostrumFlowerGenerator(NoFeatureConfig::deserialize).setRegistryName(FLOWERGEN_ID));
		
		// Register configured features
		ResourceLocation ID = NostrumMagica.Loc(ID_FLOWER_CRYSTABLOOM);
		CONFFEATURE_FLOWER_CRYSTABLOOM = FeatureUtils.register(ID.toString(), Feature.FLOWER, new RandomPatchConfiguration(32, 6, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(NostrumBlocks.crystabloom)))));
		PLACED_FLOWER_CRYSTABLOOM = PlacementUtils.register(ID.toString(), CONFFEATURE_FLOWER_CRYSTABLOOM, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP);
		
		ID = NostrumMagica.Loc(ID_FLOWER_MIDNIGHTIRIS);
		CONFFEATURE_FLOWER_MIDNIGHTIRIS = FeatureUtils.register(ID.toString(), Feature.FLOWER, new RandomPatchConfiguration(48, 6, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(NostrumBlocks.midnightIris)))));
		PLACED_FLOWER_MIDNIGHTIRIS = PlacementUtils.register(ID.toString(), CONFFEATURE_FLOWER_MIDNIGHTIRIS, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP);
		
		ID = NostrumMagica.Loc(ID_ORE_MANI);
		final List<OreConfiguration.TargetBlockState> ORE_MANI_TARGET_LIST = List.of(OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, NostrumBlocks.maniOre.defaultBlockState()));
		CONFFEATURE_ORE_MANI = FeatureUtils.register(ID.toString(), Feature.ORE, new OreConfiguration(ORE_MANI_TARGET_LIST, 9));
		PLACED_ORE_MANI = PlacementUtils.register(ID.toString(), CONFFEATURE_ORE_MANI, CountPlacement.of(15), HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(128)));
		
		ID = NostrumMagica.Loc(ID_ORE_ESSORE);
		final List<OreConfiguration.TargetBlockState> ORE_ESSORE_TARGET_LIST = List.of(OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, NostrumBlocks.essenceOre.defaultBlockState()));
		CONFFEATURE_ORE_ESSORE = FeatureUtils.register(ID.toString(), Feature.ORE, new OreConfiguration(ORE_ESSORE_TARGET_LIST, 4));
		PLACED_ORE_ESSORE = PlacementUtils.register(ID.toString(), CONFFEATURE_ORE_ESSORE, CountPlacement.of(8), HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(60)));
	}
}
