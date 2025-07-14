package com.smanzana.nostrummagica.client.model;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.entity.boss.shadowdragon.ShadowDragonEntity;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.Color;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class ShadowDragonBossModel extends EntityModel<ShadowDragonEntity> {
	
	public static LayerDefinition createLayer(CubeDeformation deform) {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

//		PartDefinition steve = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 24).addBox(-4.0F, -23.5F, -4.0F, 8.0F, 8.0F, 8.0F, deform)
//		.texOffs(32, 24).addBox(-4.0F, -15.5F, -2.0F, 8.0F, 12.0F, 4.0F, deform)
//		.texOffs(0, 0).addBox(-6.0F, -4.0F, -6.0F, 12.0F, 12.0F, 12.0F, deform), PartPose.offset(0.0F, 16.0F, 0.0F));
//
//		steve.addOrReplaceChild("left_arm_r1", CubeListBuilder.create().texOffs(16, 40).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 12.0F, 4.0F, deform), PartPose.offsetAndRotation(5.0F, -14.0F, -2.0F, -1.0472F, 0.5236F, 0.0F));
//
//		steve.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(0, 40).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 12.0F, 4.0F, deform), PartPose.offsetAndRotation(-6.0F, -14.0F, 0.0F, -1.0472F, -0.5236F, 0.0F));
		
		PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().addBox("upperlip", -6.0F, -1.0F, -24.0F, 12, 5, 16, 176, 44).addBox("upperhead", -8.0F, -8.0F, -10.0F, 16, 16, 16, 112, 30).mirror().addBox("scale", -5.0F, -12.0F, -4.0F, 2, 4, 6, 0, 0).addBox("nostril", -5.0F, -3.0F, -22.0F, 2, 2, 4, 112, 0).mirror().addBox("scale", 3.0F, -12.0F, -4.0F, 2, 4, 6, 0, 0).addBox("nostril", 3.0F, -3.0F, -22.0F, 2, 2, 4, 112, 0), PartPose.ZERO);
		partdefinition1.addOrReplaceChild("jaw", CubeListBuilder.create().addBox("jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16, 176, 65), PartPose.offset(0.0F, 4.0F, -8.0F));
		partdefinition.addOrReplaceChild("neck", CubeListBuilder.create().addBox("box", -5.0F, -5.0F, -5.0F, 10, 10, 10, 192, 104).addBox("scale", -1.0F, -9.0F, -3.0F, 2, 4, 6, 48, 0), PartPose.ZERO);
		

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	protected final ModelPart head;
	protected final ModelPart jaw;
	
	protected @Nullable EMagicElement renderElement = null;
	
	public ShadowDragonBossModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		head = root.getChild("head");
		jaw = head.getChild("jaw");
	}
	
	@Override
	public void prepareMobModel(ShadowDragonEntity entity, float idk1, float idk2, float partialTicks) {
		renderElement = entity.getRevealedElement();
	}
	
	@Override
	public void setupAnim(ShadowDragonEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		final ShadowDragonEntity.BattlePose pose = entity.getBattlePose();
		
		this.head.xRot = headPitch * ((float)Math.PI / 180F); // body shouldn't do this at all
		this.head.yRot = netHeadYaw * ((float)Math.PI / 180F); // head should do this but body shouldn't!
		this.head.zRot = 0;
		
		if (entity.isDiving()) {
			this.jaw.xRot = .3f;
		} else if (entity.isRoaring() || entity.isCharging()) {
			this.jaw.xRot = .3f * (Mth.sin((ageInTicks / 20f) * Mth.PI * 2) + 1f) / 2f;
		} else {
			this.jaw.xRot = 0;
		}
		
		if (entity.isRoaring()) {
			final float prog = Math.min(1f, (entity.getTicksInPose() + (ageInTicks % 1f)) / 40f);
			this.head.xRot = (-Mth.PI / 2) * prog;
		}
		
		if (/*entity.isFallen()*/ pose == ShadowDragonEntity.BattlePose.FALLEN) {
			final float prog = Math.min(1f, (entity.getTicksInPose() + (ageInTicks % 1f)) / 20f);
			this.head.zRot = (Mth.PI / 2) * prog;
		}
	}
	
	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		if (renderElement != null) {
			Color c = new Color(renderElement.getColor());
			red *= c.red;
			blue *= c.blue;
			green *= c.green;
			alpha *= c.alpha;
			
			this.renderElement = null;
		}
		
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
