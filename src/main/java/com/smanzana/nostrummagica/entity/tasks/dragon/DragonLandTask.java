package com.smanzana.nostrummagica.entity.tasks.dragon;

import java.util.EnumSet;

import com.smanzana.nostrummagica.entity.dragon.EntityDragon;

import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

public class DragonLandTask extends Goal {

	private final EntityDragon dragon;
	
	public DragonLandTask(EntityDragon dragon) {
		this.dragon = dragon;
		this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	@Override
	public boolean isPreemptible() {
		return false;
	}

	@Override
	public boolean shouldExecute() {
		
		return dragon.isTryingToLand() && !dragon.onGround;
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		
		if (dragon.onGround || dragon.isInWater()) {
			return false;
		}
		
		MovementController MovementController = this.dragon.getMoveHelper();
		if (!MovementController.isUpdating()) {
			return false;
		} else {
			double dx = MovementController.getX() - this.dragon.getPosX();
			double dz = MovementController.getZ() - this.dragon.getPosZ();
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
			if (dragon.world.isAirBlock(pos)) {
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
