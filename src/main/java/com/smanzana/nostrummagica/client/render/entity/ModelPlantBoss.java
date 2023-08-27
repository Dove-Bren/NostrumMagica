package com.smanzana.nostrummagica.client.render.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class ModelPlantBoss extends ModelBase {
	
	private ModelRenderer body;
	//private ModelRenderer northFrond; etc
	// private ModelRenderer centerTree;
	
	public ModelPlantBoss() {
		this.textureHeight = 256;
		this.textureWidth = 256;
		body = new ModelRenderer(this, 0, 0);
		body.addBox(-24f, -24f, -24f, 48, 48, 48);
	}
	
	@Override
	public void render(Entity entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.scalef(1f, 1f, 1f);
		GlStateManager.translatef(0, 0, 0);
		body.render(scale);
		GlStateManager.popMatrix();
	}
	
	@Override
	public void setLivingAnimations(LivingEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks) {
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTicks);
	}
	
	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
	}
}
