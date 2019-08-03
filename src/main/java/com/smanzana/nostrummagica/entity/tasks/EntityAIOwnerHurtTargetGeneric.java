package com.smanzana.nostrummagica.entity.tasks;

import com.smanzana.nostrummagica.entity.IEntityTameable;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;

public class EntityAIOwnerHurtTargetGeneric<T extends EntityCreature & IEntityTameable> extends EntityAITarget {
	
	T theEntityTameable;
	EntityLivingBase theTarget;
	private int timestamp;

	public EntityAIOwnerHurtTargetGeneric(T theEntityTameableIn) {
		super(theEntityTameableIn, false);
		this.theEntityTameable = theEntityTameableIn;
		this.setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {
		if (!this.theEntityTameable.isTamed()) {
			return false;
		} else {
			EntityLivingBase entitylivingbase = this.theEntityTameable.getOwner();

			if (entitylivingbase == null) {
				return false;
			} else {
				this.theTarget = entitylivingbase.getLastAttacker();
				int i = entitylivingbase.getLastAttackerTime();
				return i != this.timestamp && this.isSuitableTarget(this.theTarget, false);
			}
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.taskOwner.setAttackTarget(this.theTarget);
		EntityLivingBase entitylivingbase = this.theEntityTameable.getOwner();

		if (entitylivingbase != null) {
			this.timestamp = entitylivingbase.getLastAttackerTime();
		}

		super.startExecuting();
	}
}
