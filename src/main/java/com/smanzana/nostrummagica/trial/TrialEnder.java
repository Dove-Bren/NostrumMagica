package com.smanzana.nostrummagica.trial;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TrialEnder extends WorldTrial {

	public TrialEnder() {
		super(EMagicElement.ENDER);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onTeleport(EntityTeleportEvent e) {
		
		if (e.getEntity() instanceof Player) {

			Vec3 pos = e.getEntity().position();
			if (pos.distanceToSqr(e.getTargetX(), e.getTargetY(), e.getTargetZ())
					< 10000)
				return; // 100x100
			
			INostrumMagic attr = NostrumMagica.getMagicWrapper(e.getEntity());
			if (attr == null || !attr.isUnlocked())
				return;
			
			if (!attr.hasTrial(this.element))
				return;
			
			this.complete((Player) e.getEntity());
		}
	}
	
}
