package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.util.text.TextComponent;

public class LabeledBooleanSpellShapeProperty extends BooleanSpellShapeProperty {
	
	private final TextComponent falseLabel;
	private final TextComponent trueLabel;

	public LabeledBooleanSpellShapeProperty(String name, TextComponent falseLabel, TextComponent trueLabel) {
		super(name);
		this.falseLabel = falseLabel;
		this.trueLabel = trueLabel;
	}
	@Override
	public TextComponent getDisplayValue(SpellShape shape, Boolean value) {
		return value ? trueLabel : falseLabel; 
	}
}
