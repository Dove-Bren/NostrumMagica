package com.smanzana.nostrummagica.client.model;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NostrumModelLayers {

	public static final ModelLayerLocation Lux = make("lux");
	public static final ModelLayerLocation DragonEgg = make("dragon_egg");
	public static final ModelLayerLocation FlightWings = make("flight_wings");
	public static final ModelLayerLocation Golem = make("golem");
	
	
	
	private static final ModelLayerLocation make(String name) {
		return make(name, "main");
	}
	
	private static final ModelLayerLocation make(String name, String layer) {
		return new ModelLayerLocation(NostrumMagica.Loc(name), layer);
	}
	
	@SubscribeEvent
	public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(Lux, ModelLux::createLayer);
		event.registerLayerDefinition(DragonEgg, ModelDragonEgg::createLayer);
		event.registerLayerDefinition(FlightWings, ModelDragonFlightWings::createLayer);
		event.registerLayerDefinition(Golem, ModelGolem::createLayer);
	}
}
