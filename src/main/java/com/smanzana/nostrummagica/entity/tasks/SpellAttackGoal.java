package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumSet;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.spell.Spell;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.entity.ai.goal.Goal.Flag;

public class SpellAttackGoal<T extends MobEntity> extends Goal {
	
	
	protected T entity;
	protected int delay;
	protected int odds;
	protected int castTime; // can be 0; how long to be 'casting' before casting the spell
	protected boolean needsTarget;
	protected Predicate<T> predicate;
	
	protected Spell spells[];
	
	protected int attackTicks;
	protected int castTicks;
	
	public SpellAttackGoal(T entity, int delay, int odds, boolean needsTarget, Predicate<T> predicate, Spell ... spells) {
		this(entity, delay, odds, needsTarget, predicate, 0, spells);
	}
	
	public SpellAttackGoal(T entity, int delay, int odds, boolean needsTarget, Predicate<T> predicate, int castTime, Spell ... spells) {
		this.entity = entity;
		this.spells = spells;
		this.delay = delay;
		this.castTime = castTime;
		this.odds = odds;
		this.needsTarget = needsTarget;
		this.predicate = predicate;
		
		if (castTime > 0) {
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}
	}
	
	@Override
	public boolean canUse() {
		this.attackTicks = Math.max(0, this.attackTicks-1);
		
		if (!entity.isAlive())
			return false;
		
		if (this.predicate != null && !this.predicate.apply(entity)) {
			return false;
		}
		
		if (needsTarget && (entity.getTarget() == null || !entity.getTarget().isAlive()))
			return false;
		
		if (needsTarget && !entity.getSensing().canSee(entity.getTarget())){
			return false;
		}
		
		if (needsTarget && entity.getTarget() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity.getTarget();
			if (player.isCreative() || player.isSpectator()) {
				return false;
			}
		}
		
		return (this.attackTicks == 0 && (odds <= 0 || entity.getRandom().nextInt(odds) == 0));
	}
	
	@Override
	public boolean canContinueToUse() {
		return castTicks < castTime;
	}
	
	protected Spell pickSpell(Spell[] spells, T entity) {
		if (spells == null || spells.length == 0)
			return null;
		
		return spells[entity.getRandom().nextInt(spells.length)];
	}
	
	protected @Nullable LivingEntity getTarget() {
		if (needsTarget && null != entity.getTarget()) {
			return entity.getTarget();
		}
		
		return null;
	}
	
	protected void deductMana(Spell spell, T entity) {
		; // Usually, don't actually take mana
	}
	
	protected void castSpell() {
		Spell spell = pickSpell(spells, entity);
		
		if (spell == null) {
			return;
		}
		
		LivingEntity target = getTarget();
		@Nullable LivingEntity oldTarget = entity.getTarget();
		if (target != null) {
			entity.lookAt(target, 360f, 180f);
			entity.setTarget(target);
		}
		
		deductMana(spell, entity);
		spell.cast(entity, 1);
		attackTicks = this.delay;
		entity.setTarget(oldTarget);
	}

	@Override
	public void start() {
		super.start();
		castTicks = 0;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		LivingEntity target = getTarget();
		if (target != null) {
			this.entity.lookAt(getTarget(), 360f, 180);
		}
		
		castTicks++;
		if (castTicks >= castTime) {
			castSpell();
		} else {
			doCastTick(castTicks, castTime);
		}
	}
	
	protected void doCastTick(int ticksElapsed, int maxTicks) {
		; // do nothing by default
	}
}
