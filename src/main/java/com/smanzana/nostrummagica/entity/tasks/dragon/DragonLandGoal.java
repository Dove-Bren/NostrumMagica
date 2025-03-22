package com.smanzana.nostrummagica.entity.tasks.dragon;

import java.util.EnumSet;

import com.smanzana.nostrummagica.entity.dragon.DragonEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;

public class DragonLandGoal extends Goal {

	private final DragonEntity dragon;
	
	public DragonLandGoal(DragonEntity dragon) {
		this.dragon = dragon;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	@Override
	public boolean isInterruptable() {
		return false;
	}

	@Override
	public boolean canUse() {
		
		return dragon.isTryingToLand() && !dragon.isOnGround();
	}
	
	@Override
	public boolean canContinueToUse() {
		
		if (dragon.isOnGround() || dragon.isInWater()) {
			return false;
		}
		
		MoveControl MovementController = this.dragon.getMoveControl();
		if (!MovementController.hasWanted()) {
			return false;
		} else {
			double dx = MovementController.getWantedX() - this.dragon.getX();
			double dz = MovementController.getWantedZ() - this.dragon.getZ();
			double dxz = dx * dx + dz * dz;
			
			// Keep running unless our XZ distance gets too big
			return dxz <= 3600.0D;
		}
	}
	
	@Override
	public void start() {
		
		// Don't trust heightmap; just loop. Not that bad.
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos().set(dragon.blockPosition());
		while(pos.getY() > 0) {
			if (dragon.level.isEmptyBlock(pos)) {
				pos.setY(pos.getY() - 1);
			} else {
				break;
			}
		}
		
		double y = pos.getY();
		double x = (Math.cos(dragon.getYRot()) * 10.0);
		double z = (Math.sin(dragon.getYRot()) * 10.0);
		x += pos.getX();
		z += pos.getZ();
		
		if (!dragon.getNavigation().moveTo(x, y, z, 1.0D)) {
			dragon.getMoveControl().setWantedPosition(x, y, z, 1.0D);
		}
	}
	
}
