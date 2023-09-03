package com.smanzana.nostrummagica.client.render.entity;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.items.ICapeProvider;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class ModelAetherCloak extends ModelBase {

	private final IBakedModel[] models;
	
	public ModelAetherCloak(IBakedModel[] models, int textureWidth, int textureHeight) {
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.models = models;
	}
	
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		renderEx(entityIn, null, null, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	public void renderEx(Entity entityIn, @Nullable ICapeProvider provider, @Nullable ItemStack stack,
			float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
			float scale) {
		if (!(entityIn instanceof LivingEntity)) {
			return;
		}
		
		final LivingEntity living = (LivingEntity) entityIn;
		final float objScale = .425f;
		final boolean isFlying = living.isElytraFlying();
		final boolean hasChestpiece = (!living.getItemStackFromSlot(EquipmentSlotType.CHEST).isEmpty());
		final @Nullable ResourceLocation[] textures = provider.getCapeTextures(living, stack);
		
		// Get how 'forward' we're moving for cape rotation
		Vec3d look = entityIn.getLook(ageInTicks % 1f);
		double motionForward = look
				.subtract(0, look.y, 0)
				.dotProduct(new Vec3d(entityIn.getMotion().x, 0, entityIn.getMotion().z));
		float rot = -10f;
		final float moveMaxRot = (!isFlying && motionForward > 0 ? -20f : 10f);
		//final double yVelOverride = .25;
		final float cloakAffectVelocity = limbSwingAmount;
				// Imagine your cape moving realisitically depending on if you were going up or down 
				//(entityIn.getMotion().y < -yVelOverride ? -1f : (entityIn.getMotion().y > yVelOverride ? 1f : limbSwingAmount));
		rot += (cloakAffectVelocity * moveMaxRot); // Add amount for how fast we're moving
		if (entityIn.isSneaking()) {
			rot -= 30;
		}
		
		GlStateManager.enableRescaleNormal();
		GlStateManager.disableCull();
		
		GlStateManager.pushMatrix();
		GlStateManager.scalef(objScale, -objScale, objScale);
		GlStateManager.translatef(0, entityIn.isSneaking() ? -.3 : 0, hasChestpiece ? .15 : 0);
		GlStateManager.rotatef(rot, 1, 0, 0);
		//GlStateManager.rotatef(180f, 1, 0, 0);
		//GlStateManager.rotatef(180f, 0, 1, 0);
		int index = 0;
		for (IBakedModel model : models) {
			GlStateManager.pushMatrix();
			
			final int color = provider.getColor(living, stack, index);
			final ResourceLocation texture = textures == null ? null : textures[index];
			if (texture != null) {
				Minecraft.getInstance().getTextureManager().bindTexture(texture);
			} else {
				// Default main texture -- for blocks and .objs
				Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
			}
			
			if (provider != null) {
				provider.preRender(entityIn, index, stack, netHeadYaw, ageInTicks % 1f);
			}
			RenderFuncs.RenderModelWithColor(model, color);
			GlStateManager.popMatrix();
			index++;
		}
		//model.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		GlStateManager.popMatrix();
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		//model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		
	}

	/**
	 * Used for easily adding entity-dependent animations. The second and third float params here are the same second
	 * and third as in the setRotationAngles method.
	 */
	public void setLivingAnimations(LivingEntity entitylivingbaseIn, float p_78086_2_, float p_78086_3_, float partialTickTime) {
		super.setLivingAnimations(entitylivingbaseIn, p_78086_2_, p_78086_3_, partialTickTime);
		//model.setLivingAnimations(entitylivingbaseIn, p_78086_2_, p_78086_3_, partialTickTime);
	}
	
}
