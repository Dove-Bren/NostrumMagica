package com.smanzana.nostrummagica.entity.tasks;

import com.google.common.base.Predicate;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;

public class PanicGenericGoal<T extends PathfinderMob> extends PanicGoal {

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
	public boolean canUse() {
		if (filter != null) {
			if (!filter.apply(this.creature)) {
				return false;
			}
		}
		
		return super.canUse();
	}
	
}
