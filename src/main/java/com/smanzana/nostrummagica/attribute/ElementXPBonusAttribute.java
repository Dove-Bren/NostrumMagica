package com.smanzana.nostrummagica.attribute;

import com.smanzana.nostrummagica.attribute.IPrintableAttribute.IPercentageAttribute;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class ElementXPBonusAttribute extends RangedAttribute implements IPercentageAttribute {
	
	public static final String ID_PREFIX = "elemxp_bonus_";

	private final EMagicElement element;
	
	public ElementXPBonusAttribute(EMagicElement elem, String name) {
		super(name, 0, -100.0D, 500.0D);
		this.setShouldWatch(true);
		this.element = elem;
	}
	
	public EMagicElement getElement() {
		return element;
	}

}
