package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.entity.dragon.EntityShadowDragonRed;

import net.minecraft.client.renderer.entity.EntityRendererManager;

public class RenderShadowDragonRed extends RenderDragonRed<EntityShadowDragonRed> {

	public RenderShadowDragonRed(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelShadowDragonRed(), shadowSizeIn);
	}

}
