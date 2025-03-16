package com.smanzana.nostrummagica.effect;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ImmobilizeEffect extends MobEffect {

	public static final String ID = "immobilize";
	private static final String POTENCY_UUID = "06f01f54-6586-4ee8-abd9-d3773b73ad9d";
	
	public ImmobilizeEffect() {
		super(MobEffectCategory.HARMFUL, 0x4A5C61); // 0x5A6C81 is slowness
		
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED, POTENCY_UUID, (double)-1.00F, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
}
