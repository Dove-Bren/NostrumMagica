package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.entity.EntityLux;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.Entity;

public class ModelLux extends ModelBase {
	
	private ModelRenderer main;
	
	public ModelLux() {
		main = new ModelRenderer(this);
		main.setTextureSize(64, 64);
		
		main.addBox(-2f, -16f, -2f, 4, 32, 4);
		main.addBox(-4, -2, -4, 8, 4, 8);
		
	}
	
	private float getSwingRot(float swingProgress) {
		return (float) (Math.sin(Math.PI * 2 * swingProgress) * 30.0);
	}
	
	@Override
	public void render(Entity entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		final EntityLux lux = (EntityLux) entity;
		
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, 1.5, 0);
		GlStateManager.translatef(0, -entity.height / 2, 0);
		if (!lux.isRoosting()) {
			GlStateManager.rotatef(getSwingRot(lux.getSwingProgress(time % 1)), 0, 0, 1);
		}
		GlStateManager.scalef(.25, .25, .25);
		main.render(scale);
		GlStateManager.popMatrix();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
	}
	
}
