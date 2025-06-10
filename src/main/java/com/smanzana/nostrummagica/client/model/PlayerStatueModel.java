package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.entity.boss.playerstatue.PlayerStatueEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class PlayerStatueModel extends EntityModel<PlayerStatueEntity> {

	public static LayerDefinition createLayer(CubeDeformation deform) {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition steve = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 24).addBox(-4.0F, -23.5F, -4.0F, 8.0F, 8.0F, 8.0F, deform)
		.texOffs(32, 24).addBox(-4.0F, -15.5F, -2.0F, 8.0F, 12.0F, 4.0F, deform)
		.texOffs(0, 0).addBox(-6.0F, -4.0F, -6.0F, 12.0F, 12.0F, 12.0F, deform), PartPose.offset(0.0F, 16.0F, 0.0F));

		steve.addOrReplaceChild("left_arm_r1", CubeListBuilder.create().texOffs(16, 40).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 12.0F, 4.0F, deform), PartPose.offsetAndRotation(5.0F, -14.0F, -2.0F, -1.0472F, 0.5236F, 0.0F));

		steve.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(0, 40).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 12.0F, 4.0F, deform), PartPose.offsetAndRotation(-6.0F, -14.0F, 0.0F, -1.0472F, -0.5236F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}
	private final ModelPart steve;

	public PlayerStatueModel(ModelPart root) {
		this.steve = root.getChild("root");
	}

	@Override
	public void setupAnim(PlayerStatueEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		steve.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
	
}
