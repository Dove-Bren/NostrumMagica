package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.nbt.INBT;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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
	
	public abstract T[] getPossibleValues();
	
	public TextComponent getDisplayName(SpellShape shape) {
		return new TranslationTextComponent("shapeprop." + shape.getShapeKey() + "." + this.getName() + ".name");
	}
	
	public TextComponent getDisplayDescription(SpellShape shape) {
		return new TranslationTextComponent("shapeprop." + shape.getShapeKey() + "." + this.getName() + ".desc");
	}
	
	public abstract TextComponent getDisplayValue(SpellShape shape, T value);
	
}
