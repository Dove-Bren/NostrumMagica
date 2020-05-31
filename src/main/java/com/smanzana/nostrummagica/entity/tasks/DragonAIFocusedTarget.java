package com.smanzana.nostrummagica.entity.tasks;

import com.smanzana.nostrummagica.entity.dragon.EntityDragon;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class DragonAIFocusedTarget<T extends EntityLivingBase> extends EntityAIBase {

	private T target;
	private EntityDragon dragon;
	
	public DragonAIFocusedTarget(EntityDragon dragon, T target, boolean checkSight) {
		this.target = target;
		this.dragon = dragon;
	}

	@Override
	public boolean shouldExecute() {
		return this.dragon.getAttackTarget() == null;
	}
	
	@Override
	public void startExecuting() {
		this.dragon.setAttackTarget(target);
	}
	
	@Override
	public boolean continueExecuting() {
		return false;
	}
}
