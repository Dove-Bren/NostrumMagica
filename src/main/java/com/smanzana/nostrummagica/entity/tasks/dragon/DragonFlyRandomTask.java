package com.smanzana.nostrummagica.entity.tasks.dragon;

import java.util.EnumSet;
import java.util.Random;

import com.smanzana.nostrummagica.entity.dragon.EntityDragon;

import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

public class DragonFlyRandomTask extends Goal {

	private final EntityDragon parentEntity;

    public DragonFlyRandomTask(EntityDragon dragon)
    {
        this.parentEntity = dragon;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    /**
     * Returns whether the Goal should begin execution.
     */
    public boolean shouldExecute()
    {
        MovementController MovementController = this.parentEntity.getMoveHelper();

        if (MovementController.isUpdating())
        {
            return false;
        }
        else
        {
            double d0 = MovementController.getX() - this.parentEntity.posX;
            double d1 = MovementController.getY() - this.parentEntity.posY;
            double d2 = MovementController.getZ() - this.parentEntity.posZ;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d3 >= 1.0D && d3 <= 3600.0D) {
            	return false;
            }
        }
        
        return this.parentEntity.getRNG().nextInt(25) == 0;
    }

    /**
     * Returns whether an in-progress Goal should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        return false;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        Random random = this.parentEntity.getRNG();
        double x = this.parentEntity.posX + (double)((random.nextFloat() * 2.0F - 1.0F) * 32.0F);
        double z = this.parentEntity.posZ + (double)((random.nextFloat() * 2.0F - 1.0F) * 32.0F);
        
        // Find y
        double y;
        {
        	BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.parentEntity.getPosition());
    		while(pos.getY() > 0) {
    			if (this.parentEntity.world.isAirBlock(pos)) {
    				pos.setY(pos.getY() - 1);
    			} else {
    				break;
    			}
    		}
        	
        	for (int i = 0; i < 20 + (random.nextInt(6) - 3); i++) {
        		if (this.parentEntity.world.isAirBlock(pos)) {
        			pos.setY(pos.getY() + 1);
        		}
        	}
        	
        	y = pos.getY();
        }
        
        if (!parentEntity.getNavigator().tryMoveToXYZ(x, y, z, 1.0D)) {
        	parentEntity.getMoveHelper().setMoveTo(x, y, z, 1.0D);
		}
        //this.parentEntity.getMoveHelper().setMoveTo(x, y, z, 1.0D);
    }
	
}
