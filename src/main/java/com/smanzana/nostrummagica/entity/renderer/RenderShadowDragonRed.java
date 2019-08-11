package com.smanzana.nostrummagica.entity.renderer;

import net.minecraft.client.renderer.entity.RenderManager;

public class RenderShadowDragonRed extends RenderDragonRed {

	public RenderShadowDragonRed(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelShadowDragonRed(), shadowSizeIn);
	}

}
