package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.entity.EntityLux;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelLux extends EntityModel<EntityLux> {
	
	private ModelRenderer main;
	
	public ModelLux() {
		main = new ModelRenderer(this);
		main.setTextureSize(64, 64);

		main.rotationPointY = 0;
		main.addBox(-2f, -16f, -2f, 4, 32, 4);
		main.addBox(-4, -2, -4, 8, 4, 8);
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		main.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	private float getSwingRot(float swingProgress) {
		return (float) (Math.sin(Math.PI * 2 * swingProgress) * Math.PI * .166666);
	}

	@Override
	public void setRotationAngles(EntityLux entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		if (!entityIn.isRoosting()) {
			final float angle = getSwingRot(entityIn.getSwingProgress(ageInTicks % 1f));
			main.rotateAngleZ = angle;
		}
		
	}
	
}
