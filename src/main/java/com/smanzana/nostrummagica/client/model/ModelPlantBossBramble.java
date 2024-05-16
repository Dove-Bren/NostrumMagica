package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBossBramble;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelPlantBossBramble extends EntityModel<EntityPlantBossBramble> {
	
	private ModelRenderer main;
	
	public ModelPlantBossBramble() {
		
		// Render expects model to be for 5 blocks
		//final float width = 5 * 16;
		
		this.textureHeight = 256;
		this.textureWidth = 256;
		main = new ModelRenderer(this, 0, 0);
		
		// Main horizontal stretch
		main.setTextureOffset(0, 157);
		main.addBox(-40, 16, 0, 80, 12, 8);
		
		// Leg 1
		main.setTextureOffset(192, 139);
		main.addBox(-48, 16, 0, 8, 32, 8);
		
		// Leg 2
		main.setTextureOffset(192, 139);
		main.addBox(40, 16, 0, 8, 32, 8).mirror = true;
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		main.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	@Override
	public void setLivingAnimations(EntityPlantBossBramble entityIn, float limbSwing, float limbSwingAmount, float partialTicks) {
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTicks);
	}

	@Override
	public void setRotationAngles(EntityPlantBossBramble entityIn, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		;
	}
}
