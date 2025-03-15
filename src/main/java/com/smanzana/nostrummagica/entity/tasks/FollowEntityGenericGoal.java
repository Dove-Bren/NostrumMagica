package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumSet;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import net.minecraft.entity.ai.goal.Goal.Flag;

public class FollowEntityGenericGoal<T extends MobEntity> extends Goal {
	
	private final T entity;
	private LivingEntity theTarget;
	private World world;
	private final double followSpeed;
	private final PathNavigator petPathfinder;
	private int timeToRecalcPath;
	private float maxDist;
	private float minDist;
	private float oldWaterCost;
	private boolean canTeleport;
	

	public FollowEntityGenericGoal(T entityIn, double followSpeedIn, float minDistIn, float maxDistIn, boolean canTeleport) {
		this(entityIn, followSpeedIn, minDistIn, maxDistIn, canTeleport, null);
	}
	
	public FollowEntityGenericGoal(T entityIn, double followSpeedIn, float minDistIn, float maxDistIn, boolean canTeleport, LivingEntity target) {
		this.entity = entityIn;
		this.world = entity.level;
		this.followSpeed = followSpeedIn;
		this.petPathfinder = entity.getNavigation();
		this.minDist = minDistIn;
		this.maxDist = maxDistIn;
		this.canTeleport = canTeleport;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		
		this.theTarget = target;
	}
	
	protected LivingEntity getTarget(T entity) {
		return this.theTarget;
	}
	
	protected boolean canFollow(T entity) {
		return true;
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean canUse() {
		LivingEntity entitylivingbase = this.getTarget(this.entity);

		if (entitylivingbase == null) {
			return false;
		} else if (entitylivingbase instanceof PlayerEntity && ((PlayerEntity)entitylivingbase).isSpectator()) {
			return false;
		} else if (!this.canFollow(entity)) {
			return false;
		} else if (this.entity.distanceToSqr(entitylivingbase) < (double)(this.minDist * this.minDist)) {
			return false;
		} else {
			this.theTarget = entitylivingbase;
			return true;
		}
	}

	/**
	 * Returns whether an in-progress Goal should continue executing
	 */
	public boolean canContinueToUse() {
		return !this.petPathfinder.isDone() && this.entity.distanceToSqr(this.theTarget) > (double)(this.maxDist * this.maxDist) && this.canFollow(entity);
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		this.timeToRecalcPath = 0;
		this.oldWaterCost = this.entity.getPathfindingMalus(PathNodeType.WATER);
		this.entity.setPathfindingMalus(PathNodeType.WATER, 0.0F);
	}

	/**
	 * Resets the task
	 */
	public void stop() {
		this.theTarget = null;
		this.petPathfinder.stop();
		this.entity.setPathfindingMalus(PathNodeType.WATER, this.oldWaterCost);
	}
	
	protected boolean canTeleportToBlock(BlockPos pos) {
		BlockState blockstate = this.world.getBlockState(pos);
		return blockstate.isValidSpawn(this.world, pos, this.entity.getType()) && this.world.isEmptyBlock(pos.above()) && this.world.isEmptyBlock(pos.above(2));
	}

	/**
	 * Updates the task
	 */
	public void tick() {
		this.entity.getLookControl().setLookAt(this.theTarget, 10.0F, (float)this.entity.getMaxHeadXRot());

		if (this.canFollow(entity)) {
			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = 10;

				if (!this.petPathfinder.moveTo(this.theTarget, this.followSpeed) && this.canTeleport) {
					if (!this.entity.isLeashed()) {
						if (this.entity.distanceToSqr(this.theTarget) >= 144.0D) {
							int i = MathHelper.floor(this.theTarget.getX()) - 2;
							int j = MathHelper.floor(this.theTarget.getZ()) - 2;
							int k = MathHelper.floor(this.theTarget.getBoundingBox().minY);
							
							BlockPos.Mutable pos1 = new BlockPos.Mutable();

							for (int l = 0; l <= 4; ++l) {
								for (int i1 = 0; i1 <= 4; ++i1) {
									pos1.set(i + l, k - 1, j + i1);
									if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && canTeleportToBlock(pos1)) {
										this.entity.moveTo((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), this.entity.yRot, this.entity.xRot);
										this.petPathfinder.stop();
										return;
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
}
