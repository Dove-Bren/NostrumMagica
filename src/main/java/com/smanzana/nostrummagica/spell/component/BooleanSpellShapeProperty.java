package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.BaseComponent;

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
	public Boolean readValue(Tag tag) {
		return tag instanceof ByteTag ? (((ByteTag) tag).getAsByte() != 0) : getDefault();
	}

	@Override
	public Tag writeValue(Boolean value) {
		return ByteTag.valueOf(value);
	}

	@Override
	public boolean isValid(Boolean value) {
		return value != null;
	}

	@Override
	public BaseComponent getDisplayValue(SpellShape shape, Boolean value) {
		return new TextComponent(value ? "On" : "Off"); 
	}

	@Override
	public Boolean[] getPossibleValues() {
		return VALUES;
	}

}
