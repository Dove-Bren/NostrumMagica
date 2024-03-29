package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.entity.dragon.EntityShadowDragonRed;

public class ModelShadowDragonRed extends ModelDragonRed<EntityShadowDragonRed> {

	
	public ModelShadowDragonRed() {
		super(0x80808080);
	}

	
	@Override
	public void render(EntityShadowDragonRed entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.enableAlphaTest();
		GlStateManager.scalef(.5f, .5f, .5f);
		GlStateManager.translatef(0f, 1.2f, 0f);
		
		super.render(entity, time, swingProgress, swing, headAngleY, headAngleX, scale);
		
		GlStateManager.popMatrix();
	}

}
