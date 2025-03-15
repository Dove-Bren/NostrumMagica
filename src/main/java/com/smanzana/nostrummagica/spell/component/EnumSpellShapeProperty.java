package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.IPrettyEnum;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.TextComponent;

public class EnumSpellShapeProperty<E extends Enum<E> & IPrettyEnum> extends SpellShapeProperty<E> {
	
	private final Class<E> clazz;
	
	public EnumSpellShapeProperty(String name, Class<E> clazz) {
		super(name);
		this.clazz = clazz;
	}

	@Override
	public E getDefault() {
		return getPossibleValues()[0];
	}

	@Override
	public E readValue(INBT tag) {
		try {
			return Enum.valueOf(clazz, ((StringNBT) tag).getAsString().toUpperCase());
		} catch (Exception e) {
			return this.getDefault();
		}
	}

	@Override
	public INBT writeValue(E value) {
		return StringNBT.valueOf(value.name().toLowerCase());
	}

	@Override
	public boolean isValid(E value) {
		if (value == null) {
			return false;
		}
		
		return true;
	}

	@Override
	public TextComponent getDisplayValue(SpellShape shape, E value) {
		return value.getDisplayName(); 
	}

	@Override
	public E[] getPossibleValues() {
		return clazz.getEnumConstants();
	}

}
