package com.smanzana.nostrummagica.spell.component;

import net.minecraft.nbt.INBT;

public abstract class SpellShapeProperty<T> {
	
	protected final String name;
	
	public SpellShapeProperty(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract T getDefault();
	
	public abstract boolean isValid(T value);

	public abstract T readValue(INBT tag);
	
	public abstract INBT writeValue(T value);
	
}
