package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public class ModelWitchHat<T extends LivingEntity> extends HumanoidModel<T> {
	
	public static final LayerDefinition createLayer() {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0f);
		PartDefinition root = mesh.getRoot();
		
//		super(scale, 0f, 64, 32);
//		final float yOffset = -8.03125F;
//		this.hat = (new ModelPart(this)).setTexSize(64, 32);
//		this.hat.setPos(0F, 0, 0F);
//		this.hat.texOffs(0, 1).addBox(-5.0F, yOffset, -5.0F, 10.0F, 2.0F, 10.0F);
//		//this.bipedHead.addChild(hat);
//		ModelPart modelrenderer = (new ModelPart(this)).setTexSize(64, 32);
//		modelrenderer.setPos(0F, -4.0F, 0);
//		modelrenderer.texOffs(30, 0).addBox(-3.5F, yOffset, -3.5F, 7.0F, 4.0F, 7.0F);
//		modelrenderer.xRot = -0.05235988F;
//		modelrenderer.zRot = 0.02617994F;
//		this.hat.addChild(modelrenderer);
//		ModelPart modelrenderer1 = (new ModelPart(this)).setTexSize(64, 32);
//		modelrenderer1.setPos(0, -4.0F, 0);
//		modelrenderer1.texOffs(48, 7).addBox(-2.0F, yOffset, -2.0F, 4.0F, 4.0F, 4.0F);
//		modelrenderer1.xRot = -0.10471976F;
//		modelrenderer1.zRot = 0.05235988F;
//		modelrenderer.addChild(modelrenderer1);
//		ModelPart modelrenderer2 = (new ModelPart(this)).setTexSize(64, 32);
//		modelrenderer2.setPos(0, -2.0F, 0);
//		modelrenderer2.texOffs(60, 0).addBox(-1F, yOffset, -1F, 1.0F, 2.0F, 1.0F, 0.25F);
//		modelrenderer2.xRot = -0.20943952F;
//		modelrenderer2.zRot = 0.10471976F;
//		modelrenderer1.addChild(modelrenderer2);
		
		final float yOffset = -8.03125F;
		
		PartDefinition hatLast = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 1).addBox(-5.0F, yOffset, -5.0F, 10.0F, 2.0F, 10.0F), PartPose.ZERO);
		hatLast = hatLast.addOrReplaceChild("hat1", CubeListBuilder.create().texOffs(30, 0).addBox(-3.5F, yOffset, -3.5F, 7.0F, 4.0F, 7.0F), PartPose.offsetAndRotation(0F, -4.0F, 0, -0.05235988F, 0, 0.02617994F));
		hatLast = hatLast.addOrReplaceChild("hat2", CubeListBuilder.create().texOffs(48, 7).addBox(-2.0F, yOffset, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.offsetAndRotation(0, -4.0F, 0, -0.10471976F, 0, 0.05235988F));
		hatLast = hatLast.addOrReplaceChild("hat3", CubeListBuilder.create().texOffs(60, 0).addBox(-1F, yOffset, -1F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0, -2.0F, 0, -0.20943952F, 0, 0.10471976F));
		
		
		return LayerDefinition.create(mesh, 64, 32);
	}
	
	protected ModelPart hat;
	
	public ModelWitchHat(ModelPart root) {
		super(root);
		hat = root.getChild("hat");
	}

	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		//this.hat.copyModelAngles(bipedHead);
	}

	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		//this.hat.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
		this.head.visible = true;
		this.hat.visible = true;
		//super.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		this.hat.copyFrom(super.hat);
		//this.hat.setRotationPoint(-5.0F, -10.03125F, -5.0F);
		//this.hat.setRotationPoint(-4.5f, -10.03125F, -4.5f);
		this.hat.x = 0f;
		//this.hat.rotationPointY -= 10.03125F;
		this.hat.z = 0f;
		this.hat.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
	}

}
