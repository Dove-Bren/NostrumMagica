package com.smanzana.nostrummagica.entity.tasks.dragon;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.entity.dragon.RedDragonBaseEntity;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.util.math.AxisAlignedBB;

public class DragonNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

	private Predicate<T> predicate;
	
	public DragonNearestAttackableTargetGoal(CreatureEntity creature, Class<T> classTarget, boolean checkSight) {
		this(creature, classTarget, checkSight, null);
	}
	
	public DragonNearestAttackableTargetGoal(CreatureEntity creature, Class<T> classTarget, boolean checkSight, Predicate<T> predicate) {
		super(creature, classTarget, checkSight);
		this.predicate = predicate;
	}

	protected AxisAlignedBB getTargetSearchArea(double targetDistance) {
		return this.mob.getBoundingBox().inflate(targetDistance,
				((RedDragonBaseEntity) this.mob).isFlying() ? 32 : 12.0D, targetDistance);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean canAttack(LivingEntity target, EntityPredicate predicate) {
		boolean success = super.canAttack(target, predicate);
		
		if (success && this.predicate != null) {
			success = this.predicate.apply((T) target);
		}
		
		return success;
	}
	
}
