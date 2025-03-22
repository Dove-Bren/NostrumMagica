package com.smanzana.nostrummagica.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import net.minecraft.world.entity.LivingEntity;

/**
 * Generic aggro table
 * @author Skyler
 *
 * @param <E>
 * @param <T>
 */
public class AggroTable<T extends LivingEntity> {

	private final Map<T, Float> damageTable;
	
	// Doesn't prevent entities from being added, but starts reducing contribution when this is false AND
	// refuses to return them if it returns false
	private final Predicate<T> filter;
	
	public AggroTable(Predicate<T> filter) {
		damageTable = new HashMap<>();
		this.filter = filter;
	}

	public synchronized T getMainTarget() {
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
			if (this.filter.test(ent)) {
				target = ent;
				break;
			}
		}
		
		return target;
	}
	
	public synchronized void addDamage(T attacker, float damage) {
		Float val = damageTable.get(attacker);
		if (val == null) {
			val = Float.valueOf(0.0f);
		}
		
		float real = val.floatValue();
		real += damage;
		
		damageTable.put(attacker, real);
	}
	
	public synchronized void decayTick() {
		List<T> removeList = new ArrayList<>();
		
		for (Entry<T, Float> row: damageTable.entrySet()) {
			T ent = row.getKey();
			if (!this.filter.test(ent)) {
				// Reduce contribution
				float amt = Math.max(.25f, row.getValue() / (20 * 10f)); // 10 seconds of not seeing you
				row.setValue(row.getValue() - amt);
				if (row.getValue() <= 0) {
					removeList.add(ent);
				}
			}
		}
		
		for (T ent : removeList) {
			damageTable.remove(ent);
		}
	}
	
	public synchronized boolean tracking(T attacker) {
		return damageTable.containsKey(attacker);
	}
	
	public synchronized List<T> getAllTracked() {
		return new ArrayList<>(damageTable.keySet());
	}
}
