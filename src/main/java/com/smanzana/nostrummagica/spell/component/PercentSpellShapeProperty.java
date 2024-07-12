package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;

/**
 * A @FloatSpellShapeProperty but that displays as percentages
 * @author Skyler
 *
 */
public class PercentSpellShapeProperty extends FloatSpellShapeProperty {

	public PercentSpellShapeProperty(String name, float ... values) {
		super(name, values);
	}

	@Override
	public TextComponent getDisplayValue(SpellShape shape, Float value) {
		return new StringTextComponent(String.format("%.0f%%", 100 * value)); 
	}
}
