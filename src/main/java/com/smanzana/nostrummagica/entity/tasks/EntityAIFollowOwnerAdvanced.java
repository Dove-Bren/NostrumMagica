package com.smanzana.nostrummagica.entity.tasks;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.ITameableEntity;
import com.smanzana.nostrummagica.pet.PetPlacementMode;

import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityAIFollowOwnerAdvanced<T extends MobEntity> extends EntityAIBase {
	
	private final T thePet;
	private LivingEntity theOwner;
	private World theWorld;
	private final double followSpeed;
	private final PathNavigate petPathfinder;
	private int timeToRecalcPath;
	private float maxDist;
	private float minDist;
	private float oldWaterCost;
	
	private @Nullable Vec3d lastPosition;
	private int timeToRecalcPosition; // measured in existTicks of pet
	
	protected Predicate<? super T> filter;

	public EntityAIFollowOwnerAdvanced(T thePetIn, double followSpeedIn, float minDistIn, float maxDistIn) {
		this(thePetIn, followSpeedIn, minDistIn, maxDistIn, null);
	}
	
	public EntityAIFollowOwnerAdvanced(T thePetIn, double followSpeedIn, float minDistIn, float maxDistIn, Predicate<? super T> filter) {
		this.thePet = thePetIn;
		this.theWorld = thePetIn.world;
		this.followSpeed = followSpeedIn;
		this.petPathfinder = thePetIn.getNavigator();
		this.minDist = minDistIn;
		this.maxDist = maxDistIn;
		lastPosition = null;
		timeToRecalcPosition = 0;
		this.setMutexBits(3);
		
		this.filter = filter;
	}
	
	public static final boolean IsPetSittingGeneric(LivingEntity pet) {
		final boolean sitting;
		if (pet == null) {
			sitting = false;
		} else if (pet instanceof ITameableEntity) {
			sitting = ((ITameableEntity) pet).isEntitySitting();
		} else if (pet instanceof EntityTameable) {
			sitting = ((EntityTameable) pet).isSitting();
		} else {
			sitting = false;
		}
		
		return sitting;
	}
	
	protected boolean isPetSitting(T pet) {
		return IsPetSittingGeneric(pet);
	}
	
	/**
	 * Weird function. We want a deterministic 'index' of the pet in some hypothetical sorted
	 * list of all pets for the owner.
	 * As of writing, I'm planning on actually making that list every time we need it and sorting by
	 * UUID
	 * @param pet
	 * @param owner
	 * @return
	 */
	protected int getPetPositionIndex(T pet, LivingEntity owner) {
		List<LivingEntity> pets = NostrumMagica.getTamedEntities(owner);
		pets.removeIf((p) -> {
			return p == null
					|| IsPetSittingGeneric(p)
					|| thePet.isRiding()
					|| thePet.isRidingOrBeingRiddenBy(this.theOwner);
		});
		if (!pets.contains(pet)) {
			return 0;
		}
		Collections.sort(pets, (a, b) -> {
			return a.getUniqueID().compareTo(b.getUniqueID());
		});
		return pets.indexOf(pet);
	}
	
	/**
	 * Calculate the position this entity should want to be at.
	 * Note this method is expected to have no caching on it and find the theoretical ideal
	 * location.
	 * Other layers take care of caching and adjusting the position to one that the pet can
	 * actually stand at.
	 * @param pet
	 * @param owner
	 * @param mode
	 * @return
	 */
	protected Vec3d getIdealTargetPosition(T pet, LivingEntity owner, PetPlacementMode mode) {
		final int index = getPetPositionIndex(pet, owner);
		final Vec3d target;
		
		switch (mode) {
		case HEEL_DEFENSIVE:
		{
			// For defensive mode, be in front and then around the player.
			// 
			//   . .   . .  
			//  . .  +  . .  
			//   . .   . .   
			// 
			// Where each shell is filled in from inside to out, beginning from
			// the front of the player and moving to the back alternating left and
			// right sides as it goes.
			// Shell positioning will be fixed offsets, with outer shells being the same
			// offsets as inner + a x shift left or right depending on which side it's already on.
			final float spacing = 1.5f;
			final float bulge = 1f;
			float xs[] = {-spacing, spacing, -spacing - bulge, spacing + bulge, -spacing, spacing};
			float zs[] = {spacing, spacing, 0, 0, -spacing, -spacing};
			
			final int shell = (index / xs.length);
			final float offsetX = xs[index % xs.length];
			final float offsetZ = zs[index % zs.length];
			final float adjX = Math.signum(offsetX) * spacing * shell;
			
			// Get owner rotation to apply to
			final Vec3d ownerMoveVec = NostrumMagica.playerListener.getLastMove(owner);
			final float yawOwnerRad = (float) Math.atan2(ownerMoveVec.x, ownerMoveVec.z);
			
			// Get offset first as if owner was facing 
			Vec3d offset = new Vec3d(offsetX + adjX, 0, offsetZ);
			offset = offset.rotateYaw(yawOwnerRad + .0f * (float) Math.PI);
			target = owner.getPositionVector().add(offset);
			
			break;
		}
		case HEEL_FOLLOW:
		{
			// For follow mode, trail behind player in triangle
			// Skip row 1 of triangle
			//      +
			//     . .
			//    . . .
			//   . . . .
			// Gonna do with angles. 90 degree spread with first row being 0 and 90, second being 0 45 90 etc
			// with magnitude determined by row. will be more of a curved triangle than diagramed above.
			int rowCount = index;
			int row = 0;
			while (rowCount >= row+2) {
				rowCount -= row+2;
				row++;
			}
			
			// row is which 'shell' we're in.
			// rowCount is which position in that shell.
			final int rowMaxIndex = row+2;
			final float angle = 90f * ((float)rowCount / (float)(rowMaxIndex-1)); // 0,90 for row==0, 0,45,90 for row==1
			final double angleRad = 2 * Math.PI * (angle/360f);
			final float magnitude = 1.5f + (1.5f * row);
			
			// Get owner rotation to apply to
			final Vec3d ownerMoveVec = NostrumMagica.playerListener.getLastMove(owner);
			final float yawOwnerRad = (float) Math.atan2(ownerMoveVec.x, ownerMoveVec.z);
			
			// Get offset first as if owner was facing 
			Vec3d offset = new Vec3d(magnitude * Math.cos(angleRad), 0, magnitude * Math.sin(angleRad));
			offset = offset.rotateYaw(yawOwnerRad + .75f * (float) Math.PI);
			target = owner.getPositionVector().add(offset);
			
			break;
		}
		case FREE:
		default:
			// Free shouldn't ever get here, but return owner position just in case
			target = owner.getPositionVector();
			break;
		}
		
		return target;
	}
	
	protected Vec3d getTargetPosition(T pet, LivingEntity owner, PetPlacementMode mode) {
		if (timeToRecalcPosition == 0 || timeToRecalcPosition < pet.ticksExisted) {
			timeToRecalcPosition = pet.ticksExisted + 20;
			lastPosition = getIdealTargetPosition(pet, owner, mode);
			
			MutableBlockPos cursor = new MutableBlockPos();
			cursor.setPos(lastPosition.x, lastPosition.y, lastPosition.z);
			if (!isEmptyBlock(cursor) || isEmptyBlock(cursor.down())) {
				if (isEmptyBlock(cursor.down()) && !isEmptyBlock(cursor.down().down())) {
					lastPosition = lastPosition.add(0, -1, 0);
				} else if (isEmptyBlock(cursor.up()) && !isEmptyBlock(cursor)) {
					lastPosition = lastPosition.add(0, 1, 0);
				}
			}
		}
		
		return lastPosition; 
	}

	private boolean isEmptyBlock(BlockPos pos) {
		BlockState iblockstate = this.theWorld.getBlockState(pos);
		return iblockstate.getMaterial() == Material.AIR ? true : !iblockstate.isFullCube();
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {
		final LivingEntity entitylivingbase = NostrumMagica.getOwner(thePet);
		
		if (entitylivingbase == null) {
			return false;
		}
		
		if (thePet.getAttackTarget() != null) {
			return false;
		}
		
		final PetPlacementMode mode = NostrumMagica.getPetCommandManager().getPlacementMode(entitylivingbase);
		
		if (mode == PetPlacementMode.FREE) {
			return false;
		}
		
		final boolean sitting = this.isPetSitting(thePet);
		final Vec3d targetPos = getTargetPosition(thePet, entitylivingbase, mode);

		if (entitylivingbase instanceof PlayerEntity && ((PlayerEntity)entitylivingbase).isSpectator()) {
			return false;
		} else if (sitting) {
			return false;
		} else if (this.thePet.getDistanceSq(targetPos.x, targetPos.y, targetPos.z) < (double)(this.minDist * this.minDist)) {
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
		if (this.thePet.getAttackTarget() != null) {
			return false;
		}
		
		final PetPlacementMode mode = NostrumMagica.getPetCommandManager().getPlacementMode(this.theOwner);
		if (mode == PetPlacementMode.FREE) {
			return false;
		}
		final boolean sitting = this.isPetSitting(thePet);
		
		if (sitting || thePet.isRiding() || thePet.isRidingOrBeingRiddenBy(this.theOwner)) {
			return false;
		}
		
		final Vec3d targetPos = getTargetPosition(thePet, theOwner, mode);
		return !this.petPathfinder.noPath() && this.thePet.getDistanceSq(targetPos.x, targetPos.y, targetPos.z) > (double)(this.maxDist * this.maxDist);
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.timeToRecalcPath = 0;
		this.timeToRecalcPosition = 0;
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

	/**
	 * Updates the task
	 */
	public void updateTask() {
		//this.thePet.getLookHelper().setLookPositionWithEntity(this.theOwner, 10.0F, (float)this.thePet.getVerticalFaceSpeed());

		if (!isPetSitting(thePet)) {
//			final Vec3d ownerMoveVec = NostrumMagica.playerListener.getLastMove(theOwner);
//			final float yawOwner = (float) -Math.atan2(ownerMoveVec.x, ownerMoveVec.z) * 180f / (float)Math.PI;
//			this.thePet.getLookHelper().setLookPosition(x, y, z, deltaYaw, deltaPitch);
			
			//System.out.println("Moving");
			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = 10;
				final PetPlacementMode mode = NostrumMagica.getPetCommandManager().getPlacementMode(this.theOwner);
				final Vec3d targetPos = this.getTargetPosition(thePet, theOwner, mode);

				//thePet.setLocationAndAngles(targetPos.x, targetPos.y, targetPos.z, this.thePet.rotationYaw, this.thePet.rotationPitch);
				if (!this.petPathfinder.tryMoveToXYZ(targetPos.x, targetPos.y, targetPos.z, this.followSpeed)) {
					if (!this.thePet.getLeashed()) {
						if (this.thePet.getDistanceSq(this.theOwner) >= 144.0D) {
							thePet.setLocationAndAngles(targetPos.x, targetPos.y, targetPos.z, this.thePet.rotationYaw, this.thePet.rotationPitch);
						}
					}
				}
			}
		}
	}
	
}
