package com.smanzana.nostrummagica.world.gen;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.utils.DimensionUtils;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure.DragonStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure.PlantBossStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure.PortalStructure;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NostrumStructures {

	protected static final Map<Structure<?>, StructureSeparationSettings> CUSTOM_SEPARATION_SETTINGS = new HashMap<>();
	
	public static final PortalStructure DUNGEON_PORTAL = new PortalStructure();
	public static final StructureFeature<?, ?> CONFIGURED_DUNGEON_PORTAL = DUNGEON_PORTAL.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);
	
	public static final DragonStructure DUNGEON_DRAGON = new DragonStructure();
	public static final StructureFeature<?, ?> CONFIGURED_DUNGEON_DRAGON = DUNGEON_DRAGON.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);
	
	public static final PlantBossStructure DUNGEON_PLANTBOSS = new PlantBossStructure();
	public static final StructureFeature<?, ?> CONFIGUREDDUNGEON_PLANTBOSS = DUNGEON_PLANTBOSS.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);

	private static final String DUNGEONGEN_PORTAL_ID = "nostrum_dungeons_portal";
	private static final String DUNGEONGEN_DRAGON_ID = "nostrum_dungeons_dragon";
	private static final String DUNGEONGEN_PLANTBOSS_ID = "nostrum_dungeons_plantboss";

	@SubscribeEvent
	public static void registerStructures(RegistryEvent.Register<Structure<?>> event) {
		// Load rooms now, since dungeons require them
		// TODO make only happen once?
		DungeonRoomRegistry.instance().loadRegistryFromDisk();
		
		CUSTOM_SEPARATION_SETTINGS.clear();
		registerStructurePieceTypes();
		
		registerStructure(event, DUNGEON_PORTAL, CONFIGURED_DUNGEON_PORTAL, NostrumMagica.Loc(DUNGEONGEN_PORTAL_ID), 8, 16);
		registerStructure(event, DUNGEON_DRAGON, CONFIGURED_DUNGEON_DRAGON, NostrumMagica.Loc(DUNGEONGEN_DRAGON_ID), 10, 20);
		registerStructure(event, DUNGEON_PLANTBOSS, CONFIGUREDDUNGEON_PLANTBOSS, NostrumMagica.Loc(DUNGEONGEN_PLANTBOSS_ID), 10, 20);
	}
	
	private static void registerStructure(RegistryEvent.Register<Structure<?>> event, Structure<?> structure, StructureFeature<?, ?> config, ResourceLocation name, int min, int max) {
		// Register structure itself
		event.getRegistry().register(structure.setRegistryName(name));
		Structure.NAME_STRUCTURE_BIMAP.put(name.toString(), structure);
		
		// Create seperation settings for structure
		final int randOffsetMult = 0x26F1BDCF;
		StructureSeparationSettings seperation = new StructureSeparationSettings(max, min, randOffsetMult);
		
		// Force into dimension structure settings map
		DimensionStructuresSettings.field_236191_b_ = ImmutableMap.<Structure<?>, StructureSeparationSettings>builder().putAll(DimensionStructuresSettings.field_236191_b_).
				put(structure, seperation).build();
		
		// Stash seperation settings in our own map for injection on world load
		CUSTOM_SEPARATION_SETTINGS.put(structure, seperation);
		
		// Register the configured version of our structure
		Registry.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE, new ResourceLocation(name.getNamespace(), "configured_".concat(name.getPath())), config);
		
		// Put in flat generation settings, which apparently is useful for playing nice with other mods
		FlatGenerationSettings.STRUCTURES.put(structure, config);
	}
	
	//@SubscribeEvent Imagine.
	//public static void registerStructurePieceTypes(RegistryEvent.Register<IStructurePieceType> event) {
	protected static void registerStructurePieceTypes() {
		//event.getRegistry().register(NostrumDungeonStructure.DungeonPieceSerializer.instance);
		IStructurePieceType.register(NostrumDungeonStructure.DungeonPieceSerializer.instance, NostrumDungeonStructure.DungeonPieceSerializer.PIECE_ID);
	}

	@SubscribeEvent
	public static void load(WorldEvent.Load event) {
		if(event.getWorld() instanceof ServerWorld && DimensionUtils.IsOverworld((ServerWorld)event.getWorld())) {
			final ServerWorld serverWorld = (ServerWorld)event.getWorld();
			final ServerChunkProvider provider = serverWorld.getChunkProvider();
			Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(provider.generator.func_235957_b_().func_236195_a_());
			tempMap.putAll(CUSTOM_SEPARATION_SETTINGS);
			provider.generator.func_235957_b_().field_236193_d_ = tempMap;
		}
	}
}
