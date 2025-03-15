package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumSet;

import com.smanzana.petcommand.api.entity.ITameableEntity;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;

public class OwnerHurtTargetGenericGoal<T extends CreatureEntity & ITameableEntity> extends TargetGoal {
	
	protected static final EntityPredicate CanAttack = new EntityPredicate().allowUnseeable().ignoreInvisibilityTesting();
	protected T entityTameable;
	protected LivingEntity theTarget;
	private int timestamp;

	public OwnerHurtTargetGenericGoal(T entityTameableIn) {
		super(entityTameableIn, false);
		this.entityTameable = entityTameableIn;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	@Override
	public boolean canUse() {
		if (!this.entityTameable.isEntitySitting()) {
			return false;
		} else {
			LivingEntity entitylivingbase = this.entityTameable.getLivingOwner();

			if (entitylivingbase == null) {
				return false;
			} else {
				this.theTarget = entitylivingbase.getLastHurtMob();
				int i = entitylivingbase.getLastHurtMobTimestamp();
				return i != this.timestamp && this.canAttack(this.theTarget, CanAttack);
			}
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void start() {
		this.mob.setTarget(this.theTarget);
		LivingEntity entitylivingbase = this.entityTameable.getLivingOwner();

		if (entitylivingbase != null) {
			this.timestamp = entitylivingbase.getLastHurtByMobTimestamp();
		}

		super.start();
	}
}
