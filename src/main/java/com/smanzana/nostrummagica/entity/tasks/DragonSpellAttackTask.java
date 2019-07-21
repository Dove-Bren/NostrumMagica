package com.smanzana.nostrummagica.entity.tasks;

import com.smanzana.nostrummagica.entity.EntityDragon;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.entity.ai.EntityAIBase;

public class DragonSpellAttackTask<T extends EntityDragon> extends EntityAIBase {
	
	
	protected T dragon;
	protected Spell spells[];
	protected int delay;
	protected int odds;
	protected boolean needsTarget;
	
	protected int attackTicks;
	
	public DragonSpellAttackTask(T dragon, int delay, int odds, boolean needsTarget, Spell ... spells) {
		this.dragon = dragon;
		this.spells = spells;
		this.delay = delay;
		this.odds = odds;
		this.needsTarget = needsTarget;
		
		this.setMutexBits(0);
	}
	
	@Override
	public boolean shouldExecute() {
		this.attackTicks = Math.max(0, this.attackTicks-1);
		
		if (dragon.isDead)
			return false;
		
		if (needsTarget && dragon.getAttackTarget() == null)
			return false;
		
		if (this.attackTicks == 0) {
			if (odds > 0 && dragon.getRNG().nextInt(odds) == 0) {
				return true;
			}
		}
		
		if (needsTarget && !dragon.getEntitySenses().canSee(dragon.getAttackTarget())){
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
		
		Spell spell = spells[dragon.getRNG().nextInt(spells.length)];
		
		if (needsTarget && null != dragon.getAttackTarget()) {
			dragon.faceEntity(dragon.getAttackTarget(), 360f, 180f);
		}
		
		spell.cast(dragon, 1);
		attackTicks = this.delay;
	}
	
	@Override
	public void updateTask() {
		
	}
}
