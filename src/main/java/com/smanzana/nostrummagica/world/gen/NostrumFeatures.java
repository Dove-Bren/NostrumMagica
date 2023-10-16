package com.smanzana.nostrummagica.world.gen;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumFeatures {

	private static final String FLOWERGEN_ID = "nostrum_flowers";
	private static final String DUNGEONGEN_ID = "nostrum_shrines";
	
	@ObjectHolder(FLOWERGEN_ID) public static NostrumFlowerGenerator flowers;
	@ObjectHolder(DUNGEONGEN_ID) public static NostrumDungeonGenerator dungeons;
	
	@SubscribeEvent
	public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
		final IForgeRegistry<Feature<?>> registry = event.getRegistry();
		
		registry.register(new NostrumFlowerGenerator(NostrumFlowerGenerator.NostrumFlowerConfig::deserialize).setRegistryName(FLOWERGEN_ID));
		registry.register(new NostrumDungeonGenerator(NostrumDungeonGenerator.NostrumDungeonConfig::deserialize).setRegistryName(DUNGEONGEN_ID));
	}
	
}
