package com.smanzana.nostrummagica.effect;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MagicShieldEffect extends Effect {

	public static final String ID = "potions-shieldm";
	
	public MagicShieldEffect() {
		super(EffectType.BENEFICIAL, 0xFF559496);
	}
	
	@Override
	public boolean isReady(int duration, int amp) {
		return duration > 0; // Every tick
	}
	
	@Override
	public void applyAttributesModifiersToEntity(LivingEntity entity, AttributeModifierManager attributeMap, int amplifier) {
		// Sneaky! We've just been applied
		//NostrumMagica.specialEffectProxy
		int armor = 4 * (int) Math.pow(2, amplifier);
		NostrumMagica.magicEffectProxy.applyMagicalShield(entity, (double) armor);
		
		NostrumMagicaSounds.SHIELD_APPLY.play(entity);
		
		super.applyAttributesModifiersToEntity(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn, AttributeModifierManager attributeMapIn, int amplifier) {
		NostrumMagica.magicEffectProxy.remove(SpecialEffect.SHIELD_MAGIC, entityLivingBaseIn);
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack matrixStackIn, int x, int y, float z) {
		PotionIcon.MAGICSHIELD.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.MAGICSHIELD.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
}
