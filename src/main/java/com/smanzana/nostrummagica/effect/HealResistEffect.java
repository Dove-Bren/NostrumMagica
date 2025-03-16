package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class HealResistEffect extends MobEffect {

	public static final String ID = "heal_resist";
	
	public HealResistEffect() {
		super(MobEffectCategory.HARMFUL, 0xFF0E0485);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No tick effects
	}
	
	private static boolean RecursionGuard = false;
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityHeal(LivingHealEvent event) {
		if (RecursionGuard) {
			return;
		}
		RecursionGuard = true;
		if (event.getAmount() > 0f && !event.isCanceled()) {
			MobEffectInstance effect = event.getEntityLiving().getEffect(NostrumEffects.healResist);
			if (effect != null && effect.getDuration() > 0) {
				final float origAmt = event.getAmount();
				final float reduc = Math.max(0f, (3 - (effect.getAmplifier()+1)) / 3f);
				event.setAmount(origAmt * reduc);
				
				// If amp is higher than 2 (100% reduc), start hurting
				if (effect.getAmplifier() > 2) {
					final float hurt = (float) ((effect.getAmplifier()+1)-3) / 3f;
					final float amt = hurt * origAmt;
					final LivingEntity target = event.getEntityLiving();
					target.hurt(DamageSource.MAGIC, amt); // Vanilla magic ; no modification
					NostrumMagicaSounds.CAST_CONTINUE.play(target);
					NostrumParticles.FILLED_ORB.spawn(target.level, new SpawnParams(
							50, target.getX(), target.getY() + target.getBbHeight()/2, target.getZ(), 0,
							30, 10,
							new Vec3(0, .1, 0), new Vec3(.2, .05, .2)
							).color(0xFF0E0485).gravity(true));
				}
			}
		}
		RecursionGuard = false;
	}
}
