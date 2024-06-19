package com.smanzana.nostrummagica.entity.tasks.dragon;

import com.smanzana.nostrummagica.entity.dragon.DragonEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

public class DragonFocusedTargetGoal<T extends LivingEntity> extends Goal {

	private T target;
	private DragonEntity dragon;
	
	public DragonFocusedTargetGoal(DragonEntity dragon, T target, boolean checkSight) {
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
	public boolean shouldContinueExecuting() {
		return false;
	}
}
