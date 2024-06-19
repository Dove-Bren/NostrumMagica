package com.smanzana.nostrummagica.entity.tasks.dragon;

import java.util.Date;
import java.util.EnumSet;

import com.smanzana.nostrummagica.entity.dragon.FlyingDragonEntity;

import net.minecraft.entity.ai.goal.Goal;

public class DragonTakeoffLandGoal extends Goal {
	
	private FlyingDragonEntity dragon;
	private static Date cur;
	
	public DragonTakeoffLandGoal(FlyingDragonEntity dragon) {
		this.dragon = dragon;
		
		this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	@Override
	public boolean shouldExecute() {
		if (!dragon.isAlive())
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
	public boolean shouldContinueExecuting() {
		return dragon.isFlightTransitioning();
	}
	
	@Override
	public void tick() {
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
