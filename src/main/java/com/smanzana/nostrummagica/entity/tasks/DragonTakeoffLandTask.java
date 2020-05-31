package com.smanzana.nostrummagica.entity.tasks;

import java.util.Date;

import com.smanzana.nostrummagica.entity.dragon.EntityDragonFlying;

import net.minecraft.entity.ai.EntityAIBase;

public class DragonTakeoffLandTask extends EntityAIBase {
	
	private EntityDragonFlying dragon;
	private static Date cur;
	
	public DragonTakeoffLandTask(EntityDragonFlying dragon) {
		this.dragon = dragon;
		
		this.setMutexBits(3);
	}
	
	@Override
	public boolean shouldExecute() {
		if (dragon.isDead)
			return false;
		
		if (cur != null) {
			Date now = new Date();
			Date then = new Date(cur.getTime() + (1000 * 30));
			if (now.before(then))
				return false;
		} else if (dragon.isFlightTransitioning()) {
			// Missed it, but it's already changing
			cur = new Date();
			return false;
		}
		
		return true;
	}

	@Override
	public boolean continueExecuting() {
		return dragon.isFlightTransitioning();
	}
	
	@Override
	public void updateTask() {
		;
	}
	
	@Override
	public void startExecuting() {
		if (dragon.isFlying()) {
			dragon.startLanding();
		} else {
			dragon.startFlying();
		}
		
		cur = new Date();
	}
	
}
