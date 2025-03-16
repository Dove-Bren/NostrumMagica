package com.smanzana.nostrummagica.listener;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ManaArmorListener {

	public ManaArmorListener() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityDamaged(LivingDamageEvent e) {
		final Entity ent = e.getEntity();
		if (ent == null || ent.getCommandSenderWorld() == null || ent.getCommandSenderWorld().isClientSide() || e.isCanceled()) {
			return;
		}
		
		@Nullable IManaArmor armor = NostrumMagica.getManaArmor(ent);
		if (armor != null && armor.hasArmor()) {
			if (armor.canHandle(ent, e.getSource(), e.getAmount())) {
				final float newAmt = armor.handle(ent, e.getSource(), e.getAmount());
				e.setAmount(newAmt);
				if (newAmt <= 0) {
					e.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityDeath(LivingDeathEvent e) {
		final Entity ent = e.getEntity();
		if (ent == null || ent.getCommandSenderWorld() == null || ent.getCommandSenderWorld().isClientSide() || e.isCanceled()) {
			return;
		}
		
		@Nullable IManaArmor armor = NostrumMagica.getManaArmor(ent);
		if (armor != null && armor.hasArmor()) {
			armor.setHasArmor(false, 0);
		}
	}
}
