package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.entity.HookShotEntity;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelHookShot extends EntityModel<HookShotEntity> {
	
	private static final float width = 2f;
	private static final float height = 2f;
	
	private final ModelRenderer main;

	public ModelHookShot() {
		main = new ModelRenderer(this);
		main.addBox(-width, 0, -width, width*2, height*2, width*2);
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		main.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	@Override
	public void setRotationAngles(HookShotEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		
	}
}
