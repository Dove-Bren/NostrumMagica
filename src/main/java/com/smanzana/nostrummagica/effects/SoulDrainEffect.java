package com.smanzana.nostrummagica.effects;

import java.util.List;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.utils.Entities;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public class SoulDrainEffect extends Effect {

	public static final String ID = "soul_drain";
	
	public SoulDrainEffect() {
		super(EffectType.HARMFUL, 0xFFE5799B);
		
		this.addAttributesModifier(Attributes.MOVEMENT_SPEED,
				"13dfa3dc-1105-40a9-92e4-bbc506eb90ed", -0.1D, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
	
	public boolean isReady(int duration, int amp) {
		if (duration <= 0)
			return false;
		
		final int interval = 20;
		return (duration % interval == 0);
	}
	
	protected void playHealthEffect(LivingEntity source, float amt, LivingEntity dest) {
		NostrumParticles.FILLED_ORB.spawn(source.world, new SpawnParams(
				5, source.getPosX(), source.getPosY() + .75, source.getPosZ(), 0,
				40, 0,
				dest.getEntityId()
				).color(0xFFE2346B).dieOnTarget(true));
	}
	
	protected void playDamageEffect(LivingEntity source, float amt) {
		NostrumParticles.GLOW_ORB.spawn(source.world, new SpawnParams(
				5, source.getPosX(), source.getPosY() + .75, source.getPosZ(), 0,
				40, 0,
				new Vector3d(0, .1, 0), new Vector3d(.1, 0, .1)
				).color(0xFFE2346B).gravity(true));
	}
	
	protected void sendHealth(LivingEntity source, float amt, LivingEntity dest) {
		dest.heal(amt);
	}
	
	protected void doDamage(LivingEntity source, float amt) {
		source.hurtResistantTime = 0;
		source.attackEntityFrom(DamageSource.MAGIC, amt);
	}
	
	protected List<LivingEntity> findSoulDestinations(LivingEntity source) {
		return Entities.GetEntities((ServerWorld) source.world, (living) -> {
			return living.getDistanceSq(source) <= 144
					&& living.getActivePotionEffect(NostrumEffects.soulVampire) != null;
		});
	}

	@Override
	public void performEffect(LivingEntity entity, int amp) {
		if (!entity.world.isRemote()) {
			// Half the time, it's just a visual tick
			final float amount = (float) Math.pow(2f, amp);
			
			doDamage(entity, amount);
			// Reset damage timer
			entity.hurtResistantTime = 10;
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
