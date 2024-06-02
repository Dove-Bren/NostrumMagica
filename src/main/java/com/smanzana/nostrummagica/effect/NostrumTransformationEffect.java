package com.smanzana.nostrummagica.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;

/**
 * Way to mark entities for later transformation.
 * Not intended to be put on a player.
 * @author Skyler
 *
 */
public class NostrumTransformationEffect extends Effect {

	public static final String ID = "potions-transformation";
	
	public NostrumTransformationEffect() {
		super(EffectType.NEUTRAL, 0xFF000000);
	}
	
	public boolean isReady(int duration, int amp) {
		return duration % 20 == 0;
	}

	@Override
	public void performEffect(LivingEntity entity, int amp) {
		if (entity.getHealth() > 1f) {
			entity.attackEntityFrom(DamageSource.MAGIC, 1f);
		}
    }
	
	@Override
	public boolean shouldRender(EffectInstance effect) {
		return false;
	}
	
}
