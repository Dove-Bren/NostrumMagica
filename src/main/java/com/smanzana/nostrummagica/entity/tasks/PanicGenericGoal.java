package com.smanzana.nostrummagica.entity.tasks;

import com.google.common.base.Predicate;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.PanicGoal;

public class PanicGenericGoal<T extends CreatureEntity> extends PanicGoal {

	private Predicate<T> filter;
	private T creature;
	
	public PanicGenericGoal(T creature, double speedIn) {
		this(creature, speedIn, null);
	}
	
	public PanicGenericGoal(T creature, double speedIn, Predicate<T> filter) {
		super(creature, speedIn);
		
		this.creature = creature;
		this.filter = filter;
	}

	@Override
	public boolean shouldExecute() {
		if (filter != null) {
			if (!filter.apply(this.creature)) {
				return false;
			}
		}
		
		return super.shouldExecute();
	}
	
}
