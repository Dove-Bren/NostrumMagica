package com.smanzana.nostrummagica.potions;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public class PhysicalShieldPotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions-shieldp");
	
	private static PhysicalShieldPotion instance;
	public static PhysicalShieldPotion instance() {
		if (instance == null)
			instance = new PhysicalShieldPotion();
		
		return instance;
	}
	
	private PhysicalShieldPotion() {
		super(false, 0xFF80805D);
		
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
		NostrumMagica.magicEffectProxy.apply(SpecialEffect.SHIELD_PHYSICAL, (double) armor, entity);
		
		super.applyAttributesModifiersToEntity(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
		NostrumMagica.magicEffectProxy.remove(SpecialEffect.SHIELD_PHYSICAL, entityLivingBaseIn);
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }
}
