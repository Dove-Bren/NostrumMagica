package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public abstract class RenderInvisibleMultiPartEntity<T extends Entity> extends EntityRenderer<T> {

	public RenderInvisibleMultiPartEntity(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}
	
	@Override
	public void render(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		// Don't actually render anything
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
