package com.smanzana.nostrummagica.integration.curios;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.RainbowItemColor;
import com.smanzana.nostrummagica.integration.curios.client.SpelltomeCurioRenderer;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.item.NostrumItems;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

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
		if (NostrumMagica.CuriosProxy.isEnabled()) {
			ev.getItemColors().register(new RainbowItemColor(1), NostrumCurios.neckKoid);
		}
	}
	
	@SubscribeEvent
	public void onClientInit(FMLClientSetupEvent event) {
		if (NostrumMagica.CuriosProxy.isEnabled()) {
			CuriosRendererRegistry.register(NostrumItems.spellTomeCombat, () -> new SpelltomeCurioRenderer());
			CuriosRendererRegistry.register(NostrumItems.spellTomeAdvanced, () -> new SpelltomeCurioRenderer());
			CuriosRendererRegistry.register(NostrumItems.spellTomeDeath, () -> new SpelltomeCurioRenderer());
			CuriosRendererRegistry.register(NostrumItems.spellTomeLiving, () -> new SpelltomeCurioRenderer());
			CuriosRendererRegistry.register(NostrumItems.spellTomeMuted, () -> new SpelltomeCurioRenderer());
			CuriosRendererRegistry.register(NostrumItems.spellTomeNovice, () -> new SpelltomeCurioRenderer());
			CuriosRendererRegistry.register(NostrumItems.spellTomeSpooky, () -> new SpelltomeCurioRenderer());
		}
	}
}
