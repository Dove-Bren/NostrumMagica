package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.entity.EntityLux;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;

public class ModelLux extends EntityModel<EntityLux> {
	
	private RendererModel main;
	
	public ModelLux() {
		main = new RendererModel(this);
		main.setTextureSize(64, 64);
		
		main.addBox(-2f, -16f, -2f, 4, 32, 4);
		main.addBox(-4, -2, -4, 8, 4, 8);
		
	}
	
	private float getSwingRot(float swingProgress) {
		return (float) (Math.sin(Math.PI * 2 * swingProgress) * 30.0);
	}
	
	@Override
	public void render(EntityLux entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		final EntityLux lux = (EntityLux) entity;
		
		GlStateManager.pushMatrix();
		GlStateManager.translated(0, 1.5, 0);
		GlStateManager.translatef(0, -entity.getHeight() / 2, 0);
		if (!lux.isRoosting()) {
			GlStateManager.rotatef(getSwingRot(lux.getSwingProgress(time % 1)), 0, 0, 1);
		}
		GlStateManager.scaled(.25, .25, .25);
		main.render(scale);
		GlStateManager.popMatrix();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
	}
	
}
