package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;

import net.minecraft.client.model.ModelWolf;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.entity.model.WolfModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ModelArcaneWolf extends WolfModel<EntityArcaneWolf> {
	
	private RendererModel wolfHeadSnootBridge;
	
	public ModelArcaneWolf(int color) {
		//super(); Don't bother creating parent model
		
		// package-protected versions mean we have to use reflection to set them
		RendererModel wolfMane;
		RendererModel wolfTail;
		
		this.wolfHeadMain = new RendererModel(this, 0, 0) {
			@Override
			public void renderWithRotation(float scale) {
				super.render(scale);
			}
		};
		this.wolfHeadMain.addBox(-2.0F, -3.0F, -2.0F, 6, 6, 4, 0.0F);
		this.wolfHeadMain.setRotationPoint(-1.0F, 13.5F, -7.0F);
		this.wolfHeadMain.setTextureOffset(16, 14).addBox(-1.999F, -4.5F, 0.0F, 2, 2, 1, 0.0F); // Ear
		this.wolfHeadMain.setTextureOffset(16, 11).addBox(-1.5F, -5.25F, -0.001F, 1, 1, 1, 0.0F); // EarTop TODO texture
		this.wolfHeadMain.setTextureOffset(16, 14).addBox(1.999F, -4.5F, 0.0F, 2, 2, 1, 0.0F); // Ear
		this.wolfHeadMain.setTextureOffset(16, 11).addBox(2.5F, -5.25F, -0.001F, 1, 1, 1, 0.0F); // EarTop TODO texture
		this.wolfHeadMain.setTextureOffset(0, 10).addBox(-0.5F, 0.0F, -5.0F, 3, 3, 4, 0.0F); // Snoot
		this.wolfHeadMain.setTextureOffset(42, 13).addBox(-2.5F, -1.5F, -1.5F, 1, 4, 3, 0.0F); // Right face floof // TODO texture
		this.wolfHeadMain.setTextureOffset(42, 13).addBox(3.5F, -1.5F, -1.5F, 1, 4, 3, 0.0F); // Left face floof // TODO texture
		//this.wolfHeadMain.setTextureOffset(18, 14).addBox(0.5F, -1.0F, -4.0F, 1, 2, 2, 0.0F); // SnootBridge
		wolfHeadSnootBridge = new RendererModel(this, 23, 14);
		wolfHeadSnootBridge.offsetX = 0;
		wolfHeadSnootBridge.offsetY = .016F;
		wolfHeadSnootBridge.offsetZ = -.075F;
		wolfHeadSnootBridge.setRotationPoint(0.5F, -0.5F, 0F);
		wolfHeadSnootBridge.addBox(0, 0, 0, 1, 1, 2, 0.0F); // SnootBridge
		wolfHeadSnootBridge.rotateAngleX = 10f;
		wolfHeadMain.addChild(wolfHeadSnootBridge);
		this.wolfBody = new RendererModel(this, 18, 14);
		this.wolfBody.addBox(-3.0F, -2.0F, -3.0F, 6, 9, 6, 0.0F);
		this.wolfBody.setRotationPoint(0.0F, 14.0F, 2.0F);
		this.wolfBody.setTextureOffset(21, 0).addBox(-3.5F, 1.5F, -3.5F, 7, 5, 7, 0.0F); // Butt floof // TODO texture
		
		
		wolfMane = new RendererModel(this, 21, 0);
		wolfMane.addBox(-3.0F, -3.0F, -3.0F, 8, 6, 7, 0.0F);
		wolfMane.setRotationPoint(-1.0F, 14.0F, 2.0F);
		this.wolfLeg1 = new RendererModel(this, 0, 18);
		this.wolfLeg1.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		this.wolfLeg1.setRotationPoint(-2.5F, 16.0F, 7.0F);
		this.wolfLeg2 = new RendererModel(this, 0, 18);
		this.wolfLeg2.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		this.wolfLeg2.setRotationPoint(0.5F, 16.0F, 7.0F);
		this.wolfLeg3 = new RendererModel(this, 0, 18);
		this.wolfLeg3.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		this.wolfLeg3.setRotationPoint(-2.5F, 16.0F, -4.0F);
		this.wolfLeg4 = new RendererModel(this, 0, 18);
		this.wolfLeg4.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		this.wolfLeg4.setRotationPoint(0.5F, 16.0F, -4.0F);
		wolfTail = new RendererModel(this, 9, 18);
		wolfTail.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		wolfTail.setRotationPoint(-1.0F, 12.0F, 8.0F);
		
		try {
			ObfuscationReflectionHelper.setPrivateValue(ModelWolf.class, this, wolfMane, "field_78186_h"); //wolfMane
			ObfuscationReflectionHelper.setPrivateValue(ModelWolf.class, this, wolfTail, "field_78180_g"); //wolfTail
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ModelArcaneWolf() {
		this(-1);
	}
	
	@Override
	public void render(Entity entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.scalef(1.25f, 1.25f, 1.25f);
		GlStateManager.translatef(0, (-8f) / 24f, 0);
		super.render(entity, time, swingProgress, swing, headAngleY, headAngleX, scale);
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
