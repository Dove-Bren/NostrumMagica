package com.smanzana.nostrummagica.potions;

import java.awt.Color;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RootedPotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions-rooted");
	
	private static RootedPotion instance;
	public static RootedPotion instance() {
		if (instance == null)
			instance = new RootedPotion();
		
		return instance;
	}
	
	private RootedPotion() {
		super(true, (new Color(100, 60, 25)).getRGB());

		this.setPotionName("potion.rooted.name");
		this.setRegistryName(Resource);
	}
	
	public boolean isReady(int duration, int amp) {
		return duration > 0; // Every tick
	}

	@Override
	public void performEffect(LivingEntity entity, int amp)
    {
        if (entity.isRiding()) {
        	entity.dismountRidingEntity();
        }
        
        if (entity.getMotion().y > 0) {
        	entity.getMotion().y = 0;
        }
        entity.getMotion().x = 0.0;
        entity.getMotion().z = 0.0;
    }
	
	@Override
	public void applyAttributesModifiersToEntity(LivingEntity entity, AbstractAttributeMap attributeMap, int amplifier) {
		// Sneaky! We've just been applied
		NostrumMagica.magicEffectProxy.applyRootedEffect(entity);
		super.applyAttributesModifiersToEntity(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
		NostrumMagica.magicEffectProxy.remove(SpecialEffect.ROOTED, entityLivingBaseIn);
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		PotionIcon.ROOTED.draw(mc, x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		PotionIcon.ROOTED.draw(mc, x + 3, y + 3);
	}
	
}
