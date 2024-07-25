package com.smanzana.nostrummagica.attribute;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class AllElementXPBonusAttribute extends RangedAttribute {
	
	public static final String ID = "elemxp_bonus_all";

	public AllElementXPBonusAttribute(String name) {
		super(name, 0, -100.0D, 500.0D);
		this.setShouldWatch(true);
	}
}
