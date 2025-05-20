package com.smanzana.nostrummagica.effect;

import java.util.List;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.util.Entities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public class SoulDrainEffect extends MobEffect {

	public static final String ID = "soul_drain";
	
	public SoulDrainEffect() {
		super(MobEffectCategory.HARMFUL, 0xFFE5799B);
		
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
				"13dfa3dc-1105-40a9-92e4-bbc506eb90ed", -0.1D, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		if (duration <= 0)
			return false;
		
		final int interval = 20;
		return (duration % interval == 0);
	}
	
	protected void playHealthEffect(LivingEntity source, float amt, LivingEntity dest) {
		NostrumParticles.FILLED_ORB.spawn(source.level, new SpawnParams(
				5, source.getX(), source.getY() + .75, source.getZ(), 0,
				40, 0,
				dest.getId()
				).color(0xFFE2346B).dieWithTarget(true));
	}
	
	protected void playDamageEffect(LivingEntity source, float amt) {
		NostrumParticles.GLOW_ORB.spawn(source.level, new SpawnParams(
				5, source.getX(), source.getY() + .75, source.getZ(), 0,
				40, 0,
				new Vec3(0, .1, 0), new Vec3(.1, 0, .1)
				).color(0xFFE2346B).gravity(true));
	}
	
	protected void sendHealth(LivingEntity source, float amt, LivingEntity dest) {
		dest.heal(amt);
	}
	
	protected void doDamage(LivingEntity source, float amt) {
		source.invulnerableTime = 0;
		source.hurt(DamageSource.MAGIC, amt); // Vanilla magic - no modifications
	}
	
	protected List<LivingEntity> findSoulDestinations(LivingEntity source) {
		return Entities.GetEntities((ServerLevel) source.level, (living) -> {
			return living.distanceToSqr(source) <= 144
					&& living.getEffect(NostrumEffects.soulVampire) != null;
		});
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amp) {
		if (!entity.level.isClientSide()) {
			// Half the time, it's just a visual tick
			final float amount = (float) Math.pow(2f, amp);
			
			doDamage(entity, amount);
			// Reset damage timer
			entity.invulnerableTime = 10;
			playDamageEffect(entity, amount);
			
			// Look for transfer destinations
			for (LivingEntity dest : findSoulDestinations(entity)) {
				//final int blizzardCount = MagicArmor.GetSetCount(entity, EMagicElement.ICE, MagicArmor.Type.TRUE);
				sendHealth(entity, amount, dest);
				playHealthEffect(entity, amount, dest);
			}
		}
    }
}
