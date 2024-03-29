package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;

public class ModelTameDragonRed extends ModelDragonRed<EntityTameDragonRed> {

	
	public ModelTameDragonRed() {
		super();
	}

	
	@Override
	public void render(EntityTameDragonRed entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.scalef(.6f, .6f, .6f);
		
		float age = entity.getGrowingAge();
		final float growthMod = .6f + .4f * age;
		GlStateManager.scalef(growthMod, growthMod, growthMod);
		
		// 1f makes full-grown touch the ground.
		// .4 is 40%, which is max we shrink by.
		// 2.76 is size of full-grown tamed red ragons.
		GlStateManager.translatef(0f, 1f + ((.4f * entity.getHeight()) * (1f - age)), 0f);
		
		
		super.render(entity, time, swingProgress, swing, headAngleY, headAngleX, scale);
		
		GlStateManager.color3f(1f, 0, 0);
		GlStateManager.popMatrix();
	}

}
