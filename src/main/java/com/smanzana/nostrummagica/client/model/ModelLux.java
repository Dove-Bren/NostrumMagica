package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.entity.LuxEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelLux extends EntityModel<LuxEntity> {
	
	private ModelPart main;
	
	public ModelLux() {
		main = new ModelPart(this);
		main.setTexSize(64, 64);

		main.y = 0;
		main.addBox(-2f, -16f, -2f, 4, 32, 4);
		main.addBox(-4, -2, -4, 8, 4, 8);
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		main.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	private float getSwingRot(float swingProgress) {
		return (float) (Math.sin(Math.PI * 2 * swingProgress) * Math.PI * .166666);
	}

	@Override
	public void setupAnim(LuxEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		if (!entityIn.isRoosting()) {
			final float angle = getSwingRot(entityIn.getAttackAnim(ageInTicks % 1f));
			main.zRot = angle;
		}
		
	}
	
}
