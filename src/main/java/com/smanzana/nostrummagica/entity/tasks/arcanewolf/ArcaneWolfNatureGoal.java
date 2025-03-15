package com.smanzana.nostrummagica.entity.tasks.arcanewolf;

import java.util.List;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.ArcaneWolfElementalType;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.potion.EffectInstance;

public class ArcaneWolfNatureGoal extends Goal {

	protected final ArcaneWolfEntity wolf;
	
	// Cooldown, done as compared to wolf.ticksExisted
	protected int cooldownTicks = 0;
	protected final int manaCost;
	
	public ArcaneWolfNatureGoal(ArcaneWolfEntity wolf, int manaCost) {
		this.wolf = wolf;
		this.manaCost = manaCost;
		
		//this.setMutexBits(0); // Can execute with any! Nice!
	}
	
	@Override
	public boolean canUse() {
		return wolf.isAlive()
				&& !wolf.isOrderedToSit()
				&& wolf.getOwner() != null
				&& wolf.tickCount >= cooldownTicks
				&& wolf.getMana() >= manaCost
				&& wolf.getElementalType() == ArcaneWolfElementalType.NATURE;
	}
	
	protected List<LivingEntity> getTargets(ArcaneWolfEntity wolf) {
		LivingEntity owner = wolf.getOwner();
		List<LivingEntity> tames = PetFuncs.GetTamedEntities(owner);
		tames.add(owner);
		tames.removeIf((e) -> { return e.distanceTo(wolf) > 15;});
		return tames;
	}
	
	protected boolean applyTo(ArcaneWolfEntity wolf, LivingEntity target) {
		// Nature keeps the "Nature's blessing" status effect constant
		EffectInstance effect = target.getEffect(NostrumEffects.naturesBlessing);
		if (effect == null || effect.getDuration() < 11 * 20) {
			target.addEffect(new EffectInstance(NostrumEffects.naturesBlessing, 20 * 30, 0));
		}
		
		return effect == null; // Only charge for applying the first time
	}
	
	@Override
	public void start() {
		int backoff = 5;
		List<LivingEntity> targets = this.getTargets(wolf);
		for (LivingEntity target : targets) {
			if (applyTo(wolf, target)) {
				wolf.addMana(-manaCost);
				NostrumParticles.FILLED_ORB.spawn(wolf.level, new SpawnParams(
						1, wolf.getX(), wolf.getY() + wolf.getBbHeight()/2, wolf.getZ(), 0, 20, 0, target.getId()
						).color(ArcaneWolfElementalType.NATURE.getColor()));
			}
		}
		
		cooldownTicks = wolf.tickCount + backoff;
	}

}
