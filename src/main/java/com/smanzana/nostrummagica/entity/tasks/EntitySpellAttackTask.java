package com.smanzana.nostrummagica.entity.tasks;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntitySpellAttackTask<T extends EntityLiving> extends EntityAIBase {
	
	
	protected T entity;
	protected Spell spells[];
	protected int delay;
	protected int odds;
	protected boolean needsTarget;
	protected Predicate<T> predicate;
	
	protected int attackTicks;
	
	public EntitySpellAttackTask(T entity, int delay, int odds, boolean needsTarget, Predicate<T> predicate, Spell ... spells) {
		this.entity = entity;
		this.spells = spells;
		this.delay = delay;
		this.odds = odds;
		this.needsTarget = needsTarget;
		this.predicate = predicate;
		
		this.setMutexBits(0);
	}
	
	@Override
	public boolean shouldExecute() {
		this.attackTicks = Math.max(0, this.attackTicks-1);
		
		if (entity.isDead)
			return false;
		
		if (this.predicate != null && !this.predicate.apply(entity)) {
			return false;
		}
		
		if (needsTarget && entity.getAttackTarget() == null)
			return false;
		
		if (this.attackTicks == 0) {
			if (odds > 0 && entity.getRNG().nextInt(odds) == 0) {
				return true;
			}
		}
		
		if (needsTarget && !entity.getEntitySenses().canSee(entity.getAttackTarget())){
			return false;
		}
		
		return false;
	}
	
	@Override
	public boolean continueExecuting() {
		return false;
	}

	@Override
	public void startExecuting() {
		if (spells == null || spells.length == 0)
			return;
		
		Spell spell = spells[entity.getRNG().nextInt(spells.length)];
		
		if (needsTarget && null != entity.getAttackTarget()) {
			entity.faceEntity(entity.getAttackTarget(), 360f, 180f);
		}
		
		spell.cast(entity, 1);
		attackTicks = this.delay;
	}
	
	@Override
	public void updateTask() {
		
	}
}
