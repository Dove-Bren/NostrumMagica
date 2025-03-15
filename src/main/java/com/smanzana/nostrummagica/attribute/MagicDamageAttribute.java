package com.smanzana.nostrummagica.attribute;

import com.smanzana.nostrummagica.attribute.IPrintableAttribute.IPercentageAttribute;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class MagicDamageAttribute extends RangedAttribute implements IPercentageAttribute {

	public static final String ID = "magic_damage";
	
	public MagicDamageAttribute(String name) {
		super(name, 0, -100.0D, 1000.0D);
		this.setSyncable(true);
	}
}
