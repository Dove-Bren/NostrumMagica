package com.smanzana.nostrummagica.entity.tasks;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateGround;

// Copying and making extendable. >:(
public class EntityAITemptGeneric extends EntityAITempt
{
	/** The entity using this AI that is tempted by the player. */
	public final EntityCreature temptedEntity;
	public final double speed;
	/** X position of player tempting this mob */
	protected double targetX;
	/** Y position of player tempting this mob */
	protected double targetY;
	/** Z position of player tempting this mob */
	protected double targetZ;
	/** Tempting player's pitch */
	protected double pitch;
	/** Tempting player's yaw */
	protected double yaw;
	/** The player that is tempting the entity that is using this AI. */
	protected EntityPlayer temptingPlayer;
	/**
	 * A counter that is decremented each time the shouldExecute method is called. The shouldExecute method will always
	 * return false if delayTemptCounter is greater than 0.
	 */
	private int delayTemptCounter;
	/** True if this EntityAITempt task is running */
	protected boolean isRunning;
	protected final Set<Item> temptItem;
	/** Whether the entity using this AI will be scared by the tempter's sudden movement. */
	public final boolean scaredByPlayerMovement;

	public EntityAITemptGeneric(EntityCreature temptedEntityIn, double speedIn, Item temptItemIn, boolean scaredByPlayerMovementIn) {
		this(temptedEntityIn, speedIn, scaredByPlayerMovementIn, Sets.newHashSet(temptItemIn));
	}

	public EntityAITemptGeneric(EntityCreature temptedEntityIn, double speedIn, boolean scaredByPlayerMovementIn, Set<Item> temptItemIn) {
		super(temptedEntityIn, speedIn, scaredByPlayerMovementIn, temptItemIn);
		this.temptedEntity = temptedEntityIn;
		this.speed = speedIn;
		this.temptItem = temptItemIn;
		this.scaredByPlayerMovement = scaredByPlayerMovementIn;
		this.setMutexBits(3);

		if (!(temptedEntityIn.getNavigator() instanceof PathNavigateGround)) {
			throw new IllegalArgumentException("Unsupported mob type for TemptGoal");
		}
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {
		if (this.delayTemptCounter > 0) {
			--this.delayTemptCounter;
			return false;
		} else {
			this.temptingPlayer = this.temptedEntity.world.getClosestPlayerToEntity(this.temptedEntity, 10.0D);

			if (this.temptingPlayer == null) {
				return false;
			} else {
				return this.isTempting(this.temptingPlayer.getHeldItemMainhand()) || this.isTempting(this.temptingPlayer.getHeldItemOffhand());
			}
		}
	}

	protected boolean isTempting(ItemStack stack) {
		return this.temptItem.contains(stack.getItem());
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {
		if (this.scaredByPlayerMovement) {
			if (this.temptedEntity.getDistanceSq(this.temptingPlayer) < 36.0D) {
				if (this.temptingPlayer.getDistanceSq(this.targetX, this.targetY, this.targetZ) > 0.010000000000000002D) {
					return false;
				}

				if (Math.abs((double)this.temptingPlayer.rotationPitch - this.pitch) > 5.0D || Math.abs((double)this.temptingPlayer.rotationYaw - this.yaw) > 5.0D) {
					return false;
				}
			} else {
				this.targetX = this.temptingPlayer.posX;
				this.targetY = this.temptingPlayer.posY;
				this.targetZ = this.temptingPlayer.posZ;
			}

			this.pitch = (double)this.temptingPlayer.rotationPitch;
			this.yaw = (double)this.temptingPlayer.rotationYaw;
		}

		return this.shouldExecute();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.targetX = this.temptingPlayer.posX;
		this.targetY = this.temptingPlayer.posY;
		this.targetZ = this.temptingPlayer.posZ;
		this.isRunning = true;
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {
		this.temptingPlayer = null;
		this.temptedEntity.getNavigator().clearPath();
		this.delayTemptCounter = 100;
		this.isRunning = false;
	}
	
	protected void moveToTemptingPlayer(EntityCreature tempted, EntityPlayer player) {
		if (this.temptedEntity.getDistanceSq(this.temptingPlayer) < 6.25D) {
			this.temptedEntity.getNavigator().clearPath();
		} else {
			this.temptedEntity.getNavigator().tryMoveToEntityLiving(this.temptingPlayer, this.speed);
		}
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask() {
		this.temptedEntity.getLookHelper().setLookPositionWithEntity(this.temptingPlayer, (float)(this.temptedEntity.getHorizontalFaceSpeed() + 20), (float)this.temptedEntity.getVerticalFaceSpeed());

		moveToTemptingPlayer(temptedEntity, temptingPlayer);
	}

	/**
	 * @see #isRunning
	 */
	public boolean isRunning() {
		return this.isRunning;
	}
}