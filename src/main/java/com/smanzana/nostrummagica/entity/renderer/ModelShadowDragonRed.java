package com.smanzana.nostrummagica.entity.renderer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class ModelShadowDragonRed extends ModelDragonRed {

	
	public ModelShadowDragonRed() {
		super(0x80808080);
	}

	
	@Override
	public void render(Entity entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.scale(.5, .5, .5);
		GlStateManager.translate(0f, 1.2f, 0f);
		
		super.render(entity, time, swingProgress, swing, headAngleY, headAngleX, scale);
		
		GlStateManager.popMatrix();
	}

}
