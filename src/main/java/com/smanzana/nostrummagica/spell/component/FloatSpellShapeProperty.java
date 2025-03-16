package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.BaseComponent;

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
	public Float readValue(Tag tag) {
		return tag instanceof FloatTag ? ((FloatTag) tag).getAsFloat() : getDefault();
	}

	@Override
	public Tag writeValue(Float value) {
		return FloatTag.valueOf(value);
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
	public BaseComponent getDisplayValue(SpellShape shape, Float value) {
		return new TextComponent(String.format("%.1f", value)); 
	}

	@Override
	public Float[] getPossibleValues() {
		return possibleValuesBoxed;
	}

}
