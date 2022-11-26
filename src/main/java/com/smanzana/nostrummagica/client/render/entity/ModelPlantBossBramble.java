package com.smanzana.nostrummagica.client.render.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class ModelPlantBossBramble extends ModelBase {
	
	private ModelRenderer main;
	
	public ModelPlantBossBramble() {
		
		// Render expects model to be for 5 blocks
		//final float width = 5 * 16;
		
		this.textureHeight = 256;
		this.textureWidth = 256;
		main = new ModelRenderer(this, 0, 0);
		
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
	public void render(Entity entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.scale(1f, 1f, 1f);
		GlStateManager.translate(0, 0, 0);
		//GlStateManager.rotate(90f, 0, 1, 0);
		main.render(scale);
		GlStateManager.popMatrix();
	}
	
	@Override
	public void setLivingAnimations(EntityLivingBase entityIn, float limbSwing, float limbSwingAmount, float partialTicks) {
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTicks);
	}
	
	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
	}
}
