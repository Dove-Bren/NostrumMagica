package com.smanzana.nostrummagica.trials;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TrialWind extends ShrineTrial {

	public TrialWind() {
		super(EMagicElement.WIND);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onFall(LivingFallEvent e) {
		
		if (e.getEntityLiving() instanceof PlayerEntity) {
			
			if (e.getDistance() < 100.0f)
				return;
			
			INostrumMagic attr = NostrumMagica.getMagicWrapper(e.getEntityLiving());
			if (attr == null || !attr.isUnlocked())
				return;
			
			if (!attr.hasTrial(this.element))
				return;
			
			this.complete((PlayerEntity) e.getEntityLiving());
		}
	}
	
}
