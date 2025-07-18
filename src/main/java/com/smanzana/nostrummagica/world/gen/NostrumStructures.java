package com.smanzana.nostrummagica.world.gen;

import java.util.List;

import com.mojang.serialization.Codec;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.DragonStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.ElementalTrialStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.KaniDungeonStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.LegacySorceryStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.ManiCastleStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.PlantBossStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.PortalStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.SorceryIslandStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.VaniSolarStructure;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumStructures {

	private static final String DUNGEONGEN_PORTAL_ID = "nostrum_dungeons_portal";
	private static final String DUNGEONGEN_DRAGON_ID = "nostrum_dungeons_dragon";
	private static final String DUNGEONGEN_PLANTBOSS_ID = "nostrum_dungeons_plantboss";
	private static final String DUNGEONGEN_MANI_CASTLE_ID = "struct_mani_castle";
	private static final String DUNGEONGEN_SORCERY_ISLAND_ID = "struct_sorcery_island";
	private static final String DUNGEONGEN_KANI_DUNGEON_ID = "struct_kani_dungeon";
	private static final String DUNGEONGEN_VANI_SOLAR_ID = "struct_vani_solar";
	private static final String DUNGEONGEN_LEGACY_SORCERY_ID = "struct_legacy_sorcery_dungeon";
	private static final String DUNGEONGEN_ELEMENTAL_TRIAL_ID = "struct_elemental_trial";
	private static final String DUNGEONGEN_PORTAL_CONF_ID = "configured_" + DUNGEONGEN_PORTAL_ID;
	private static final String DUNGEONGEN_DRAGON_CONF_ID = "configured_" + DUNGEONGEN_DRAGON_ID;
	private static final String DUNGEONGEN_PLANTBOSS_CONF_ID = "configured_" + DUNGEONGEN_PLANTBOSS_ID;
	private static final String DUNGEONGEN_MANI_CASTLE_CONF_ID = "configured_" + DUNGEONGEN_MANI_CASTLE_ID;
	private static final String DUNGEONGEN_SORCERY_ISLAND_CONF_ID = "configured_" + DUNGEONGEN_SORCERY_ISLAND_ID;
	private static final String DUNGEONGEN_KANI_DUNGEON_CONF_ID = "configured_" + DUNGEONGEN_KANI_DUNGEON_ID;
	private static final String DUNGEONGEN_VANI_SOLAR_CONF_ID = "configured_" + DUNGEONGEN_VANI_SOLAR_ID;
	private static final String DUNGEONGEN_LEGACY_SORCERY_CONF_ID = "configured_" + DUNGEONGEN_LEGACY_SORCERY_ID;
	private static final String DUNGEONGEN_ELEMENTAL_TRIAL_CONF_ID = "configured_" + DUNGEONGEN_ELEMENTAL_TRIAL_ID;
	
	@ObjectHolder(DUNGEONGEN_PORTAL_ID) public static PortalStructure DUNGEON_PORTAL;
	protected static ConfiguredStructureFeature<?, ?> CONFIGURED_DUNGEON_PORTAL;
	public static Holder<ConfiguredStructureFeature<?, ?>> REF_DUNGEON_PORTAL;
	
	@ObjectHolder(DUNGEONGEN_DRAGON_ID) public static DragonStructure DUNGEON_DRAGON;
	public static ConfiguredStructureFeature<?, ?> CONFIGURED_DUNGEON_DRAGON;
	public static Holder<ConfiguredStructureFeature<?, ?>> REF_DUNGEON_DRAGON;
	
	@ObjectHolder(DUNGEONGEN_PLANTBOSS_ID) public static PlantBossStructure DUNGEON_PLANTBOSS;
	public static ConfiguredStructureFeature<?, ?> CONFIGUREDDUNGEON_PLANTBOSS;
	public static Holder<ConfiguredStructureFeature<?, ?>> REF_DUNGEON_PLANTBOSS;
	
	@ObjectHolder(DUNGEONGEN_MANI_CASTLE_ID) public static ManiCastleStructure DUNGEON_MANI_CASTLE;
	public static ConfiguredStructureFeature<?, ?> CONFIGUREDDUNGEON_MANI_CASTLE;
	public static Holder<ConfiguredStructureFeature<?, ?>> REF_DUNGEON_MANI_CASTLE;
	
	@ObjectHolder(DUNGEONGEN_SORCERY_ISLAND_ID) public static SorceryIslandStructure DUNGEON_SORCERY_ISLAND;
	public static ConfiguredStructureFeature<?, ?> CONFIGUREDDUNGEON_SORCERY_ISLAND;
	public static Holder<ConfiguredStructureFeature<?, ?>> REF_DUNGEON_SORCERY_ISLAND;
	
	@ObjectHolder(DUNGEONGEN_KANI_DUNGEON_ID) public static KaniDungeonStructure DUNGEON_KANI_DUNGEON;
	public static ConfiguredStructureFeature<?, ?> CONFIGUREDDUNGEON_KANI_DUNGEON;
	public static Holder<ConfiguredStructureFeature<?, ?>> REF_DUNGEON_KANI_DUNGEON;
	
	@ObjectHolder(DUNGEONGEN_VANI_SOLAR_ID) public static VaniSolarStructure DUNGEON_VANI_SOLAR;
	public static ConfiguredStructureFeature<?, ?> CONFIGUREDDUNGEON_VANI_SOLAR;
	public static Holder<ConfiguredStructureFeature<?, ?>> REF_DUNGEON_VANI_SOLAR;
	
	@ObjectHolder(DUNGEONGEN_LEGACY_SORCERY_ID) public static LegacySorceryStructure DUNGEON_LEGACY_SORCERY;
	public static ConfiguredStructureFeature<?, ?> CONFIGUREDDUNGEON_LEGACY_SORCERY;
	public static Holder<ConfiguredStructureFeature<?, ?>> REF_DUNGEON_LEGACY_SORCERY;
	
	@ObjectHolder(DUNGEONGEN_ELEMENTAL_TRIAL_ID) public static ElementalTrialStructure DUNGEON_ELEMENTAL_TRIAL;
	public static ConfiguredStructureFeature<?, ?> CONFIGUREDDUNGEON_ELEMENTAL_TRIAL;
	public static Holder<ConfiguredStructureFeature<?, ?>> REF_DUNGEON_ELEMENTAL_TRIAL;
	
	public static StructurePlacementType<GridStructureSetPlacement> PLACEMENT_FIXED_GRID;

	@SubscribeEvent
	public static void registerStructures(RegistryEvent.Register<StructureFeature<?>> event) {
		StructureFeature<NoneFeatureConfiguration> structure;
		ConfiguredStructureFeature<?, ?> configured;
		
		// getChunkPosForStructure()
		// min/max settings are actually:
		// max is that every MAX x MAX chunk block, there is a spawn attempt
		// the distance from the exact floor boundary is a MIN x MIN chunk region.
		// I think the 'average' + 'minimum distance' wording is that average is really
		// average (spawn attempts every 'average' blocks) and min is min in that if two
		// regions spawn towards eachother, that would be the distance between? Except it's not. It's
		// (max-min) ?
		
		structure = new PortalStructure();
		configured = structure.configured(FeatureConfiguration.NONE, Tags.Biomes.IS_OVERWORLD);
		// Avg dist: sqrt(24^2 + 24^2) = 543 blocks
		CONFIGURED_DUNGEON_PORTAL = configured;
		REF_DUNGEON_PORTAL = registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_PORTAL_ID), NostrumMagica.Loc(DUNGEONGEN_PORTAL_CONF_ID));
		
		structure = new DragonStructure();
		configured = structure.configured(FeatureConfiguration.NONE, Tags.Biomes.IS_OVERWORLD);
		// Avg dist: sqrt(32^2 + 32^2) = 724 blocks
		CONFIGURED_DUNGEON_DRAGON = configured;
		REF_DUNGEON_DRAGON = registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_DRAGON_ID), NostrumMagica.Loc(DUNGEONGEN_DRAGON_CONF_ID));
		
		structure = new PlantBossStructure();
		configured = structure.configured(FeatureConfiguration.NONE, Tags.Biomes.IS_OVERWORLD);
		// Avg dist: sqrt(32^2 + 32^2) = 724 blocks
		CONFIGUREDDUNGEON_PLANTBOSS = configured;
		REF_DUNGEON_PLANTBOSS = registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_PLANTBOSS_ID), NostrumMagica.Loc(DUNGEONGEN_PLANTBOSS_CONF_ID));
		
		final TagKey<Biome> fakeSorceryBiomeKey = TagKey.create(Registry.BIOME_REGISTRY, NostrumMagica.Loc("sorcery_dimension"));
		
		structure = new ManiCastleStructure();
		configured = structure.configured(FeatureConfiguration.NONE, fakeSorceryBiomeKey);
		CONFIGUREDDUNGEON_MANI_CASTLE = configured;
		REF_DUNGEON_MANI_CASTLE = registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_MANI_CASTLE_ID), NostrumMagica.Loc(DUNGEONGEN_MANI_CASTLE_CONF_ID));
		
		structure = new SorceryIslandStructure();
		configured = structure.configured(FeatureConfiguration.NONE, fakeSorceryBiomeKey);
		CONFIGUREDDUNGEON_SORCERY_ISLAND = configured;
		REF_DUNGEON_SORCERY_ISLAND = registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_SORCERY_ISLAND_ID), NostrumMagica.Loc(DUNGEONGEN_SORCERY_ISLAND_CONF_ID));
		
		structure = new KaniDungeonStructure();
		configured = structure.configured(FeatureConfiguration.NONE, fakeSorceryBiomeKey);
		CONFIGUREDDUNGEON_KANI_DUNGEON = configured;
		REF_DUNGEON_KANI_DUNGEON = registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_KANI_DUNGEON_ID), NostrumMagica.Loc(DUNGEONGEN_KANI_DUNGEON_CONF_ID));
		
		structure = new VaniSolarStructure();
		configured = structure.configured(FeatureConfiguration.NONE, fakeSorceryBiomeKey);
		CONFIGUREDDUNGEON_VANI_SOLAR = configured;
		REF_DUNGEON_VANI_SOLAR = registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_VANI_SOLAR_ID), NostrumMagica.Loc(DUNGEONGEN_VANI_SOLAR_CONF_ID));
		
		structure = new LegacySorceryStructure();
		configured = structure.configured(FeatureConfiguration.NONE, fakeSorceryBiomeKey);
		CONFIGUREDDUNGEON_LEGACY_SORCERY = configured;
		REF_DUNGEON_LEGACY_SORCERY = registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_LEGACY_SORCERY_ID), NostrumMagica.Loc(DUNGEONGEN_LEGACY_SORCERY_CONF_ID));
		
		structure = new ElementalTrialStructure();
		configured = structure.configured(FeatureConfiguration.NONE, fakeSorceryBiomeKey);
		CONFIGUREDDUNGEON_ELEMENTAL_TRIAL = configured;
		REF_DUNGEON_ELEMENTAL_TRIAL = registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_ELEMENTAL_TRIAL_ID), NostrumMagica.Loc(DUNGEONGEN_ELEMENTAL_TRIAL_CONF_ID));
		
		
		// Register structure sets, which include rules of how to place them and distances between things in the same set.
		// Putting everything into ONE set so that dungeons don't generate into eachother, but making portal weight by heigher.
		final int averageChunks = 45; // 702 block average
		final int minChunks = 16; // 256 block minimum
		StructureSet structures = new StructureSet(
				List.of(
						new StructureSet.StructureSelectionEntry(REF_DUNGEON_PORTAL, 2),
						new StructureSet.StructureSelectionEntry(REF_DUNGEON_DRAGON, 1),
						new StructureSet.StructureSelectionEntry(REF_DUNGEON_PLANTBOSS, 1)
				),
				new RandomSpreadStructurePlacement(averageChunks, minChunks, RandomSpreadType.LINEAR, 0x26F1BDCF)
		);
		BuiltinRegistries.register(BuiltinRegistries.STRUCTURE_SETS, NostrumMagica.Loc("dungeon_structures"), structures);
		
		// Register custom structure placement types. Would prob need to be earlier if anything besides empty dim chunk gen used it
		PLACEMENT_FIXED_GRID = registerPlacement(NostrumMagica.Loc("fixed_grid"), GridStructureSetPlacement.CODEC);
	}
	
	private static Holder<ConfiguredStructureFeature<?, ?>> registerStructure(RegistryEvent.Register<StructureFeature<?>> event, StructureFeature<?> structure, ConfiguredStructureFeature<?, ?> config, ResourceLocation structName, ResourceLocation confName) {
		// Register structure itself
		event.getRegistry().register(structure.setRegistryName(structName));
		
		// Register configured structure feature and return reference to it
		return BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, confName, config);
		
		
