package com.smanzana.nostrummagica.entity.tasks.arcanewolf;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.ArcaneWolfElementalType;
import com.smanzana.nostrummagica.entity.NostrumTameLightning;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.SoundEvents;

public class ArcaneWolfAIStormTask extends EntityAIBase {

	protected final EntityArcaneWolf wolf;
	
	// Cooldown, done as compared to wolf.ticksExisted
	protected int cooldownTicks = 0;
	protected final int manaCost;

	protected boolean active;
	protected @Nullable EntityLivingBase activeTarget;
	
	public ArcaneWolfAIStormTask(EntityArcaneWolf wolf, int manaCost) {
		this.wolf = wolf;
		this.manaCost = manaCost;
		
		active = false;
		
		this.setMutexBits(3);
	}
	
	@Override
	public boolean shouldExecute() {
		return !wolf.isDead
				&& !wolf.isSitting()
				&& wolf.getAttackTarget() != null
				&& wolf.ticksExisted >= cooldownTicks
				&& wolf.getMana() >= manaCost
				&& wolf.getElementalType() == ArcaneWolfElementalType.STORM
				&& !(wolf.getAttackTarget() instanceof EntityCreeper);
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		return this.active
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
		active = false;
		activeTarget = null;
	}
	
	protected void launchEntity(EntityArcaneWolf wolf, EntityLivingBase target) {
		target.setVelocity(0, 1.5, 0);
	}
	
	protected boolean shouldBlastEntity(EntityArcaneWolf wolf, EntityLivingBase target) {
		return target != null
				&& !target.isDead
				&& target.motionY < 0;
	}
	
	protected void blastEntity(EntityArcaneWolf wolf, EntityLivingBase target) {
		for (int i = 0; i < 2; i++) {
			target.world.addWeatherEffect(
					new NostrumTameLightning(target.world,
							target.posX + (wolf.getRNG().nextFloat()-.5f),
							target.posY,
							target.posZ + (wolf.getRNG().nextFloat()-.5f))
					);
		}
	}
	
	@Override
	public void startExecuting() {
		// Storm blasts the target upwards. After some time, it strikes them with several bolts of lighting!
		// We'll do this by first shooting them upwards. As soon as they start falling again, we'll BLAST EM
		this.active = true;
		this.activeTarget = wolf.getAttackTarget();
		wolf.addMana(-manaCost);
		
		launchEntity(wolf, this.activeTarget);
		wolf.playSound(SoundEvents.ENTITY_WOLF_AMBIENT, 1f, .5f);
	}
	
	@Override
	public void updateTask() {
		if (shouldBlastEntity(wolf, this.activeTarget)) {
			blastEntity(wolf, this.activeTarget);
			this.active = false; // Signal that we're done
		}
		if (wolf.ticksExisted % 25 == 0) {
			wolf.playSound(SoundEvents.ENTITY_WOLF_GROWL, 1f, 1f);
		}
		wolf.faceEntity(this.activeTarget, 30f, 180f);
	}

}
