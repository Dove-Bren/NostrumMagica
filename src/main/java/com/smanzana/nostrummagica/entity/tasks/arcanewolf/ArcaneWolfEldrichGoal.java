package com.smanzana.nostrummagica.entity.tasks.arcanewolf;

import java.util.EnumSet;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect.PredefinedEffect;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.ArcaneWolfElementalType;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.SpawnPredefinedEffectMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellDamage;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundEvents;

public class ArcaneWolfEldrichGoal extends Goal {

	protected final ArcaneWolfEntity wolf;
	
	// Cooldown, done as compared to wolf.ticksExisted
	protected int cooldownTicks = 0;
	protected final int manaCost;
	
	protected int activeTicks;
	protected @Nullable LivingEntity activeTarget;
	
	public ArcaneWolfEldrichGoal(ArcaneWolfEntity wolf, int manaCost) {
		this.wolf = wolf;
		this.manaCost = manaCost;
		
		this.activeTicks = 0;
		
		this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	@Override
	public boolean shouldExecute() {
		return wolf.isAlive()
				&& !wolf.isSitting()
				&& wolf.getAttackTarget() != null
				&& wolf.ticksExisted >= cooldownTicks
				&& wolf.getMana() >= manaCost
				&& wolf.getElementalType() == ArcaneWolfElementalType.ELDRICH
				;
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		return this.activeTicks > 0
				&& this.activeTarget != null
				&& this.activeTarget.isAlive();
	}
	
	@Override
	public boolean isPreemptible() {
		return false;
	}
	
	@Override
	public void resetTask() {
		final int backoff = 20 * 30;
		cooldownTicks = wolf.ticksExisted + backoff;
		activeTicks = 0;
		activeTarget = null;
	}
	
	protected void strikeEntity(ArcaneWolfEntity wolf, LivingEntity target) {
//		// Capture velocity before attack
//		double velX = target.getMotion().x;
//		double velY = target.getMotion().y;
//		double velZ = target.getMotion().z;
		
		wolf.setLastAttackedEntity(target);
		target.setRevengeTarget(wolf);
		target.hurtResistantTime = 0;
		SpellDamage.DamageEntity(target, EMagicElement.ENDER, 4f, wolf);
		NostrumMagicaSounds.STATUS_DEBUFF3.play(target);
		
//		// Reset motion; we don't want knockback!
//		target.getMotion().x = velX;
//		target.getMotion().y = velY;
//		target.getMotion().z = velZ;
//		target.velocityChanged = true;
	}
	
	protected void startEffect(ArcaneWolfEntity wolf, LivingEntity target, int duration) {
		NetworkHandler.sendToAllTracking(
				new SpawnPredefinedEffectMessage(PredefinedEffect.ELDRICH_BLAST, duration, DimensionUtils.GetDimension(target), target.getEntityId()),
				target);
	}
	
	@Override
	public void startExecuting() {
		// Eldrich blast attack is a slower multihit spell
		// Figure out how long to last for (modified by caster's efficiency!) and then set our active time
		// to that. Then, each tick, maybe do damage or update effect.
		// Effect is actually going to be 'predefined' client effect to avoid spamming packets.
		float base = 20 * 5;
		EffectInstance boostEffect = wolf.getActivePotionEffect(NostrumEffects.magicBoost);
		if (boostEffect != null) {
			base *= Math.pow(1.5, boostEffect.getAmplifier() + 1);
		}
		
		this.activeTicks = (int) base;
		this.activeTarget = wolf.getAttackTarget();
		wolf.addMana(-manaCost);
		
		startEffect(wolf, this.activeTarget, this.activeTicks);
		wolf.playSound(SoundEvents.ENTITY_WOLF_AMBIENT, 1f, .5f);
	}
	
	@Override
	public void tick() {
		if (activeTicks % 30 == 0) {
			strikeEntity(wolf, this.activeTarget);
		}
		if (activeTicks % 25 == 0) {
			wolf.playSound(SoundEvents.ENTITY_WOLF_GROWL, 1f, 1f);
		}
		wolf.faceEntity(this.activeTarget, 30f, 180f);
		activeTicks--;
	}

}
