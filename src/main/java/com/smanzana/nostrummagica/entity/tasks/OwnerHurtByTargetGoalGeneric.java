package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumSet;

import com.smanzana.petcommand.api.entity.ITameableEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class OwnerHurtByTargetGoalGeneric<T extends PathfinderMob & ITameableEntity> extends TargetGoal {
	
	protected static final TargetingConditions CanAttack = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
	protected T theDefendingTameable;
	protected LivingEntity theOwnerAttacker;
	private int timestamp;

	public OwnerHurtByTargetGoalGeneric(T theDefendingTameableIn) {
		super(theDefendingTameableIn, false);
		this.theDefendingTameable = theDefendingTameableIn;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean canUse() {
		if (!this.theDefendingTameable.isEntitySitting()) {
			return false;
		} else {
			LivingEntity entitylivingbase = this.theDefendingTameable.getLivingOwner();

			if (entitylivingbase == null) {
				return false;
			} else {
				this.theOwnerAttacker = entitylivingbase.getLastHurtByMob();
				int i = entitylivingbase.getLastHurtByMobTimestamp();
				return i != this.timestamp && this.canAttack(this.theOwnerAttacker, CanAttack);
			}
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		this.mob.setTarget(this.theOwnerAttacker);
		LivingEntity entitylivingbase = this.theDefendingTameable.getLivingOwner();

		if (entitylivingbase != null) {
			this.timestamp = entitylivingbase.getLastHurtByMobTimestamp();
		}

		super.start();
	}
}
