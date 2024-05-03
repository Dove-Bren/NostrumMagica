package com.smanzana.nostrummagica.attributes;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class AttributeMagicPotency extends RangedAttribute {

	public static final String ID = "magic_potency";
	
	public AttributeMagicPotency(String name) {
		super(name, 0, -100.0D, 100.0D);
		this.setShouldWatch(true);
	}

}
