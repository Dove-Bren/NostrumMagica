package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumSet;
import java.util.Random;

import com.google.common.base.Predicate;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class StayHomeGoal<T extends CreatureEntity> extends Goal {

	private Predicate<T> filter;
	private T creature;
	private double maxDistSq;
	private double speed;
	private Random rand;
	
	public StayHomeGoal(T creature, double speedIn, double maxDistSq) {
		this(creature, speedIn, maxDistSq, null);
	}
	
	public StayHomeGoal(T creature, double speedIn, double maxDistSq, Predicate<T> filter) {
		this.creature = creature;
		this.filter = filter;
		this.maxDistSq = maxDistSq;
		this.speed = speedIn;
		this.rand = new Random();
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (filter != null) {
			if (!filter.apply(this.creature)) {
				return false;
			}
		}
		
		BlockPos home = creature.getRestrictCenter();
		if (home == null) {
			return false;
		}
		
		return creature.position().distanceToSqr(home.getX(), home.getY(), home.getZ()) > this.maxDistSq;
	}
	
	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start()
	{
		BlockPos home = creature.getRestrictCenter();
		if (home != null) {
			// Try and find a place to move
			Vector3d targ = null;
			int attempts = 20;
			do {
				double dist = this.rand.nextDouble() * Math.sqrt(this.maxDistSq);
				float angle = (float) (this.rand.nextDouble() * (2 * Math.PI));
				float tilt = (float) (this.rand.nextDouble() * (2 * Math.PI)) * .5f;
				
				targ = new Vector3d(
						home.getX() + (Math.cos(angle) * dist),
						home.getY() + (Math.cos(tilt) * dist),
						home.getZ() + (Math.sin(angle) * dist));
				if (!creature.level.isEmptyBlock(new BlockPos(targ.x, targ.y, targ.z))) {
					targ = null;
				}
			} while (targ == null && attempts > 0);
			
			if (targ == null) {
				targ = new Vector3d(home.getX() + .5, home.getY() + 1, home.getZ() + .5);
			}
			
			//this.creature.getNavigator().tryMoveToXYZ(targ.xCoord, targ.yCoord, targ.zCoord, this.speed);
			this.creature.getMoveControl().setWantedPosition(targ.x, targ.y, targ.z, this.speed);
		}
	}

	/**
	 * Returns whether an in-progress Goal should continue executing
	 */
	public boolean canContinueToUse() {
		MovementController mover = creature.getMoveControl();
		return mover.hasWanted() && ((mover.getWantedX() - creature.getX()) + (mover.getWantedY() - creature.getY()) + (mover.getWantedZ() - creature.getZ()) > 2);
	}
}
