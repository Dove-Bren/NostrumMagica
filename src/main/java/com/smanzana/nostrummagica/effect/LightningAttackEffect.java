package com.smanzana.nostrummagica.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class LightningAttackEffect extends MobEffect {

	public static final String ID = "lightningattack";
	
	public LightningAttackEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFFFFF200);
		this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "3AA5821F-0F7B-4E94-BF6C-7A58449F587B", 5.0D, AttributeModifier.Operation.ADDITION);
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "45e147fd-c876-48f2-b65a-6454fe86b46d".toUpperCase(), -0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
	
	@Override
	public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
		return modifier.getAmount(); // No change per level
	}
	
	@Override
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No tick actions
	}
	
	@Override
	public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
		super.addAttributeModifiers(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributeModifiers(LivingEntity entityLivingBaseIn, AttributeMap attributeMapIn, int amplifier) {
		super.removeAttributeModifiers(entityLivingBaseIn, attributeMapIn, amplifier);
    }
}
