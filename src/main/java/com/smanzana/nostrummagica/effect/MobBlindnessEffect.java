package com.smanzana.nostrummagica.effect;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class MobBlindnessEffect extends Effect {

	public static final String ID = "mob_blindness";
	
	public MobBlindnessEffect() {
		super(EffectType.HARMFUL, 0xFF000000);
	}
}
