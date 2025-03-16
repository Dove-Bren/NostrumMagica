package com.smanzana.nostrummagica.effect;

import java.awt.Color;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RootedEffect extends MobEffect {

	public static final String ID = "rooted";
	
	public RootedEffect() {
		super(MobEffectCategory.HARMFUL, (new Color(100, 60, 25)).getRGB());
		
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "ceea9fa1-aee7-4fe1-b8e8-e9b8cfc2f762", -.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
	
	@Override
	public boolean isDurationEffectTick(int duration, int amp) {
		return duration > 0; // Every tick
	}
	
	@Override
	public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
		// Amp 0-2 have no slowness.
		if (amplifier <= 2) {
			return 0.0;
		}
		return super.getAttributeModifierValue(amplifier-3, modifier);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amp) {
		if (entity.isPassenger()) {
			entity.stopRiding();
		}

		final Vec3 motion = entity.getDeltaMovement();
		
		if (motion.y > 0) {
			entity.setDeltaMovement(motion.x, 0, motion.z);
		}
	}
	
	@Override
	public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
		// Sneaky! We've just been applied
		NostrumMagica.magicEffectProxy.applyRootedEffect(entity);
		super.addAttributeModifiers(entity, attributeMap, amplifier);
	}
	
	@Override
	public void removeAttributeModifiers(LivingEntity entityLivingBaseIn, AttributeMap attributeMapIn, int amplifier) {
		NostrumMagica.magicEffectProxy.remove(SpecialEffect.ROOTED, entityLivingBaseIn);
		super.removeAttributeModifiers(entityLivingBaseIn, attributeMapIn, amplifier);
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack matrixStackIn, int x, int y, float z) {
		PotionIcon.ROOTED.draw(matrixStackIn, gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(MobEffectInstance effect, GuiComponent gui, PoseStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.ROOTED.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
	
}
