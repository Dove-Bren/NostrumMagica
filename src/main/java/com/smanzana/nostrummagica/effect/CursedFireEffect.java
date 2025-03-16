package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellDamage;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class CursedFireEffect extends MobEffect {

	public static final String ID = "cursed_fire";
	
	public CursedFireEffect() {
		super(MobEffectCategory.HARMFUL, 0xFFB028B9);
		
		this.addAttributeModifier(Attributes.ATTACK_SPEED,
				"30886707-7d8b-424d-b1f5-ccf8b4dd8173", -.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		if (duration <= 0)
			return false;
		
		final int interval = Math.max(1, (int) (20.0 * (10.0 / Math.pow(2, amp))));
		return (duration % interval == 0); // 10 seconds, 5 second, 2.5 seconds, ...
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amp) {
		float damage = 2.0f;
		SpellDamage.DamageEntity(entity, EMagicElement.FIRE, damage, null);
        NostrumMagicaSounds.DAMAGE_FIRE.play(entity);
    }
}
