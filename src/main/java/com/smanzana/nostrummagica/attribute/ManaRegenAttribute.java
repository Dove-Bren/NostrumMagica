package com.smanzana.nostrummagica.attribute;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.network.chat.Component;

public class ManaRegenAttribute extends RangedAttribute implements IPrintableAttribute {
	
	public static final String ID = "mana_regen";
	
	public ManaRegenAttribute(String name) {
		super(name, 0, -100.0D, 500.0D);
		this.setSyncable(true);
	}

	@Override
	public Component formatModifier(AttributeModifier modifier) {
		return IPrintableAttribute.formatAttributeValuePercentage(this, modifier);
	}

}
