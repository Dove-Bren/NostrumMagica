package com.smanzana.nostrummagica.attribute;

import com.smanzana.nostrummagica.attribute.IPrintableAttribute.IPercentageAttribute;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class MagicResistAttribute extends RangedAttribute implements IPercentageAttribute {
	
	public static final String ID = "magic_resist";
	
	public MagicResistAttribute(String name) {
		super(name, 0, -100.0D, 100.0D);
		this.setSyncable(true);
	}
}
