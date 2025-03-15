package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelLux;
import com.smanzana.nostrummagica.entity.LuxEntity;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderLux extends MobRenderer<LuxEntity, ModelLux> {

	public RenderLux(EntityRendererManager renderManagerIn, float scale) {
		super(renderManagerIn, new ModelLux(), .33f);
	}
	
	@Override
	public void render(LuxEntity lux, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		this.model = new ModelLux();
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, (-lux.getBbHeight() / 4), 0);
		matrixStackIn.scale(.25f, .25f, .25f);
		
		super.render(lux, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		matrixStackIn.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(LuxEntity entity) {
		return new ResourceLocation(NostrumMagica.MODID, "textures/entity/sprite_core.png");
	}
	
}
