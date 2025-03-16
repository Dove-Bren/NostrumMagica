package com.smanzana.nostrummagica.attribute;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class BonusJumpAttribute extends RangedAttribute {
	
	public static final String ID = "bonus_jump";
	
	public BonusJumpAttribute(String name) {
		super(name, 0, 0D, 20.0D);
		this.setSyncable(true);
	}
}
