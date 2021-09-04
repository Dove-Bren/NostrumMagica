package com.smanzana.nostrummagica.entity.renderer;

import com.smanzana.nostrummagica.entity.EntityLux;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
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
		GlStateManager.translate(0, 1.5, 0);
		GlStateManager.translate(0, -entity.height / 2, 0);
		if (!lux.isRoosting()) {
			GlStateManager.rotate(getSwingRot(lux.getSwingProgress(time % 1)), 0, 0, 1);
		}
		GlStateManager.scale(.25, .25, .25);
		main.render(scale);
		GlStateManager.popMatrix();
		GlStateManager.color(1f, 1f, 1f, 1f);
	}
	
}
