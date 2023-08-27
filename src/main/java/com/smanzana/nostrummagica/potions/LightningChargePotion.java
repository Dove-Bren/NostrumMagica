package com.smanzana.nostrummagica.potions;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LightningChargePotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions-lightningcharge");
	
	private static LightningChargePotion instance;
	public static LightningChargePotion instance() {
		if (instance == null)
			instance = new LightningChargePotion();
		
		return instance;
	}
	
	private LightningChargePotion() {
		super(false, 0xFFFFF200);

		this.setBeneficial();
		this.setPotionName("potion.lightningcharge.name");
		this.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "3AA5821F-1B8B-4E94-BF6C-7A58449F587B", 0.2D, 1);
		this.setRegistryName(Resource);
	}
	
	@Override
	public double getAttributeModifierAmount(int amplifier, AttributeModifier modifier) {
		return modifier.getAmount(); // No change per level
	}
	
	@Override
	public boolean isReady(int duration, int amp) {
		return false; // No tick actions
	}
	
	@Override
	public void applyAttributesModifiersToEntity(LivingEntity entity, AbstractAttributeMap attributeMap, int amplifier) {
		super.applyAttributesModifiersToEntity(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		PotionIcon.LIGHTNINGMOVE.draw(mc, x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		PotionIcon.LIGHTNINGMOVE.draw(mc, x + 3, y + 3);
	}
}
