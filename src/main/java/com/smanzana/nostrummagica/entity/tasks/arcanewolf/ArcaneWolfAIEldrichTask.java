package com.smanzana.nostrummagica.entity.tasks.arcanewolf;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect.PredefinedEffect;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.ArcaneWolfElementalType;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.SpawnPredefinedEffectMessage;
import com.smanzana.nostrummagica.potions.MagicBoostPotion;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.MagicDamageSource;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;

public class ArcaneWolfAIEldrichTask extends EntityAIBase {

	protected final EntityArcaneWolf wolf;
	
	// Cooldown, done as compared to wolf.ticksExisted
	protected int cooldownTicks = 0;
	protected final int manaCost;
	
	protected int activeTicks;
	protected @Nullable EntityLivingBase activeTarget;
	
	public ArcaneWolfAIEldrichTask(EntityArcaneWolf wolf, int manaCost) {
		this.wolf = wolf;
		this.manaCost = manaCost;
		
		this.activeTicks = 0;
		
		this.setMutexBits(3);
	}
	
	@Override
	public boolean shouldExecute() {
		return !wolf.isDead
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
				&& !this.activeTarget.isDead;
	}
	
	@Override
	public boolean isInterruptible() {
		return false;
	}
	
	@Override
	public void resetTask() {
		final int backoff = 20 * 30;
		cooldownTicks = wolf.ticksExisted + backoff;
		activeTicks = 0;
		activeTarget = null;
	}
	
	protected void strikeEntity(EntityArcaneWolf wolf, EntityLivingBase target) {
//		// Capture velocity before attack
//		double velX = target.motionX;
//		double velY = target.motionY;
//		double velZ = target.motionZ;
		
		wolf.setLastAttackedEntity(target);
		target.setRevengeTarget(wolf);
		target.hurtResistantTime = 0;
		target.attackEntityFrom(new MagicDamageSource(wolf, EMagicElement.ENDER), 4);
		NostrumMagicaSounds.STATUS_DEBUFF3.play(target);
		
//		// Reset motion; we don't want knockback!
//		target.motionX = velX;
//		target.motionY = velY;
//		target.motionZ = velZ;
//		target.velocityChanged = true;
	}
	
	protected void startEffect(EntityArcaneWolf wolf, EntityLivingBase target, int duration) {
		NetworkHandler.getSyncChannel().sendToAllTracking(
				new SpawnPredefinedEffectMessage(PredefinedEffect.ELDRICH_BLAST, duration, target.dimension, target.getEntityId()),
				target);
	}
	
	@Override
	public void startExecuting() {
		// Eldrich blast attack is a slower multihit spell
		// Figure out how long to last for (modified by caster's efficiency!) and then set our active time
		// to that. Then, each tick, maybe do damage or update effect.
		// Effect is actually going to be 'predefined' client effect to avoid spamming packets.
		float base = 20 * 5;
		PotionEffect boostEffect = wolf.getActivePotionEffect(MagicBoostPotion.instance());
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
	public void updateTask() {
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
