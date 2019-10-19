package com.smanzana.nostrummagica.entity.renderer;

import com.smanzana.nostrummagica.entity.EntityTameDragonRed;

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
		
		float age = 1f;
		
		if (entity instanceof EntityTameDragonRed) {
			age = ((EntityTameDragonRed) entity).getGrowingAge();
			final float growthMod = .6f + .4f * age;
			GlStateManager.scale(growthMod, growthMod, growthMod);
		}
		
		// 1f makes full-grown touch the ground.
		// .4 is 40%, which is max we shrink by.
		// 2.76 is size of full-grown tamed red ragons.
		GlStateManager.translate(0f, 1f + ((.4f * entity.height) * (1f - age)), 0f);
		
		
		super.render(entity, time, swingProgress, swing, headAngleY, headAngleX, scale);
		
		GlStateManager.color(1f, 0, 0);
		GlStateManager.popMatrix();
	}

}
