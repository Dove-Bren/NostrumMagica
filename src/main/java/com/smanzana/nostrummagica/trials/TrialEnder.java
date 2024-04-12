package com.smanzana.nostrummagica.trials;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TrialEnder extends WorldTrial {

	public TrialEnder() {
		super(EMagicElement.ENDER);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onTeleport(EnderTeleportEvent e) {
		
		if (e.getEntityLiving() instanceof PlayerEntity) {

			Vector3d pos = e.getEntityLiving().getPositionVector();
			if (pos.squareDistanceTo(e.getTargetX(), e.getTargetY(), e.getTargetZ())
					< 10000)
				return; // 100x100
			
			INostrumMagic attr = NostrumMagica.getMagicWrapper(e.getEntityLiving());
			if (attr == null || !attr.isUnlocked())
				return;
			
			if (!attr.hasTrial(this.element))
				return;
			
			this.complete((PlayerEntity) e.getEntityLiving());
		}
	}
	
}
