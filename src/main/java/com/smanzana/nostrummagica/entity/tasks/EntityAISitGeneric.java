package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumSet;

import com.smanzana.nostrummagica.entity.ITameableEntity;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

public class EntityAISitGeneric<T extends CreatureEntity & ITameableEntity> extends Goal {
	
	private final T entity;

	public EntityAISitGeneric(T entityIn) {
		this.entity = entityIn;
		this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean shouldExecute() {
		if (!this.entity.isEntityTamed()) {
			return false;
		} else if (this.entity.isInWater()) {
			return false;
		} else if (!this.entity.onGround) {
			return false;
		} else {
			LivingEntity entitylivingbase = this.entity.getLivingOwner();
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
