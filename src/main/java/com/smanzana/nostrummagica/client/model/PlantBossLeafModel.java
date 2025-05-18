package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.entity.plantboss.PlantBossEntity;
import com.smanzana.nostrummagica.entity.plantboss.PlantBossEntity.PlantBossLeafLimb;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class PlantBossLeafModel extends EntityModel<PlantBossEntity.PlantBossLeafLimb> {
	
	public static final LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
//		this.texHeight = 256;
//		this.texWidth = 256;
//		main = new ModelPart(this, 92, 250);
//		
//		main.texOffs(92, 250);
//		main.addBox(-16, -4, 0, 32, 2, 4);
//		
//		main.texOffs(19, 199);
//		main.addBox(-30, -4, 4, 60, 2, 49);
//
//		// bottom
//		main.texOffs(38, 199);
//		main.addBox(-4, -2, 4, 8, 2, 49).mirror = true;
//		
//		main.texOffs(71, 190);
//		main.addBox(-26, -4, 53, 52, 2, 5);
//		
//		main.texOffs(81, 183);
//		main.addBox(-22, -4, 58, 44, 2, 3);
//		
//		main.texOffs(100, 177);
//		main.addBox(-12, -4, 61, 24, 2, 4);
//		
//		main.texOffs(194, 2254);
//		main.addBox(-32, -4, 8, 2, 2, 29);
//		
//		main.texOffs(0, 225);
//		main.addBox(30, -4, 8, 2, 2, 29);
		
		root.addOrReplaceChild("main", CubeListBuilder.create().texOffs(92, 250).addBox(-16, -4, 0, 32, 2, 4)
				.texOffs(19, 199)
				.addBox(-30, -4, 4, 60, 2, 49)
		
				// bottom
				.texOffs(38, 199)
				.addBox(-4, -2, 4, 8, 2, 49, true)
				
				.texOffs(71, 190)
				.addBox(-26, -4, 53, 52, 2, 5)
				
				.texOffs(81, 183)
				.addBox(-22, -4, 58, 44, 2, 3)
				
				.texOffs(100, 177)
				.addBox(-12, -4, 61, 24, 2, 4)
				
				.texOffs(194, 2254)
				.addBox(-32, -4, 8, 2, 2, 29)
				
				.texOffs(0, 225)
				.addBox(30, -4, 8, 2, 2, 29)
				, PartPose.ZERO);
		
		return LayerDefinition.create(mesh, 256, 256);
	}
	
	private ModelPart main;
	
	public PlantBossLeafModel(ModelPart root) {
		main = root;
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		matrixStackIn.pushPose();
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90f));
		main.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.popPose();
	}
	
	@Override
	public void prepareMobModel(PlantBossEntity.PlantBossLeafLimb entityIn, float limbSwing, float limbSwingAmount, float partialTicks) {
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTicks);
	}

	@Override
	public void setupAnim(PlantBossLeafLimb entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// TODO Auto-generated method stub
		
	}
}
