package com.smanzana.nostrummagica.integration.curios;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.RainbowItemColor;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CuriosClientProxy extends CuriosProxy {

	public CuriosClientProxy() {
		super();
	}
	
	@Override
	public void init() {
		super.init();
	}
	
	@SubscribeEvent
	public static void registerColorHandlers(ColorHandlerEvent.Item ev) {
		if (NostrumMagica.instance.curios.isEnabled()) {
			ev.getItemColors().register(new RainbowItemColor(1), NostrumCurios.neckKoid);
		}
	}
}
