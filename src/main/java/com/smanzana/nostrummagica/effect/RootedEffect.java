package com.smanzana.nostrummagica.effect;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RootedEffect extends Effect {

	public static final String ID = "potions-rooted";
	
	public RootedEffect() {
		super(EffectType.HARMFUL, (new Color(100, 60, 25)).getRGB());
	}
	
	public boolean isReady(int duration, int amp) {
		return duration > 0; // Every tick
	}

	@Override
	public void performEffect(LivingEntity entity, int amp)
    {
        if (entity.isPassenger()) {
        	entity.stopRiding();
        }
        
        final Vector3d motion = entity.getMotion();
        final double y = (motion.y > 0 ? 0 : motion.y);
        entity.setMotion(0, y, 0);
    }
	
	@Override
	public void applyAttributesModifiersToEntity(LivingEntity entity, AttributeModifierManager attributeMap, int amplifier) {
		// Sneaky! We've just been applied
		NostrumMagica.magicEffectProxy.applyRootedEffect(entity);
		super.applyAttributesModifiersToEntity(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn, AttributeModifierManager attributeMapIn, int amplifier) {
		NostrumMagica.magicEffectProxy.remove(SpecialEffect.ROOTED, entityLivingBaseIn);
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack matrixStackIn, int x, int y, float z) {
		PotionIcon.ROOTED.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.ROOTED.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
	
}
