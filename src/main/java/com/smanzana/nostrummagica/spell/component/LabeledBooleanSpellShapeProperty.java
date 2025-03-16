package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.network.chat.BaseComponent;

public class LabeledBooleanSpellShapeProperty extends BooleanSpellShapeProperty {
	
	private final BaseComponent falseLabel;
	private final BaseComponent trueLabel;

	public LabeledBooleanSpellShapeProperty(String name, BaseComponent falseLabel, BaseComponent trueLabel) {
		super(name);
		this.falseLabel = falseLabel;
		this.trueLabel = trueLabel;
	}
	@Override
	public BaseComponent getDisplayValue(SpellShape shape, Boolean value) {
		return value ? trueLabel : falseLabel; 
	}
}
