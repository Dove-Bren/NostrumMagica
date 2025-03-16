package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public abstract class RenderInvisibleMultiPartEntity<T extends Entity> extends EntityRenderer<T> {

	public RenderInvisibleMultiPartEntity(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn);
	}
	
	@Override
	public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		// Don't actually render anything
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
