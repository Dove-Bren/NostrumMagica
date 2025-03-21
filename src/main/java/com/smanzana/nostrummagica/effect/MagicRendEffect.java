package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.attribute.NostrumAttributes;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class MagicRendEffect extends MobEffect {

	public static final String ID = "magic_rend";
	private static final String MOD_UUID = "23a1bd05-7864-473a-bf4e-52e419849473";
	
	public MagicRendEffect() {
		super(MobEffectCategory.HARMFUL, 0xFFE36338);
		this.addAttributeModifier(NostrumAttributes.magicResist, MOD_UUID, -20D, AttributeModifier.Operation.ADDITION);
	}
}
