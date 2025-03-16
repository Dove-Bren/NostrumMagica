package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.nbt.Tag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.BaseComponent;

public class IntSpellShapeProperty extends SpellShapeProperty<Integer> {

	private final int[] possibleValues;
	private final Integer[] possibleValuesBoxed;
	
	public IntSpellShapeProperty(String name, int ... values) {
		super(name);
		this.possibleValues = values.clone();
		
		possibleValuesBoxed = new Integer[values.length];
		for (int i = 0; i < values.length; i++) {
			possibleValuesBoxed[i] = values[i];
		}
	}

	@Override
	public Integer getDefault() {
		return possibleValues[0];
	}

	@Override
	public Integer readValue(Tag tag) {
		return tag instanceof IntTag ? ((IntTag) tag).getAsInt() : getDefault();
	}

	@Override
	public Tag writeValue(Integer value) {
		return IntTag.valueOf(value);
	}

	@Override
	public boolean isValid(Integer value) {
		if (value == null) {
			return false;
		}
		
		for (float f : possibleValues) {
			if (f == value) {
				return true;
			}
		}
		return false;
	}

	@Override
	public BaseComponent getDisplayValue(SpellShape shape, Integer value) {
		return new TextComponent("" + value); 
	}

	@Override
	public Integer[] getPossibleValues() {
		return this.possibleValuesBoxed;
	}

}
