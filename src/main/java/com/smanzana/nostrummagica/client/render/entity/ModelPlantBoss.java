package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelPlantBoss extends EntityModel<EntityPlantBoss> {
	
	private ModelRenderer body;
	//private ModelRenderer northFrond; etc
	// private ModelRenderer centerTree;
	
	public ModelPlantBoss() {
		this.textureHeight = 256;
		this.textureWidth = 256;
		body = new ModelRenderer(this, 0, 0);
		body.addBox(-24f, -24f, -24f, 48, 48, 48);
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);;
	}
	
	@Override
	public void setLivingAnimations(EntityPlantBoss entityIn, float limbSwing, float limbSwingAmount, float partialTicks) {
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTicks);
	}

	@Override
	public void setRotationAngles(EntityPlantBoss entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// TODO Auto-generated method stub
		
	}
	
}
