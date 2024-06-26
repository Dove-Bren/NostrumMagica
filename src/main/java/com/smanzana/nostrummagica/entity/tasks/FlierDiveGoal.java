package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumSet;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

/**
 * Attempts to dive into the enemy to make a melee attack.
 * @author Skyler
 *
 * @param <T>
 */
public class FlierDiveGoal<T extends MobEntity> extends Goal
{
	protected final T entity;
	private final double moveSpeedAmp;
	private final int attackDelay;
	private final float maxAttackDistance;
	private final boolean requiresSight;
	
	private long lastAttackTicks;
	private int stallTicks;

	public FlierDiveGoal(T entity, double speedAmplifier, int delay, float maxDistance, boolean requiresSight) {
		this.entity = entity;
		this.moveSpeedAmp = speedAmplifier;
		this.attackDelay = delay;
		this.maxAttackDistance = maxDistance * maxDistance;
		this.requiresSight = requiresSight;
		this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
		
		lastAttackTicks = 0;
	}
	
	protected boolean attackedTooRecently() {
		return lastAttackTicks != 0 && entity.world.getGameTime() <= lastAttackTicks + attackDelay;
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean shouldExecute() {
		if (!entity.isAlive() || entity.getAttackTarget() == null) {
			return false;
		}
		
		LivingEntity target = entity.getAttackTarget();
		if (entity.getDistanceSq(target) > maxAttackDistance) {
			return false;
		}
		
		if (requiresSight && !entity.getEntitySenses().canSee(target)) {
			return false;
		}
		
		if (attackedTooRecently()) {
			return false;
		}
		
		if (!NostrumMagica.rand.nextBoolean() || !NostrumMagica.rand.nextBoolean()) {
			return false;
		}
		
		return true;
	}

	/**
	 * Returns whether an in-progress Goal should continue executing
	 */
	public boolean shouldContinueExecuting() {
		return (stallTicks < (20 * 1) && !attackedTooRecently());
	}
	
	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		super.startExecuting();
		stallTicks = 0;
	}

	/**
	 * Resets the task
	 */
	public void resetTask() {
		super.resetTask();
		this.stallTicks = 0;
		//this.lastAttackTicks = 0;
	}
	
	public void attackTarget(T entity, LivingEntity target) {
		entity.attackEntityAsMob(entity.getAttackTarget());		
	}

	/**
	 * Updates the task
	 */
	public void tick() {
		LivingEntity target = this.entity.getAttackTarget();

		if (target != null) {
			
			// If close enough, attack!
			if (entity.getDistanceSq(target.getPosX(), target.getPosY() + (target.getHeight() / 2), target.getPosZ()) < Math.max(entity.getWidth() * entity.getWidth(), 1.5)) {
				this.attackTarget(entity, target);
				this.lastAttackTicks = entity.world.getGameTime();
				entity.getMoveHelper().strafe(1f, 0f);
			} else {
			
				// Attempt to move towards the target
				entity.getMoveHelper().setMoveTo(target.getPosX(), target.getPosY() + (target.getHeight() / 2), target.getPosZ(), moveSpeedAmp);
				
				if (Math.abs(entity.prevPosX - entity.getPosX())
						+ Math.abs(entity.prevPosY - entity.getPosY())
						+ Math.abs(entity.prevPosZ - entity.getPosZ()) < .01) {
					// Stuck?
					stallTicks++;
				} else {
					stallTicks = 0;
				}
			}
		}
	}
}
