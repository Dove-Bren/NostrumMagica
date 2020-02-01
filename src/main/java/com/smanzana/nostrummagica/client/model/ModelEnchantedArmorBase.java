package com.smanzana.nostrummagica.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ModelEnchantedArmorBase extends ModelBiped {
	
	private static final int TEXTURE_WIDTH = 64;
	private static final int TEXTURE_HEIGHT = 96;

	private ModelRenderer head;
	private ModelRenderer body;
	private ModelRenderer armLeft;
	private ModelRenderer armRight;
	private ModelRenderer legLeft;
	private ModelRenderer legRight;
	private ModelRenderer bootLeft;
	private ModelRenderer bootRight;
	
	// Level is number of other set pieces. 0 is base, 3 is max coolness.
	public ModelEnchantedArmorBase(float scale, int level) {
		super(scale, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		
		scale = 0.01f;
		
		head = new ModelRenderer(this, 0, 0);
		head.addBox(-5, -9, -5, 10, 10, 10, scale);
		head.setTextureOffset(0, 0);
		head.addBox(-6, -9, -2, 1, 5, 4, scale);
		head.setTextureOffset(30, 0);
		head.addBox(5, -9, -2, 1, 5, 4, scale);
		head.setTextureOffset(0, 20);
		head.addBox(-6, -10, -2, 12, 1, 4, scale);
		
		// Testing features -- physical/spikes/horns?
		if (level >= 1) {
			head.setTextureOffset(48, 6);
			head.addBox(-3, -10, -7, 1, 1, 4, scale);
			head.addBox(-3, -13, -5, 1, 3, 1, scale);
			head.addBox(-3, -14, -6, 1, 2, 1, scale);
			head.addBox(-3, -11, -9, 1, 1, 3, scale);
			head.addBox(-3, -12, -10, 1, 1, 2, scale);
			
			head.addBox(2, -10, -7, 1, 1, 4, scale);
			head.addBox(2, -13, -5, 1, 3, 1, scale);
			head.addBox(2, -14, -6, 1, 2, 1, scale);
			head.addBox(2, -11, -9, 1, 1, 3, scale);
			head.addBox(2, -12, -10, 1, 1, 2, scale);
		}
		
		// Testing features -- physical/spikes/tendrils?
		if (level >= 2) {
	        head.setTextureOffset(48, 0);
	        head.addBox(-10, -9, -1, 4, 1, 2, scale);
	        head.addBox(-10, -11, -1, 1, 2, 2, scale);
	        head.addBox(-8, -8, -1, 1, 1, 2, scale);
	
	        head.addBox(6, -9, -1, 4, 1, 2, scale);
	        head.addBox(9, -11, -1, 1, 2, 2, scale);
	        head.addBox(7, -8, -1, 1, 1, 2, scale);
		}
		
		if (level >= 3) {
			head.setTextureOffset(48, 0);
	        head.addBox(-4, -14, -1, 1, 4, 2, scale);
	        head.addBox(-5, -16, -1, 1, 3, 2, scale);
	        head.addBox(-3, -13, -1, 1, 1, 2, scale);
	        
	        head.addBox(3, -14, -1, 1, 4, 2, scale);
	        head.addBox(4, -16, -1, 1, 3, 2, scale);
	        head.addBox(2, -13, -1, 1, 1, 2, scale);
		}
		

		
		body = new ModelRenderer(this, 0, 27);
		body.addBox(-5.0F, 0F, -3.01F, 10, 12, 6);
		
		// Test feature growth 2
		if (level >= 2) {
			body.setTextureOffset(34, 54);
			body.addBox(-6, 9, -3, 1, 1, 6, scale);
			body.addBox(5, 9, -3, 1, 1, 6, scale);
			
			body.setTextureOffset(48, 54);
			body.addBox(-7, 10, -3, 2, 2, 6, scale);
			body.addBox(5, 10, -3, 2, 2, 6, scale);
		}
		
		// Test feature growth 3
		if (level >= 3) {
			body.setTextureOffset(34, 62);
			body.addBox(-6, 9, -4, 12, 1, 1, scale);
			body.addBox(-6, 9, 3, 12, 1, 1, scale);
			
			body.setTextureOffset(32, 64);
			body.addBox(-7, 10, -5, 14, 2, 2, scale);
			body.addBox(-7, 10, 3, 14, 2, 2, scale);
		}
		
		
		armLeft = new ModelRenderer(this, 0, 45);
		armLeft.addBox(-1F, -2.50F, -3F, 4, 9, 6, scale);
		armLeft.setTextureOffset(20, 45);
		armLeft.addBox(3.0F, -3.50F, -2F, 1, 3, 4, scale);
		
		// Test feature growth 1
		if (level >= 1) {
			armLeft.setTextureOffset(48, 11);
			armLeft.addBox(3, 1, 0, 2, 1, 1, scale);
			armLeft.addBox(3, 4, -1, 2, 1, 1, scale);
		}
		
		// test feature growth 2
		if (level >= 2) {
			body.setTextureOffset(34, 20);
			body.addBox(4, -1.5f, -4, 7, 2, 8, scale * 1.5f);
		}
		
		// test feature growth 3
		if (level >= 3) {
			body.setTextureOffset(34, 40);
			body.addBox(4, -2.5f, -3, 5, 1, 6, scale * 1.5f);
			body.setTextureOffset(50, 40);
			body.addBox(5, -3.5f, -1, 2, 1, 3, scale * 1.5f);
		}
		
		armRight = new ModelRenderer(this, 0, 45);
		armRight.addBox(-3.0F, -2.50F, -3F, 4, 9, 6, scale);
		armRight.setTextureOffset(20, 45);
		armRight.addBox(-4.0F, -3.50F, -2F, 1, 3, 4, scale);
		armRight.mirror = true;
		
		// Test feature growth 1
		if (level >= 1) {
			armRight.setTextureOffset(58, 11);
			armRight.addBox(-5, 1, 0, 2, 1, 1, scale);
			armRight.addBox(-5, 4, -1, 2, 1, 1, scale);
		}
		
		// test feature growth 2
		if (level >= 2) {
			body.setTextureOffset(34, 30);
			body.addBox(-11, -1.5f, -4, 7, 2, 8, scale * 1.5f);
		}
		
		// test feature growth 3
		if (level >= 3) {
			body.setTextureOffset(34, 47);
			body.addBox(-9, -2.5f, -3, 5, 1, 6, scale * 1.5f);
			body.setTextureOffset(50, 47);
			body.addBox(-7, -3.5f, -1, 2, 1, 3, scale * 1.5f);
			
			// Also cool bracelet thing
			armRight.setTextureOffset(46, 13);
			armRight.addBox(-4, 5, -4, 4, 1, 5, scale * 1.5f);
		}
		
		legLeft = new ModelRenderer(this, 0, 60);
		legLeft.addBox(-2F, -.95F, -3F, 5, 10, 6, scale);
		
		legRight = new ModelRenderer(this, 0, 60);
		legRight.addBox(-3F, -.99F, -3F, 5, 10, 6, scale);
		legRight.mirror = true;
		
		bootLeft = new ModelRenderer(this, 0, 76);
		bootLeft.addBox(-2F, 10F, -4F, 4, 2, 7, scale * .9f);
		bootLeft.setTextureOffset(0, 85);
		bootLeft.addBox(-2F, 7F, -3F, 5, 3, 6, scale * .9f);
		bootLeft.setTextureOffset(0, 78);
		bootLeft.addBox(0F, 8.4F, 3F, 2, 1, 1, scale * .9f);
		
		// test feature growth 2
		if (level >= 2) {
			bootLeft.setTextureOffset(34, 68);
			bootLeft.addBox(3, 8, -2, 1, 1, 4, scale * .9f);
			bootLeft.setTextureOffset(44, 68);
			bootLeft.addBox(4, 8, 0, 1, 1, 4, scale * .9f);
		}
		
		bootRight = new ModelRenderer(this, 0, 76);
		bootRight.addBox(-2F, 10.1F, -4F, 4, 2, 7, scale * .9f);
		bootRight.setTextureOffset(0, 85);
		bootRight.addBox(-3F, 7.1F, -3F, 5, 3, 6, scale * .9f);
		bootRight.setTextureOffset(0, 78);
		bootRight.addBox(-2F, 8.4F, 3F, 2, 1, 1, scale * .9f);
		bootRight.mirror = true;
		
		// test feature growth 2
		if (level >= 2) {
			bootRight.setTextureOffset(34, 73);
			bootRight.addBox(-4, 8, -2, 1, 1, 4, scale * .9f);
			bootRight.setTextureOffset(44, 73);
			bootRight.addBox(-5, 8, 0, 1, 1, 4, scale * .9f);
		}
	}
	
	@Override
	public void setInvisible(boolean invisible) {
		super.setInvisible(invisible);
	}
	
	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		bipedBody.showModel = false;
		bipedHead.showModel = false;
		bipedHeadwear.showModel = false;
		bipedLeftArm.showModel = false;
		bipedRightArm.showModel = false;
		bipedLeftLeg.showModel = false;
		bipedRightLeg.showModel = false;
		// We actually set parent to be invisible so this just gets angles and stuff
		super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		
		//this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		GlStateManager.pushMatrix();

		if (this.isChild)
		{
			GlStateManager.scale(0.75F, 0.75F, 0.75F);
			GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
			if (head.showModel) {
				copyOffsetAndRots(head, bipedHead);
				this.head.render(scale);
			}
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			if (body.showModel) {
				copyOffsetAndRots(body, bipedBody);
				this.body.render(scale);
			}
			
			if (armRight.showModel) {
				copyOffsetAndRots(armRight, bipedRightArm);
				this.armRight.render(scale);
			}
			
			if (armLeft.showModel) {
				copyOffsetAndRots(armLeft, bipedLeftArm);
				this.armLeft.render(scale);
			}

			if (legRight.showModel) {
				copyOffsetAndRots(legRight, bipedRightLeg);
				this.legRight.render(scale);
			}
			
			if (legLeft.showModel) {
				copyOffsetAndRots(legLeft, bipedLeftLeg);
				this.legLeft.render(scale);
			}
			
			// Boots are based off of leg model. Copy from biped's legs
			if (bootRight.showModel) {
				copyOffsetAndRots(bootRight, bipedRightLeg);
				this.bootRight.render(scale);
			}
			
			if (bootLeft.showModel) {
				copyOffsetAndRots(bootLeft, bipedLeftLeg);
				this.bootLeft.render(scale);
			}
		}
		else
		{
			if (entityIn.isSneaking())
			{
				GlStateManager.translate(0.0F, 0.2F, 0.0F);
			}
			
			if (head.showModel) {
				copyOffsetAndRots(head, bipedHead);
				this.head.render(scale);
			}

			if (body.showModel) {
				copyOffsetAndRots(body, bipedBody);
				this.body.render(scale);
			}
			
			if (armRight.showModel) {
				copyOffsetAndRots(armRight, bipedRightArm);
				this.armRight.render(scale);
			}
			
			if (armLeft.showModel) {
				copyOffsetAndRots(armLeft, bipedLeftArm);
				this.armLeft.render(scale);
			}

			if (legRight.showModel) {
				copyOffsetAndRots(legRight, bipedRightLeg);
				this.legRight.render(scale);
			}
			
			if (legLeft.showModel) {
				copyOffsetAndRots(legLeft, bipedLeftLeg);
				this.legLeft.render(scale);
			}
			
			// Boots are based off of leg model. Copy from biped's legs
			if (bootRight.showModel) {
				copyOffsetAndRots(bootRight, bipedRightLeg);
				this.bootRight.render(scale);
			}
			
			if (bootLeft.showModel) {
				copyOffsetAndRots(bootLeft, bipedLeftLeg);
				this.bootLeft.render(scale);
			}
		}

		GlStateManager.popMatrix();
	}
	
	protected void copyOffsetAndRots(ModelRenderer to, ModelRenderer from) {
		to.offsetX = from.offsetX;
		to.offsetY = from.offsetY;
		to.offsetZ = from.offsetZ;
		to.rotationPointX = from.rotationPointX;
		to.rotationPointY = from.rotationPointY;
		to.rotationPointZ = from.rotationPointZ;
		to.rotateAngleX = from.rotateAngleX;
		to.rotateAngleY = from.rotateAngleY;
		to.rotateAngleZ = from.rotateAngleZ;
	}
	
	/**
	 * Sets all parts but the related ones invisible for rendering.
	 * @param slot The slot we're about to render
	 */
	public void setVisibleFrom(EntityEquipmentSlot slot) {
		bootLeft.showModel = bootRight.showModel = (slot == EntityEquipmentSlot.FEET);
		legLeft.showModel = legRight.showModel = (slot == EntityEquipmentSlot.LEGS);
		body.showModel = armLeft.showModel = armRight.showModel = (slot == EntityEquipmentSlot.CHEST);
		head.showModel = (slot == EntityEquipmentSlot.HEAD);
	}
}
