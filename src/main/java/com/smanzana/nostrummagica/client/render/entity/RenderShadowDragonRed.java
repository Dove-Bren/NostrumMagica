package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.model.ModelShadowDragonRed;
import com.smanzana.nostrummagica.entity.dragon.ShadowRedDragonEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class RenderShadowDragonRed extends RenderDragonRed<ShadowRedDragonEntity> {

	public RenderShadowDragonRed(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelShadowDragonRed(), shadowSizeIn);
	}
	
	@Override
	protected void scale(ShadowRedDragonEntity entityIn, PoseStack matrixStackIn, float partialTicks) {
		super.scale(entityIn, matrixStackIn, partialTicks);
		
		matrixStackIn.scale(.5f, .5f, .5f);
	}

}
