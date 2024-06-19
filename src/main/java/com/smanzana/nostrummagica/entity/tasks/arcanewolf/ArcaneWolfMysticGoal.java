package com.smanzana.nostrummagica.entity.tasks.arcanewolf;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.ArcaneWolfElementalType;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.vector.Vector3d;

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
	public boolean shouldExecute() {
		return wolf.isAlive()
				&& !wolf.isSitting()
				&& wolf.getOwner() != null
				&& wolf.ticksExisted >= cooldownTicks
				&& wolf.getMana() >= manaCost
				&& wolf.getElementalType() == ArcaneWolfElementalType.MYSTIC;
	}
	
	protected List<LivingEntity> getTargets(ArcaneWolfEntity wolf) {
		LivingEntity owner = wolf.getOwner();
		List<LivingEntity> tames = PetFuncs.GetTamedEntities(owner);
		tames.add(owner);
		tames.removeIf((e) -> { return e.getDistance(wolf) > 15;});
		return tames;
	}
	
	protected boolean isBadEffect(EffectInstance effect) {
		return effect.getPotion().getEffectType() == EffectType.HARMFUL;
	}
	
	protected boolean applyTo(ArcaneWolfEntity wolf, LivingEntity target) {
		// Mystic removes negative status effects from allies
		List<EffectInstance> removeList = new ArrayList<>();
		for (EffectInstance effect : target.getActivePotionEffects()) {
			if (isBadEffect(effect)) {
				removeList.add(effect);
			}
		}
		
		for (EffectInstance effect : removeList) {
			target.removePotionEffect(effect.getPotion());
		}
		
		return !removeList.isEmpty();
	}
	
	@Override
	public void startExecuting() {
		boolean applied = false;
		List<LivingEntity> targets = this.getTargets(wolf);
		for (LivingEntity target : targets) {
			if (applyTo(wolf, target)) {
				applied = true;
				NostrumMagicaSounds.SHIELD_ABSORB.play(target);
				for (int i = 0; i < 10; i++) {
					final double angleRad = 2 * Math.PI * ((double) i / 10.0);
					NostrumParticles.FILLED_ORB.spawn(wolf.world, new SpawnParams(
							1, target.getPosX(), target.getPosY() + target.getEyeHeight(), target.getPosZ(), 0, 30, 0,
							new Vector3d(Math.cos(angleRad) * .1, .05, Math.sin(angleRad) * .1), null
							).color(ArcaneWolfElementalType.MYSTIC.getColor()));
				}
			}
		}
		
		int backoff = 5;
		if (applied) {
			backoff = 20 * 10;
			wolf.addMana(-manaCost);
		}
		cooldownTicks = wolf.ticksExisted + backoff;
	}

}
