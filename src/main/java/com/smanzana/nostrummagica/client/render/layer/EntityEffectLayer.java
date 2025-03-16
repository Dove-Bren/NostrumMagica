package com.smanzana.nostrummagica.client.render.layer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.render.IEffectRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;

public class EntityEffectLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	
	public EntityEffectLayer(LivingEntityRenderer<T, M> rendererIn) {
		super(rendererIn);
	}
	
	@Override
	public void render(PoseStack stack, MultiBufferSource typeBuffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (shouldRender(entity)) {
			for (MobEffectInstance effect : entity.getActiveEffects()) {
				@Nullable IEffectRenderer renderer = IEffectRenderer.GetRenderer(effect.getEffect());
				if (renderer != null) {
					renderer.renderEffectOnEntity(effect, stack, typeBuffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
				}
			}
		}
	}
	
	public boolean shouldRender(T entity) {
		return !entity.isSpectator();
	}
}
