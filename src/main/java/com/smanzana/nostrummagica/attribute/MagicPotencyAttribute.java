package com.smanzana.nostrummagica.attribute;

import com.smanzana.nostrummagica.attribute.IPrintableAttribute.IPercentageAttribute;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class MagicPotencyAttribute extends RangedAttribute implements IPercentageAttribute {

	public static final String ID = "magic_potency";
	
	public MagicPotencyAttribute(String name) {
		super(name, 0, -100.0D, 200.0D);
		this.setSyncable(true);
	}
}
