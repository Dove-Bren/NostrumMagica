package com.smanzana.nostrummagica.entity.tasks.dragon;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRedBase;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.util.math.AxisAlignedBB;

public class DragonAINearestAttackableTarget<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {

	private Predicate<T> predicate;
	
	public DragonAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight) {
		this(creature, classTarget, checkSight, null);
	}
	
	public DragonAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight, Predicate<T> predicate) {
		super(creature, classTarget, checkSight);
		this.predicate = predicate;
	}

	protected AxisAlignedBB getTargetableArea(double targetDistance) {
		return this.taskOwner.getEntityBoundingBox().expand(targetDistance,
				((EntityDragonRedBase) this.taskOwner).isFlying() ? 32 : 12.0D, targetDistance);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean isSuitableTarget(EntityLivingBase target, boolean includeInvincibles) {
		boolean success = super.isSuitableTarget(target, includeInvincibles);
		
		if (success && predicate != null) {
			success = this.predicate.apply((T) target);
		}
		
		return success;
	}
	
}
