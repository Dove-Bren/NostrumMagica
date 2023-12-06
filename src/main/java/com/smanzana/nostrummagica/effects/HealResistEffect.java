package com.smanzana.nostrummagica.effects;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class HealResistEffect extends Effect {

	public static final String ID = "heal_resist";
	
	public HealResistEffect() {
		super(EffectType.HARMFUL, 0xFF0E0485);
	}
	
	public boolean isReady(int duration, int amp) {
		return false; // No tick effects
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityHeal(LivingHealEvent event) {
		if (event.getAmount() > 0f) {
			EffectInstance effect = event.getEntityLiving().getActivePotionEffect(NostrumEffects.healResist);
			if (effect != null && effect.getDuration() > 0) {
				final float reduc = Math.max(0f, 1 - (.25f * (effect.getAmplifier() + 1)));
				
				//NostrumMagica.logger.debug("HealResist: " + event.getAmount() + " => " + (event.getAmount() * reduc));
				
				event.setAmount(event.getAmount() * reduc);
			}
		}
		
	}
}
