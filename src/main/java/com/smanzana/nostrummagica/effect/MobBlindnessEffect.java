package com.smanzana.nostrummagica.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class MobBlindnessEffect extends MobEffect {

	public static final String ID = "mob_blindness";
	
	public MobBlindnessEffect() {
		super(MobEffectCategory.HARMFUL, 0xFF000000);
	}
}
