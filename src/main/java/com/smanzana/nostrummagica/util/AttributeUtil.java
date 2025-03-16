package com.smanzana.nostrummagica.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class AttributeUtil {

	public static final double GetAttributeValueSafe(LivingEntity entity, Attribute attribute) {
		if (entity.getAttributes().hasAttribute(attribute)) {
			try {
				return entity.getAttributeValue(attribute);
			} catch (Exception e) {
				;
			}
		}
		return 0.0;
	}
	
}
