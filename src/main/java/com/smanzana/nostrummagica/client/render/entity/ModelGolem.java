package com.smanzana.nostrummagica.client.render.entity;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.entity.golem.EntityGolem;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;

public class ModelGolem<T extends EntityGolem> extends EntityModel<T> {

	private RendererModel head;
	private RendererModel body1;
	private RendererModel body2;
	private RendererModel body3;
	
	private static final int textureHeight = 64;
	private static final int textureWidth = 64;
	
	//															   1.4 circles per second
	private static final float rate1 = (float) (((2.0 * Math.PI) * 1.4) / 20.0f);
	private static final float rate2 = (float) (((2.0 * Math.PI) * 0.3) / 20.0f);
	private static final float rate3 = (float) (((2.0 * Math.PI) * 0.8) / 20.0f);
	
	public ModelGolem() {
		
		int centerX, centerZ;
		centerX = centerZ = 0;
		
		head = new RendererModel(this, 0, 0);
		head.addBox(-4, -5, -4, 8, 10, 8);
		head.setTextureSize(textureWidth, textureHeight);
		head.setRotationPoint(centerX, 5.0f, centerZ); // 34
		
		body1 = new RendererModel(this, 0, 0);
		body1.addBox(-6, -3, -2, 16, 6, 10);
		body1.setTextureSize(textureWidth, textureHeight);
		body1.setRotationPoint(centerX, 26.0f, centerZ); // 24
		
		body2 = new RendererModel(this, 0, 0);
		body2.addBox(-4, -4, -6, 10, 8, 8);
		body2.setTextureSize(textureWidth, textureHeight);
		body2.setRotationPoint(centerX, 17.0f, centerZ); // 14
		
		body3 = new RendererModel(this, 0, 0);
		body3.addBox(-6, -2, -4, 8, 4, 8);
		body3.setTextureSize(textureWidth, textureHeight);
		body3.setRotationPoint(centerX, 34.0f, centerZ); // 10
		
	}
	
	@Override
	public void render(T entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		setRotationAngles(entity, time, swingProgress, swing, headAngleY, headAngleX, scale);
		
		GL11.glPushMatrix();
		
		float modelScale = 1.0f;// / 20.0f; // 16 pixels wide model to .8 blocks
		GL11.glScalef(modelScale, modelScale * .5f, modelScale);
		
		head.render(scale);
		body1.render(scale);
		body2.render(scale);
		body3.render(scale);
		
		
		GL11.glPopMatrix();
	}
	
	@Override
	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		float ticks = entityIn.ticksExisted;
		
		float speedup = 1.0f;
		if (((EntityGolem) entityIn).getAttackTarget() != null)
			speedup = 2.0f;
		
		body1.rotateAngleY = ticks * -rate1 * speedup;
		body2.rotateAngleY = ticks * rate2 * speedup;
		body3.rotateAngleY = ticks * rate3 * speedup;
	}
	
}
