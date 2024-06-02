package com.smanzana.nostrummagica.entity.tasks.arcanewolf;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.ArcaneWolfElementalType;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.potion.EffectInstance;

public class ArcaneWolfAIBarrierTask extends Goal {

	protected final EntityArcaneWolf wolf;
	
	// Cooldown, done as compared to wolf.ticksExisted
	protected int cooldownTicks = 0;
	protected final int manaCost;
	
	public ArcaneWolfAIBarrierTask(EntityArcaneWolf wolf, int manaCost) {
		this.wolf = wolf;
		this.manaCost = manaCost;
		
		//this.setMutexFlags(0); // Can execute with any! Nice!
	}
	
	@Override
	public boolean shouldExecute() {
		return wolf.isAlive()
				&& !wolf.isSitting()
				&& wolf.getOwner() != null
				&& wolf.ticksExisted >= cooldownTicks
				&& wolf.getMana() >= manaCost
				&& wolf.getElementalType() == ArcaneWolfElementalType.BARRIER;
	}
	
	protected List<LivingEntity> getTargets(EntityArcaneWolf wolf) {
		LivingEntity owner = wolf.getOwner();
		List<LivingEntity> tames = PetFuncs.GetTamedEntities(owner);
		tames.add(owner);
		tames.removeIf((e) -> { return e.getDistance(wolf) > 15;});
		return tames;
	}
	
	protected boolean applyTo(EntityArcaneWolf wolf, LivingEntity target) {
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
			doPhysical = wolf.getRNG().nextBoolean();
		} else {
			doPhysical = !hasPhysical;
		}
		
		if (doPhysical) {
			EffectInstance effect = new EffectInstance(NostrumEffects.physicalShield, 20 * 15, 0);
			if (!hasPhysical) {
				// Re-apply potion effect
				target.addPotionEffect(effect);
				// Change out the amount though
				NostrumMagica.magicEffectProxy.applyPhysicalShield(target, amtToAdd);
				applied = true;
			} else {
				// Refresh potion effect
				target.getActivePotionEffect(NostrumEffects.physicalShield).combine(effect);
				
				if (currentPhysical.getAmt() < maxAmt) {
					// Add 1 to current amount
					NostrumMagica.magicEffectProxy.applyPhysicalShield(target, Math.min(maxAmt, amtToAdd + currentPhysical.getAmt()));
					applied = true;
				}
			}
		} else {
			EffectInstance effect = new EffectInstance(NostrumEffects.magicShield, 20 * 15, 0);
			if (!hasMagical) {
				// Re-apply potion effect
				target.addPotionEffect(effect);
				// Change out the amount though
				NostrumMagica.magicEffectProxy.applyMagicalShield(target, amtToAdd);
				applied = true;
			} else {
				// Refresh potion effect
				target.getActivePotionEffect(NostrumEffects.magicShield).combine(effect);
				
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
	public void startExecuting() {
		boolean applied = false;
		int backoff = 5;
		List<LivingEntity> targets = this.getTargets(wolf);
		for (LivingEntity target : targets) {
			if (applyTo(wolf, target)) {
				applied = true;
				NostrumParticles.FILLED_ORB.spawn(wolf.world, new SpawnParams(
						1, wolf.getPosX(), wolf.getPosY() + wolf.getHeight()/2, wolf.getPosZ(), 0, 40, 0, target.getEntityId()
						).color(ArcaneWolfElementalType.BARRIER.getColor()));
			}
		}
		
		if (applied) {
			wolf.addMana(-manaCost);
			backoff = 20;
		}
		cooldownTicks = wolf.ticksExisted + backoff;
	}

}
