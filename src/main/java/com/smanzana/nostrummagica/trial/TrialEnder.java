package com.smanzana.nostrummagica.trial;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TrialEnder extends WorldTrial {

	public TrialEnder() {
		super(EMagicElement.ENDER);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onTeleport(EntityTeleportEvent e) {
		
		if (e.getEntity() instanceof PlayerEntity) {

			Vector3d pos = e.getEntity().getPositionVec();
			if (pos.squareDistanceTo(e.getTargetX(), e.getTargetY(), e.getTargetZ())
					< 10000)
				return; // 100x100
			
			INostrumMagic attr = NostrumMagica.getMagicWrapper(e.getEntity());
			if (attr == null || !attr.isUnlocked())
				return;
			
			if (!attr.hasTrial(this.element))
				return;
			
			this.complete((PlayerEntity) e.getEntity());
		}
	}
	
}
