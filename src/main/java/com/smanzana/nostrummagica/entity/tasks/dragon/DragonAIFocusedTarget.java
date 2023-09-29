package com.smanzana.nostrummagica.entity.tasks.dragon;

import com.smanzana.nostrummagica.entity.dragon.EntityDragon;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

public class DragonAIFocusedTarget<T extends LivingEntity> extends Goal {

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
	public boolean shouldContinueExecuting() {
		return false;
	}
}
