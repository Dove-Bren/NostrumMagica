package com.smanzana.nostrummagica.attribute;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class AttributeManaRegen extends RangedAttribute {
	
	public static final String ID = "mana_regen";
	
	public AttributeManaRegen(String name) {
		super(name, 0, -100.0D, 500.0D);
		this.setShouldWatch(true);
	}

}
