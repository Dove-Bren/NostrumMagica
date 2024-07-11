package com.smanzana.nostrummagica.spell.component;

import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;

public class FloatSpellShapeProperty extends SpellShapeProperty<Float> {

	private final float[] possibleValues;
	
	public FloatSpellShapeProperty(String name, float ... values) {
		super(name);
		this.possibleValues = values.clone();
	}

	@Override
	public Float getDefault() {
		return possibleValues[0];
	}

	@Override
	public Float readValue(INBT tag) {
		return tag instanceof FloatNBT ? ((FloatNBT) tag).getFloat() : getDefault();
	}

	@Override
	public INBT writeValue(Float value) {
		return FloatNBT.valueOf(value);
	}

	@Override
	public boolean isValid(Float value) {
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
