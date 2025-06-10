package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.entity.boss.plantboss.PlantBossBrambleEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class PlantBossBrambleModel extends EntityModel<PlantBossBrambleEntity> {
	
	public static final LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
//		this.texHeight = 256;
//		this.texWidth = 256;
//		main = new ModelPart(this, 0, 0);
//		
//		// Main horizontal stretch
//		main.texOffs(0, 157);
//		main.addBox(-40, 16, 0, 80, 12, 8);
//		
//		// Leg 1
//		main.texOffs(192, 139);
//		main.addBox(-48, 16, 0, 8, 32, 8);
//		
//		// Leg 2
//		main.texOffs(192, 139);
//		main.addBox(40, 16, 0, 8, 32, 8).mirror = true;
		
		root.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 157).addBox(-40, 16, 0, 80, 12, 8)
				.texOffs(192, 139).addBox(-48, 16, 0, 8, 32, 8)
				.texOffs(192, 139).addBox(40, 16, 0, 8, 32, 8, true), PartPose.ZERO);
		
		return LayerDefinition.create(mesh, 256, 256);
	}
	
	private ModelPart main;
	
	public PlantBossBrambleModel(ModelPart root) {
		
		// Render expects model to be for 5 blocks
		//final float width = 5 * 16;
		main = root;
		
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		main.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	@Override
	public void prepareMobModel(PlantBossBrambleEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks) {
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTicks);
	}

	@Override
	public void setupAnim(PlantBossBrambleEntity entityIn, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		;
	}
}
