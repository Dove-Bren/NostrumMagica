package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class DamageSplitEffect extends MobEffect {

	public static final String ID = "damage_split";
	
	public DamageSplitEffect() {
		super(MobEffectCategory.NEUTRAL, 0xFF42C775); 
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No tick effects
	}
	
	protected static float getDamagePerc(int amp) {
		return Math.min(.9f, .2f + .1f * amp); // 20%, 30%, 40%, ... capped at 90%
	}
	
	protected static void splashDamage(LivingEntity source, DamageSource damageType, float amt) {
		final float range = 10;
		for (Entity ent : source.getLevel().getEntities(source, AABB.ofSize(source.position(), range * 2, range * 2, range * 2), e -> e instanceof LivingEntity)) {
			LivingEntity living = (LivingEntity) ent;
			//DamageSource.indirectMagic(source, damageType.getEntity())
			living.hurt(damageType, amt);
		}
	}
	
	protected static boolean recurseGuard = false;
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onEntityAttack(LivingHurtEvent event) {
		if (recurseGuard) {
			return;
		}
		
		recurseGuard = true;
		{
			if (event.getAmount() > 0f && !event.isCanceled()) {
				LivingEntity ent = event.getEntityLiving();
				MobEffectInstance effect = ent.getEffect(NostrumEffects.damageSplit);
				
				if (effect != null && effect.getDuration() > 0) {
					final float perc = getDamagePerc(effect.getAmplifier());
					final float reduc = perc * event.getAmount();
					event.setAmount(Math.max(0, event.getAmount() - reduc));
					
					splashDamage(ent, event.getSource(), reduc);
					
					NostrumParticles.GLOW_ORB.spawn(ent.getLevel(), new SpawnParams(
							50,
							ent.getX(), ent.getY() + ent.getBbHeight()/2f, ent.getZ(), .15, 50, 30,
							Vec3.ZERO, new Vec3(.1, .1, .1)
							).color(0xFFDD4422));
					
					NostrumMagicaSounds.HOOKSHOT_TICK.play(ent);
				}
			}
		}
		recurseGuard = false;
	}
}
