package com.smanzana.nostrummagica.trials;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TrialPhysical extends ShrineTrial {

	public TrialPhysical() {
		super(EMagicElement.PHYSICAL);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent e) {
		if (e.getEntityLiving() instanceof EntityIronGolem) {
			DamageSource source = e.getSource();
			if (source.getTrueSource() == null ||
					!(source.getTrueSource() instanceof EntityPlayer))
				return;
			
			EntityPlayer player = (EntityPlayer) source.getTrueSource();
			
			if (player.getHeldItemMainhand() != null)
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
