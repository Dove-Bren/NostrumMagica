package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumSet;

import com.smanzana.petcommand.api.entity.ITameableEntity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class SitGenericGoal<T extends PathfinderMob & ITameableEntity> extends Goal {
	
	private final T entity;

	public SitGenericGoal(T entityIn) {
		this.entity = entityIn;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean canUse() {
		if (!this.entity.isEntityTamed()) {
			return false;
		} else if (this.entity.isInWater()) {
			return false;
		} else if (!this.entity.isOnGround()) {
			return false;
		} else {
			LivingEntity entitylivingbase = this.entity.getLivingOwner();
			return entitylivingbase == null ? this.entity.isEntitySitting() : (this.entity.distanceToSqr(entitylivingbase) < 144.0D && entitylivingbase.getLastHurtByMob() != null ? false : entity.isEntitySitting());
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		this.entity.getNavigation().stop();
	}

}
