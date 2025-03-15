package com.smanzana.nostrummagica.attribute;

import com.smanzana.nostrummagica.attribute.IPrintableAttribute.IPercentageAttribute;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class AllElementXPBonusAttribute extends RangedAttribute implements IPercentageAttribute {
	
	public static final String ID = "elemxp_bonus_all";

	public AllElementXPBonusAttribute(String name) {
		super(name, 0, -100.0D, 500.0D);
		this.setSyncable(true);
	}
}
