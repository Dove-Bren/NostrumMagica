package com.smanzana.nostrummagica.entity.tasks.arcanewolf;

import java.util.EnumSet;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.ArcaneWolfElementalType;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.NostrumTameLightning;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;

public class ArcaneWolfAIStormTask extends Goal {

	protected final EntityArcaneWolf wolf;
	
	// Cooldown, done as compared to wolf.ticksExisted
	protected int cooldownTicks = 0;
	protected final int manaCost;

	protected boolean active;
	protected @Nullable LivingEntity activeTarget;
	
	public ArcaneWolfAIStormTask(EntityArcaneWolf wolf, int manaCost) {
		this.wolf = wolf;
		this.manaCost = manaCost;
		
		active = false;
		
		this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	@Override
	public boolean shouldExecute() {
		return wolf.isAlive()
				&& !wolf.isSitting()
				&& wolf.getAttackTarget() != null
				&& wolf.ticksExisted >= cooldownTicks
				&& wolf.getMana() >= manaCost
				&& wolf.getElementalType() == ArcaneWolfElementalType.STORM
				&& !(wolf.getAttackTarget() instanceof CreeperEntity);
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		return this.active
				&& this.activeTarget != null
				&& this.activeTarget.isAlive();
	}
	
	@Override
	public boolean isPreemptible() {
		return false;
	}
	
	@Override
	public void resetTask() {
		final int backoff = 20 * 30;
		cooldownTicks = wolf.ticksExisted + backoff;
		active = false;
		activeTarget = null;
	}
	
	protected void launchEntity(EntityArcaneWolf wolf, LivingEntity target) {
		target.setVelocity(0, 1.5, 0);
	}
	
	protected boolean shouldBlastEntity(EntityArcaneWolf wolf, LivingEntity target) {
		return target != null
				&& target.isAlive()
				&& target.getMotion().y < 0;
	}
	
	protected void blastEntity(EntityArcaneWolf wolf, LivingEntity target) {
		for (int i = 0; i < 2; i++) {
			((ServerWorld) target.world).addLightningBolt(
					(new NostrumTameLightning(NostrumEntityTypes.tameLightning, target.world,
							target.getPosX() + (wolf.getRNG().nextFloat()-.5f),
							target.getPosY(),
							target.getPosZ() + (wolf.getRNG().nextFloat()-.5f))
					).setEntityToIgnore(wolf));
		}
	}
	
	@Override
	public void startExecuting() {
		// Storm blasts the target upwards. After some time, it strikes them with several bolts of lighting!
		// We'll do this by first shooting them upwards. As soon as they start falling again, we'll BLAST EM
		this.active = true;
		this.activeTarget = wolf.getAttackTarget();
		wolf.addMana(-manaCost);
		
		launchEntity(wolf, this.activeTarget);
		wolf.playSound(SoundEvents.ENTITY_WOLF_AMBIENT, 1f, .5f);
	}
	
	@Override
	public void tick() {
		if (shouldBlastEntity(wolf, this.activeTarget)) {
			blastEntity(wolf, this.activeTarget);
			this.active = false; // Signal that we're done
		}
		if (wolf.ticksExisted % 25 == 0) {
			wolf.playSound(SoundEvents.ENTITY_WOLF_GROWL, 1f, 1f);
		}
		wolf.faceEntity(this.activeTarget, 30f, 180f);
	}

}
