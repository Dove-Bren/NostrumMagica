package com.smanzana.nostrummagica.entity.tasks.dragon;

import java.util.EnumSet;
import java.util.Random;

import com.smanzana.nostrummagica.entity.dragon.DragonEntity;
import com.smanzana.nostrummagica.entity.dragon.FlyingDragonEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class DragonFlyEvasionGoal extends Goal {
	
	private DragonEntity dragon;
	
	private int cooldown;
	
	public DragonFlyEvasionGoal(DragonEntity dragon, double speedIn) {
		this.dragon = dragon;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		
		cooldown = 0;
	}
	
	@Override
	public boolean canUse() {
		
		// Check cooldown. If actually waiting, decrement and early out.
		if (cooldown > 0) {
			cooldown--;
			return false;
		}
		
		boolean flying = false;
		LivingEntity target;
		
		if (this.dragon instanceof FlyingDragonEntity) {
			flying = ((FlyingDragonEntity) this.dragon).isFlying();
		}
		
		if (!flying) {
			return false;
		}
		
		target = this.dragon.getTarget();
		
		if (target == null) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean canContinueToUse() {
		boolean flying = false;
		
		if (this.dragon instanceof FlyingDragonEntity) {
			flying = ((FlyingDragonEntity) this.dragon).isFlying();
		}
		
		if (!flying) {
			return false;
		}
		
		if (this.dragon.getNavigation().isDone()) {
			return false;
		}
		
		return true;
	}

	@Override
	public void start() {
		
		// Need to get a random spot around the player, and then try and path to that
		Random random = this.dragon.getRandom();
		double angle = random.nextDouble() * 2D * Math.PI;
		double range = 24.0 + (8.0 * random.nextDouble());
        double x = this.dragon.getX() + (range * Math.cos(angle));
        double z = this.dragon.getZ() + (range * Math.sin(angle));
        
        // Find y
        double y;
        {
        	BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos().set(this.dragon.blockPosition());
        	
        	// First, find the ground
    		while(pos.getY() > 0) {
    			if (this.dragon.level.isEmptyBlock(pos)) {
    				pos.setY(pos.getY() - 1);
    			} else {
    				break;
    			}
    		}
    		
    		// Next, find a place to path to a bit above the ground
    		// note: pos here instead is the top of the dragon, so we shift it up and look
    		// for a place that will fit the dragon
    		pos.setY(pos.getY() + (int) Math.ceil(dragon.getBbHeight()));
        	
    		int height = 15 + (random.nextInt(6) - 3);
        	for (int i = 0; i < height; i++) {
        		if (this.dragon.level.isEmptyBlock(pos)) {
        			pos.setY(pos.getY() + 1);
        		} else {
        			pos.setY(pos.getY() - 1);
        			break;
        		}
        	}
        	
        	y = pos.getY() - (int) Math.ceil(dragon.getBbHeight());
        }
        
        if (!dragon.getNavigation().moveTo(x, y, z, 1.0D)) {
        	dragon.getMoveControl().setWantedPosition(x, y, z, 1.0D);
		}
        
        this.cooldown = random.nextInt(20 * 4) + (20 * 3);
	}
	
	@Override
	public boolean isInterruptable() {
		return true;
	}
	
	public void reset() {
		this.cooldown = 0;
	}
	
}
