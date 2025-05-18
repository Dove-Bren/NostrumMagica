package com.smanzana.nostrummagica.client.model;

import com.smanzana.nostrummagica.entity.dragon.ShadowRedDragonEntity;

public class ShadowDragonRedModel extends DragonRedModel<ShadowRedDragonEntity> {

	
	public ShadowDragonRedModel() {
		super(0x40808080);
	}

	int unused; // Delete me?
	
//	@Override
//	public void render(EntityShadowDragonRed entity, float time, float swingProgress,
//			float swing, float headAngleY, float headAngleX, float scale) {
//		GlStateManager.pushMatrix();
//		GlStateManager.enableBlend();
//		GlStateManager.enableAlphaTest();
//		GlStateManager.scalef(.5f, .5f, .5f);
//		GlStateManager.translatef(0f, 1.2f, 0f);
//		
//		super.render(entity, time, swingProgress, swing, headAngleY, headAngleX, scale);
//		
//		GlStateManager.popMatrix();
//	}

}
