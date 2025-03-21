package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.attribute.NostrumAttributes;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class MagicBoostEffect extends MobEffect {

	public static final String ID = "magboost";
	private static final String POTENCY_UUID = "718e46ce-f549-4f18-8dcb-d690590e9ba5";
	
	public MagicBoostEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFF47FFAF);
		
		this.addAttributeModifier(NostrumAttributes.magicDamage, POTENCY_UUID, 50.D, AttributeModifier.Operation.ADDITION);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No tick effects
	}
}
