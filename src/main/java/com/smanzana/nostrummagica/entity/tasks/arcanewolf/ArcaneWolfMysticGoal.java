package com.smanzana.nostrummagica.entity.tasks.arcanewolf;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.ArcaneWolfElementalType;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.phys.Vec3;

public class ArcaneWolfMysticGoal extends Goal {

	protected final ArcaneWolfEntity wolf;
	
	// Cooldown, done as compared to wolf.ticksExisted
	protected int cooldownTicks = 0;
	protected final int manaCost;
	
	public ArcaneWolfMysticGoal(ArcaneWolfEntity wolf, int manaCost) {
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
				&& wolf.getElementalType() == ArcaneWolfElementalType.MYSTIC;
	}
	
	protected List<LivingEntity> getTargets(ArcaneWolfEntity wolf) {
		LivingEntity owner = wolf.getOwner();
		List<LivingEntity> tames = PetFuncs.GetTamedEntities(owner);
		tames.add(owner);
		tames.removeIf((e) -> { return e.distanceTo(wolf) > 15;});
		return tames;
	}
	
	protected boolean isBadEffect(MobEffectInstance effect) {
		return effect.getEffect().getCategory() == MobEffectCategory.HARMFUL;
	}
	
	protected boolean applyTo(ArcaneWolfEntity wolf, LivingEntity target) {
		// Mystic removes negative status effects from allies
		List<MobEffectInstance> removeList = new ArrayList<>();
		for (MobEffectInstance effect : target.getActiveEffects()) {
			if (isBadEffect(effect)) {
				removeList.add(effect);
			}
		}
		
		for (MobEffectInstance effect : removeList) {
			target.removeEffect(effect.getEffect());
		}
		
		return !removeList.isEmpty();
	}
	
	@Override
	public void start() {
		boolean applied = false;
		List<LivingEntity> targets = this.getTargets(wolf);
		for (LivingEntity target : targets) {
			if (applyTo(wolf, target)) {
				applied = true;
				NostrumMagicaSounds.SHIELD_ABSORB.play(target);
				for (int i = 0; i < 10; i++) {
					final double angleRad = 2 * Math.PI * ((double) i / 10.0);
					NostrumParticles.FILLED_ORB.spawn(wolf.level, new SpawnParams(
							1, target.getX(), target.getY() + target.getEyeHeight(), target.getZ(), 0, 30, 0,
							new Vec3(Math.cos(angleRad) * .1, .05, Math.sin(angleRad) * .1), null
							).color(ArcaneWolfElementalType.MYSTIC.getColor()));
				}
			}
		}
		
		int backoff = 5;
		if (applied) {
			backoff = 20 * 10;
			wolf.addMana(-manaCost);
		}
		cooldownTicks = wolf.tickCount + backoff;
	}

}
