package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

/**
 * Fake potion effect that shows when you have a familiar and when you don't
 * @author Skyler
 *
 */
public class FamiliarEffect extends MobEffect {
	
	public static final String ID = "familiar-shadow";

	public FamiliarEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFF310033);

		//this.setPotionName("potion.familiar.name");
		//this.setRegistryName(Resource);
	}
	
	@Override
	public boolean isDurationEffectTick(int duration, int amp) {
		return duration > 0 && duration % 5 == 0; // Check every 1/4 a second :shrug:
		// Note: actual apply check is done in anon class when applying, and validates familiar status
	}
	
	@Override
	public void applyEffectTick(LivingEntity entityLivingBaseIn, int p_76394_2_) {
		;
	}
	
	@Override
	public void removeAttributeModifiers(LivingEntity entityLivingBaseIn, AttributeMap attributeMapIn, int amplifier) {
		// We were removed for whatever reason. Kill the familiars
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entityLivingBaseIn);
		if (attr != null)
			attr.clearFamiliars();
	}
}
