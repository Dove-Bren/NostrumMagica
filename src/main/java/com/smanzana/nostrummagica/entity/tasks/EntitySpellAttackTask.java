package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumSet;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

public class EntitySpellAttackTask<T extends MobEntity> extends Goal {
	
	
	protected T entity;
	protected int delay;
	protected int odds;
	protected int castTime; // can be 0; how long to be 'casting' before casting the spell
	protected boolean needsTarget;
	protected Predicate<T> predicate;
	
	protected Spell spells[];
	
	protected int attackTicks;
	protected int castTicks;
	
	public EntitySpellAttackTask(T entity, int delay, int odds, boolean needsTarget, Predicate<T> predicate, Spell ... spells) {
		this(entity, delay, odds, needsTarget, predicate, 0, spells);
	}
	
	public EntitySpellAttackTask(T entity, int delay, int odds, boolean needsTarget, Predicate<T> predicate, int castTime, Spell ... spells) {
		this.entity = entity;
		this.spells = spells;
		this.delay = delay;
		this.castTime = castTime;
		this.odds = odds;
		this.needsTarget = needsTarget;
		this.predicate = predicate;
		
		if (castTime > 0) {
			this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}
	}
	
	@Override
	public boolean shouldExecute() {
		this.attackTicks = Math.max(0, this.attackTicks-1);
		
		if (!entity.isAlive())
			return false;
		
		if (this.predicate != null && !this.predicate.apply(entity)) {
			return false;
		}
		
		if (needsTarget && (entity.getAttackTarget() == null || !entity.getAttackTarget().isAlive()))
			return false;
		
		if (needsTarget && !entity.getEntitySenses().canSee(entity.getAttackTarget())){
			return false;
		}
		
		if (needsTarget && entity.getAttackTarget() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity.getAttackTarget();
			if (player.isCreative() || player.isSpectator()) {
				return false;
			}
		}
		
		return (this.attackTicks == 0 && (odds <= 0 || entity.getRNG().nextInt(odds) == 0));
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		return castTicks < castTime;
	}
	
	protected Spell pickSpell(Spell[] spells, T entity) {
		if (spells == null || spells.length == 0)
			return null;
		
		return spells[entity.getRNG().nextInt(spells.length)];
	}
	
	protected @Nullable LivingEntity getTarget() {
		if (needsTarget && null != entity.getAttackTarget()) {
			return entity.getAttackTarget();
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
		@Nullable LivingEntity oldTarget = entity.getAttackTarget();
		if (target != null) {
			entity.faceEntity(target, 360f, 180f);
			entity.setAttackTarget(target);
		}
		
		deductMana(spell, entity);
		spell.cast(entity, 1);
		attackTicks = this.delay;
		entity.setAttackTarget(oldTarget);
	}

	@Override
	public void startExecuting() {
		super.startExecuting();
		castTicks = 0;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		LivingEntity target = getTarget();
		if (target != null) {
			this.entity.faceEntity(getTarget(), 360f, 180);
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
