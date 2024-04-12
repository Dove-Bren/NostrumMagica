package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.items.MagicArmor;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class ModelDragonFlightWings<T extends LivingEntity> extends EntityModel<T> {

	private /* final */ RendererModel rightWing;
	private final RendererModel leftWing;
	
	private static final <T extends LivingEntity> RendererModel createWing(EntityModel<T> base) {
		RendererModel wing = new RendererModel(base);
		wing.setRotationPoint(0.0F, 0, 0.0F);
		wing.setTextureOffset(1, 10).addBox(-19, -13, 0, 19, 2, 1, false);
		wing.setTextureOffset(3, 8).addBox(-17, -14, 0, 15, 1, 1, false);
		wing.setTextureOffset(5, 6).addBox(-15, -15, 0, 11, 1, 1, false);
		wing.setTextureOffset(6, 4).addBox(-13, -16, 0, 8, 1, 1, false);
		wing.setTextureOffset(7, 2).addBox(-11, -17, 0, 5, 1, 1, false);
		wing.setTextureOffset(7, 0).addBox(-10, -18, 0, 4, 1, 1, false);
		wing.setTextureOffset(0, 13).addBox(-21, -11, 0, 22, 2, 1, false);
		wing.setTextureOffset(0, 16).addBox(-22, -9, 0, 23, 3, 1, false);
		wing.setTextureOffset(2, 20).addBox(-3, -6, 0, 2, 1, 1, false);
		wing.setTextureOffset(8, 20).addBox(-8, -6, 0, 3, 1, 1, false);
		wing.setTextureOffset(16, 20).addBox(-23, -6, 0, 12, 1, 1, false);
		wing.setTextureOffset(17, 22).addBox(-23, -5, 0, 11, 1, 1, false);
		wing.setTextureOffset(18, 24).addBox(-16, -4, 0, 4, 1, 1, false);
		wing.setTextureOffset(18, 26).addBox(-15, -3, 0, 3, 1, 1, false);
		wing.setTextureOffset(19, 28).addBox(-15, -2, 0, 2, 1, 1, false);
		wing.setTextureOffset(19, 30).addBox(-14, -1, 0, 1, 1, 1, false);
		wing.setTextureOffset(33, 30).addBox(-23, -1, 0, 1, 1, 1, false);
		wing.setTextureOffset(28, 24).addBox(-23, -4, 0, 4, 1, 1, false);
		wing.setTextureOffset(30, 26).addBox(-23, -3, 0, 3, 1, 1, false);
		wing.setTextureOffset(31, 28).addBox(-23, -2, 0, 2, 1, 1, false);
		wing.setTextureOffset(3, 22).addBox(-3, -5, 0, 1, 1, 1, false);
		wing.setTextureOffset(9, 22).addBox(-7, -5, 0, 1, 1, 1, false);
		return wing;
	}

	public ModelDragonFlightWings() {
		textureWidth = 64;
		textureHeight = 64;
		this.rightWing = createWing(this);
		this.leftWing = createWing(this);
		this.leftWing.mirror = true;
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
	{
//		{
//			this.rightWing = createWing(this);
//		}
		GlStateManager.disableRescaleNormal();
		//GlStateManager.disableCull();
		this.rightWing.render(scale);
		this.leftWing.render(scale);
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	@Override
	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
		
		final float livingJitterPeriod = 100f;
		// 0f - 1f
		final float livingJitterPerc = (ageInTicks % livingJitterPeriod) / livingJitterPeriod;
		// -1f - 1f (sin)
		final float livingJitterMod = MathHelper.sin(3.1415f * 2 * livingJitterPerc);
		
		final float wingFlapPerc = MagicArmor.GetWingFlap((LivingEntity) entityIn, ageInTicks - (int) ageInTicks);
		final float wingFlapMod = MathHelper.sin(3.1415f * 2 * wingFlapPerc);
		
		rightWing.rotateAngleX = rightWing.rotateAngleZ = 0;
		rightWing.offsetY = entityIn.getHeight() * .3f;
		rightWing.offsetX = -entityIn.getWidth() * .25f;
		rightWing.offsetZ = entityIn.getWidth() * .3f;
		if (entityIn instanceof LivingEntity && ((LivingEntity)entityIn).isElytraFlying()) {
			rightWing.rotateAngleY = (float) (Math.PI * (.20 + livingJitterMod * .005 + wingFlapMod * .25));
		} else {
			// Standing still
			rightWing.rotateAngleY = (float) (Math.PI * (.25 + livingJitterMod * .005));
		}
		
		//rightWing.rotateAngleY = (float) (Math.PI * (.20 + livingJitterMod * .005 + -.3));
		
		leftWing.rotateAngleX = rightWing.rotateAngleX;
		leftWing.rotateAngleY = (float) (-rightWing.rotateAngleY + Math.PI);
		leftWing.rotateAngleZ = rightWing.rotateAngleZ;
		leftWing.offsetX = -rightWing.offsetX;
		leftWing.offsetY = rightWing.offsetY;
		leftWing.offsetZ = rightWing.offsetZ;
		
//		float f = 0.2617994F;
//		float f1 = -0.2617994F;
//		float f2 = 0.0F;
//		float f3 = 0.0F;
//
//		if (entityIn instanceof LivingEntity && ((LivingEntity)entityIn).isElytraFlying())
//		{
//			float f4 = 1.0F;
//
//			if (entityIn.getMotion().y < 0.0D)
//			{
//				Vector3d Vector3d = (new Vector3d(entityIn.getMotion().x, entityIn.getMotion().y, entityIn.getMotion().z)).normalize();
//				f4 = 1.0F - (float)Math.pow(-Vector3d.yCoord, 1.5D);
//			}
//
//			f = f4 * 0.34906584F + (1.0F - f4) * f;
//			f1 = f4 * -((float)Math.PI / 2F) + (1.0F - f4) * f1;
//		}
//		else if (entityIn.isSneaking())
//		{
//			f = ((float)Math.PI * 2F / 9F);
//			f1 = -((float)Math.PI / 4F);
//			f2 = 3.0F;
//			f3 = 0.08726646F;
//		}
//
//		this.leftWing.rotationPointX = 5.0F;
//		this.leftWing.rotationPointY = f2;
//
//		if (entityIn instanceof AbstractClientPlayer)
//		{
//			AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer)entityIn;
//			abstractclientplayer.rotateElytraX = (float)((double)abstractclientplayer.rotateElytraX + (double)(f - abstractclientplayer.rotateElytraX) * 0.1D);
//			abstractclientplayer.rotateElytraY = (float)((double)abstractclientplayer.rotateElytraY + (double)(f3 - abstractclientplayer.rotateElytraY) * 0.1D);
//			abstractclientplayer.rotateElytraZ = (float)((double)abstractclientplayer.rotateElytraZ + (double)(f1 - abstractclientplayer.rotateElytraZ) * 0.1D);
//			this.leftWing.rotateAngleX = abstractclientplayer.rotateElytraX;
//			this.leftWing.rotateAngleY = abstractclientplayer.rotateElytraY;
//			this.leftWing.rotateAngleZ = abstractclientplayer.rotateElytraZ;
//		}
//		else
//		{
//			this.leftWing.rotateAngleX = f;
//			this.leftWing.rotateAngleZ = f1;
//			this.leftWing.rotateAngleY = f3;
//		}
//
//		this.rightWing.rotationPointX = -this.leftWing.rotationPointX;
//		this.rightWing.rotateAngleY = -this.leftWing.rotateAngleY;
//		this.rightWing.rotationPointY = this.leftWing.rotationPointY;
//		this.rightWing.rotateAngleX = this.leftWing.rotateAngleX;
//		this.rightWing.rotateAngleZ = -this.leftWing.rotateAngleZ;
	}

	/**
	 * Used for easily adding entity-dependent animations. The second and third float params here are the same second
	 * and third as in the setRotationAngles method.
	 */
	public void setLivingAnimations(T entitylivingbaseIn, float p_78086_2_, float p_78086_3_, float partialTickTime) {
		super.setLivingAnimations(entitylivingbaseIn, p_78086_2_, p_78086_3_, partialTickTime);
	}
	
}
