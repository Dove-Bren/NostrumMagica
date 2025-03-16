package com.smanzana.nostrummagica.attribute;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class MagicReductionAttribute extends RangedAttribute {
	
	public static final String ID_PREFIX = "magic_reduct_";
	
	private final EMagicElement element;
	
	public MagicReductionAttribute(EMagicElement elem, String name) {
		super(name, 0, -20.0D, 20.0D);
		this.element = elem;
		this.setSyncable(true);
	}
	
	public EMagicElement getElement() {
		return element;
	}
}
