package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.attribute.NostrumAttributes;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class MagicResistEffect extends MobEffect {

	public static final String ID = "magres";
	
	public MagicResistEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFFA5359A);
		this.addAttributeModifier(NostrumAttributes.magicResist, "662c96d6-19d7-4fe8-a6ff-b46befaa16a2", 20.D, AttributeModifier.Operation.ADDITION);
	}
	
	@Override
	public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
		// Effect used to be a (... * .75 ^ (amp+1)) on damage.
		return super.getAttributeModifierValue(amplifier, modifier);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return duration > 0; // Every tick
	}
}
