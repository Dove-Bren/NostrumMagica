package com.smanzana.nostrummagica.attribute;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class MagicXPBonusAttribute extends RangedAttribute {
	
	public static final String ID = "magicxp_bonus";
	
	public MagicXPBonusAttribute(String name) {
		super(name, 0, -100.0D, 500.0D);
		this.setShouldWatch(true);
	}

}
