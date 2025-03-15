package com.smanzana.nostrummagica.entity.tasks.dragon;

import java.util.Date;
import java.util.EnumSet;

import com.smanzana.nostrummagica.entity.dragon.FlyingDragonEntity;

import net.minecraft.entity.ai.goal.Goal;

import net.minecraft.entity.ai.goal.Goal.Flag;

public class DragonTakeoffLandGoal extends Goal {
	
	private FlyingDragonEntity dragon;
	private static Date cur;
	
	public DragonTakeoffLandGoal(FlyingDragonEntity dragon) {
		this.dragon = dragon;
		
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	@Override
	public boolean canUse() {
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
	public boolean canContinueToUse() {
		return dragon.isFlightTransitioning();
	}
	
	@Override
	public void tick() {
		;
	}
	
	@Override
	public void start() {
		if (dragon.isFlying()) {
			dragon.startLanding();
		} else {
			dragon.startFlying();
		}
		
		cur = new Date();
	}
	
}
