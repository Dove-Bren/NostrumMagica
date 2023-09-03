package com.smanzana.nostrummagica.entity.tasks;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.entity.IEntityTameable;

import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIFollowOwnerGeneric<T extends EntityCreature & IEntityTameable> extends EntityAIBase {
	
	private final T thePet;
	private LivingEntity theOwner;
	private World theWorld;
	private final double followSpeed;
	private final PathNavigate petPathfinder;
	private int timeToRecalcPath;
	private float maxDist;
	private float minDist;
	private float oldWaterCost;
	
	protected Predicate<? super T> filter;

	public EntityAIFollowOwnerGeneric(T thePetIn, double followSpeedIn, float minDistIn, float maxDistIn) {
		this(thePetIn, followSpeedIn, minDistIn, maxDistIn, null);
	}
	
	public EntityAIFollowOwnerGeneric(T thePetIn, double followSpeedIn, float minDistIn, float maxDistIn, Predicate<? super T> filter) {
		this.thePet = thePetIn;
		this.theWorld = thePetIn.world;
		this.followSpeed = followSpeedIn;
		this.petPathfinder = thePetIn.getNavigator();
		this.minDist = minDistIn;
		this.maxDist = maxDistIn;
		this.setMutexBits(3);
		
		this.filter = filter;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {
		LivingEntity entitylivingbase = this.thePet.getLivingOwner();

		if (entitylivingbase == null) {
			return false;
		} else if (entitylivingbase instanceof PlayerEntity && ((PlayerEntity)entitylivingbase).isSpectator()) {
			return false;
		} else if (this.thePet.isEntitySitting()) {
			return false;
		} else if (this.thePet.getDistanceSq(entitylivingbase) < (double)(this.minDist * this.minDist)) {
			return false;
		} else if (this.filter != null && !this.filter.apply(this.thePet)) {
			return false;
		} else {
			this.theOwner = entitylivingbase;
			return true;
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {
		return !this.petPathfinder.noPath() && this.thePet.getDistanceSq(this.theOwner) > (double)(this.maxDist * this.maxDist) && !this.thePet.isEntitySitting();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.timeToRecalcPath = 0;
		this.oldWaterCost = this.thePet.getPathPriority(PathNodeType.WATER);
		this.thePet.setPathPriority(PathNodeType.WATER, 0.0F);
	}

	/**
	 * Resets the task
	 */
	public void resetTask() {
		this.theOwner = null;
		this.petPathfinder.clearPath();
		this.thePet.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
	}

	protected boolean isEmptyBlock(BlockPos pos) {
		return IsEmptyBlock(theWorld, pos);
	}
	
	protected static boolean IsEmptyBlock(World world, BlockPos pos) {
		BlockState iblockstate = world.getBlockState(pos);
		return iblockstate.getMaterial() == Material.AIR ? true : !iblockstate.isFullCube();
	}
	
	public static boolean TeleportAroundEntity(Entity teleportingEntity, Entity targetEntity) {
		if (teleportingEntity == null || targetEntity == null || teleportingEntity.world == null || teleportingEntity.world != targetEntity.world) {
			return false;
		}
		
		final World theWorld = targetEntity.world;
		int i = MathHelper.floor(targetEntity.posX) - 2;
		int j = MathHelper.floor(targetEntity.posZ) - 2;
		int k = MathHelper.floor(targetEntity.getBoundingBox().minY);
		
		MutableBlockPos pos1 = new MutableBlockPos();
		MutableBlockPos pos2 = new MutableBlockPos();
		MutableBlockPos pos3 = new MutableBlockPos();

		for (int l = 0; l <= 4; ++l) {
			for (int i1 = 0; i1 <= 4; ++i1) {
				pos1.setPos(i + l, k - 1, j + i1);
				pos2.setPos(i + l, k, j + i1);
				pos3.setPos(i + l, k + 1, j + i1);
				if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && theWorld.getBlockState(new BlockPos(pos1)).isSideSolid(theWorld, pos1, Direction.UP) && IsEmptyBlock(theWorld, pos2) && IsEmptyBlock(theWorld, pos3)) {
					teleportingEntity.setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), teleportingEntity.rotationYaw, teleportingEntity.rotationPitch);
					if (teleportingEntity instanceof MobEntity) {
						((MobEntity) teleportingEntity).getNavigator().clearPath();
					}
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Updates the task
	 */
	public void updateTask() {
		this.thePet.getLookHelper().setLookPositionWithEntity(this.theOwner, 10.0F, (float)this.thePet.getVerticalFaceSpeed());

		if (!this.thePet.isEntitySitting()) {
			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = 10;

				if (!this.petPathfinder.tryMoveToEntityLiving(this.theOwner, this.followSpeed)) {
					if (!this.thePet.getLeashed()) {
						if (this.thePet.getDistanceSq(this.theOwner) >= 144.0D) {
							TeleportAroundEntity(thePet, theOwner);
						}
					}
				}
			}
		}
	}
	
}
