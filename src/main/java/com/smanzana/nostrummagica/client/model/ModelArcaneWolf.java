package com.smanzana.nostrummagica.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;

import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelArcaneWolf extends WolfModel<ArcaneWolfEntity> {
	
	private static final MeshDefinition createWolfMeshClone() {
		MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(-1.0F, 13.5F, -7.0F));
      partdefinition1.addOrReplaceChild("real_head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F).texOffs(16, 14).addBox(-2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F).texOffs(16, 14).addBox(2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F).texOffs(0, 10).addBox(-0.5F, 0.0F, -5.0F, 3.0F, 3.0F, 4.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 14).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 9.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 14.0F, 2.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("upper_body", CubeListBuilder.create().texOffs(21, 0).addBox(-3.0F, -3.0F, -3.0F, 8.0F, 6.0F, 7.0F), PartPose.offsetAndRotation(-1.0F, 14.0F, -3.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F);
      partdefinition.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-2.5F, 16.0F, 7.0F));
      partdefinition.addOrReplaceChild("left_hind_leg", cubelistbuilder, PartPose.offset(0.5F, 16.0F, 7.0F));
      partdefinition.addOrReplaceChild("right_front_leg", cubelistbuilder, PartPose.offset(-2.5F, 16.0F, -4.0F));
      partdefinition.addOrReplaceChild("left_front_leg", cubelistbuilder, PartPose.offset(0.5F, 16.0F, -4.0F));
      PartDefinition partdefinition2 = partdefinition.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 12.0F, 8.0F, ((float)Math.PI / 5F), 0.0F, 0.0F));
      partdefinition2.addOrReplaceChild("real_tail", CubeListBuilder.create().texOffs(9, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F), PartPose.ZERO);
      return meshdefinition;//LayerDefinition.create(meshdefinition, 64, 32);
	}
	
	public static final LayerDefinition createLayer() {
		MeshDefinition mesh = createWolfMeshClone();
		PartDefinition root = mesh.getRoot();
		
		root.getChild("head").addOrReplaceChild("snoot_bridge", CubeListBuilder.create().texOffs(23, 14).addBox(0, 0, 0, 1, 1, 2), PartPose.offsetAndRotation(0.5F, -0.5F + (16 *.016F), 16 * -.075F, 10f, 0f, 0f));
		root.getChild("body").addOrReplaceChild("butt_floof", CubeListBuilder.create().texOffs(21, 0).addBox(-3.5F, 1.5F, -3.5F, 7, 5, 7), PartPose.ZERO);
		root.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(21, 0).addBox(-3.0F, -3.0F, -3.0F, 8, 6, 7), PartPose.offset(-1.0F, 14.0F, 2.0F));
		
		
		return LayerDefinition.create(mesh, 32, 32);
	}
	
	// Want to use WolfModel's, which used to be public :(
	protected ModelPart head;
	protected ModelPart body;
	protected ModelPart legBackRight;
	protected ModelPart legBackLeft;
	protected ModelPart legFrontRight;
	protected ModelPart legFrontLeft;
	protected ModelPart tail;
	protected ModelPart mane;
	
	protected ModelPart headSnootBridge;
	
	public ModelArcaneWolf(ModelPart root) {
		super(root);
		
		this.head = root.getChild("head");
		this.headSnootBridge = root.getChild("snoot_bridge");
		this.body = root.getChild("body");
		this.mane = root.getChild("mane");
		this.legBackRight = root.getChild("right_hind_leg");
		this.legBackLeft = root.getChild("left_hind_leg");
		this.legFrontRight = root.getChild("right_front_leg");
		this.legFrontLeft = root.getChild("left_front_leg");
		this.tail = root.getChild("tail");
	}
	
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of(this.head);
	}

	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.body, this.legBackRight, this.legBackLeft, this.legFrontRight, this.legFrontLeft, this.tail, this.mane);
	}
	
