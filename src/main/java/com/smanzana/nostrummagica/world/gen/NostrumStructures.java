package com.smanzana.nostrummagica.world.gen;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.DragonStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.PlantBossStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructures.PortalStructure;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
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
	public static StructureFeature<?, ?> CONFIGURED_DUNGEON_PORTAL;
	
	@ObjectHolder(DUNGEONGEN_DRAGON_ID) public static DragonStructure DUNGEON_DRAGON;
	public static StructureFeature<?, ?> CONFIGURED_DUNGEON_DRAGON;
	
	@ObjectHolder(DUNGEONGEN_PLANTBOSS_ID) public static PlantBossStructure DUNGEON_PLANTBOSS;
	public static StructureFeature<?, ?> CONFIGUREDDUNGEON_PLANTBOSS;

	protected static final Map<Structure<?>, StructureSeparationSettings> CUSTOM_SEPARATION_SETTINGS = new HashMap<>();

	@SubscribeEvent
	public static void registerStructures(RegistryEvent.Register<Structure<?>> event) {
		CUSTOM_SEPARATION_SETTINGS.clear();
		
		Structure<NoFeatureConfig> structure;
		StructureFeature<?, ?> configured;
		
		// getChunkPosForStructure()
		// min/max settings are actually:
		// max is that every MAX x MAX chunk block, there is a spawn attempt
		// the distance from the exact floor boundary is a MIN x MIN chunk region.
		// I think the 'average' + 'minimum distance' wording is that average is really
		// average (spawn attempts every 'average' blocks) and min is min in that if two
		// regions spawn towards eachother, that would be the distance between? Except it's not. It's
		// (max-min) ?
		
		structure = new PortalStructure();
		configured = structure.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);
		// Avg dist: sqrt(24^2 + 24^2) = 543 blocks
		registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_PORTAL_ID), NostrumMagica.Loc(DUNGEONGEN_PORTAL_CONF_ID), 12, 24, 0x26F1BDCF);
		CONFIGURED_DUNGEON_PORTAL = configured;
		
		structure = new DragonStructure();
		configured = structure.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);
		// Avg dist: sqrt(32^2 + 32^2) = 724 blocks
		registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_DRAGON_ID), NostrumMagica.Loc(DUNGEONGEN_DRAGON_CONF_ID), 24, 32, 0x4558c30e);
		CONFIGURED_DUNGEON_DRAGON = configured;
		
		structure = new PlantBossStructure();
		configured = structure.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);
		// Avg dist: sqrt(32^2 + 32^2) = 724 blocks
		registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_PLANTBOSS_ID), NostrumMagica.Loc(DUNGEONGEN_PLANTBOSS_CONF_ID), 20, 38, 0x2cc3005e);
		CONFIGUREDDUNGEON_PLANTBOSS = configured;

		MinecraftForge.EVENT_BUS.addListener(NostrumStructures::loadWorld);
	}
	
	private static void registerStructure(RegistryEvent.Register<Structure<?>> event, Structure<?> structure, StructureFeature<?, ?> config, ResourceLocation structName, ResourceLocation confName, int min, int max, int rand) {
		// Register structure itself
		event.getRegistry().register(structure.setRegistryName(structName));
		Structure.NAME_STRUCTURE_BIMAP.put(structName.toString(), structure);
		
		// Create seperation settings for structure
		StructureSeparationSettings seperation = new StructureSeparationSettings(max, min, rand);
		
		// Force into dimension structure settings map
		DimensionStructuresSettings.field_236191_b_ = ImmutableMap.<Structure<?>, StructureSeparationSettings>builder().putAll(DimensionStructuresSettings.field_236191_b_).
				put(structure, seperation).build();
		
		// Stash seperation settings in our own map for injection on world load
		CUSTOM_SEPARATION_SETTINGS.put(structure, seperation);
		
		// Register the configured version of our structure
		Registry.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE, confName, config);
		
		// Put in flat generation settings, which apparently is useful for playing nice with other mods
		FlatGenerationSettings.STRUCTURES.put(structure, config);
	}
	
	//@SubscribeEvent subscribed to listener in #registerStructures as a hack because we can't mix busses
	public static void loadWorld(WorldEvent.Load event) {
		if(event.getWorld() instanceof ServerWorld && DimensionUtils.IsOverworld((ServerWorld)event.getWorld())) {
			final ServerWorld serverWorld = (ServerWorld)event.getWorld();
			final ServerChunkProvider provider = serverWorld.getChunkProvider();
			Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(provider.generator.func_235957_b_().func_236195_a_());
			tempMap.putAll(CUSTOM_SEPARATION_SETTINGS);
			provider.generator.func_235957_b_().field_236193_d_ = tempMap;
		}
	}
}
