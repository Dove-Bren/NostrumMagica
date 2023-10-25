package com.smanzana.nostrummagica.world.gen;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure.DragonStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure.PlantBossStructure;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure.PortalStructure;

import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumFeatures {

	private static final String FLOWERGEN_ID = "nostrum_flowers";
	private static final String DUNGEONGEN_PORTAL_ID = "nostrum_dungeons_portal";
	private static final String DUNGEONGEN_DRAGON_ID = "nostrum_dungeons_dragon";
	private static final String DUNGEONGEN_PLANTBOSS_ID = "nostrum_dungeons_plantboss";
	
	@ObjectHolder(FLOWERGEN_ID) public static NostrumFlowerGenerator flowers;
	@ObjectHolder(DUNGEONGEN_PORTAL_ID) public static NostrumDungeonStructure portalDungeon;
	@ObjectHolder(DUNGEONGEN_DRAGON_ID) public static NostrumDungeonStructure dragonDungeon;
	@ObjectHolder(DUNGEONGEN_PLANTBOSS_ID) public static NostrumDungeonStructure plantbossDungeon;
	
	@SubscribeEvent
	public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
		final IForgeRegistry<Feature<?>> registry = event.getRegistry();
		
		// Load rooms now, since dungeons require them
		DungeonRoomRegistry.instance().loadRegistryFromDisk();
		
		//registry.register(new NostrumFlowerGenerator(NostrumFlowerGenerator.NostrumFlowerConfig::deserialize).setRegistryName(FLOWERGEN_ID));
		registry.register(new NostrumFlowerGenerator(NoFeatureConfig::deserialize).setRegistryName(FLOWERGEN_ID));
		registry.register(new PortalStructure(NostrumDungeonStructure.NostrumDungeonConfig::deserialize).setRegistryName(DUNGEONGEN_PORTAL_ID));
		registry.register(new DragonStructure(NostrumDungeonStructure.NostrumDungeonConfig::deserialize).setRegistryName(DUNGEONGEN_DRAGON_ID));
		registry.register(new PlantBossStructure(NostrumDungeonStructure.NostrumDungeonConfig::deserialize).setRegistryName(DUNGEONGEN_PLANTBOSS_ID));
		
		registerStructurePieceTypes();
	}
	
	//@SubscribeEvent Imagine.
	//public static void registerStructurePieceTypes(RegistryEvent.Register<IStructurePieceType> event) {
	protected static void registerStructurePieceTypes() {
		//event.getRegistry().register(NostrumDungeonStructure.DungeonPieceSerializer.instance);
		IStructurePieceType.register(NostrumDungeonStructure.DungeonPieceSerializer.instance, NostrumDungeonStructure.DungeonPieceSerializer.PIECE_ID);
	}
	
}
