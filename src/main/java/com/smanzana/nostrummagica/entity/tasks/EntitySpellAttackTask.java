package com.smanzana.nostrummagica.entity.tasks;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;

public class EntitySpellAttackTask<T extends EntityLiving> extends EntityAIBase {
	
	
	protected T entity;
	protected int delay;
	protected int odds;
	protected boolean needsTarget;
	protected Predicate<T> predicate;
	
	protected Spell spells[];
	
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
		
		if (needsTarget && (entity.getAttackTarget() == null || entity.getAttackTarget().isDead))
			return false;
		
		if (needsTarget && !entity.getEntitySenses().canSee(entity.getAttackTarget())){
			return false;
		}
		
		if (needsTarget && entity.getAttackTarget() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity.getAttackTarget();
			if (player.isCreative() || player.isSpectator()) {
				return false;
			}
		}
		
		return (this.attackTicks == 0 && (odds <= 0 || entity.getRNG().nextInt(odds) == 0));
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		return false;
	}
	
	public Spell pickSpell(Spell[] spells, T entity) {
		if (spells == null || spells.length == 0)
			return null;
		
		return spells[entity.getRNG().nextInt(spells.length)];
	}
	
	public @Nullable EntityLivingBase getTarget() {
		if (needsTarget && null != entity.getAttackTarget()) {
			return entity.getAttackTarget();
		}
		
		return null;
	}

	@Override
	public void startExecuting() {
		Spell spell = pickSpell(spells, entity);
		
		if (spell == null) {
			return;
		}
		
		EntityLivingBase target = getTarget();
		if (target != null) {
			entity.faceEntity(target, 360f, 180f);
		}
		
		spell.cast(entity, 1);
		attackTicks = this.delay;
	}
	
	@Override
	public void updateTask() {
		
	}
}
