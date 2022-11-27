package com.smanzana.nostrummagica.entity.tasks;

import com.smanzana.nostrummagica.entity.IEntityTameable;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAISitGeneric<T extends EntityCreature & IEntityTameable> extends EntityAIBase {
	
	private final T entity;

	public EntityAISitGeneric(T entityIn) {
		this.entity = entityIn;
		this.setMutexBits(5);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {
		if (!this.entity.isEntityTamed()) {
			return false;
		} else if (this.entity.isInWater()) {
			return false;
		} else if (!this.entity.onGround) {
			return false;
		} else {
			EntityLivingBase entitylivingbase = this.entity.getLivingOwner();
			return entitylivingbase == null ? this.entity.isEntitySitting() : (this.entity.getDistanceSq(entitylivingbase) < 144.0D && entitylivingbase.getRevengeTarget() != null ? false : entity.isEntitySitting());
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.entity.getNavigator().clearPath();
	}

}
