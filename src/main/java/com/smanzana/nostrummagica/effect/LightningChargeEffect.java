package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.attribute.NostrumAttributes;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class LightningChargeEffect extends MobEffect {

	public static final String ID = "lightningcharge";
	
	public LightningChargeEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFFFFF200);
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "3AA5821F-1B8B-4E94-BF6C-7A58449F587B", 0.2D, AttributeModifier.Operation.MULTIPLY_BASE);
		this.addAttributeModifier(NostrumAttributes.magicDamage, "7e570829-4031-4c7f-ba1d-1948ff102f11", 100.0D, AttributeModifier.Operation.ADDITION);
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