//	protected void renderBaseWolf(EntityArcaneWolf entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
//		//super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
//		this.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
//		if (this.isChild) {
//			//float f = 2.0F;
//			GlStateManager.pushMatrix();
//			GlStateManager.translatef(0.0F, 5.0F * scale, 2.0F * scale);
//			this.head.renderWithRotation(scale);
//			GlStateManager.popMatrix();
//			GlStateManager.pushMatrix();
//			GlStateManager.scalef(0.5F, 0.5F, 0.5F);
//			GlStateManager.translatef(0.0F, 24.0F * scale, 0.0F);
//			this.body.render(scale);
//			this.legBackRight.render(scale);
//			this.legBackLeft.render(scale);
//			this.legFrontRight.render(scale);
//			this.legFrontLeft.render(scale);
//			this.tail.renderWithRotation(scale);
//			this.mane.render(scale);
//			GlStateManager.popMatrix();
//		} else {
//			this.head.renderWithRotation(scale);
//			this.body.render(scale);
//			this.legBackRight.render(scale);
//			this.legBackLeft.render(scale);
//			this.legFrontRight.render(scale);
//			this.legFrontLeft.render(scale);
//			this.tail.renderWithRotation(scale);
//			this.mane.render(scale);
//		}
//	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		matrixStackIn.pushPose();
		matrixStackIn.scale(1.25f, 1.25f, 1.25f);
		matrixStackIn.translate(0, (-8f) / 24f, 0);
		super.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.popPose();
	}
	
//	@Override
//	public void render(EntityArcaneWolf entity, float time, float swingProgress,
//			float swing, float headAngleY, float headAngleX, float scale) {
//		GlStateManager.pushMatrix();
//		GlStateManager.scalef(1.25f, 1.25f, 1.25f);
//		GlStateManager.translatef(0, (-8f) / 24f, 0);
//		//super.render(entity, time, swingProgress, swing, headAngleY, headAngleX, scale);
//		renderBaseWolf(entity, time, swingProgress, swing, headAngleY, headAngleX, scale);
//		GlStateManager.popMatrix();
//	}
	
	@Override
	public void prepareMobModel(ArcaneWolfEntity entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
		//super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTick);
		if (entityIn.isAngry() /*isAngry()*/) {
			this.tail.yRot = 0.0F;
		} else {
			this.tail.yRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		}

		if (entityIn.isInSittingPose()) {
			this.mane.setPos(-1.0F, 16.0F, -3.0F);
			this.mane.xRot = 1.2566371F;
			this.mane.yRot = 0.0F;
			this.body.setPos(0.0F, 18.0F, 0.0F);
			this.body.xRot = ((float)Math.PI / 4F);
			this.tail.setPos(-1.0F, 21.0F, 6.0F);
			this.legBackRight.setPos(-2.5F, 22.0F, 2.0F);
			this.legBackRight.xRot = ((float)Math.PI * 1.5F);
			this.legBackLeft.setPos(0.5F, 22.0F, 2.0F);
			this.legBackLeft.xRot = ((float)Math.PI * 1.5F);
			this.legFrontRight.xRot = 5.811947F;
			this.legFrontRight.setPos(-2.49F, 17.0F, -4.0F);
			this.legFrontLeft.xRot = 5.811947F;
			this.legFrontLeft.setPos(0.51F, 17.0F, -4.0F);
		} else {
			this.body.setPos(0.0F, 14.0F, 2.0F);
			this.body.xRot = ((float)Math.PI / 2F);
			this.mane.setPos(-1.0F, 14.0F, -3.0F);
			this.mane.xRot = this.body.xRot;
			this.tail.setPos(-1.0F, 12.0F, 8.0F);
			this.legBackRight.setPos(-2.5F, 16.0F, 7.0F);
			this.legBackLeft.setPos(0.5F, 16.0F, 7.0F);
			this.legFrontRight.setPos(-2.5F, 16.0F, -4.0F);
			this.legFrontLeft.setPos(0.5F, 16.0F, -4.0F);
			this.legBackRight.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
			this.legBackLeft.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
			this.legFrontRight.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
			this.legFrontLeft.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		}

		this.head.zRot = entityIn.getHeadRollAngle(partialTick) + entityIn.getBodyRollAngle(partialTick, 0.0F);
		this.mane.zRot = entityIn.getBodyRollAngle(partialTick, -0.08F);
		this.body.zRot = entityIn.getBodyRollAngle(partialTick, -0.16F);
		this.tail.zRot = entityIn.getBodyRollAngle(partialTick, -0.2F);
	}
	
	@Override
	public void setupAnim(ArcaneWolfEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Want to just call super, but can't override the fields...
		super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		this.head.xRot = headPitch * ((float)Math.PI / 180F);
		this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
		this.tail.xRot = ageInTicks;
	}
}
