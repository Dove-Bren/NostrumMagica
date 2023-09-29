package com.smanzana.nostrummagica.entity.tasks;

import com.google.common.base.Predicate;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.EntityAIPanic;

public class EntityAIPanicGeneric<T extends CreatureEntity> extends EntityAIPanic {

	private Predicate<T> filter;
	private T creature;
	
	public EntityAIPanicGeneric(T creature, double speedIn) {
		this(creature, speedIn, null);
	}
	
	public EntityAIPanicGeneric(T creature, double speedIn, Predicate<T> filter) {
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
