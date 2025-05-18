package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.model.ShadowDragonRedModel;
import com.smanzana.nostrummagica.entity.dragon.ShadowRedDragonEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ShadowDragonRedRenderer extends DragonRedRenderer<ShadowRedDragonEntity> {

	public ShadowDragonRedRenderer(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ShadowDragonRedModel(), shadowSizeIn);
	}
	
	@Override
	protected void scale(ShadowRedDragonEntity entityIn, PoseStack matrixStackIn, float partialTicks) {
		super.scale(entityIn, matrixStackIn, partialTicks);
		
		matrixStackIn.scale(.5f, .5f, .5f);
	}

}
