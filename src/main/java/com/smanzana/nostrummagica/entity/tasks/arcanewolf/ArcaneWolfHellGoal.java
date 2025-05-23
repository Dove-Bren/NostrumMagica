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

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class ArcaneWolfHellGoal extends Goal {

	protected final ArcaneWolfEntity wolf;
	
	// Cooldown, done as compared to wolf.ticksExisted
	protected int cooldownTicks = 0;
	protected final int manaCost;
	
	protected int activeTicks;
	protected @Nullable LivingEntity activeTarget;
	
	public ArcaneWolfHellGoal(ArcaneWolfEntity wolf, int manaCost) {
		this.wolf = wolf;
		this.manaCost = manaCost;
		
		this.activeTicks = 0;
		
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	@Override
	public boolean canUse() {
		return wolf.isAlive()
				&& !wolf.isOrderedToSit()
				&& wolf.getTarget() != null
				&& wolf.tickCount >= cooldownTicks
				&& wolf.getMana() >= manaCost
				&& wolf.getElementalType() == ArcaneWolfElementalType.HELL
				;
	}
	
	@Override
	public boolean canContinueToUse() {
		return this.activeTicks > 0
				&& this.activeTarget != null
				&& this.activeTarget.isAlive();
	}
	
	@Override
	public boolean isInterruptable() {
		return false;
	}
	
	@Override
	public void stop() {
		final int backoff = 20 * 30;
		cooldownTicks = wolf.tickCount + backoff;
		activeTicks = 0;
		activeTarget = null;
	}
	
	protected void poisonEntity(ArcaneWolfEntity wolf, LivingEntity target) {
		// Capture velocity before attack
		double velX = target.getDeltaMovement().x;
		double velY = target.getDeltaMovement().y;
		double velZ = target.getDeltaMovement().z;
		
		wolf.setLastHurtMob(target);
		target.setLastHurtByMob(wolf);
		target.invulnerableTime = 0;
		target.hurt(DamageSource.mobAttack(wolf), .5f);
		
		// Reset motion; we don't want knockback!
		target.setDeltaMovement(velX, velY, velZ);
		target.hurtMarked = true;
	}
	
	protected void burnEntity(ArcaneWolfEntity wolf, LivingEntity target) {
		// Capture velocity before attack
		double velX = target.getDeltaMovement().x;
		double velY = target.getDeltaMovement().y;
		double velZ = target.getDeltaMovement().z;
		
		wolf.setLastHurtMob(target);
		target.setLastHurtByMob(wolf);
		target.invulnerableTime = 0;
		SpellDamage.DamageEntity(target, EMagicElement.FIRE, 4f, wolf);
		NostrumMagicaSounds.DAMAGE_FIRE.play(target);
		
		// Reset motion; we don't want knockback!
		target.setDeltaMovement(velX, velY, velZ);
		target.hurtMarked = true;
	}
	
	protected void startBurnEffect(ArcaneWolfEntity wolf, LivingEntity target, int duration) {
		NetworkHandler.sendToAllTracking(
				new SpawnPredefinedEffectMessage(PredefinedEffect.HELL_BURN, duration, DimensionUtils.GetDimension(target), target.getId()),
				target);
	}
	
	@Override
	public void start() {
		// Hell ability is a rapid 'poison' effect with fire visual effects
		// Figure out how long to last for (modified by caster's efficiency!) and then set our active time
		// to that. Then, each tick, maybe do damage or update effect.
		// Effect is actually going to be 'predefined' client effect to avoid spamming packets.
		float base = 20 * 5;
		MobEffectInstance boostEffect = wolf.getEffect(NostrumEffects.magicBoost);
		if (boostEffect != null) {
			base *= Math.pow(1.5, boostEffect.getAmplifier() + 1);
		}
		
		this.activeTicks = (int) base;
		this.activeTarget = wolf.getTarget();
		wolf.addMana(-manaCost);
		
		startBurnEffect(wolf, this.activeTarget, this.activeTicks);
		wolf.playSound(SoundEvents.WOLF_AMBIENT, 1f, .5f);
	}
	
	@Override
	public void tick() {
		if (activeTicks % 40 == 0) {
			burnEntity(wolf, this.activeTarget);
		} else if (activeTicks % 10 == 0) {
			poisonEntity(wolf, this.activeTarget);
		}
		if (activeTicks % 25 == 0) {
			wolf.playSound(SoundEvents.WOLF_GROWL, 1f, 1f);
		}
		wolf.lookAt(this.activeTarget, 30f, 180f);
		activeTicks--;
	}
}
