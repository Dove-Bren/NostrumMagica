package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class DragonFlightWingsModel<T extends LivingEntity> extends EntityModel<T> {
	
	private static final CubeListBuilder createWing(boolean mirror) {
		return CubeListBuilder.create()
			.texOffs(1, 10).addBox(-19, -13, 0, 19, 2, 1, mirror)
			.texOffs(3, 8).addBox(-17, -14, 0, 15, 1, 1, mirror)
			.texOffs(5, 6).addBox(-15, -15, 0, 11, 1, 1, mirror)
			.texOffs(6, 4).addBox(-13, -16, 0, 8, 1, 1, mirror)
			.texOffs(7, 2).addBox(-11, -17, 0, 5, 1, 1, mirror)
			.texOffs(7, 0).addBox(-10, -18, 0, 4, 1, 1, mirror)
			.texOffs(0, 13).addBox(-21, -11, 0, 22, 2, 1, mirror)
			.texOffs(0, 16).addBox(-22, -9, 0, 23, 3, 1, mirror)
			.texOffs(2, 20).addBox(-3, -6, 0, 2, 1, 1, mirror)
			.texOffs(8, 20).addBox(-8, -6, 0, 3, 1, 1, mirror)
			.texOffs(16, 20).addBox(-23, -6, 0, 12, 1, 1, mirror)
			.texOffs(17, 22).addBox(-23, -5, 0, 11, 1, 1, mirror)
			.texOffs(18, 24).addBox(-16, -4, 0, 4, 1, 1, mirror)
			.texOffs(18, 26).addBox(-15, -3, 0, 3, 1, 1, mirror)
			.texOffs(19, 28).addBox(-15, -2, 0, 2, 1, 1, mirror)
			.texOffs(19, 30).addBox(-14, -1, 0, 1, 1, 1, mirror)
			.texOffs(33, 30).addBox(-23, -1, 0, 1, 1, 1, mirror)
			.texOffs(28, 24).addBox(-23, -4, 0, 4, 1, 1, mirror)
			.texOffs(30, 26).addBox(-23, -3, 0, 3, 1, 1, mirror)
			.texOffs(31, 28).addBox(-23, -2, 0, 2, 1, 1, mirror)
			.texOffs(3, 22).addBox(-3, -5, 0, 1, 1, 1, mirror)
			.texOffs(9, 22).addBox(-7, -5, 0, 1, 1, 1, mirror);
	}
	
	public static final LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
		root.addOrReplaceChild("right", createWing(false), PartPose.ZERO);
		root.addOrReplaceChild("left", createWing(false), PartPose.ZERO);
		
		return LayerDefinition.create(mesh, 64, 64);
	}

	private /* final */ ModelPart rightWing;
	private final ModelPart leftWing;

	public DragonFlightWingsModel(ModelPart root) {
		rightWing = root.getChild("right");
		leftWing = root.getChild("left");
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
//		{
//			this.rightWing = createWing(this);
//		}
//		GlStateManager.disableRescaleNormal();
		//GlStateManager.disableCull();
		this.rightWing.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		this.leftWing.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
	 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		
		final float livingJitterPeriod = 100f;
		// 0f - 1f
		final float livingJitterPerc = (ageInTicks % livingJitterPeriod) / livingJitterPeriod;
		// -1f - 1f (sin)
		final float livingJitterMod = Mth.sin(3.1415f * 2 * livingJitterPerc);
		
		final float wingFlapPerc = ElementalArmor.GetWingFlap((LivingEntity) entityIn, ageInTicks - (int) ageInTicks);
		final float wingFlapMod = Mth.sin(3.1415f * 2 * wingFlapPerc);
		
		rightWing.xRot = rightWing.zRot = 0;
		if (entityIn instanceof LivingEntity && ((LivingEntity)entityIn).isFallFlying()) {
			rightWing.yRot = (float) (Math.PI * (.20 + livingJitterMod * .005 + wingFlapMod * .25));
		} else {
			// Standing still
			rightWing.yRot = (float) (Math.PI * (.25 + livingJitterMod * .005));
		}
		
		//rightWing.rotateAngleY = (float) (Math.PI * (.20 + livingJitterMod * .005 + -.3));
		
		leftWing.xRot = rightWing.xRot;
		leftWing.yRot = (float) (-rightWing.yRot + Math.PI);
		leftWing.zRot = rightWing.zRot;
	}

	/**
	 * Used for easily adding entity-dependent animations. The second and third float params here are the same second
	 * and third as in the setRotationAngles method.
	 */
	public void prepareMobModel(T entitylivingbaseIn, float p_78086_2_, float p_78086_3_, float partialTickTime) {
		super.prepareMobModel(entitylivingbaseIn, p_78086_2_, p_78086_3_, partialTickTime);
	}
	
}
