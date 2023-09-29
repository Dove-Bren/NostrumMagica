package com.smanzana.nostrummagica.entity.tasks;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.pet.PetTargetMode;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;

public class PetTargetGoal<T extends CreatureEntity> extends TargetGoal {
	
	public static final EntityPredicate Filter = new EntityPredicate().setLineOfSiteRequired();
	
	protected T thePet;
	protected LivingEntity theOwner;
	protected int targetTicks;
	
	public PetTargetGoal(T petIn) {
		super(petIn, false);
		this.thePet = petIn;
		this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean shouldExecute() {
		final LivingEntity entitylivingbase = NostrumMagica.getOwner(thePet);
		
		if (entitylivingbase == null) {
			return false;
		}
		
		final PetTargetMode mode = NostrumMagica.instance.getPetCommandManager().getTargetMode(entitylivingbase);
		
		if (mode == PetTargetMode.FREE) {
			return false;
		}
		
		theOwner = entitylivingbase;
		return true;
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		return this.shouldExecute();
	}
	
	protected static @Nullable LivingEntity FindAggressiveTarget(MobEntity attacker, double range) {
		LivingEntity owner = NostrumMagica.getOwner(attacker);
		List<LivingEntity> tamed = (owner == null ? Lists.newArrayList() : NostrumMagica.getTamedEntities(owner));
		List<Entity> entities = attacker.world.getEntitiesInAABBexcluding(attacker, attacker.getBoundingBox().grow(range), (e) -> {
			return e instanceof LivingEntity
					&& e != attacker
					&& e != owner
					&& !tamed.contains(e)
					&& Filter.canTarget(attacker, (LivingEntity) e)
					&& !NostrumMagica.IsSameTeam(attacker, (LivingEntity) e);
		});
		Collections.sort(entities, (a, b) -> {
			return (int) (a.getDistanceSq(attacker) - b.getDistanceSq(attacker));
		});
		return entities.isEmpty() ? null : (LivingEntity)entities.get(0);
	}
	
	protected @Nullable LivingEntity findAggressiveTarget(T thePet) {
		return FindAggressiveTarget(thePet, 10);
	}
	
	@Override
	public void tick() {
		if (targetTicks > 0) {
			targetTicks--;
		}
		
		if (thePet.getAttackTarget() != null) {
			if (!thePet.getAttackTarget().isAlive()) {
				thePet.setAttackTarget(null);
			}
		}
		
		final PetTargetMode mode = NostrumMagica.instance.getPetCommandManager().getTargetMode(theOwner);
		switch (mode) {
		case AGGRESSIVE:
			if (thePet.getAttackTarget() == null && targetTicks <= 0) {
				targetTicks = 20;
				thePet.setAttackTarget(findAggressiveTarget(thePet));
			}
			break;
		case DEFENSIVE:
			if (theOwner.getRevengeTarget() != null) {
				thePet.setAttackTarget(theOwner.getRevengeTarget());
			}
			break;
		case FREE:
			; // Shouldn't ever get here
			break;
		case PASSIVE:
			; // Don't automatically set target at all
			break;
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		tick();

		super.startExecuting();
	}
}
