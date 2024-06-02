package com.smanzana.nostrummagica.trial;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TrialLightning extends WorldTrial {

	public TrialLightning() {
		super(EMagicElement.LIGHTNING);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onLightning(EntityStruckByLightningEvent e) {
		
		if (e.getEntity() instanceof PlayerEntity) {
		
			INostrumMagic attr = NostrumMagica.getMagicWrapper(e.getEntity());
			if (attr == null || !attr.isUnlocked())
				return;
			
			if (!attr.hasTrial(this.element))
				return;
			
			this.complete((PlayerEntity) e.getEntity());
		}
	}
	
}
