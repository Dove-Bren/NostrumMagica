package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;

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

	public abstract T readValue(Tag tag);
	
	public abstract Tag writeValue(T value);
	
	public abstract T[] getPossibleValues();
	
	public BaseComponent getDisplayName(SpellShape shape) {
		return new TranslatableComponent("shapeprop." + shape.getShapeKey() + "." + this.getName() + ".name");
	}
	
	public BaseComponent getDisplayDescription(SpellShape shape) {
		return new TranslatableComponent("shapeprop." + shape.getShapeKey() + "." + this.getName() + ".desc");
	}
	
	public abstract BaseComponent getDisplayValue(SpellShape shape, T value);
	
}
