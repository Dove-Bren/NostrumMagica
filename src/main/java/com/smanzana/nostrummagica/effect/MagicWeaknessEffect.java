package com.smanzana.nostrummagica.effect;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class MagicWeaknessEffect extends Effect {

	public static final String ID = "magic_weakness";
	
	public MagicWeaknessEffect() {
		super(EffectType.HARMFUL, 0xFF5E8ABE);
	}
}
