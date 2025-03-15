package com.smanzana.nostrummagica.entity.tasks;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

// Slightly extending to make it a little easier to just customize some things
public class GenericTemptGoal extends TemptGoal {
	
	protected double speed; // :( private in parent
	
	public GenericTemptGoal(CreatureEntity creatureIn, double speedIn, Ingredient temptItemIn, boolean scaredByPlayerMovementIn) {
		this(creatureIn, speedIn, scaredByPlayerMovementIn, temptItemIn);
	}

	public GenericTemptGoal(CreatureEntity creatureIn, double speedIn, boolean scaredByPlayerMovementIn, Ingredient temptItemIn) {
		super(creatureIn, speedIn, scaredByPlayerMovementIn, temptItemIn);
		this.speed = speedIn;
	}

	protected boolean shouldFollowItem(ItemStack stack) {
		return super.shouldFollowItem(stack);
	}

	protected void moveToclosestPlayer(CreatureEntity tempted, PlayerEntity player) {
		if (this.mob.distanceToSqr(this.player) < 6.25D) {
			this.mob.getNavigation().stop();
		} else {
			this.mob.getNavigation().moveTo(this.player, this.speed);
		}
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	@Override
	public void tick() {
		this.mob.getLookControl().setLookAt(this.player, (float)(this.mob.getMaxHeadYRot() + 20), (float)this.mob.getMaxHeadXRot());
		moveToclosestPlayer(mob, player);
	}
}