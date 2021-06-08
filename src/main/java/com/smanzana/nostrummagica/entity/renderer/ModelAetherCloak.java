package com.smanzana.nostrummagica.entity.renderer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class ModelAetherCloak extends ModelBase {

	private final ModelOBJ model;
	
	public ModelAetherCloak(ResourceLocation capeModel, int textureWidth, int textureHeight) {
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.model = new ModelOBJ() {

			@Override
			protected ResourceLocation[] getEntityModels() {
				return new ResourceLocation[] {
						capeModel
					};
			}

			@Override
			protected boolean preRender(Entity entity, int model, VertexBuffer buffer, double x, double y, double z,
					float entityYaw, float partialTicks) {
				
				
				return true;
			}
			
		};
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		final float objScale = .425f;
		final boolean isFlying = (entityIn instanceof EntityLivingBase && ((EntityLivingBase)entityIn).isElytraFlying());
		final boolean hasChestpiece = (entityIn instanceof EntityLivingBase
				&& ((EntityLivingBase)entityIn).getItemStackFromSlot(EntityEquipmentSlot.CHEST) != null);
		
		// Get how 'forward' we're moving for cape rotation
		Vec3d look = entityIn.getLook(ageInTicks % 1f);
		double motionForward = look
				.subtract(0, look.yCoord, 0)
				.dotProduct(new Vec3d(entityIn.motionX, 0, entityIn.motionZ));
		float rot = -10f;
		final float moveMaxRot = (!isFlying && motionForward > 0 ? -20f : 10f);
		//final double yVelOverride = .25;
		final float cloakAffectVelocity = limbSwingAmount;
				// Imagine your cape moving realisitically depending on if you were going up or down 
				//(entityIn.motionY < -yVelOverride ? -1f : (entityIn.motionY > yVelOverride ? 1f : limbSwingAmount));
		rot += (cloakAffectVelocity * moveMaxRot); // Add amount for how fast we're moving
		if (entityIn.isSneaking()) {
			rot -= 30;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.rotate(180f, 0, 1, 0);
		GlStateManager.scale(objScale, objScale, objScale);
		GlStateManager.translate(0, entityIn.isSneaking() ? .4 : 0, hasChestpiece ? -.15 : 0);
		GlStateManager.rotate(rot, 1, 0, 0);
		model.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		GlStateManager.popMatrix();
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		
	}

	/**
	 * Used for easily adding entity-dependent animations. The second and third float params here are the same second
	 * and third as in the setRotationAngles method.
	 */
	public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float p_78086_2_, float p_78086_3_, float partialTickTime) {
		super.setLivingAnimations(entitylivingbaseIn, p_78086_2_, p_78086_3_, partialTickTime);
		model.setLivingAnimations(entitylivingbaseIn, p_78086_2_, p_78086_3_, partialTickTime);
	}
	
}
