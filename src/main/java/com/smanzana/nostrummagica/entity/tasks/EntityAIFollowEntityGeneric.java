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
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIFollowEntityGeneric<T extends MobEntity> extends Goal {
	
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
	

	public EntityAIFollowEntityGeneric(T entityIn, double followSpeedIn, float minDistIn, float maxDistIn, boolean canTeleport) {
		this(entityIn, followSpeedIn, minDistIn, maxDistIn, canTeleport, null);
	}
	
	public EntityAIFollowEntityGeneric(T entityIn, double followSpeedIn, float minDistIn, float maxDistIn, boolean canTeleport, LivingEntity target) {
		this.entity = entityIn;
		this.world = entity.world;
		this.followSpeed = followSpeedIn;
		this.petPathfinder = entity.getNavigator();
		this.minDist = minDistIn;
		this.maxDist = maxDistIn;
		this.canTeleport = canTeleport;
		this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		
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
	public boolean shouldExecute() {
		LivingEntity entitylivingbase = this.getTarget(this.entity);

		if (entitylivingbase == null) {
			return false;
		} else if (entitylivingbase instanceof PlayerEntity && ((PlayerEntity)entitylivingbase).isSpectator()) {
			return false;
		} else if (!this.canFollow(entity)) {
			return false;
		} else if (this.entity.getDistanceSq(entitylivingbase) < (double)(this.minDist * this.minDist)) {
			return false;
		} else {
			this.theTarget = entitylivingbase;
			return true;
		}
	}

	/**
	 * Returns whether an in-progress Goal should continue executing
	 */
	public boolean shouldContinueExecuting() {
		return !this.petPathfinder.noPath() && this.entity.getDistanceSq(this.theTarget) > (double)(this.maxDist * this.maxDist) && this.canFollow(entity);
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.timeToRecalcPath = 0;
		this.oldWaterCost = this.entity.getPathPriority(PathNodeType.WATER);
		this.entity.setPathPriority(PathNodeType.WATER, 0.0F);
	}

	/**
	 * Resets the task
	 */
	public void resetTask() {
		this.theTarget = null;
		this.petPathfinder.clearPath();
		this.entity.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
	}
	
	protected boolean canTeleportToBlock(BlockPos pos) {
		BlockState blockstate = this.world.getBlockState(pos);
		return blockstate.canEntitySpawn(this.world, pos, this.entity.getType()) && this.world.isAirBlock(pos.up()) && this.world.isAirBlock(pos.up(2));
	}

	/**
	 * Updates the task
	 */
	public void tick() {
		this.entity.getLookController().setLookPositionWithEntity(this.theTarget, 10.0F, (float)this.entity.getVerticalFaceSpeed());

		if (this.canFollow(entity)) {
			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = 10;

				if (!this.petPathfinder.tryMoveToEntityLiving(this.theTarget, this.followSpeed) && this.canTeleport) {
					if (!this.entity.getLeashed()) {
						if (this.entity.getDistanceSq(this.theTarget) >= 144.0D) {
							int i = MathHelper.floor(this.theTarget.posX) - 2;
							int j = MathHelper.floor(this.theTarget.posZ) - 2;
							int k = MathHelper.floor(this.theTarget.getBoundingBox().minY);
							
							MutableBlockPos pos1 = new MutableBlockPos();

							for (int l = 0; l <= 4; ++l) {
								for (int i1 = 0; i1 <= 4; ++i1) {
									pos1.setPos(i + l, k - 1, j + i1);
									if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && canTeleportToBlock(pos1)) {
										this.entity.setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), this.entity.rotationYaw, this.entity.rotationPitch);
										this.petPathfinder.clearPath();
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
