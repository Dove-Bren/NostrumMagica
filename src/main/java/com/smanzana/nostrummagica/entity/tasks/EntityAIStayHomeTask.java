package com.smanzana.nostrummagica.entity.tasks;

import java.util.Random;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EntityAIStayHomeTask<T extends EntityCreature> extends EntityAIBase {

	private Predicate<T> filter;
	private T creature;
	private double maxDistSq;
	private double speed;
	private Random rand;
	
	public EntityAIStayHomeTask(T creature, double speedIn, double maxDistSq) {
		this(creature, speedIn, maxDistSq, null);
	}
	
	public EntityAIStayHomeTask(T creature, double speedIn, double maxDistSq, Predicate<T> filter) {
		this.creature = creature;
		this.filter = filter;
		this.maxDistSq = maxDistSq;
		this.speed = speedIn;
		this.rand = new Random();
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		if (filter != null) {
			if (!filter.apply(this.creature)) {
				return false;
			}
		}
		
		BlockPos home = creature.getHomePosition();
		if (home == null) {
			return false;
		}
		
		return creature.getPositionVector().squareDistanceTo(home.getX(), home.getY(), home.getZ()) > this.maxDistSq;
	}
	
	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting()
	{
		BlockPos home = creature.getHomePosition();
		if (home != null) {
			// Try and find a place to move
			Vec3d targ = null;
			int attempts = 20;
			do {
				double dist = this.rand.nextDouble() * Math.sqrt(this.maxDistSq);
				float angle = (float) (this.rand.nextDouble() * (2 * Math.PI));
				float tilt = (float) (this.rand.nextDouble() * (2 * Math.PI)) * .5f;
				
				targ = new Vec3d(
						home.getX() + (Math.cos(angle) * dist),
						home.getY() + (Math.cos(tilt) * dist),
						home.getZ() + (Math.sin(angle) * dist));
				if (!creature.worldObj.isAirBlock(new BlockPos(targ.xCoord, targ.yCoord, targ.zCoord))) {
					targ = null;
				}
			} while (targ == null && attempts > 0);
			
			if (targ == null) {
				targ = new Vec3d(home.getX() + .5, home.getY() + 1, home.getZ() + .5);
			}
			
			//this.creature.getNavigator().tryMoveToXYZ(targ.xCoord, targ.yCoord, targ.zCoord, this.speed);
			this.creature.getMoveHelper().setMoveTo(targ.xCoord, targ.yCoord, targ.zCoord, this.speed);
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean continueExecuting() {
		EntityMoveHelper mover = creature.getMoveHelper();
		return mover.isUpdating() && ((mover.getX() - creature.posX) + (mover.getY() - creature.posY) + (mover.getZ() - creature.posZ) > 2);
	}
}
