package com.smanzana.nostrummagica.entity.tasks;

import java.util.Random;

import com.smanzana.nostrummagica.entity.EntityDragon;
import com.smanzana.nostrummagica.entity.EntityDragonRed;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

public class DragonFlyEvasionTask extends EntityAIBase {
	
	private EntityDragon dragon;
	
	public DragonFlyEvasionTask(EntityDragon dragon, double speedIn) {
		this.dragon = dragon;
		this.setMutexBits(3);
	}
	
	@Override
	public boolean shouldExecute() {
		boolean flying = false;
		EntityLivingBase target;
		
		if (this.dragon instanceof EntityDragonRed) {
			flying = ((EntityDragonRed) this.dragon).isFlying();
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
	public boolean continueExecuting() {
		boolean flying = false;
		
		if (this.dragon instanceof EntityDragonRed) {
			flying = ((EntityDragonRed) this.dragon).isFlying();
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
        double x = this.dragon.posX + (range * Math.cos(angle));
        double z = this.dragon.posZ + (range * Math.sin(angle));
        
        // Find y
        double y;
        {
        	BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.dragon.getPosition());
        	
        	// First, find the ground
    		while(pos.getY() > 0) {
    			if (this.dragon.worldObj.isAirBlock(pos)) {
    				pos.setY(pos.getY() - 1);
    			} else {
    				break;
    			}
    		}
    		
    		// Next, find a place to path to a bit above the ground
    		// note: pos here instead is the top of the dragon, so we shift it up and look
    		// for a place that will fit the dragon
    		pos.setY(pos.getY() + (int) Math.ceil(dragon.height));
        	
    		int height = 15 + (random.nextInt(6) - 3);
        	for (int i = 0; i < height; i++) {
        		if (this.dragon.worldObj.isAirBlock(pos)) {
        			pos.setY(pos.getY() + 1);
        		} else {
        			pos.setY(pos.getY() - 1);
        			break;
        		}
        	}
        	
        	y = pos.getY() - (int) Math.ceil(dragon.height);
        }
        
        if (!dragon.getNavigator().tryMoveToXYZ(x, y, z, 1.0D)) {
        	dragon.getMoveHelper().setMoveTo(x, y, z, 1.0D);
		}
	}
	
	@Override
	public boolean isInterruptible() {
		return true;
	}
	
}
