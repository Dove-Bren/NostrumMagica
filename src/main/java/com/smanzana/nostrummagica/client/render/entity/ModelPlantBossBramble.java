package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBossBramble;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;

public class ModelPlantBossBramble extends EntityModel<EntityPlantBossBramble> {
	
	private RendererModel main;
	
	public ModelPlantBossBramble() {
		
		// Render expects model to be for 5 blocks
		//final float width = 5 * 16;
		
		this.textureHeight = 256;
		this.textureWidth = 256;
		main = new RendererModel(this, 0, 0);
		
		// Main horizontal stretch
		main.setTextureOffset(0, 157);
		main.addBox(-40, 16, 0, 80, 12, 8);
		
		// Leg 1
		main.setTextureOffset(192, 139);
		main.addBox(-48, 16, 0, 8, 32, 8);
		
		// Leg 2
		main.setTextureOffset(192, 139);
		main.addBox(40, 16, 0, 8, 32, 8).mirror = true;
	}
	
	@Override
	public void render(EntityPlantBossBramble entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.scalef(1f, 1f, 1f);
		GlStateManager.translatef(0, 0, 0);
		//GlStateManager.rotatef(90f, 0, 1, 0);
		main.render(scale);
		GlStateManager.popMatrix();
	}
	
	@Override
	public void setLivingAnimations(EntityPlantBossBramble entityIn, float limbSwing, float limbSwingAmount, float partialTicks) {
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTicks);
	}
	
	@Override
	public void setRotationAngles(EntityPlantBossBramble entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
	}
}
