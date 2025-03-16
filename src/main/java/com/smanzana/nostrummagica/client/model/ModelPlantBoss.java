package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.entity.plantboss.PlantBossEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelPlantBoss extends EntityModel<PlantBossEntity> {
	
	private ModelPart body;
	//private ModelRenderer northFrond; etc
	// private ModelRenderer centerTree;
	
	public ModelPlantBoss() {
		this.texHeight = 256;
		this.texWidth = 256;
		body = new ModelPart(this, 0, 0);
		body.addBox(-24f, -24f, -24f, 48, 48, 48);
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);;
	}
	
	@Override
	public void prepareMobModel(PlantBossEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks) {
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTicks);
	}

	@Override
	public void setupAnim(PlantBossEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// TODO Auto-generated method stub
		
	}
	
}
