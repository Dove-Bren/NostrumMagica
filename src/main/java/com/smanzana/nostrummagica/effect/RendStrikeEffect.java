package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class RendStrikeEffect extends MobEffect {

	public static final String ID = "rend_strike";
	
	public RendStrikeEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFF7B7B7B);
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
				MobEffectInstance effect = source.getEffect(NostrumEffects.rendStrike);
				if (effect != null && effect.getDuration() > 0) {
					// Apply rend effect to target, and remove it from the source
					MobEffectInstance rend = new MobEffectInstance(NostrumEffects.rend, 20 * 5, effect.getAmplifier());
					event.getEntityLiving().addEffect(rend);
					source.removeEffect(NostrumEffects.rendStrike);
					NostrumMagicaSounds.MELT_METAL.play(event.getEntityLiving());
					
					// And fall through and let event happen
				}
			}
		}
	}
}
