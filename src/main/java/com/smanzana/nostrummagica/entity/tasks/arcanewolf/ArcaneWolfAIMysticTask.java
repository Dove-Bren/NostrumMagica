package com.smanzana.nostrummagica.entity.tasks.arcanewolf;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.ArcaneWolfElementalType;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;

public class ArcaneWolfAIMysticTask extends Goal {

	protected final EntityArcaneWolf wolf;
	
	// Cooldown, done as compared to wolf.ticksExisted
	protected int cooldownTicks = 0;
	protected final int manaCost;
	
	public ArcaneWolfAIMysticTask(EntityArcaneWolf wolf, int manaCost) {
		this.wolf = wolf;
		this.manaCost = manaCost;
		
		this.setMutexBits(0); // Can execute with any! Nice!
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
	
	protected List<LivingEntity> getTargets(EntityArcaneWolf wolf) {
		LivingEntity owner = wolf.getOwner();
		List<LivingEntity> tames = NostrumMagica.getTamedEntities(owner);
		tames.add(owner);
		tames.removeIf((e) -> { return e.getDistance(wolf) > 15;});
		return tames;
	}
	
	protected boolean isBadEffect(PotionEffect effect) {
		return effect.getPotion().isBadEffect();
	}
	
	protected boolean applyTo(EntityArcaneWolf wolf, LivingEntity target) {
		// Mystic removes negative status effects from allies
		List<PotionEffect> removeList = new ArrayList<>();
		for (PotionEffect effect : target.getActivePotionEffects()) {
			if (isBadEffect(effect)) {
				removeList.add(effect);
			}
		}
		
		for (PotionEffect effect : removeList) {
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
							1, target.posX, target.posY + target.getEyeHeight(), target.posZ, 0, 30, 0,
							new Vec3d(Math.cos(angleRad) * .1, .05, Math.sin(angleRad) * .1), null
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
