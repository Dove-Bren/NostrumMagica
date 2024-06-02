package com.smanzana.nostrummagica.attribute;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class AttributeMagicReduction extends RangedAttribute {
	
	public static final String ID_PREFIX = "magic_reduct_";
	
	private final EMagicElement element;
	
	public AttributeMagicReduction(EMagicElement elem, String name) {
		super(name, 0, -20.0D, 20.0D);
		this.element = elem;
		this.setShouldWatch(true);
	}
	
	public EMagicElement getElement() {
		return element;
	}
}
