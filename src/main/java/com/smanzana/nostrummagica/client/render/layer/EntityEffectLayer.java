package com.smanzana.nostrummagica.client.render.layer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.client.render.IEffectRenderer;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;

public class EntityEffectLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
	
	public EntityEffectLayer(LivingRenderer<T, M> rendererIn) {
		super(rendererIn);
	}
	
	@Override
	public void render(MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (shouldRender(entity)) {
			for (EffectInstance effect : entity.getActivePotionEffects()) {
				@Nullable IEffectRenderer renderer = IEffectRenderer.GetRenderer(effect.getPotion());
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
