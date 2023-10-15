package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;

public class ModelPlantBoss extends EntityModel<EntityPlantBoss> {
	
	private RendererModel body;
	//private RendererModel northFrond; etc
	// private RendererModel centerTree;
	
	public ModelPlantBoss() {
		this.textureHeight = 256;
		this.textureWidth = 256;
		body = new RendererModel(this, 0, 0);
		body.addBox(-24f, -24f, -24f, 48, 48, 48);
	}
	
	@Override
	public void render(EntityPlantBoss entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.scalef(1f, 1f, 1f);
		GlStateManager.translatef(0, 0, 0);
		body.render(scale);
		GlStateManager.popMatrix();
	}
	
	@Override
	public void setLivingAnimations(EntityPlantBoss entityIn, float limbSwing, float limbSwingAmount, float partialTicks) {
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTicks);
	}
	
	@Override
	public void setRotationAngles(EntityPlantBoss entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
	}
}
