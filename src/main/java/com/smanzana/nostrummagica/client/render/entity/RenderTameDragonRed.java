package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;

import net.minecraft.client.renderer.entity.EntityRendererManager;

public class RenderTameDragonRed extends RenderDragonRed<EntityTameDragonRed> {

	public RenderTameDragonRed(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelTameDragonRed(), shadowSizeIn);
	}

}
