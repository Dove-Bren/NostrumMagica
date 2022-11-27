package com.smanzana.nostrummagica.entity.tasks;

import com.smanzana.nostrummagica.entity.IEntityTameable;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;

public class EntityAIOwnerHurtTargetGeneric<T extends EntityCreature & IEntityTameable> extends EntityAITarget {
	
	T entityTameable;
	EntityLivingBase theTarget;
	private int timestamp;

	public EntityAIOwnerHurtTargetGeneric(T entityTameableIn) {
		super(entityTameableIn, false);
		this.entityTameable = entityTameableIn;
		this.setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {
		if (!this.entityTameable.isEntitySitting()) {
			return false;
		} else {
			EntityLivingBase entitylivingbase = this.entityTameable.getLivingOwner();

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
		EntityLivingBase entitylivingbase = this.entityTameable.getLivingOwner();

		if (entitylivingbase != null) {
			this.timestamp = entitylivingbase.getRevengeTimer();
		}

		super.startExecuting();
	}
}
