package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;

public class FloatSpellShapeProperty extends SpellShapeProperty<Float> {

	private final float[] possibleValues;
	private final Float[] possibleValuesBoxed;
	
	public FloatSpellShapeProperty(String name, float ... values) {
		super(name);
		this.possibleValues = values.clone();
		
		possibleValuesBoxed = new Float[values.length];
		for (int i = 0; i < values.length; i++) {
			possibleValuesBoxed[i] = values[i];
		}
	}

	@Override
	public Float getDefault() {
		return possibleValues[0];
	}

	@Override
	public Float readValue(INBT tag) {
		return tag instanceof FloatNBT ? ((FloatNBT) tag).getAsFloat() : getDefault();
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

	@Override
	public TextComponent getDisplayValue(SpellShape shape, Float value) {
		return new StringTextComponent(String.format("%.1f", value)); 
	}

	@Override
	public Float[] getPossibleValues() {
		return possibleValuesBoxed;
	}

}
