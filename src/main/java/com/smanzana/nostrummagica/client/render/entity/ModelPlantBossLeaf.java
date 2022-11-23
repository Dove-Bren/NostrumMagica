package com.smanzana.nostrummagica.client.render.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class ModelPlantBossLeaf extends ModelBase {
	
	private ModelRenderer main;
	
	public ModelPlantBossLeaf() {
		
		this.textureHeight = 256;
		this.textureWidth = 256;
		main = new ModelRenderer(this, 92, 250);
		
		main.setTextureOffset(92, 250);
		main.addBox(-16, -4, 0, 32, 2, 4);
		
		main.setTextureOffset(19, 199);
		main.addBox(-30, -4, 4, 60, 2, 49);

		// bottom
		main.setTextureOffset(38, 199);
		main.addBox(-4, -2, 4, 8, 2, 49).mirror = true;
		
		main.setTextureOffset(71, 190);
		main.addBox(-26, -4, 53, 52, 2, 5);
		
		main.setTextureOffset(81, 183);
		main.addBox(-22, -4, 58, 44, 2, 3);
		
		main.setTextureOffset(100, 177);
		main.addBox(-12, -4, 61, 24, 2, 4);
		
		main.setTextureOffset(194, 2254);
		main.addBox(-32, -4, 8, 2, 2, 29);
		
		main.setTextureOffset(0, 225);
		main.addBox(30, -4, 8, 2, 2, 29);
	}
	
	@Override
	public void render(Entity entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.scale(1f, 1f, 1f);
		GlStateManager.translate(0, 0, 0);
		GlStateManager.rotate(90f, 0, 1, 0);
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
