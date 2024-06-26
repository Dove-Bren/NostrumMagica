package com.smanzana.nostrummagica.entity.tasks.dragon;

import java.util.EnumSet;
import java.util.Random;

import com.smanzana.nostrummagica.entity.dragon.DragonEntity;
import com.smanzana.nostrummagica.entity.dragon.FlyingDragonEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

public class DragonFlyEvasionGoal extends Goal {
	
	private DragonEntity dragon;
	
	private int cooldown;
	
	public DragonFlyEvasionGoal(DragonEntity dragon, double speedIn) {
		this.dragon = dragon;
		this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		
		cooldown = 0;
	}
	
	@Override
	public boolean shouldExecute() {
		
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
		
		target = this.dragon.getAttackTarget();
		
		if (target == null) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		boolean flying = false;
		
		if (this.dragon instanceof FlyingDragonEntity) {
			flying = ((FlyingDragonEntity) this.dragon).isFlying();
		}
		
		if (!flying) {
			return false;
		}
		
		if (this.dragon.getNavigator().noPath()) {
			return false;
		}
		
		return true;
	}

	@Override
	public void startExecuting() {
		
		// Need to get a random spot around the player, and then try and path to that
		Random random = this.dragon.getRNG();
		double angle = random.nextDouble() * 2D * Math.PI;
		double range = 24.0 + (8.0 * random.nextDouble());
        double x = this.dragon.getPosX() + (range * Math.cos(angle));
        double z = this.dragon.getPosZ() + (range * Math.sin(angle));
        
        // Find y
        double y;
        {
        	BlockPos.Mutable pos = new BlockPos.Mutable().setPos(this.dragon.getPosition());
        	
        	// First, find the ground
    		while(pos.getY() > 0) {
    			if (this.dragon.world.isAirBlock(pos)) {
    				pos.setY(pos.getY() - 1);
    			} else {
    				break;
    			}
    		}
    		
    		// Next, find a place to path to a bit above the ground
    		// note: pos here instead is the top of the dragon, so we shift it up and look
    		// for a place that will fit the dragon
    		pos.setY(pos.getY() + (int) Math.ceil(dragon.getHeight()));
        	
    		int height = 15 + (random.nextInt(6) - 3);
        	for (int i = 0; i < height; i++) {
        		if (this.dragon.world.isAirBlock(pos)) {
        			pos.setY(pos.getY() + 1);
        		} else {
        			pos.setY(pos.getY() - 1);
        			break;
        		}
        	}
        	
        	y = pos.getY() - (int) Math.ceil(dragon.getHeight());
        }
        
        if (!dragon.getNavigator().tryMoveToXYZ(x, y, z, 1.0D)) {
        	dragon.getMoveHelper().setMoveTo(x, y, z, 1.0D);
		}
        
        this.cooldown = random.nextInt(20 * 4) + (20 * 3);
	}
	
	@Override
	public boolean isPreemptible() {
		return true;
	}
	
	public void reset() {
		this.cooldown = 0;
	}
	
}
