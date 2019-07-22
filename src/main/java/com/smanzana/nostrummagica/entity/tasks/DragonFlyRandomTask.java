package com.smanzana.nostrummagica.entity.tasks;

import java.util.Random;

import com.smanzana.nostrummagica.entity.EntityDragon;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.BlockPos;

public class DragonFlyRandomTask extends EntityAIBase {

	private final EntityDragon parentEntity;

    public DragonFlyRandomTask(EntityDragon dragon)
    {
        this.parentEntity = dragon;
        this.setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        EntityMoveHelper entitymovehelper = this.parentEntity.getMoveHelper();

        if (entitymovehelper.isUpdating())
        {
            return false;
        }
        else
        {
            double d0 = entitymovehelper.getX() - this.parentEntity.posX;
            double d1 = entitymovehelper.getY() - this.parentEntity.posY;
            double d2 = entitymovehelper.getZ() - this.parentEntity.posZ;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d3 >= 1.0D && d3 <= 3600.0D) {
            	return false;
            }
        }
        
        return this.parentEntity.getRNG().nextInt(25) == 0;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
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
    			if (this.parentEntity.worldObj.isAirBlock(pos)) {
    				pos.setY(pos.getY() - 1);
    			} else {
    				break;
    			}
    		}
        	
        	for (int i = 0; i < 20 + (random.nextInt(6) - 3); i++) {
        		if (this.parentEntity.worldObj.isAirBlock(pos)) {
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
