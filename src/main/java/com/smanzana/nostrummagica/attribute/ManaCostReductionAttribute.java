package com.smanzana.nostrummagica.attribute;

import com.smanzana.nostrummagica.attribute.IPrintableAttribute.IPercentageAttribute;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class ManaCostReductionAttribute extends RangedAttribute implements IPercentageAttribute {
	
	public static final String ID = "mana_cost_reduction";
	
	public ManaCostReductionAttribute(String name) {
		super(name, 0, -200.0D, 100.0D);
		this.setShouldWatch(true);
	}

}
