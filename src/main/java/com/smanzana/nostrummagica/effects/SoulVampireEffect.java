package com.smanzana.nostrummagica.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class SoulVampireEffect extends Effect {

	public static final String ID = "soul_vampire";
	
	public SoulVampireEffect() {
		super(EffectType.BENEFICIAL, 0xFFE2346B);
	}
	
	public boolean isReady(int duration, int amp) {
		return false; // No special effects
	}

	@Override
	public void performEffect(LivingEntity entity, int amp) {
		;
    }
	
}
