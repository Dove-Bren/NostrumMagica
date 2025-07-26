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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class EnchantedArmorBaseModel<T extends LivingEntity> extends HumanoidModel<T> {
	
	private static final int TEXTURE_WIDTH = 129;
	private static final int TEXTURE_HEIGHT = 96;
	
	public static final LayerDefinition createLayer(int level) {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0f);
		PartDefinition root = mesh.getRoot();
		
		final CubeDeformation deform = new CubeDeformation(0.01f);
		final CubeDeformation deformInner = new CubeDeformation(0.01f * .9f);
		final CubeDeformation deformOuter = new CubeDeformation(0.01f * 1.5f);
		
		CubeListBuilder cubes = CubeListBuilder.create();
		cubes.addBox(-5, -9, -5, 10, 10, 10, deform);
		cubes.texOffs(0, 0);
		cubes.addBox(-6, -9, -2, 1, 5, 4, deform);
		cubes.texOffs(30, 0);
		cubes.addBox(5, -9, -2, 1, 5, 4, deform);
		cubes.texOffs(0, 20);
		cubes.addBox(-6, -10, -2, 12, 1, 4, deform);
		
		// Testing features -- neutral/spikes/horns?
		if (level >= 1) {
			cubes.texOffs(48, 6);
			cubes.addBox(-3, -10, -7, 1, 1, 4, deform);
			cubes.addBox(-3, -13, -5, 1, 3, 1, deform);
			cubes.addBox(-3, -14, -6, 1, 2, 1, deform);
			cubes.addBox(-3, -11, -9, 1, 1, 3, deform);
			cubes.addBox(-3, -12, -10, 1, 1, 2, deform);
			
			cubes.addBox(2, -10, -7, 1, 1, 4, deform);
			cubes.addBox(2, -13, -5, 1, 3, 1, deform);
			cubes.addBox(2, -14, -6, 1, 2, 1, deform);
			cubes.addBox(2, -11, -9, 1, 1, 3, deform);
			cubes.addBox(2, -12, -10, 1, 1, 2, deform);
		}
		
		// Testing features -- neutral/spikes/tendrils?
		if (level >= 2) {
	        cubes.texOffs(48, 0);
	        cubes.addBox(-10, -9, -1, 4, 1, 2, deform);
	        cubes.addBox(-10, -11, -1, 1, 2, 2, deform);
	        cubes.addBox(-8, -8, -1, 1, 1, 2, deform);
	
	        cubes.addBox(6, -9, -1, 4, 1, 2, deform);
	        cubes.addBox(9, -11, -1, 1, 2, 2, deform);
	        cubes.addBox(7, -8, -1, 1, 1, 2, deform);
		}
		
		if (level >= 3) {
			cubes.texOffs(48, 0);
	        cubes.addBox(-4, -14, -1, 1, 4, 2, deform);
	        cubes.addBox(-5, -16, -1, 1, 3, 2, deform);
	        cubes.addBox(-3, -13, -1, 1, 1, 2, deform);
	        
	        cubes.addBox(3, -14, -1, 1, 4, 2, deform);
	        cubes.addBox(4, -16, -1, 1, 3, 2, deform);
	        cubes.addBox(2, -13, -1, 1, 1, 2, deform);
		}
		
		root.addOrReplaceChild("head", cubes, PartPose.ZERO);

		cubes = CubeListBuilder.create();
		cubes.texOffs(0, 27);
		cubes.addBox(-5.0F, 0F, -3.01F, 10, 12, 6);
		
		// Test feature growth 2
		if (level >= 2) {
			cubes.texOffs(34, 54);
			cubes.addBox(-6, 9, -3, 1, 1, 6, deform);
			cubes.addBox(5, 9, -3, 1, 1, 6, deform);
			
			cubes.texOffs(48, 54);
			cubes.addBox(-7, 10, -3, 2, 2, 6, deform);
			cubes.addBox(5, 10, -3, 2, 2, 6, deform);
			
			cubes.texOffs(34, 20);
			cubes.addBox(4, -1.5f, -4, 7, 2, 8, deformOuter);
			
			cubes.texOffs(34, 30);
			cubes.addBox(-11, -1.5f, -4, 7, 2, 8, deformOuter);
		}
		
		// Test feature growth 3
		if (level >= 3) {
			cubes.texOffs(34, 62);
			cubes.addBox(-6, 9, -4, 12, 1, 1, deform);
			cubes.addBox(-6, 9, 3, 12, 1, 1, deform);
			
			cubes.texOffs(32, 64);
			cubes.addBox(-7, 10, -5, 14, 2, 2, deform);
			cubes.addBox(-7, 10, 3, 14, 2, 2, deform);
			
			cubes.texOffs(34, 40);
			cubes.addBox(4, -2.5f, -3, 5, 1, 6, deformOuter);
			cubes.texOffs(50, 40);
			cubes.addBox(5, -3.5f, -1, 2, 1, 3, deformOuter);
			
			cubes.texOffs(34, 47);
			cubes.addBox(-9, -2.5f, -3, 5, 1, 6, deformOuter);
			cubes.texOffs(50, 47);
			cubes.addBox(-7, -3.5f, -1, 2, 1, 3, deformOuter);
		}
		
		if (level >= 4) {
			// Outer cubes armor
			cubes.texOffs(22, 78);
			cubes.addBox(-5.5F, 0F, -4F, 11, 4, 8, deform);
			
			cubes.texOffs(22, 90);
			cubes.addBox(-2F, 4F, -4F, 4, 5, 1, deform);
		}
		
		PartDefinition body = root.addOrReplaceChild("body", cubes, PartPose.ZERO);
		if (level >= 4) {
			// Belt Tassels
			cubes = CubeListBuilder.create();
			cubes.texOffs(66 / 3, 48 / 3)
				.addBox(0 + (-6F), 0 + (12F), 0 + (-2F), 1, 5, 4, deform, 3f, 3f); // 3x texture scale. Should be 1/3?
			body.addOrReplaceChild("tassel_right", cubes, PartPose.rotation(0f, 0f, 0.125f));
			
			cubes = CubeListBuilder.create();
			cubes.texOffs(66 / 3, 48 / 3).mirror()
				.addBox(0 + (5F), 0 + (12F), 0 + (-2F), 1, 5, 4, deform, 3f, 3f); // 3x texture scale. Should be 1/3?
			body.addOrReplaceChild("tassel_right", cubes, PartPose.rotation(0f, 0f, -0.125f));
			
			// Shoulder Tassels
			body.addOrReplaceChild("shoulder_tassel_right", CubeListBuilder.create()
					.texOffs(66 / 3, 0 / 3).addBox(0 + (-11F), 0 + (.5F), 0 + (-2F), 1, 5, 5, deform, 3, 3)
					.texOffs(66 / 3, 30 / 3).addBox(0 + (-11F), 0 + (.5F), 0.01f + (3F), 6, 5, 1, deform, 3, 3)
					, PartPose.ZERO);
			

			body.addOrReplaceChild("shoulder_tassel_left", CubeListBuilder.create().mirror()
					.texOffs(66 / 3, 0 / 3).addBox(0 + (10F), 0 + (.5F), 0 + (-2F), 1, 5, 5, deform, 3, 3)
					.texOffs(66 / 3, 30 / 3).addBox(0 + (5F), 0 + (.5F), 0.01f + (3F), 6, 5, 1, deform, 3, 3)
					, PartPose.ZERO);
		}
		
		cubes = CubeListBuilder.create();
		cubes.texOffs(0, 45);
		cubes.addBox(-1F, -2.50F, -3F, 4, 9, 6, deform);
		cubes.texOffs(20, 45);
		cubes.addBox(3.0F, -3.50F, -2F, 1, 3, 4, deform);
		
		// Test feature growth 1
		if (level >= 1) {
			cubes.texOffs(48, 11);
			cubes.addBox(3, 1, 0, 2, 1, 1, deform);
			cubes.addBox(3, 4, -1, 2, 1, 1, deform);
		}
		root.addOrReplaceChild("left_arm", cubes, PartPose.ZERO);
		
		cubes = CubeListBuilder.create().mirror();
		cubes.texOffs(0, 45);
		cubes.addBox(-3.0F, -2.50F, -3F, 4, 9, 6, deform);
		cubes.texOffs(20, 45);
		cubes.addBox(-4.0F, -3.50F, -2F, 1, 3, 4, deform);
		
		// Test feature growth 1
		if (level >= 1) {
			cubes.texOffs(58, 11);
			cubes.addBox(-5, 1, 0, 2, 1, 1, deform);
			cubes.addBox(-5, 4, -1, 2, 1, 1, deform);
		}
		
		// test feature growth 2
		// test feature growth 3
		if (level >= 3) {
			
			// Also cool bracelet thing
			cubes.texOffs(46, 13);
			cubes.addBox(-4, 5, -4, 4, 1, 5, deformOuter);
		}
		root.addOrReplaceChild("right_arm", cubes, PartPose.ZERO);
		
		cubes = CubeListBuilder.create();
		cubes.texOffs(0, 60);
		cubes.addBox(-2F, -.95F, -3F, 5, 10, 6, deform);
		
		if (level >= 4) {
			cubes.texOffs(22, 90);
			cubes.addBox(-1F, -1.5F, -3.5F, 3, 5, 1, deform);
		}
		
		root.addOrReplaceChild("left_leg", cubes, PartPose.ZERO);
		
		cubes = CubeListBuilder.create().mirror();
		cubes.texOffs(0, 60);
		cubes.addBox(-3F, -.99F, -3F, 5, 10, 6, deform);
		
		if (level >= 4) {
			cubes.texOffs(22, 90);
			cubes.addBox(-2F, -1.5F, -3.5F, 3, 5, 1, deform);
		}
		
		root.addOrReplaceChild("right_leg", cubes, PartPose.ZERO);
		
		cubes = CubeListBuilder.create();
		cubes.texOffs(0, 76);
		cubes.addBox(-2F, 10F, -4F, 4, 2, 7, deformInner);
		cubes.texOffs(0, 85);
		cubes.addBox(-2F, 7F, -3F, 5, 3, 6, deformInner);
		cubes.texOffs(0, 78);
		cubes.addBox(0F, 8.4F, 3F, 2, 1, 1, deformInner);
		
		// test feature growth 2
		if (level >= 2) {
			cubes.texOffs(34, 68);
			cubes.addBox(3, 8, -2, 1, 1, 4, deformInner);
			cubes.texOffs(44, 68);
			cubes.addBox(4, 8, 0, 1, 1, 4, deformInner);
		}
		
		root.addOrReplaceChild("left_boot", cubes, PartPose.ZERO);
		
		cubes = CubeListBuilder.create().mirror();
		cubes.texOffs(0, 76);
		cubes.addBox(-2F, 10.1F, -4F, 4, 2, 7, deformInner);
		cubes.texOffs(0, 85);
		cubes.addBox(-3F, 7.1F, -3F, 5, 3, 6, deformInner);
		cubes.texOffs(0, 78);
		cubes.addBox(-2F, 8.4F, 3F, 2, 1, 1, deformInner);
		
		// test feature growth 2
		if (level >= 2) {
			cubes.texOffs(34, 73);
			cubes.addBox(-4, 8, -2, 1, 1, 4, deformInner);
			cubes.texOffs(44, 73);
			cubes.addBox(-5, 8, 0, 1, 1, 4, deformInner);
		}
		
		root.addOrReplaceChild("right_boot", cubes, PartPose.ZERO);
		
		return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
	}

	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart armLeft;
	private final ModelPart armRight;
	private final ModelPart legLeft;
	private final ModelPart legRight;
	private final ModelPart bootLeft;
	private final ModelPart bootRight;
	
	// Level is number of other set pieces. 0 is base, 4 is max coolness.
	public EnchantedArmorBaseModel(ModelPart root) {
		super(root);
		
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.armRight = root.getChild("right_arm");
		this.armLeft = root.getChild("left_arm");
		this.legRight = root.getChild("right_leg");
		this.legLeft = root.getChild("left_leg");
		this.bootRight = root.getChild("right_boot");
		this.bootLeft = root.getChild("left_boot");
	}
	
	@Override
	public void setAllVisible(boolean visible) {
		super.setAllVisible(visible);
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		// We actually set parent to be invisible so this just gets angles and stuff... or it used to and now does nothing?
		super.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		
		//this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		matrixStackIn.pushPose();

		if (this.young)
		{
			matrixStackIn.scale(0.75F, 0.75F, 0.75F);
			matrixStackIn.translate(0.0F, 16.0F, 0.0F);
			if (head.visible) {
				copyOffsetAndRots(head, head);
				this.head.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}
			matrixStackIn.popPose();
			matrixStackIn.pushPose();
			matrixStackIn.scale(0.5F, 0.5F, 0.5F);
			matrixStackIn.translate(0.0F, 24.0F, 0.0F);
			if (body.visible) {
				copyOffsetAndRots(body, body);
				this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}
			
			if (armRight.visible) {
				copyOffsetAndRots(armRight, rightArm);
				this.armRight.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}
			
			if (armLeft.visible) {
				copyOffsetAndRots(armLeft, leftArm);
				this.armLeft.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}

			if (legRight.visible) {
				copyOffsetAndRots(legRight, rightLeg);
				this.legRight.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}
			
			if (legLeft.visible) {
				copyOffsetAndRots(legLeft, leftLeg);
				this.legLeft.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}
			
			// Boots are based off of leg model. Copy from biped's legs
			if (bootRight.visible) {
				copyOffsetAndRots(bootRight, rightLeg);
				this.bootRight.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}
			
			if (bootLeft.visible) {
				copyOffsetAndRots(bootLeft, leftLeg);
				this.bootLeft.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}
		}
		else
		{
			if (head.visible) {
				copyOffsetAndRots(head, head);
				this.head.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}

			if (body.visible) {
				copyOffsetAndRots(body, body);
				this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}
			
			if (armRight.visible) {
				copyOffsetAndRots(armRight, rightArm);
				this.armRight.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}
			
			if (armLeft.visible) {
				copyOffsetAndRots(armLeft, leftArm);
				this.armLeft.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}

			if (legRight.visible) {
				copyOffsetAndRots(legRight, rightLeg);
				this.legRight.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}
			
			if (legLeft.visible) {
				copyOffsetAndRots(legLeft, leftLeg);
				this.legLeft.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}
			
			// Boots are based off of leg model. Copy from biped's legs
			if (bootRight.visible) {
				copyOffsetAndRots(bootRight, rightLeg);
				this.bootRight.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}
			
			if (bootLeft.visible) {
				copyOffsetAndRots(bootLeft, leftLeg);
				this.bootLeft.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			}
		}

		matrixStackIn.popPose();
	}
	
	protected void copyOffsetAndRots(ModelPart to, ModelPart from) {
//		to.offsetX = from.offsetX;
//		to.offsetY = from.offsetY;
//		to.offsetZ = from.offsetZ;
		to.x = from.x;
		to.y = from.y;
		to.z = from.z;
		to.xRot = from.xRot;
		to.yRot = from.yRot;
		to.zRot = from.zRot;
	}
	
	/**
	 * Sets all parts but the related ones invisible for rendering.
	 * @param slot The slot we're about to render
	 */
	public void setVisibleFrom(EquipmentSlot slot) {
		bootLeft.visible = bootRight.visible = (slot == EquipmentSlot.FEET);
		legLeft.visible = legRight.visible = (slot == EquipmentSlot.LEGS);
		body.visible = armLeft.visible = armRight.visible = (slot == EquipmentSlot.CHEST);
		head.visible = (slot == EquipmentSlot.HEAD);
	}
}
