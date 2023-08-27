package com.smanzana.nostrummagica.potions;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

		this.setBeneficial();
		this.setPotionName("potion.shieldphysical.name");
		this.setRegistryName(Resource);
	}
	
	@Override
	public boolean isReady(int duration, int amp) {
		return duration > 0; // Every tick
	}
	
	@Override
	public void applyAttributesModifiersToEntity(LivingEntity entity, AbstractAttributeMap attributeMap, int amplifier) {
		// Sneaky! We've just been applied
		//NostrumMagica.specialEffectProxy
		int armor = 4 * (int) Math.pow(2, amplifier - 1);
		NostrumMagica.magicEffectProxy.applyPhysicalShield(entity, (double) armor);
		
		NostrumMagicaSounds.SHIELD_APPLY.play(entity);
		
		super.applyAttributesModifiersToEntity(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
		NostrumMagica.magicEffectProxy.remove(SpecialEffect.SHIELD_PHYSICAL, entityLivingBaseIn);
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		PotionIcon.PHYSICALSHIELD.draw(mc, x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		PotionIcon.PHYSICALSHIELD.draw(mc, x + 3, y + 3);
	}
}
