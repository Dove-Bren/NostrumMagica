package com.smanzana.nostrummagica.entity.tasks;

import com.smanzana.nostrummagica.entity.EntityDragonRedBase;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.util.math.AxisAlignedBB;

public class DragonAINearestAttackableTarget<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {

	public DragonAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight) {
		super(creature, classTarget, checkSight);
	}

	protected AxisAlignedBB getTargetableArea(double targetDistance) {
		return this.taskOwner.getEntityBoundingBox().expand(targetDistance,
				((EntityDragonRedBase) this.taskOwner).isFlying() ? 32 : 12.0D, targetDistance);
	}
	
}
