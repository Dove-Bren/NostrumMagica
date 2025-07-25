package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.entity.golem.MagicGolemEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class GolemModel<T extends MagicGolemEntity> extends EntityModel<T> {
	
	private static final int textureHeight = 64;
	private static final int textureWidth = 64;
	
	public static final LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		final int centerX, centerZ;
		centerX = centerZ = 0;
		
		root.addOrReplaceChild("head", CubeListBuilder.create().addBox(-4, -5, -4, 8, 10, 8), PartPose.offset(centerX, 5.0f, centerZ));
		root.addOrReplaceChild("body1", CubeListBuilder.create().addBox(-6, -3, -2, 16, 6, 10), PartPose.offset(centerX, 26.0f, centerZ));
		root.addOrReplaceChild("body2", CubeListBuilder.create().addBox(-4, -4, -6, 10, 8, 8), PartPose.offset(centerX, 17.0f, centerZ));
		root.addOrReplaceChild("body3", CubeListBuilder.create().addBox(-6, -2, -4, 8, 4, 8), PartPose.offset(centerX, 34.0f, centerZ));
		
		return LayerDefinition.create(mesh, textureWidth, textureHeight);
	}

	private ModelPart head;
	private ModelPart body1;
	private ModelPart body2;
	private ModelPart body3;
	
	//															   1.4 circles per second
	private static final float rate1 = (float) (((2.0 * Math.PI) * 1.4) / 20.0f);
	private static final float rate2 = (float) (((2.0 * Math.PI) * 0.3) / 20.0f);
	private static final float rate3 = (float) (((2.0 * Math.PI) * 0.8) / 20.0f);
	
	public GolemModel(ModelPart root) {
		head = root.getChild("head");
		body1 = root.getChild("body1");
		body2 = root.getChild("body2");
		body3 = root.getChild("body3");
		
//		head = new ModelPart(this, 0, 0);
//		head.addBox(-4, -5, -4, 8, 10, 8);
//		head.setTexSize(textureWidth, textureHeight);
//		head.setPos(centerX, 5.0f, centerZ); // 34
		
//		body1 = new ModelPart(this, 0, 0);
//		body1.addBox(-6, -3, -2, 16, 6, 10);
//		body1.setTexSize(textureWidth, textureHeight);
//		body1.setPos(centerX, 26.0f, centerZ); // 24
//		
//		body2 = new ModelPart(this, 0, 0);
//		body2.addBox(-4, -4, -6, 10, 8, 8);
//		body2.setTexSize(textureWidth, textureHeight);
//		body2.setPos(centerX, 17.0f, centerZ); // 14
//		
//		body3 = new ModelPart(this, 0, 0);
//		body3.addBox(-6, -2, -4, 8, 4, 8);
//		body3.setTexSize(textureWidth, textureHeight);
//		body3.setPos(centerX, 34.0f, centerZ); // 10
		
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		matrixStackIn.pushPose();
		
		float modelScale = 1.0f;// / 20.0f; // 16 pixels wide model to .8 blocks
		matrixStackIn.scale(modelScale, modelScale * .5f, modelScale);
		
		head.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		body1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		body2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		body3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		
		matrixStackIn.popPose();
	}
	
	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		float ticks = entityIn.tickCount;
		
		float speedup = 1.0f;
		if (((MagicGolemEntity) entityIn).getTarget() != null)
			speedup = 2.0f;
		
		body1.yRot = ticks * -rate1 * speedup;
		body2.yRot = ticks * rate2 * speedup;
		body3.yRot = ticks * rate3 * speedup;
	}
	
}
