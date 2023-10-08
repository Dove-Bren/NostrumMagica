package com.smanzana.nostrummagica.entity.tasks.dragon;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRedBase;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NearestAttackableTargetGoal;
import net.minecraft.util.math.AxisAlignedBB;

public class DragonAINearestAttackableTarget<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

	private Predicate<T> predicate;
	
	public DragonAINearestAttackableTarget(CreatureEntity creature, Class<T> classTarget, boolean checkSight) {
		this(creature, classTarget, checkSight, null);
	}
	
	public DragonAINearestAttackableTarget(CreatureEntity creature, Class<T> classTarget, boolean checkSight, Predicate<T> predicate) {
		super(creature, classTarget, checkSight);
		this.predicate = predicate;
	}

	protected AxisAlignedBB getTargetableArea(double targetDistance) {
		return this.taskOwner.getBoundingBox().expand(targetDistance,
				((EntityDragonRedBase) this.taskOwner).isFlying() ? 32 : 12.0D, targetDistance);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean isSuitableTarget(LivingEntity target, boolean includeInvincibles) {
		boolean success = super.isSuitableTarget(target, includeInvincibles);
		
		if (success && predicate != null) {
			success = this.predicate.apply((T) target);
		}
		
		return success;
	}
	
}
