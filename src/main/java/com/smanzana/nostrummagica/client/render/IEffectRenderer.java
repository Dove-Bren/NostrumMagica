package com.smanzana.nostrummagica.client.render;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IEffectRenderer {

	@OnlyIn(Dist.CLIENT)
	public void renderEffectOnEntity(EffectInstance effect, MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch);
	
	
	
	
	public static final Map<Effect, IEffectRenderer> REGISTRY = new HashMap<>();
	public static void RegisterRenderer(Effect effect, IEffectRenderer renderer) {
		REGISTRY.put(effect, renderer);
	}
	
	public static @Nullable IEffectRenderer GetRenderer(Effect effect) {
		return REGISTRY.get(effect);
	}
}
