package com.smanzana.nostrummagica.attribute;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class MagicResistAttribute extends RangedAttribute {
	
	public static final String ID = "magic_resist";
	
	public MagicResistAttribute(String name) {
		super(name, 0, -100.0D, 100.0D);
		this.setShouldWatch(true);
	}

}
