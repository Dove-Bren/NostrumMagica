package com.smanzana.nostrummagica.entity.tasks;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;

// Slightly extending to make it a little easier to just customize some things
public class GenericTemptGoal extends TemptGoal {
	
	protected double speed; // :( private in parent
	
	public GenericTemptGoal(PathfinderMob creatureIn, double speedIn, Ingredient temptItemIn, boolean scaredByPlayerMovementIn) {
		this(creatureIn, speedIn, scaredByPlayerMovementIn, temptItemIn);
	}

	public GenericTemptGoal(PathfinderMob creatureIn, double speedIn, boolean scaredByPlayerMovementIn, Ingredient temptItemIn) {
		super(creatureIn, speedIn, temptItemIn, scaredByPlayerMovementIn);
		this.speed = speedIn;
	}

	protected void moveToclosestPlayer(PathfinderMob tempted, Player player) {
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