package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.attribute.NostrumAttributes;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ManaRegenEffect extends MobEffect {

	public static final String ID = "mana-regen";
	
	public ManaRegenEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFFBB6DFF);
		this.addAttributeModifier(NostrumAttributes.manaRegen,
				"74149d64-b22a-4dd9-ab68-030fc195ecfc", 50D, AttributeModifier.Operation.ADDITION);
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No special effects
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amp) {
		;
    }
	
	public String getEffectName() {
		return "mana-regen";
	}
}
