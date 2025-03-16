package com.smanzana.nostrummagica.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Way to mark entities for later transformation.
 * Not intended to be put on a player.
 * @author Skyler
 *
 */
public class NostrumTransformationEffect extends MobEffect {

	public static final String ID = "transformation";
	
	public NostrumTransformationEffect() {
		super(MobEffectCategory.NEUTRAL, 0xFF000000);
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
	public boolean shouldRender(MobEffectInstance effect) {
		return false;
	}
	
}
