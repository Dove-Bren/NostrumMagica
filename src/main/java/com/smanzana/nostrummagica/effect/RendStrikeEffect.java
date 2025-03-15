package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class RendStrikeEffect extends Effect {

	public static final String ID = "rend_strike";
	
	public RendStrikeEffect() {
		super(EffectType.BENEFICIAL, 0xFF7B7B7B);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No tick effects
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onEntityAttack(LivingAttackEvent event) {
		if (event.getAmount() > 0f && !event.isCanceled()) {
			// Is this an attack from an entity?
			if (event.getSource().getEntity() != null
					&& event.getSource().getEntity() instanceof LivingEntity) {
				LivingEntity source = (LivingEntity) event.getSource().getEntity();
				EffectInstance effect = source.getEffect(NostrumEffects.rendStrike);
				if (effect != null && effect.getDuration() > 0) {
					// Apply rend effect to target, and remove it from the source
					EffectInstance rend = new EffectInstance(NostrumEffects.rend, 20 * 5, effect.getAmplifier());
					event.getEntityLiving().addEffect(rend);
					source.removeEffect(NostrumEffects.rendStrike);
					NostrumMagicaSounds.MELT_METAL.play(event.getEntityLiving());
					
					// And fall through and let event happen
				}
			}
		}
	}
}
