package com.smanzana.nostrummagica.effects;

import com.mojang.blaze3d.matrix.MatrixStack;

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

public class LightningAttackEffect extends Effect {

	public static final String ID = "potions-lightningattack";
	
	public LightningAttackEffect() {
		super(EffectType.BENEFICIAL, 0xFFFFF200);
		this.addAttributesModifier(Attributes.ATTACK_DAMAGE, "3AA5821F-0F7B-4E94-BF6C-7A58449F587B", 5.0D, AttributeModifier.Operation.ADDITION);
		this.addAttributesModifier(Attributes.MOVEMENT_SPEED, "45e147fd-c876-48f2-b65a-6454fe86b46d".toUpperCase(), -0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
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
	public void applyAttributesModifiersToEntity(LivingEntity entity, AttributeModifierManager attributeMap, int amplifier) {
		super.applyAttributesModifiersToEntity(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn, AttributeModifierManager attributeMapIn, int amplifier) {
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack stack, int x, int y, float z) {
		PotionIcon.LIGHTNINGATTACK.draw(gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack stack, int x, int y, float z, float alpha) {
		PotionIcon.LIGHTNINGATTACK.draw(Minecraft.getInstance(), x + 3, y + 3);
	}
}
