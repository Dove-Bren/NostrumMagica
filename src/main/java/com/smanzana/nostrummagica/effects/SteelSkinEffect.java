package com.smanzana.nostrummagica.effects;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class SteelSkinEffect extends Effect {

	public static final String ID = "steel_skin";
	
	public SteelSkinEffect() {
		super(EffectType.BENEFICIAL, 0xFF394653); 
	}
	
	public boolean isReady(int duration, int amp) {
		return false; // No tick effects
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onEntityAttack(LivingHurtEvent event) {
		if (event.getAmount() > 0f && !event.isCanceled()) {
			LivingEntity ent = event.getEntityLiving();
			EffectInstance effect = ent.getActivePotionEffect(NostrumEffects.steelSkin);
			
			if (effect != null && effect.getDuration() > 0) {
				final int reduc = 2 * (effect.getAmplifier() + 1);
				event.setAmount(Math.max(0, event.getAmount() - reduc));
				NostrumMagicaSounds.HOOKSHOT_TICK.play(ent);
			}
		}
	}
}
