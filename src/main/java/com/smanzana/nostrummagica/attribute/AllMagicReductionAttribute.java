package com.smanzana.nostrummagica.attribute;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class AllMagicReductionAttribute extends RangedAttribute {
	
	public static final String ID = "magic_reduct_all";
	
	public AllMagicReductionAttribute(String name) {
		super(name, 0, -20.0D, 20.0D);
		this.setShouldWatch(true);
	}
}
