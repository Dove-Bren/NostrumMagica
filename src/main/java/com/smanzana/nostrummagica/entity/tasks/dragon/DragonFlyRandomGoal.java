package com.smanzana.nostrummagica.entity.tasks.dragon;

import java.util.EnumSet;
import java.util.Random;

import com.smanzana.nostrummagica.entity.dragon.DragonEntity;

import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.core.BlockPos;

public class DragonFlyRandomGoal extends Goal {

	private final DragonEntity parentEntity;

    public DragonFlyRandomGoal(DragonEntity dragon)
    {
        this.parentEntity = dragon;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    /**
     * Returns whether the Goal should begin execution.
     */
    public boolean canUse()
    {
        MoveControl MovementController = this.parentEntity.getMoveControl();

        if (MovementController.hasWanted())
        {
            return false;
        }
        else
        {
            double d0 = MovementController.getWantedX() - this.parentEntity.getX();
            double d1 = MovementController.getWantedY() - this.parentEntity.getY();
            double d2 = MovementController.getWantedZ() - this.parentEntity.getZ();
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d3 >= 1.0D && d3 <= 3600.0D) {
            	return false;
            }
        }
        
        return this.parentEntity.getRandom().nextInt(25) == 0;
    }

    /**
     * Returns whether an in-progress Goal should continue executing
     */
    public boolean canContinueToUse()
    {
        return false;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start()
    {
        Random random = this.parentEntity.getRandom();
        double x = this.parentEntity.getX() + (double)((random.nextFloat() * 2.0F - 1.0F) * 32.0F);
        double z = this.parentEntity.getZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * 32.0F);
        
        // Find y
        double y;
        {
        	BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos().set(this.parentEntity.blockPosition());
    		while(pos.getY() > 0) {
    			if (this.parentEntity.level.isEmptyBlock(pos)) {
    				pos.setY(pos.getY() - 1);
    			} else {
    				break;
    			}
    		}
        	
        	for (int i = 0; i < 20 + (random.nextInt(6) - 3); i++) {
        		if (this.parentEntity.level.isEmptyBlock(pos)) {
        			pos.setY(pos.getY() + 1);
        		}
        	}
        	
        	y = pos.getY();
        }
        
        if (!parentEntity.getNavigation().moveTo(x, y, z, 1.0D)) {
        	parentEntity.getMoveControl().setWantedPosition(x, y, z, 1.0D);
		}
        //this.parentEntity.getMoveHelper().setMoveTo(x, y, z, 1.0D);
    }
	
}