//		StructureFeature.STRUCTURES_REGISTRY.put(structName.toString(), structure);
//		
//		// Create seperation settings for structure
//		StructureFeatureConfiguration seperation = new StructureFeatureConfiguration(max, min, rand);
//		
//		// Force into dimension structure settings map
//		StructureSettings.DEFAULTS = ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration>builder().putAll(StructureSettings.DEFAULTS).
//				put(structure, seperation).build();
//		
//		// Stash seperation settings in our own map for injection on world load
//		CUSTOM_SEPARATION_SETTINGS.put(structure, seperation);
//		
//		// Register the configured version of our structure
//		Registry.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, confName, config);
//		
//		// Put in flat generation settings, which apparently is useful for playing nice with other mods
//		FlatLevelGeneratorSettings.STRUCTURE_FEATURES.put(structure, config);
//		
//		BuiltinRegistries.STRUCTURE_SETS;
	}
	
	//@SubscribeEvent subscribed to listener in #registerStructures as a hack because we can't mix busses
//	public static void loadWorld(WorldEvent.Load event) {
//		if(event.getWorld() instanceof ServerLevel && DimensionUtils.IsOverworld((ServerLevel)event.getWorld())) {
//			final ServerLevel serverWorld = (ServerLevel)event.getWorld();
//			final ServerChunkCache provider = serverWorld.getChunkSource();
//			Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(provider.generator.getSettings().structureConfig());
//			tempMap.putAll(CUSTOM_SEPARATION_SETTINGS);
//			provider.generator.getSettings().structureConfig = tempMap;
//		}
//	}
	
	private static <SP extends StructurePlacement> StructurePlacementType<SP> registerPlacement(ResourceLocation name, Codec<SP> codec) {
		return Registry.register(Registry.STRUCTURE_PLACEMENT_TYPE, name, () -> {
			return codec;
		});
	}
}
