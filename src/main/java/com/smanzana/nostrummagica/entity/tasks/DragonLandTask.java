package com.smanzana.nostrummagica.entity.tasks;

import com.smanzana.nostrummagica.entity.dragon.EntityDragon;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.BlockPos;

public class DragonLandTask extends EntityAIBase {

	private final EntityDragon dragon;
	
	public DragonLandTask(EntityDragon dragon) {
		this.dragon = dragon;
		this.setMutexBits(3);
	}
	
	@Override
	public boolean isInterruptible() {
		return false;
	}

	@Override
	public boolean shouldExecute() {
		
		return dragon.isTryingToLand() && !dragon.onGround;
	}
	
	@Override
	public boolean continueExecuting() {
		
		if (dragon.onGround || dragon.isInWater()) {
			return false;
		}
		
		EntityMoveHelper entitymovehelper = this.dragon.getMoveHelper();
		if (!entitymovehelper.isUpdating()) {
			return false;
		} else {
			double dx = entitymovehelper.getX() - this.dragon.posX;
			double dz = entitymovehelper.getZ() - this.dragon.posZ;
			double dxz = dx * dx + dz * dz;
			
			// Keep running unless our XZ distance gets too big
			return dxz <= 3600.0D;
		}
	}
	
	@Override
	public void startExecuting() {
		
		// Don't trust heightmap; just loop. Not that bad.
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(dragon.getPosition());
		while(pos.getY() > 0) {
			if (dragon.worldObj.isAirBlock(pos)) {
				pos.setY(pos.getY() - 1);
			} else {
				break;
			}
		}
		
		double y = pos.getY();
		double x = (Math.cos(dragon.rotationYaw) * 10.0);
		double z = (Math.sin(dragon.rotationYaw) * 10.0);
		x += pos.getX();
		z += pos.getZ();
		
		if (!dragon.getNavigator().tryMoveToXYZ(x, y, z, 1.0D)) {
			dragon.getMoveHelper().setMoveTo(x, y, z, 1.0D);
		}
	}
	
}
