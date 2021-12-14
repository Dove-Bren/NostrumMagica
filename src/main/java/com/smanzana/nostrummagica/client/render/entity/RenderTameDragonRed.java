package com.smanzana.nostrummagica.client.render.entity;

import net.minecraft.client.renderer.entity.RenderManager;

public class RenderTameDragonRed extends RenderDragonRed {

	public RenderTameDragonRed(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelTameDragonRed(), shadowSizeIn);
	}

}
