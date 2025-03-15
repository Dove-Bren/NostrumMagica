package com.smanzana.nostrummagica.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;

import net.minecraft.client.renderer.entity.model.WolfModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class ModelArcaneWolf extends WolfModel<ArcaneWolfEntity> {
	
	// Want to use WolfModel's, which used to be public :(
	protected ModelRenderer head;
	protected ModelRenderer body;
	protected ModelRenderer legBackRight;
	protected ModelRenderer legBackLeft;
	protected ModelRenderer legFrontRight;
	protected ModelRenderer legFrontLeft;
	protected ModelRenderer tail;
	protected ModelRenderer mane;
	
	protected ModelRenderer headSnootBridge;
	
	public ModelArcaneWolf(int color) {
		//super(); Don't bother creating parent model
		
		this.head = new ModelRenderer(this, 0, 0);
		this.head.addBox(-2.0F, -3.0F, -2.0F, 6, 6, 4, 0.0F);
		this.head.setPos(-1.0F, 13.5F, -7.0F);
		this.head.texOffs(16, 14).addBox(-1.999F, -4.5F, 0.0F, 2, 2, 1, 0.0F); // Ear
		this.head.texOffs(16, 11).addBox(-1.5F, -5.25F, -0.001F, 1, 1, 1, 0.0F); // EarTop TODO texture
		this.head.texOffs(16, 14).addBox(1.999F, -4.5F, 0.0F, 2, 2, 1, 0.0F); // Ear
		this.head.texOffs(16, 11).addBox(2.5F, -5.25F, -0.001F, 1, 1, 1, 0.0F); // EarTop TODO texture
		this.head.texOffs(0, 10).addBox(-0.5F, 0.0F, -5.0F, 3, 3, 4, 0.0F); // Snoot
		this.head.texOffs(42, 13).addBox(-2.5F, -1.5F, -1.5F, 1, 4, 3, 0.0F); // Right face floof // TODO texture
		this.head.texOffs(42, 13).addBox(3.5F, -1.5F, -1.5F, 1, 4, 3, 0.0F); // Left face floof // TODO texture
		//this.head.setTextureOffset(18, 14).addBox(0.5F, -1.0F, -4.0F, 1, 2, 2, 0.0F); // SnootBridge
		headSnootBridge = new ModelRenderer(this, 23, 14);
//		headSnootBridge.offsetX = 0;
//		headSnootBridge.offsetY = .016F;
//		headSnootBridge.offsetZ = -.075F;
		headSnootBridge.setPos(0.5F, -0.5F + (16 *.016F), 16 * -.075F);
		headSnootBridge.addBox(0, 0, 0, 1, 1, 2, 0.0F); // SnootBridge
		headSnootBridge.xRot = 10f;
		head.addChild(headSnootBridge);
		this.body = new ModelRenderer(this, 18, 14);
		this.body.addBox(-3.0F, -2.0F, -3.0F, 6, 9, 6, 0.0F);
		this.body.setPos(0.0F, 14.0F, 2.0F);
		this.body.texOffs(21, 0).addBox(-3.5F, 1.5F, -3.5F, 7, 5, 7, 0.0F); // Butt floof // TODO texture
		
		
		mane = new ModelRenderer(this, 21, 0);
		mane.addBox(-3.0F, -3.0F, -3.0F, 8, 6, 7, 0.0F);
		mane.setPos(-1.0F, 14.0F, 2.0F);
		this.legBackRight = new ModelRenderer(this, 0, 18);
		this.legBackRight.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		this.legBackRight.setPos(-2.5F, 16.0F, 7.0F);
		this.legBackLeft = new ModelRenderer(this, 0, 18);
		this.legBackLeft.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		this.legBackLeft.setPos(0.5F, 16.0F, 7.0F);
		this.legFrontRight = new ModelRenderer(this, 0, 18);
		this.legFrontRight.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		this.legFrontRight.setPos(-2.5F, 16.0F, -4.0F);
		this.legFrontLeft = new ModelRenderer(this, 0, 18);
		this.legFrontLeft.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		this.legFrontLeft.setPos(0.5F, 16.0F, -4.0F);
		tail = new ModelRenderer(this, 9, 18);
		tail.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		tail.setPos(-1.0F, 12.0F, 8.0F);
	}
	
	public ModelArcaneWolf() {
		this(-1);
	}
	
	protected Iterable<ModelRenderer> headParts() {
		return ImmutableList.of(this.head);
	}

	protected Iterable<ModelRenderer> bodyParts() {
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
	public void renderToBuffer(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
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
			this.tail.yRot = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
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
			this.legBackRight.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
			this.legBackLeft.xRot = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
			this.legFrontRight.xRot = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
			this.legFrontLeft.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
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
