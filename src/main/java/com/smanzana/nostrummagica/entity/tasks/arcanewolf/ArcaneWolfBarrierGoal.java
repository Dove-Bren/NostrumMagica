package com.smanzana.nostrummagica.entity.tasks.arcanewolf;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.ArcaneWolfElementalType;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.util.TargetLocation;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class ArcaneWolfBarrierGoal extends Goal {

	protected final ArcaneWolfEntity wolf;
	
	// Cooldown, done as compared to wolf.ticksExisted
	protected int cooldownTicks = 0;
	protected final int manaCost;
	
	public ArcaneWolfBarrierGoal(ArcaneWolfEntity wolf, int manaCost) {
		this.wolf = wolf;
		this.manaCost = manaCost;
		
		//this.setMutexFlags(0); // Can execute with any! Nice!
	}
	
	@Override
	public boolean canUse() {
		return wolf.isAlive()
				&& !wolf.isOrderedToSit()
				&& wolf.getOwner() != null
				&& wolf.tickCount >= cooldownTicks
				&& wolf.getMana() >= manaCost
				&& wolf.getElementalType() == ArcaneWolfElementalType.BARRIER;
	}
	
	protected List<LivingEntity> getTargets(ArcaneWolfEntity wolf) {
		LivingEntity owner = wolf.getOwner();
		List<LivingEntity> tames = PetFuncs.GetTamedEntities(owner);
		tames.add(owner);
		tames.removeIf((e) -> { return e.distanceTo(wolf) > 15;});
		return tames;
	}
	
	protected boolean applyTo(ArcaneWolfEntity wolf, LivingEntity target) {
		// Barrier buffs up their physical and magical armor, adding some if they have none
		final double amtToAdd = 1;
		final double maxAmt = 4;
		final boolean doPhysical;
		final EffectData currentPhysical = NostrumMagica.magicEffectProxy.getData(target, SpecialEffect.SHIELD_PHYSICAL);
		final EffectData currentMagical = NostrumMagica.magicEffectProxy.getData(target, SpecialEffect.SHIELD_MAGIC);
		final boolean hasPhysical = currentPhysical != null && currentPhysical.getAmt() > 0;
		final boolean hasMagical = currentMagical != null && currentMagical.getAmt() > 0;
		boolean applied = false;
		
		if (hasPhysical == hasMagical) {
			doPhysical = wolf.getRandom().nextBoolean();
		} else {
			doPhysical = !hasPhysical;
		}
		
		if (doPhysical) {
			MobEffectInstance effect = new MobEffectInstance(NostrumEffects.physicalShield, 20 * 15, 0);
			if (!hasPhysical) {
				// Re-apply potion effect
				target.addEffect(effect);
				// Change out the amount though
				NostrumMagica.magicEffectProxy.applyPhysicalShield(target, amtToAdd);
				applied = true;
			} else {
				// Refresh potion effect
				target.getEffect(NostrumEffects.physicalShield).update(effect);
				
				if (currentPhysical.getAmt() < maxAmt) {
					// Add 1 to current amount
					NostrumMagica.magicEffectProxy.applyPhysicalShield(target, Math.min(maxAmt, amtToAdd + currentPhysical.getAmt()));
					applied = true;
				}
			}
		} else {
			MobEffectInstance effect = new MobEffectInstance(NostrumEffects.magicShield, 20 * 15, 0);
			if (!hasMagical) {
				// Re-apply potion effect
				target.addEffect(effect);
				// Change out the amount though
				NostrumMagica.magicEffectProxy.applyMagicalShield(target, amtToAdd);
				applied = true;
			} else {
				// Refresh potion effect
				target.getEffect(NostrumEffects.magicShield).update(effect);
				
				if (currentMagical.getAmt() < maxAmt) {
					// Add 1 to current amount
					NostrumMagica.magicEffectProxy.applyMagicalShield(target, Math.min(maxAmt, amtToAdd + currentMagical.getAmt()));
					applied = true;
				}
			}
		}
		
		return applied;
	}
	
	@Override
	public void start() {
		boolean applied = false;
		int backoff = 5;
		List<LivingEntity> targets = this.getTargets(wolf);
		for (LivingEntity target : targets) {
			if (applyTo(wolf, target)) {
				applied = true;
				NostrumParticles.FILLED_ORB.spawn(wolf.level, new SpawnParams(
						1, wolf.getX(), wolf.getY() + wolf.getBbHeight()/2, wolf.getZ(), 0, 40, 0, new TargetLocation(target, true)
						).setTargetBehavior(TargetBehavior.ORBIT_LAZY).color(ArcaneWolfElementalType.BARRIER.getColor()));
			}
		}
		
		if (applied) {
			wolf.addMana(-manaCost);
			backoff = 20;
		}
		cooldownTicks = wolf.tickCount + backoff;
	}

}
