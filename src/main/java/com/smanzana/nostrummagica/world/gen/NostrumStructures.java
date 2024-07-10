package com.smanzana.nostrummagica.world.gen;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure.DragonStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure.PlantBossStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure.PortalStructure;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
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
		registerStructurePieceTypes();
		
		Structure<NoFeatureConfig> structure;
		StructureFeature<?, ?> configured;
		
		structure = new PortalStructure();
		configured = structure.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);
		registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_PORTAL_ID), NostrumMagica.Loc(DUNGEONGEN_PORTAL_CONF_ID), 20, 32, 0x26F1BDCF);
		CONFIGURED_DUNGEON_PORTAL = configured;
		
		structure = new DragonStructure();
		configured = structure.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);
		registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_DRAGON_ID), NostrumMagica.Loc(DUNGEONGEN_DRAGON_CONF_ID), 20, 48, 0x4558c30e);
		CONFIGURED_DUNGEON_DRAGON = configured;
		
		structure = new PlantBossStructure();
		configured = structure.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);
		registerStructure(event, structure, configured, NostrumMagica.Loc(DUNGEONGEN_PLANTBOSS_ID), NostrumMagica.Loc(DUNGEONGEN_PLANTBOSS_CONF_ID), 20, 48, 0x2cc3005e);
		CONFIGUREDDUNGEON_PLANTBOSS = configured;
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
	
	//@SubscribeEvent Imagine.
	//public static void registerStructurePieceTypes(RegistryEvent.Register<IStructurePieceType> event) {
	protected static void registerStructurePieceTypes() {
		//event.getRegistry().register(NostrumDungeonStructure.DungeonPieceSerializer.instance);
		IStructurePieceType.register(NostrumDungeonStructure.DungeonPieceSerializer.instance, NostrumDungeonStructure.DungeonPieceSerializer.PIECE_ID);
		
		MinecraftForge.EVENT_BUS.addListener(NostrumStructures::loadWorld);
	}

	//@SubscribeEvent subscribed to listener in #registerStructurePieceTypes as a hack because we can't mix busses
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
