package com.smanzana.nostrummagica.trial;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TrialPhysical extends WorldTrial {

	public TrialPhysical() {
		super(EMagicElement.PHYSICAL);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent e) {
		if (e.getEntityLiving() instanceof IronGolem) {
			DamageSource source = e.getSource();
			if (source.getEntity() == null ||
					!(source.getEntity() instanceof Player))
				return;
			
			Player player = (Player) source.getEntity();
			
			if (!player.getMainHandItem().isEmpty())
				return;
			
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null || !attr.isUnlocked())
				return;
			
			if (!attr.hasTrial(this.element))
				return;
			
			this.complete(player);
		}
	}
	
}
