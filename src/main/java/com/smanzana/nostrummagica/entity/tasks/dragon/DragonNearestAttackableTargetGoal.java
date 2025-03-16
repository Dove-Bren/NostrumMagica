package com.smanzana.nostrummagica.entity.tasks.dragon;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.entity.dragon.RedDragonBaseEntity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.phys.AABB;

public class DragonNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

	private Predicate<T> predicate;
	
	public DragonNearestAttackableTargetGoal(PathfinderMob creature, Class<T> classTarget, boolean checkSight) {
		this(creature, classTarget, checkSight, null);
	}
	
	public DragonNearestAttackableTargetGoal(PathfinderMob creature, Class<T> classTarget, boolean checkSight, Predicate<T> predicate) {
		super(creature, classTarget, checkSight);
		this.predicate = predicate;
	}

	protected AABB getTargetSearchArea(double targetDistance) {
		return this.mob.getBoundingBox().inflate(targetDistance,
				((RedDragonBaseEntity) this.mob).isFlying() ? 32 : 12.0D, targetDistance);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean canAttack(LivingEntity target, TargetingConditions predicate) {
		boolean success = super.canAttack(target, predicate);
		
		if (success && this.predicate != null) {
			success = this.predicate.apply((T) target);
		}
		
		return success;
	}
	
}
