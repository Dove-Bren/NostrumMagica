package com.smanzana.nostrummagica.potions;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public class MagicShieldPotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions-shieldm");
	
	private static MagicShieldPotion instance;
	public static MagicShieldPotion instance() {
		if (instance == null)
			instance = new MagicShieldPotion();
		
		return instance;
	}
	
	private MagicShieldPotion() {
		super(false, 0xFF559496);
		
		NostrumMagica.registerPotion(this, Resource);
	}
	
	@Override
	public boolean isReady(int duration, int amp) {
		return duration > 0; // Every tick
	}
	
	@Override
	public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap attributeMap, int amplifier) {
		// Sneaky! We've just been applied
		//NostrumMagica.specialEffectProxy
		int armor = 4 * (int) Math.pow(2, amplifier);
		NostrumMagica.magicEffectProxy.apply(SpecialEffect.SHIELD_MAGIC, (double) armor, entity);
		
		super.applyAttributesModifiersToEntity(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
		NostrumMagica.magicEffectProxy.remove(SpecialEffect.SHIELD_MAGIC, entityLivingBaseIn);
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }
}
