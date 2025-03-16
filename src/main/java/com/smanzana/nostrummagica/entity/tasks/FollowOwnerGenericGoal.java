package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumSet;

import com.google.common.base.Predicate;
import com.smanzana.petcommand.api.ai.IFollowOwnerGoal;
import com.smanzana.petcommand.api.entity.ITameableEntity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class FollowOwnerGenericGoal<T extends PathfinderMob & ITameableEntity> extends Goal implements IFollowOwnerGoal {
	
	private final T thePet;
	private LivingEntity theOwner;
	private Level theWorld;
	private final double followSpeed;
	private final PathNavigation petPathfinder;
	private int timeToRecalcPath;
	private float maxDist;
	private float minDist;
	private float oldWaterCost;
	
	protected Predicate<? super T> filter;

	public FollowOwnerGenericGoal(T thePetIn, double followSpeedIn, float minDistIn, float maxDistIn) {
		this(thePetIn, followSpeedIn, minDistIn, maxDistIn, null);
	}
	
	public FollowOwnerGenericGoal(T thePetIn, double followSpeedIn, float minDistIn, float maxDistIn, Predicate<? super T> filter) {
		this.thePet = thePetIn;
		this.theWorld = thePetIn.level;
		this.followSpeed = followSpeedIn;
		this.petPathfinder = thePetIn.getNavigation();
		this.minDist = minDistIn;
		this.maxDist = maxDistIn;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		
		this.filter = filter;
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean canUse() {
		LivingEntity entitylivingbase = this.thePet.getLivingOwner();

		if (entitylivingbase == null) {
			return false;
		} else if (entitylivingbase instanceof Player && ((Player)entitylivingbase).isSpectator()) {
			return false;
		} else if (this.thePet.isEntitySitting()) {
			return false;
		} else if (this.thePet.distanceToSqr(entitylivingbase) < (double)(this.minDist * this.minDist)) {
			return false;
		} else if (this.filter != null && !this.filter.apply(this.thePet)) {
			return false;
		} else {
			this.theOwner = entitylivingbase;
			return true;
		}
	}

	/**
	 * Returns whether an in-progress Goal should continue executing
	 */
	public boolean canContinueToUse() {
		return !this.petPathfinder.isDone() && this.thePet.distanceToSqr(this.theOwner) > (double)(this.maxDist * this.maxDist) && !this.thePet.isEntitySitting();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		this.timeToRecalcPath = 0;
		this.oldWaterCost = this.thePet.getPathfindingMalus(BlockPathTypes.WATER);
		this.thePet.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
	}

	/**
	 * Resets the task
	 */
	public void stop() {
		this.theOwner = null;
		this.petPathfinder.stop();
		this.thePet.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
	}

	protected boolean isEmptyBlock(BlockPos pos) {
		return IsEmptyBlock(theWorld, pos);
	}
	
	protected static boolean IsEmptyBlock(Level world, BlockPos pos) {
		return world.isEmptyBlock(pos);
	}
	
	public static boolean TeleportAroundEntity(Entity teleportingEntity, Entity targetEntity) {
		if (teleportingEntity == null || targetEntity == null || teleportingEntity.level == null || teleportingEntity.level != targetEntity.level) {
			return false;
		}
		
		final Level theWorld = targetEntity.level;
		int i = Mth.floor(targetEntity.getX()) - 2;
		int j = Mth.floor(targetEntity.getZ()) - 2;
		int k = Mth.floor(targetEntity.getBoundingBox().minY);
		
		BlockPos.MutableBlockPos pos1 = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos pos2 = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos pos3 = new BlockPos.MutableBlockPos();

		for (int l = 0; l <= 4; ++l) {
			for (int i1 = 0; i1 <= 4; ++i1) {
				pos1.set(i + l, k - 1, j + i1);
				pos2.set(i + l, k, j + i1);
				pos3.set(i + l, k + 1, j + i1);
				if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && theWorld.getBlockState(new BlockPos(pos1)).isValidSpawn(theWorld, pos1, teleportingEntity.getType()) && IsEmptyBlock(theWorld, pos2) && IsEmptyBlock(theWorld, pos3)) {
					teleportingEntity.moveTo((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), teleportingEntity.yRot, teleportingEntity.xRot);
					if (teleportingEntity instanceof Mob) {
						((Mob) teleportingEntity).getNavigation().stop();
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
	public void tick() {
		this.thePet.getLookControl().setLookAt(this.theOwner, 10.0F, (float)this.thePet.getMaxHeadXRot());

		if (!this.thePet.isEntitySitting()) {
			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = 10;

				if (!this.petPathfinder.moveTo(this.theOwner, this.followSpeed)) {
					if (!this.thePet.isLeashed()) {
						if (this.thePet.distanceToSqr(this.theOwner) >= 144.0D) {
							TeleportAroundEntity(thePet, theOwner);
						}
					}
				}
			}
		}
	}
	
}
