package com.smanzana.nostrummagica.entity.tasks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIFollowEntityGeneric<T extends EntityLiving> extends EntityAIBase {
	
	private final T entity;
	private EntityLivingBase theTarget;
	private World theWorld;
	private final double followSpeed;
	private final PathNavigate petPathfinder;
	private int timeToRecalcPath;
	private float maxDist;
	private float minDist;
	private float oldWaterCost;
	private boolean canTeleport;
	

	public EntityAIFollowEntityGeneric(T entityIn, double followSpeedIn, float minDistIn, float maxDistIn, boolean canTeleport) {
		this(entityIn, followSpeedIn, minDistIn, maxDistIn, canTeleport, null);
	}
	
	public EntityAIFollowEntityGeneric(T entityIn, double followSpeedIn, float minDistIn, float maxDistIn, boolean canTeleport, EntityLivingBase target) {
		this.entity = entityIn;
		this.theWorld = entity.world;
		this.followSpeed = followSpeedIn;
		this.petPathfinder = entity.getNavigator();
		this.minDist = minDistIn;
		this.maxDist = maxDistIn;
		this.canTeleport = canTeleport;
		this.setMutexBits(3);
		
		this.theTarget = target;
	}
	
	protected EntityLivingBase getTarget(T entity) {
		return this.theTarget;
	}
	
	protected boolean canFollow(T entity) {
		return true;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {
		EntityLivingBase entitylivingbase = this.getTarget(this.entity);

		if (entitylivingbase == null) {
			return false;
		} else if (entitylivingbase instanceof EntityPlayer && ((EntityPlayer)entitylivingbase).isSpectator()) {
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
	 * Returns whether an in-progress EntityAIBase should continue executing
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

	private boolean isEmptyBlock(BlockPos pos) {
		IBlockState iblockstate = this.theWorld.getBlockState(pos);
		return iblockstate.getMaterial() == Material.AIR ? true : !iblockstate.isFullCube();
	}

	/**
	 * Updates the task
	 */
	public void updateTask() {
		this.entity.getLookHelper().setLookPositionWithEntity(this.theTarget, 10.0F, (float)this.entity.getVerticalFaceSpeed());

		if (this.canFollow(entity)) {
			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = 10;

				if (!this.petPathfinder.tryMoveToEntityLiving(this.theTarget, this.followSpeed) && this.canTeleport) {
					if (!this.entity.getLeashed()) {
						if (this.entity.getDistanceSq(this.theTarget) >= 144.0D) {
							int i = MathHelper.floor(this.theTarget.posX) - 2;
							int j = MathHelper.floor(this.theTarget.posZ) - 2;
							int k = MathHelper.floor(this.theTarget.getEntityBoundingBox().minY);
							
							MutableBlockPos pos1 = new MutableBlockPos();
							MutableBlockPos pos2 = new MutableBlockPos();
							MutableBlockPos pos3 = new MutableBlockPos();

							for (int l = 0; l <= 4; ++l) {
								for (int i1 = 0; i1 <= 4; ++i1) {
									pos1.setPos(i + l, k - 1, j + i1);
									pos2.setPos(i + l, k, j + i1);
									pos3.setPos(i + l, k + 1, j + i1);
									if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.theWorld.getBlockState(new BlockPos(pos1)).isSideSolid(theWorld, pos1, EnumFacing.UP) && this.isEmptyBlock(pos2) && this.isEmptyBlock(pos3)) {
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
