package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;

public class CursedFireEffect extends Effect {

	public static final String ID = "cursed_fire";
	
	public CursedFireEffect() {
		super(EffectType.HARMFUL, 0xFFB028B9);
		
		this.addAttributesModifier(Attributes.ATTACK_SPEED,
				"30886707-7d8b-424d-b1f5-ccf8b4dd8173", -.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
	
	public boolean isReady(int duration, int amp) {
		if (duration <= 0)
			return false;
		
		final int interval = Math.max(1, (int) (20.0 * (10.0 / Math.pow(2, amp))));
		return (duration % interval == 0); // 10 seconds, 5 second, 2.5 seconds, ...
	}

	@Override
	public void performEffect(LivingEntity entity, int amp) {
		float damage = 2.0f;
        entity.attackEntityFrom(DamageSource.MAGIC, damage);
        NostrumMagicaSounds.DAMAGE_FIRE.play(entity);
    }
}
