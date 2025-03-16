package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumSet;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Attempts to dive into the enemy to make a melee attack.
 * @author Skyler
 *
 * @param <T>
 */
public class FlierDiveGoal<T extends Mob> extends Goal
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
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
		
		lastAttackTicks = 0;
	}
	
	protected boolean attackedTooRecently() {
		return lastAttackTicks != 0 && entity.level.getGameTime() <= lastAttackTicks + attackDelay;
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean canUse() {
		if (!entity.isAlive() || entity.getTarget() == null) {
			return false;
		}
		
		LivingEntity target = entity.getTarget();
		if (entity.distanceToSqr(target) > maxAttackDistance) {
			return false;
		}
		
		if (requiresSight && !entity.getSensing().canSee(target)) {
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
	public boolean canContinueToUse() {
		return (stallTicks < (20 * 1) && !attackedTooRecently());
	}
	
	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		super.start();
		stallTicks = 0;
	}

	/**
	 * Resets the task
	 */
	public void stop() {
		super.stop();
		this.stallTicks = 0;
		//this.lastAttackTicks = 0;
	}
	
	public void attackTarget(T entity, LivingEntity target) {
		entity.doHurtTarget(entity.getTarget());		
	}

	/**
	 * Updates the task
	 */
	public void tick() {
		LivingEntity target = this.entity.getTarget();

		if (target != null) {
			
			// If close enough, attack!
			if (entity.distanceToSqr(target.getX(), target.getY() + (target.getBbHeight() / 2), target.getZ()) < Math.max(entity.getBbWidth() * entity.getBbWidth(), 1.5)) {
				this.attackTarget(entity, target);
				this.lastAttackTicks = entity.level.getGameTime();
				entity.getMoveControl().strafe(1f, 0f);
			} else {
			
				// Attempt to move towards the target
				entity.getMoveControl().setWantedPosition(target.getX(), target.getY() + (target.getBbHeight() / 2), target.getZ(), moveSpeedAmp);
				
				if (Math.abs(entity.xo - entity.getX())
						+ Math.abs(entity.yo - entity.getY())
						+ Math.abs(entity.zo - entity.getZ()) < .01) {
					// Stuck?
					stallTicks++;
				} else {
					stallTicks = 0;
				}
			}
		}
	}
}
