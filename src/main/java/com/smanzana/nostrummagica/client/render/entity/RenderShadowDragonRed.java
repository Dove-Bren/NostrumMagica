package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.entity.dragon.EntityShadowDragonRed;

import net.minecraft.client.renderer.entity.EntityRendererManager;

public class RenderShadowDragonRed extends RenderDragonRed<EntityShadowDragonRed> {

	public RenderShadowDragonRed(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelShadowDragonRed(), shadowSizeIn);
	}
	
	@Override
	protected void preRenderCallback(EntityShadowDragonRed entityIn, MatrixStack matrixStackIn, float partialTicks) {
		super.preRenderCallback(entityIn, matrixStackIn, partialTicks);
		
		matrixStackIn.scale(.5f, .5f, .5f);
	}

}
