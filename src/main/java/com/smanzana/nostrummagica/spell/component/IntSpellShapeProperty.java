package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;

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
	public Integer readValue(INBT tag) {
		return tag instanceof IntNBT ? ((IntNBT) tag).getInt() : getDefault();
	}

	@Override
	public INBT writeValue(Integer value) {
		return IntNBT.valueOf(value);
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
	public TextComponent getDisplayValue(SpellShape shape, Integer value) {
		return new StringTextComponent("" + value); 
	}

	@Override
	public Integer[] getPossibleValues() {
		return this.possibleValuesBoxed;
	}

}
