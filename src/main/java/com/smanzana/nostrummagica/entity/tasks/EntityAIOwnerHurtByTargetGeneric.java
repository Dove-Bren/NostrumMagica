package com.smanzana.nostrummagica.entity.tasks;

import com.smanzana.nostrummagica.entity.IEntityTameable;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.EntityAITarget;

public class EntityAIOwnerHurtByTargetGeneric<T extends EntityCreature & IEntityTameable> extends EntityAITarget {
	
	protected T theDefendingTameable;
	protected LivingEntity theOwnerAttacker;
	private int timestamp;

	public EntityAIOwnerHurtByTargetGeneric(T theDefendingTameableIn) {
		super(theDefendingTameableIn, false);
		this.theDefendingTameable = theDefendingTameableIn;
		this.setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {
		if (!this.theDefendingTameable.isEntitySitting()) {
			return false;
		} else {
			LivingEntity entitylivingbase = this.theDefendingTameable.getLivingOwner();

			if (entitylivingbase == null) {
				return false;
			} else {
				this.theOwnerAttacker = entitylivingbase.getRevengeTarget();
				int i = entitylivingbase.getRevengeTimer();
				return i != this.timestamp && this.isSuitableTarget(this.theOwnerAttacker, false);
			}
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.taskOwner.setAttackTarget(this.theOwnerAttacker);
		LivingEntity entitylivingbase = this.theDefendingTameable.getLivingOwner();

		if (entitylivingbase != null) {
			this.timestamp = entitylivingbase.getRevengeTimer();
		}

		super.startExecuting();
	}
}
