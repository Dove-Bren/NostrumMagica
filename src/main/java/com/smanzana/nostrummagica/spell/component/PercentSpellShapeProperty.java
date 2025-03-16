package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.BaseComponent;

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
	public BaseComponent getDisplayValue(SpellShape shape, Float value) {
		return new TextComponent(String.format("%.0f%%", 100 * value)); 
	}
}
