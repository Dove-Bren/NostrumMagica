package com.smanzana.nostrummagica.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;

public class AttributeUtil {

	public static final double GetAttributeValueSafe(LivingEntity entity, Attribute attribute) {
		if (entity.getAttributeManager().hasAttributeInstance(attribute)) {
			try {
				return entity.getAttributeValue(attribute);
			} catch (Exception e) {
				;
			}
		}
		return 0.0;
	}
	
}
