package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.entity.dragon.DragonEggEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class DragonEggModel extends EntityModel<DragonEggEntity> {
	
	private static final void addCyl(PartDefinition root, int y, int height, int radius, int idx) {
		root.addOrReplaceChild("cyl" + idx, CubeListBuilder.create().addBox(-radius, y, -radius, radius * 2, -height, radius * 2), PartPose.offset(0, 20, 0));
	}
	
	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
		int y = 28;
		int idx = 0;
		
		addCyl(root, y--, 1, 2, idx++);
		addCyl(root, y--, 1, 4, idx++);
		addCyl(root, y--, 1, 5, idx++);
		addCyl(root, y, 2, 6, idx++); y -= 2;
		addCyl(root, y, 4, 7, idx++); y -= 4;
		addCyl(root, y, 2, 6, idx++); y -= 2;
		addCyl(root, y, 2, 5, idx++); y -= 2;
		addCyl(root, y--, 1, 4, idx++);
		addCyl(root, y--, 1, 3, idx++);
		addCyl(root, y--, 1, 2, idx++);
		
		return LayerDefinition.create(mesh, textureWidth, textureHeight);
	}

	private ModelPart main;
	private float coldScale;
	
	private static final int textureHeight = 32;
	private static final int textureWidth = 32;
	
	
	
	public DragonEggModel(ModelPart part) {
//		main = new ModelPart(this, 0, 0);
//		int y = 28;
//		
//		main.setTexSize(textureWidth, textureHeight);
//		main.setPos(0, 20, 0);
//		addCyl(y--, 1, 2);
//		addCyl(y--, 1, 4);
//		addCyl(y--, 1, 5);
//		addCyl(y, 2, 6); y -= 2;
//		addCyl(y, 4, 7); y -= 4;
//		addCyl(y, 2, 6); y -= 2;
//		addCyl(y, 2, 5); y -= 2;
//		addCyl(y--, 1, 4);
//		addCyl(y--, 1, 3);
//		addCyl(y--, 1, 2);
		main = part;
	}
	
	public void setColdScale(float scale) {
		this.coldScale = scale; // Wish renderer could just pass color to render...
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer,
            int light, int overlay, float red, float green, float blue, float alpha) {
		
		// Tint based on how cold it is
		red *= 1f - (coldScale * .4f);
		green *= 1f - (coldScale * .1f);
		blue *= 1f - (coldScale * .1f);
		
		final float modelScale = 0.5f;
		matrixStack.pushPose();
		matrixStack.scale(modelScale, modelScale, modelScale);
		main.render(matrixStack, buffer, light, overlay, red, green, blue, alpha);
		matrixStack.popPose();
	}

	@Override
	public void setupAnim(DragonEggEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// TODO Auto-generated method stub
		
	}
}
