package com.smanzana.nostrummagica.entity.tasks.arcanewolf;

import java.util.EnumSet;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.ArcaneWolfElementalType;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.TameLightning;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;

public class ArcaneWolfStormGoal extends Goal {

	protected final ArcaneWolfEntity wolf;
	
	// Cooldown, done as compared to wolf.ticksExisted
	protected int cooldownTicks = 0;
	protected final int manaCost;

	protected boolean active;
	protected @Nullable LivingEntity activeTarget;
	
	public ArcaneWolfStormGoal(ArcaneWolfEntity wolf, int manaCost) {
		this.wolf = wolf;
		this.manaCost = manaCost;
		
		active = false;
		
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	@Override
	public boolean canUse() {
		return wolf.isAlive()
				&& !wolf.isOrderedToSit()
				&& wolf.getTarget() != null
				&& wolf.tickCount >= cooldownTicks
				&& wolf.getMana() >= manaCost
				&& wolf.getElementalType() == ArcaneWolfElementalType.STORM
				&& !(wolf.getTarget() instanceof Creeper);
	}
	
	@Override
	public boolean canContinueToUse() {
		return this.active
				&& this.activeTarget != null
				&& this.activeTarget.isAlive();
	}
	
	@Override
	public boolean isInterruptable() {
		return false;
	}
	
	@Override
	public void stop() {
		final int backoff = 20 * 30;
		cooldownTicks = wolf.tickCount + backoff;
		active = false;
		activeTarget = null;
	}
	
	protected void launchEntity(ArcaneWolfEntity wolf, LivingEntity target) {
		target.lerpMotion(0, 1.5, 0);
	}
	
	protected boolean shouldBlastEntity(ArcaneWolfEntity wolf, LivingEntity target) {
		return target != null
				&& target.isAlive()
				&& target.getDeltaMovement().y < 0;
	}
	
	protected void blastEntity(ArcaneWolfEntity wolf, LivingEntity target) {
		for (int i = 0; i < 2; i++) {
			((ServerLevel) target.level).addFreshEntity(
					(new TameLightning(NostrumEntityTypes.tameLightning, target.level,
							target.getX() + (wolf.getRandom().nextFloat()-.5f),
							target.getY(),
							target.getZ() + (wolf.getRandom().nextFloat()-.5f))
					).setEntityToIgnore(wolf));
		}
	}
	
	@Override
	public void start() {
		// Storm blasts the target upwards. After some time, it strikes them with several bolts of lighting!
		// We'll do this by first shooting them upwards. As soon as they start falling again, we'll BLAST EM
		this.active = true;
		this.activeTarget = wolf.getTarget();
		wolf.addMana(-manaCost);
		
		launchEntity(wolf, this.activeTarget);
		wolf.playSound(SoundEvents.WOLF_AMBIENT, 1f, .5f);
	}
	
	@Override
	public void tick() {
		if (shouldBlastEntity(wolf, this.activeTarget)) {
			blastEntity(wolf, this.activeTarget);
			this.active = false; // Signal that we're done
		}
		if (wolf.tickCount % 25 == 0) {
			wolf.playSound(SoundEvents.WOLF_GROWL, 1f, 1f);
		}
		wolf.lookAt(this.activeTarget, 30f, 180f);
	}

}
