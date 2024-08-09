package com.smanzana.nostrummagica.effect;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;

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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RootedEffect extends Effect {

	public static final String ID = "rooted";
	
	public RootedEffect() {
		super(EffectType.HARMFUL, (new Color(100, 60, 25)).getRGB());
		
		this.addAttributesModifier(Attributes.MOVEMENT_SPEED, "ceea9fa1-aee7-4fe1-b8e8-e9b8cfc2f762", -.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
	
	@Override
	public boolean isReady(int duration, int amp) {
		return duration > 0; // Every tick
	}
	
	@Override
	public double getAttributeModifierAmount(int amplifier, AttributeModifier modifier) {
		// Amp 0-2 have no slowness.
		if (amplifier <= 2) {
			return 0.0;
		}
		return super.getAttributeModifierAmount(amplifier-3, modifier);
	}

	@Override
	public void performEffect(LivingEntity entity, int amp) {
		if (entity.isPassenger()) {
			entity.stopRiding();
		}

		final Vector3d motion = entity.getMotion();
		
		if (motion.y > 0) {
			entity.setMotion(motion.x, 0, motion.z);
		}
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
