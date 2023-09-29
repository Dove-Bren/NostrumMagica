package com.smanzana.nostrummagica.entity.tasks;

import com.smanzana.nostrummagica.entity.ITameableEntity;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.EntityAITarget;

public class EntityAIOwnerHurtTargetGeneric<T extends CreatureEntity & ITameableEntity> extends EntityAITarget {
	
	T entityTameable;
	LivingEntity theTarget;
	private int timestamp;

	public EntityAIOwnerHurtTargetGeneric(T entityTameableIn) {
		super(entityTameableIn, false);
		this.entityTameable = entityTameableIn;
		this.setMutexBits(1);
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean shouldExecute() {
		if (!this.entityTameable.isEntitySitting()) {
			return false;
		} else {
			LivingEntity entitylivingbase = this.entityTameable.getLivingOwner();

			if (entitylivingbase == null) {
				return false;
			} else {
				this.theTarget = entitylivingbase.getLastAttackedEntity();
				int i = entitylivingbase.getLastAttackedEntityTime();
				return i != this.timestamp && this.isSuitableTarget(this.theTarget, false);
			}
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.taskOwner.setAttackTarget(this.theTarget);
		LivingEntity entitylivingbase = this.entityTameable.getLivingOwner();

		if (entitylivingbase != null) {
			this.timestamp = entitylivingbase.getRevengeTimer();
		}

		super.startExecuting();
	}
}
