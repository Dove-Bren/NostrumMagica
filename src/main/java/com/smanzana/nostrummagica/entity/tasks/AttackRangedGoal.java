package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumSet;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.BowItem;
import net.minecraft.util.Hand;

public class AttackRangedGoal<T extends MobEntity> extends Goal
{
	protected final T entity;
	private final double moveSpeedAmp;
	private int attackCooldown;
	private final float maxAttackDistance;
	private int attackTime = -1;
	private int seeTime;
	private boolean strafingClockwise;
	private boolean strafingBackwards;
	private int strafingTime = -1;
	protected boolean startedAttacking;

	public AttackRangedGoal(T entity, double speedAmplifier, int delay, float maxDistance) {
		this.entity = entity;
		this.moveSpeedAmp = speedAmplifier;
		this.attackCooldown = delay;
		this.maxAttackDistance = maxDistance * maxDistance;
		this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	public void setAttackCooldown(int cooldown) {
		this.attackCooldown = cooldown;
	}
	
	public boolean hasWeaponEquipped(T entity) {
		return (!this.entity.getHeldItemMainhand().isEmpty() && this.entity.getHeldItemMainhand().getItem() instanceof BowItem)
				|| (!this.entity.getHeldItemOffhand().isEmpty() && this.entity.getHeldItemOffhand().getItem() instanceof BowItem);
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean shouldExecute() {
		return this.entity.getAttackTarget() == null ? false : this.hasWeaponEquipped(entity);
	}

	/**
	 * Returns whether an in-progress Goal should continue executing
	 */
	public boolean shouldContinueExecuting() {
		return (this.shouldExecute() || !this.entity.getNavigator().noPath()) && this.hasWeaponEquipped(entity);
	}
	
	/**
	 * Called when the preparation animation starts playing. For Skeletons, this is when they have a target and have raised their bow.
	 */
	protected void startAimAnimation(T entity) {
		
	}
	
	/**
	 * Called when the target can attack if animation allowed. For skeleton's, this is 'using the bow' item
	 */
	protected void startAttackAnimation(T entity) {
		entity.setActiveHand(Hand.MAIN_HAND);
	}
	
	/**
	 * Called after an attack is made. Note: the entity presumably still has a target. For skeletons, this is stopping 'using' the bow.
	 */
	protected void resetAttackAnimation(T entity) {
		entity.resetActiveHand();
	}
	
	/**
	 * Called when the entity is no longer going to attack. For skeletons, this means dropping their bow.
	 */
	protected void resetAllAnimation(T entity) {
		
	}
	
	/**
	 * Check whether the animation has finished and we can attack.
	 * For skeletons, this is when the bow they're using's startup cooldown has finished
	 * @return
	 */
	protected boolean isAttackAnimationComplete(T entity) {
		return (getChargeTime(entity) >= 20);
	}
	
	protected int getChargeTime(T entity) {
		return entity.getItemInUseMaxCount();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		super.startExecuting();
		startAimAnimation(entity);
		startedAttacking = false;
	}

	/**
	 * Resets the task
	 */
	public void resetTask() {
		super.resetTask();
		this.seeTime = 0;
		this.attackTime = -1;
		resetAllAnimation(entity);
	}
	
	public void attackTarget(T entity, LivingEntity target, int chargeCount) {
		if (entity instanceof IRangedAttackMob) {
			IRangedAttackMob mob = (IRangedAttackMob) this.entity;
			mob.attackEntityWithRangedAttack(target, BowItem.getArrowVelocity(chargeCount));
		} else {
			NostrumMagica.logger.error("EntityAIAttackRanged tried to attack, but provided entity has no attack");
		}
		
	}

	/**
	 * Updates the task
	 */
	public void tick() {
		LivingEntity entitylivingbase = this.entity.getAttackTarget();

		if (entitylivingbase != null) {
			double d0 = this.entity.getDistanceSq(entitylivingbase.getPosX(), entitylivingbase.getBoundingBox().minY, entitylivingbase.getPosZ());
			boolean flag = this.entity.getEntitySenses().canSee(entitylivingbase);
			boolean flag1 = this.seeTime > 0;

			if (flag != flag1) {
				this.seeTime = 0;
			}

			if (flag) {
				++this.seeTime;
			} else {
				--this.seeTime;
			}

			if (d0 <= (double)this.maxAttackDistance && this.seeTime >= 20) {
				this.entity.getNavigator().clearPath();
				++this.strafingTime;
			} else {
				this.entity.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.moveSpeedAmp);
				this.strafingTime = -1;
			}

			if (this.strafingTime >= 20) {
				if ((double)this.entity.getRNG().nextFloat() < 0.3D) {
					this.strafingClockwise = !this.strafingClockwise;
				}

				if ((double)this.entity.getRNG().nextFloat() < 0.3D) {
					this.strafingBackwards = !this.strafingBackwards;
				}

				this.strafingTime = 0;
			}

			if (this.strafingTime > -1) {
				if (d0 > (double)(this.maxAttackDistance * 0.75F)) {
					this.strafingBackwards = false;
				} else if (d0 < (double)(this.maxAttackDistance * 0.5F)) {
					this.strafingBackwards = true;
				}

				this.entity.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
				this.entity.faceEntity(entitylivingbase, 30.0F, 30.0F);
			} else {
				this.entity.getLookController().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
			}
			
			if (startedAttacking) {
				if (!flag && this.seeTime < -60) {
					resetAllAnimation(entity);
					startedAttacking = false;
				} else if (flag) {
					if (isAttackAnimationComplete(entity)) {
						resetAttackAnimation(entity);
						attackTarget(this.entity, entitylivingbase, getChargeTime(entity));
						this.attackTime = this.attackCooldown;
						startedAttacking = false;
					}
				}
			} else if (--this.attackTime <= 0 && this.seeTime >= -60) {
				startedAttacking = true;
				startAttackAnimation(entity);
			}
		}
	}
}
