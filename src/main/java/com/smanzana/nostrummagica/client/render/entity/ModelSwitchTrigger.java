package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class ModelSwitchTrigger extends EntityModel<EntitySwitchTrigger> {
	
	public static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/entity/golem_ice.png");
	public static final ResourceLocation CAGE_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/block/spawner.png");
	
	protected final float width;
	protected final float height;

	public ModelSwitchTrigger() {
		this(.45f, .8f);
	}
	
	protected ModelSwitchTrigger(float width, float height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {

		matrixStackIn.push();
		final Matrix4f transform = matrixStackIn.getLast().getMatrix();
		final Matrix3f normal = matrixStackIn.getLast().getNormal();
		final Vector3f[] normals = {new Vector3f(0.5774f, -0.5774f, 0.5774f), new Vector3f(-0.5774f, -0.5774f, 0.5774f), new Vector3f(-0.5774f, -0.5774f, -0.5774f), new Vector3f(0.5774f, -0.5774f, -0.5774f)};
		
		
		for (int i = 0; i < 4; i++) {
			double angle = (2*Math.PI) * ((double) i / (double) 4);
			
			final float vx1 = (float) (Math.cos(angle) * width);
			final float vy1 = (float) (Math.sin(angle) * height);
			final float u1 = (vx1 + (width)) / (width * 2);
			final float v1 = (vy1 + (height)) / (height * 2);
			final Vector3f n1 = normals[i];
			
			angle = (2*Math.PI) * ((double) ((i+1)%4) / (double) 4);
			
			final float vx2 = (float) (Math.cos(angle) * width);
			final float vy2 = (float) (Math.sin(angle) * height);
			final float u2 = (vx2 + (width)) / (width * 2);
			final float v2 = (vy2 + (height)) / (height * 2);
			final Vector3f n2 = normals[(i+1)%4];
			
			// For znegative, add in ZN, HIGH ANGLE, LOW ANGLE
			bufferIn.pos(transform, 0, 0, -width).color(red, green, blue, alpha).tex(.5f, .5f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, n1.getX(), n1.getY(), n1.getZ()).endVertex();
			bufferIn.pos(transform, vx2, vy2, 0).color(red, green, blue, alpha).tex(u2, v2).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, n1.getX(), n1.getY(), n1.getZ()).endVertex();
			bufferIn.pos(transform, vx1, vy1, 0).color(red, green, blue, alpha).tex(u1, v1).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, n1.getX(), n1.getY(), n1.getZ()).endVertex();
			
			// for zpositive, add in ZP, LOW ANGLE, HIGH ANGLE
			bufferIn.pos(transform, 0, 0, width).color(red, green, blue, alpha).tex(.5f, .5f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, n2.getX(), n2.getY(), n2.getZ()).endVertex();
			bufferIn.pos(transform, vx1, vy1, 0).color(red, green, blue, alpha).tex(u1, v1).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, n2.getX(), n2.getY(), n2.getZ()).endVertex();
			bufferIn.pos(transform, vx2, vy2, 0).color(red, green, blue, alpha).tex(u2, v2).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, n2.getX(), n2.getY(), n2.getZ()).endVertex();
		}
		
		matrixStackIn.pop();
	}
	
	@Override
	public void setLivingAnimations(EntitySwitchTrigger trigger, float p_78086_2_, float age, float partialTickTime) {
		
	}

	@Override
	public void setRotationAngles(EntitySwitchTrigger entityIn, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		// TODO Auto-generated method stub
		
	}
}
