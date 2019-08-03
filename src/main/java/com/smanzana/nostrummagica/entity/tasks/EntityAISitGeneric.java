package com.smanzana.nostrummagica.entity.tasks;

import com.smanzana.nostrummagica.entity.IEntityTameable;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAISitGeneric<T extends EntityCreature & IEntityTameable> extends EntityAIBase {
	
	private final T theEntity;

	public EntityAISitGeneric(T entityIn) {
		this.theEntity = entityIn;
		this.setMutexBits(5);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {
		if (!this.theEntity.isTamed()) {
			return false;
		} else if (this.theEntity.isInWater()) {
			return false;
		} else if (!this.theEntity.onGround) {
			return false;
		} else {
			EntityLivingBase entitylivingbase = this.theEntity.getOwner();
			return entitylivingbase == null ? true : (this.theEntity.getDistanceSqToEntity(entitylivingbase) < 144.0D && entitylivingbase.getAITarget() != null ? false : theEntity.isSitting());
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.theEntity.getNavigator().clearPathEntity();
	}

}
