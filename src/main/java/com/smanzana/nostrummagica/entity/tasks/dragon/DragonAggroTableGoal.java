package com.smanzana.nostrummagica.entity.tasks.dragon;

import com.smanzana.nostrummagica.entity.AggroTable;
import com.smanzana.nostrummagica.entity.dragon.DragonEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class DragonAggroTableGoal<E extends DragonEntity, T extends LivingEntity> extends Goal {

	private final E dragon;
	private AggroTable<T> aggroTable;
	private final boolean checkSight;
	
	public DragonAggroTableGoal(E dragon, boolean checkSight) {
		this.checkSight = checkSight;
		this.dragon = dragon;
		aggroTable = new AggroTable<>((ent) -> {
			return !DragonAggroTableGoal.this.checkSight || DragonAggroTableGoal.this.dragon.getSensing().hasLineOfSight(ent);
		});
	}

	@Override
	public boolean canUse() {
		if (dragon == null || !dragon.isAlive()) {
			return false;
		}
		
		// not a great place for this
		aggroTable.decayTick();
		
		LivingEntity current = dragon.getTarget();
		T targ = this.getTarget();
		
		return (targ != null && targ != current);
	}
	
	@Override
	public void start() {
		this.dragon.setTarget(getTarget());
	}
	
	private T getTarget() {
		return aggroTable.getMainTarget();
	}
	
	public void addDamage(T attacker, float damage) {
		aggroTable.addDamage(attacker, damage);
	}
}
