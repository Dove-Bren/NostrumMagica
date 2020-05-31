package com.smanzana.nostrummagica.entity.tasks;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.entity.dragon.EntityDragon;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class DragonAIAggroTable<E extends EntityDragon, T extends EntityLivingBase> extends EntityAIBase {

	private E dragon;
	private Map<T, Float> damageTable;
	private boolean checkSight;
	
	public DragonAIAggroTable(E dragon, boolean checkSight) {
		damageTable = new HashMap<>();
		this.checkSight = checkSight;
		this.dragon = dragon;
	}

	@Override
	public boolean shouldExecute() {
		if (dragon == null || dragon.isDead) {
			return false;
		}
		
		EntityLivingBase current = dragon.getAttackTarget();
		T targ = this.getTarget();
		
		return (targ != null && targ != current);
	}
	
	@Override
	public void startExecuting() {
		this.dragon.setAttackTarget(getTarget());
	}
	
	private T getTarget() {
		if (this.damageTable.isEmpty()) {
			return null;
		}
		
		T target = null;
		List<Entry<T, Float>> rows = Lists.newArrayList(damageTable.entrySet());
		Collections.sort(rows, new Comparator<Entry<T, Float>>() {
			@Override
			public int compare(Entry<T, Float> arg0, Entry<T, Float> arg1) {
				if (arg0 == null || arg1 == null) {
					return arg0 == arg1 ? 0 : -1;
				}
				
				return (int) ((arg1.getValue() * 100.0f) - (arg0.getValue() * 100.0f)); 
			}
		});
		
		for (Entry<T, Float> row: rows) {
			T ent = row.getKey();
			if (!this.checkSight || dragon.getEntitySenses().canSee(ent)) {
				target = ent;
				break;
			}
		}
		
		return target;
	}
	
	public void addDamage(T attacker, float damage) {
		Float val = damageTable.get(attacker);
		if (val == null) {
			val = new Float(0.0f);
		}
		
		float real = val.floatValue();
		real += damage;
		
		damageTable.put(attacker, real);
	}
}
