package com.smanzana.nostrummagica.world.gen;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.DragonStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.PlantBossStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.PortalStructure;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumStructures {

	private static final String DUNGEONGEN_PORTAL_ID = "nostrum_dungeons_portal";
	private static final String DUNGEONGEN_DRAGON_ID = "nostrum_dungeons_dragon";
	private static final String DUNGEONGEN_PLANTBOSS_ID = "nostrum_dungeons_plantboss";
	private static final String DUNGEONGEN_PORTAL_CONF_ID = "configured_" + DUNGEONGEN_PORTAL_ID;
	private static final String DUNGEONGEN_DRAGON_CONF_ID = "configured_" + DUNGEONGEN_DRAGON_ID;
	private static final String DUNGEONGEN_PLANTBOSS_CONF_ID = "configured_" + DUNGEONGEN_PLANTBOSS_ID;
	
	@ObjectHolder(DUNGEONGEN_PORTAL_ID) public static PortalStructure DUNGEON_PORTAL;
	public static ConfiguredStructureFeature<?, ?> CONFIGURED_DUNGEON_PORTAL;
	
	@ObjectHolder(DUNGEONGEN_DRAGON_ID) public static DragonStructure DUNGEON_DRAGON;
	public static ConfiguredStructureFeature<?, ?> CONFIGURED_DUNGEON_DRAGON;
	
	@ObjectHolder(DUNGEONGEN_PLANTBOSS_ID) public static PlantBossStructure DUNGEON_PLANTBOSS;
	public static ConfiguredStructureFeature<?, ?> CONFIGUREDDUNGEON_PLANTBOSS;

	protected static final Map<StructureFeature<?>, StructureFeatureConfiguration> CUSTOM_SEPARATION_SETTINGS = new HashMap<>();

	@SubscribeEvent
	public static void registerStructures(RegistryEvent.Register<StructureFeature<?>> event) {
		CUSTOM_SEPARATION_SETTINGS.clear();
		
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
		configured = structure.configured(FeatureConfiguration.NONE);
		// Avg dist: sqrt(24^2 + 24^2) = 543 blocks
		registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_PORTAL_ID), NostrumMagica.Loc(DUNGEONGEN_PORTAL_CONF_ID), 12, 24, 0x26F1BDCF);
		CONFIGURED_DUNGEON_PORTAL = configured;
		
		structure = new DragonStructure();
		configured = structure.configured(FeatureConfiguration.NONE);
		// Avg dist: sqrt(32^2 + 32^2) = 724 blocks
		registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_DRAGON_ID), NostrumMagica.Loc(DUNGEONGEN_DRAGON_CONF_ID), 24, 32, 0x4558c30e);
		CONFIGURED_DUNGEON_DRAGON = configured;
		
		structure = new PlantBossStructure();
		configured = structure.configured(FeatureConfiguration.NONE);
		// Avg dist: sqrt(32^2 + 32^2) = 724 blocks
		registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_PLANTBOSS_ID), NostrumMagica.Loc(DUNGEONGEN_PLANTBOSS_CONF_ID), 20, 38, 0x2cc3005e);
		CONFIGUREDDUNGEON_PLANTBOSS = configured;

		MinecraftForge.EVENT_BUS.addListener(NostrumStructures::loadWorld);
	}
	
	private static void registerStructure(RegistryEvent.Register<StructureFeature<?>> event, StructureFeature<?> structure, ConfiguredStructureFeature<?, ?> config, ResourceLocation structName, ResourceLocation confName, int min, int max, int rand) {
		// Register structure itself
		event.getRegistry().register(structure.setRegistryName(structName));
		StructureFeature.STRUCTURES_REGISTRY.put(structName.toString(), structure);
		
		// Create seperation settings for structure
		StructureFeatureConfiguration seperation = new StructureFeatureConfiguration(max, min, rand);
		
		// Force into dimension structure settings map
		StructureSettings.DEFAULTS = ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration>builder().putAll(StructureSettings.DEFAULTS).
				put(structure, seperation).build();
		
		// Stash seperation settings in our own map for injection on world load
		CUSTOM_SEPARATION_SETTINGS.put(structure, seperation);
		
		// Register the configured version of our structure
		Registry.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, confName, config);
		
		// Put in flat generation settings, which apparently is useful for playing nice with other mods
		FlatLevelGeneratorSettings.STRUCTURE_FEATURES.put(structure, config);
	}
	
	//@SubscribeEvent subscribed to listener in #registerStructures as a hack because we can't mix busses
	public static void loadWorld(WorldEvent.Load event) {
		if(event.getWorld() instanceof ServerLevel && DimensionUtils.IsOverworld((ServerLevel)event.getWorld())) {
			final ServerLevel serverWorld = (ServerLevel)event.getWorld();
			final ServerChunkCache provider = serverWorld.getChunkSource();
			Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(provider.generator.getSettings().structureConfig());
			tempMap.putAll(CUSTOM_SEPARATION_SETTINGS);
			provider.generator.getSettings().structureConfig = tempMap;
		}
	}
}
