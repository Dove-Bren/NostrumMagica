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

	public static final String ID = "transformation";
	
	public NostrumTransformationEffect() {
		super(EffectType.NEUTRAL, 0xFF000000);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return duration % 20 == 0;
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amp) {
		if (entity.getHealth() > 1f) {
			entity.hurt(DamageSource.MAGIC, 1f);
		}
    }
	
	@Override
	public boolean shouldRender(EffectInstance effect) {
		return false;
	}
	
}
