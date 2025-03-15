package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.client.model.ModelShadowDragonRed;
import com.smanzana.nostrummagica.entity.dragon.ShadowRedDragonEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;

public class RenderShadowDragonRed extends RenderDragonRed<ShadowRedDragonEntity> {

	public RenderShadowDragonRed(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelShadowDragonRed(), shadowSizeIn);
	}
	
	@Override
	protected void scale(ShadowRedDragonEntity entityIn, MatrixStack matrixStackIn, float partialTicks) {
		super.scale(entityIn, matrixStackIn, partialTicks);
		
		matrixStackIn.scale(.5f, .5f, .5f);
	}

}
