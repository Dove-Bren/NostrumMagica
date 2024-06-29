package com.smanzana.nostrummagica.attribute;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class MagicPotencyAttribute extends RangedAttribute {

	public static final String ID = "magic_potency";
	
	public MagicPotencyAttribute(String name) {
		super(name, 0, -100.0D, 200.0D);
		this.setShouldWatch(true);
	}

}
