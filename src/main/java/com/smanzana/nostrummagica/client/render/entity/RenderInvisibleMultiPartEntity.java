package com.smanzana.nostrummagica.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public abstract class RenderInvisibleMultiPartEntity<T extends Entity> extends EntityRenderer<T> {

	public RenderInvisibleMultiPartEntity(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}
	
	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
		// Don't actually render anything
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	public ResourceLocation getEntityTexture(T entity) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
