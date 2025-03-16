package com.smanzana.nostrummagica.client.render;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IEffectRenderer {

	@OnlyIn(Dist.CLIENT)
	public void renderEffectOnEntity(MobEffectInstance effect, PoseStack stack, MultiBufferSource typeBuffer, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch);
	
	
	
	
	public static final Map<MobEffect, IEffectRenderer> REGISTRY = new HashMap<>();
	public static void RegisterRenderer(MobEffect effect, IEffectRenderer renderer) {
		REGISTRY.put(effect, renderer);
	}
	
	public static @Nullable IEffectRenderer GetRenderer(MobEffect effect) {
		return REGISTRY.get(effect);
	}
}
