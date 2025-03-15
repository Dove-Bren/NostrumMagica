package com.smanzana.nostrummagica.attribute;

import com.smanzana.nostrummagica.attribute.IPrintableAttribute.IPercentageAttribute;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class MagicXPBonusAttribute extends RangedAttribute implements IPercentageAttribute {
	
	public static final String ID = "magicxp_bonus";
	
	public MagicXPBonusAttribute(String name) {
		super(name, 0, -100.0D, 500.0D);
		this.setSyncable(true);
	}
}
