package com.smanzana.nostrummagica.integration.aetheria;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

//
public class AetheriaClientProxy extends AetheriaProxy {
	
	public AetheriaClientProxy() {
		super();
	}
	
	@Override
	public boolean preInit() {
		if (!super.preInit()) {
			return false;
		}

		return true;
	}
	
	@Override
	public boolean init() {
		if (!super.init()) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean postInit() {
		if (!super.postInit()) {
			return false;
		}
		
		return true;
	}
	
	@SubscribeEvent
	public void registerAllModels(ModelRegistryEvent event) {
		
	}
	
	@SubscribeEvent
	public void clientSetup(FMLClientSetupEvent event) {
		
	}
}
