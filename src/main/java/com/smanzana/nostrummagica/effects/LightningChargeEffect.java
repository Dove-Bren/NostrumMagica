package com.smanzana.nostrummagica.effects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LightningChargeEffect extends Effect {

	public static final String ID = "potions-lightningcharge";
	
	public LightningChargeEffect() {
		super(EffectType.BENEFICIAL, 0xFFFFF200);
		this.addAttributesModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "3AA5821F-1B8B-4E94-BF6C-7A58449F587B", 0.2D, AttributeModifier.Operation.MULTIPLY_BASE);
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
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, int x, int y, float z) {
		PotionIcon.LIGHTNINGMOVE.draw(gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, int x, int y, float z, float alpha) {
		PotionIcon.LIGHTNINGMOVE.draw(Minecraft.getInstance(), x + 3, y + 3);
	}
}
