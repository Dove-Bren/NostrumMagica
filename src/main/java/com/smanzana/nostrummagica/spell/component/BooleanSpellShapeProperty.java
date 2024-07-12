package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;

public class BooleanSpellShapeProperty extends SpellShapeProperty<Boolean> {
	
	private static final Boolean[] VALUES = {false, true};

	public BooleanSpellShapeProperty(String name) {
		super(name);
	}

	@Override
	public Boolean getDefault() {
		return false;
	}

	@Override
	public Boolean readValue(INBT tag) {
		return tag instanceof ByteNBT ? (((ByteNBT) tag).getByte() != 0) : getDefault();
	}

	@Override
	public INBT writeValue(Boolean value) {
		return ByteNBT.valueOf(value);
	}

	@Override
	public boolean isValid(Boolean value) {
		return value != null;
	}

	@Override
	public TextComponent getDisplayValue(SpellShape shape, Boolean value) {
		return new StringTextComponent(value ? "On" : "Off"); 
	}

	@Override
	public Boolean[] getPossibleValues() {
		return VALUES;
	}

}
