package com.smanzana.nostrummagica.spell.component;

import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.INBT;

public class BooleanSpellShapeProperty extends SpellShapeProperty<Boolean> {

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

}
