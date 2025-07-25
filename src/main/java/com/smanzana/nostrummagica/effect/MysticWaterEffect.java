package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class MysticWaterEffect extends MobEffect {

	public static final String ID = "mystic_water";
	
	public MysticWaterEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFF98E8FF);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return duration % 20 == 0; // No tick effects
	}
	
	protected void reduceAmp(LivingEntity entity, int newAmplifier) {
		MobEffectInstance instance = entity.getEffect(this);
		entity.removeEffect(this);
		
		if (newAmplifier >= 0) {
			entity.addEffect(new MobEffectInstance(this, instance.getDuration(), newAmplifier));
		}
	}
	
	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity.level.isClientSide()) {
			return;
		}
		final int diff = (int) Math.floor(entity.getMaxHealth() - entity.getHealth());
		if (diff > 1f) {
			final int heal = Math.min(diff, amplifier + 1);
			
			entity.heal(heal);
			reduceAmp(entity, (amplifier) - heal);
			NostrumParticles.FILLED_ORB.spawn(entity.level, new SpawnParams(
					5, entity.getX(), entity.getY() + .75, entity.getZ(), 1,
					40, 0,
					new TargetLocation(entity, true)
					).color(EMagicElement.ICE.getColor()).setTargetBehavior(TargetBehavior.ORBIT));
			entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1f, 1.75f);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityIgnite(LivingAttackEvent event) {
		// There isn't an event for when an entity gets caught on fire! So have to wait for first fire tick
		if (event.isCanceled()) {
			return;
		}
		
		if (event.getEntity().level.isClientSide()) {
			return;
		}
		
		if (event.getSource() == DamageSource.ON_FIRE) {
			MobEffectInstance effect = event.getEntityLiving().getEffect(NostrumEffects.mysticWater);
			if (effect != null && effect.getDuration() > 0) {
				final LivingEntity living = event.getEntityLiving();
				// Instantly extinguish and cancel in exchange for some effect
				event.setCanceled(true);
				living.clearFire();
				((MysticWaterEffect) effect.getEffect()).reduceAmp(living, effect.getAmplifier() - 5);
				event.getEntityLiving().level.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1f, 1f);
			}
		}
		
	}
}
