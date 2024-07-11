package com.smanzana.nostrummagica.spell.component;

import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;

public class IntSpellShapeProperty extends SpellShapeProperty<Integer> {

	private final int[] possibleValues;
	
	public IntSpellShapeProperty(String name, int ... values) {
		super(name);
		this.possibleValues = values.clone();
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
		return FloatNBT.valueOf(value);
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

}
