package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.attribute.NostrumAttributes;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class FasterCastingEffect extends MobEffect {

	public static final String ID = "fast_cast";
	
	public static final String UUID = "47675ff6-35fe-455c-b514-0e28ef247ba0";
	
	public FasterCastingEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFF99EE61);
		this.addAttributeModifier(NostrumAttributes.castSpeed, UUID, 20.D, AttributeModifier.Operation.ADDITION);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return super.isDurationEffectTick(duration, amp);
	}
}
