package com.smanzana.nostrummagica.client.model;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.entity.WilloEntity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelWillo extends EntityModel<WilloEntity> {
	
	private static final int SEGMENTS = 8;
	private static final float PERIOD = 20f * 2;
	
	public static final LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
		final float offset = 16f * .75f;
		final float spacing = 16f * .75f;
		for (int i = 0; i < SEGMENTS; i++) {
			root.addOrReplaceChild("left" + i, CubeListBuilder.create().texOffs(0, 18).addBox(-4.5f + (offset + (i+1) * spacing), -4.5f, -4.5f, 9, 9, 9), PartPose.ZERO);
		}
		
		for (int i = 0; i < SEGMENTS; i++) {
			root.addOrReplaceChild("right" + i, CubeListBuilder.create().texOffs(0, 18).addBox(-4.5f + (-offset + (i+1) * -spacing), -4.5f, -4.5f, 9, 9, 9), PartPose.ZERO);
		}
		
		return LayerDefinition.create(mesh, 64, 64);
	}
	
	private List<ModelPart> armLeft;
	private List<ModelPart> armRight;
	
	protected float waveProg;
	
	public ModelWillo(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		
		armLeft = new ArrayList<>(SEGMENTS);
		armRight = new ArrayList<>(SEGMENTS);
		for (int i = 0; i < SEGMENTS; i++) {
			armLeft.set(0, root.getChild("left" + i));
			armRight.set(0, root.getChild("right" + i));
		}
	}
	
	@Override
	public void prepareMobModel(WilloEntity entity, float limbSwing, float limbSwingAmount, float partialTickTime) {
		super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTickTime);
		
		// Wave timing information for rendering arms
		this.waveProg = ((float) entity.tickCount + partialTickTime) / PERIOD;
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		// Used to have all parented to a main modelrender, and adjusted the yoffset to make it do the wave with its arms.
		// There is no adjustable offset on model renderers OR on their boxes. So isntead we iterate the lists ourselves
		// and render with different offsets.
		//main.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		
		for (int i = 0; i < SEGMENTS; i++) {
			final float progressAdjLeft = (waveProg + i * .1f) % 1f;
			final float progressAdjRight = ((waveProg + .5f) + i * .1f) % 1f;
			final float offsetLeft = (float) (Math.sin(2 * Math.PI * progressAdjLeft) * .5);
			final float offsetRight = (float) (Math.sin(2 * Math.PI * progressAdjRight) * .5);
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, offsetLeft, 0);
			armLeft.get(i).render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			matrixStackIn.popPose();
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, offsetRight, 0);
			armRight.get(i).render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			matrixStackIn.popPose();
		}
	}

	@Override
	public void setupAnim(WilloEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// TODO Auto-generated method stub
		
	}
	
}
