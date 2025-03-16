package com.smanzana.nostrummagica.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class MagicWeaknessEffect extends MobEffect {

	public static final String ID = "magic_weakness";
	
	public MagicWeaknessEffect() {
		super(MobEffectCategory.HARMFUL, 0xFF5E8ABE);
	}
}
