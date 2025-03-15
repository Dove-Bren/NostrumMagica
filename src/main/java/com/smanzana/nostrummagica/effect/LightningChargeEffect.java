package com.smanzana.nostrummagica.effect;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LightningChargeEffect extends Effect {

	public static final String ID = "lightningcharge";
	
	public LightningChargeEffect() {
		super(EffectType.BENEFICIAL, 0xFFFFF200);
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "3AA5821F-1B8B-4E94-BF6C-7A58449F587B", 0.2D, AttributeModifier.Operation.MULTIPLY_BASE);
		this.addAttributeModifier(NostrumAttributes.magicDamage, "7e570829-4031-4c7f-ba1d-1948ff102f11", 100.0D, AttributeModifier.Operation.ADDITION);
	}
	
	@Override
	public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
		return modifier.getAmount(); // No change per level
	}
	
	@Override
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No tick actions
	}
	
	@Override
	public void addAttributeModifiers(LivingEntity entity, AttributeModifierManager attributeMap, int amplifier) {
		super.addAttributeModifiers(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributeModifiers(LivingEntity entityLivingBaseIn, AttributeModifierManager attributeMapIn, int amplifier) {
		super.removeAttributeModifiers(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack matrixStackIn, int x, int y, float z) {
		PotionIcon.LIGHTNINGMOVE.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.LIGHTNINGMOVE.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
}
