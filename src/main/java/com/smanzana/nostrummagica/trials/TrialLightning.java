package com.smanzana.nostrummagica.trials;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TrialLightning extends ShrineTrial {

	public TrialLightning() {
		super(EMagicElement.LIGHTNING);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onLightning(EntityStruckByLightningEvent e) {
		
		if (e.getEntity() instanceof EntityPlayer) {
		
			INostrumMagic attr = NostrumMagica.getMagicWrapper(e.getEntity());
			if (attr == null || !attr.isUnlocked())
				return;
			
			if (!attr.hasTrial(this.element))
				return;
			
			this.complete((EntityPlayer) e.getEntity());
		}
	}
	
}
