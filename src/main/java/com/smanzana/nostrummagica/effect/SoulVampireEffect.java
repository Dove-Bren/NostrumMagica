package com.smanzana.nostrummagica.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class SoulVampireEffect extends MobEffect {

	public static final String ID = "soul_vampire";
	
	public SoulVampireEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFFE2346B);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No special effects
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amp) {
		;
    }
	
}
