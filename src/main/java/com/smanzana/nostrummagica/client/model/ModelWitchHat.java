package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class ModelWitchHat<T extends LivingEntity> extends BipedModel<T> {
	
	protected ModelRenderer hat;
	
	public ModelWitchHat(float scale) {
		super(scale, 0f, 64, 32);
		final float yOffset = -8.03125F;
		this.hat = (new ModelRenderer(this)).setTextureSize(64, 32);
		this.hat.setRotationPoint(0F, 0, 0F);
		this.hat.setTextureOffset(0, 1).addBox(-5.0F, yOffset, -5.0F, 10.0F, 2.0F, 10.0F);
		//this.bipedHead.addChild(hat);
		ModelRenderer modelrenderer = (new ModelRenderer(this)).setTextureSize(64, 32);
		modelrenderer.setRotationPoint(0F, -4.0F, 0);
		modelrenderer.setTextureOffset(30, 0).addBox(-3.5F, yOffset, -3.5F, 7.0F, 4.0F, 7.0F);
		modelrenderer.rotateAngleX = -0.05235988F;
		modelrenderer.rotateAngleZ = 0.02617994F;
		this.hat.addChild(modelrenderer);
		ModelRenderer modelrenderer1 = (new ModelRenderer(this)).setTextureSize(64, 32);
		modelrenderer1.setRotationPoint(0, -4.0F, 0);
		modelrenderer1.setTextureOffset(48, 7).addBox(-2.0F, yOffset, -2.0F, 4.0F, 4.0F, 4.0F);
		modelrenderer1.rotateAngleX = -0.10471976F;
		modelrenderer1.rotateAngleZ = 0.05235988F;
		modelrenderer.addChild(modelrenderer1);
		ModelRenderer modelrenderer2 = (new ModelRenderer(this)).setTextureSize(64, 32);
		modelrenderer2.setRotationPoint(0, -2.0F, 0);
		modelrenderer2.setTextureOffset(60, 0).addBox(-1F, yOffset, -1F, 1.0F, 2.0F, 1.0F, 0.25F);
		modelrenderer2.rotateAngleX = -0.20943952F;
		modelrenderer2.rotateAngleZ = 0.10471976F;
		modelrenderer1.addChild(modelrenderer2);
	}

	@Override
	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		//this.hat.copyModelAngles(bipedHead);
	}

	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		//this.hat.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
		this.bipedHead.showModel = false;
		this.bipedHeadwear.showModel = false;
		//super.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		this.hat.copyModelAngles(this.bipedHeadwear);
		//this.hat.setRotationPoint(-5.0F, -10.03125F, -5.0F);
		//this.hat.setRotationPoint(-4.5f, -10.03125F, -4.5f);
		this.hat.rotationPointX = 0f;
		//this.hat.rotationPointY -= 10.03125F;
		this.hat.rotationPointZ = 0f;
		this.hat.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
	}

}
