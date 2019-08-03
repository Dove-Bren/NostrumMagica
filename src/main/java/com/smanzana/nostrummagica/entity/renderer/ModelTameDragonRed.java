package com.smanzana.nostrummagica.entity.renderer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class ModelTameDragonRed extends ModelDragonRed {

	
	public ModelTameDragonRed() {
		super();
	}

	
	@Override
	public void render(Entity entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.scale(.6, .6, .6);
		GlStateManager.translate(0f, 1f, 0f);
		
		super.render(entity, time, swingProgress, swing, headAngleY, headAngleX, scale);
		
		GlStateManager.popMatrix();
	}

}
