package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;

import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class ModelTimedSwitchTrigger extends ModelSwitchTrigger {
	
	private static final float width = .45f;
	private static final float height = .7f;
	
	public ModelTimedSwitchTrigger() {
		;
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		final Matrix4f transform = matrixStackIn.getLast().getMatrix();
		final Matrix3f normal = matrixStackIn.getLast().getNormal();
		final Vector3f[] normals = {new Vector3f(0.5774f, -0.5774f, 0.5774f), new Vector3f(-0.5774f, -0.5774f, 0.5774f), new Vector3f(-0.5774f, -0.5774f, -0.5774f), new Vector3f(0.5774f, -0.5774f, -0.5774f)}; 
		
		matrixStackIn.push();
		
		for (int i = 0; i < 4; i++) {
			double angle = (2*Math.PI) * ((double) i / (double) 4);
			
			final float vx1 = (float) (Math.cos(angle) * width);
			final float vz1 = (float) (Math.sin(angle) * width);
			final float u1 = (vx1 + (width)) / (width * 2);
			final float v1 = (vz1 + (width)) / (width * 2);
			final Vector3f n1 = normals[i];
			
			angle = (2*Math.PI) * ((double) ((i+1)%4) / (double) 4);
			
			final float vx2 = (float) (Math.cos(angle) * width);
			final float vz2 = (float) (Math.sin(angle) * width);
			final float u2 = (vx2 + (width)) / (width * 2);
			final float v2 = (vz2 + (width)) / (width * 2);
			final Vector3f n2 = normals[(i+1)%4];
			
			// For ypositive, add in YP, HIGH ANGLE, LOW ANGLE
			bufferIn.pos(transform, 0, (height/4f), 0).color(red, green, blue, alpha).tex(.5f, .5f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, n1.getX(), n1.getY(), n1.getZ()).endVertex();
			bufferIn.pos(transform, vx2, -height, vz2).color(red, green, blue, alpha).tex(u2, v2).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, n1.getX(), n1.getY(), n1.getZ()).endVertex();
			bufferIn.pos(transform, vx1, -height, vz1).color(red, green, blue, alpha).tex(u1, v1).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, n1.getX(), n1.getY(), n1.getZ()).endVertex();
			
			// for ynegative, add in YN, LOW ANGLE, HIGH ANGLE
			bufferIn.pos(transform, 0, -(height/4f), 0).color(red, green, blue, alpha).tex(.5f, .5f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, n2.getX(), n2.getY(), n2.getZ()).endVertex();
			bufferIn.pos(transform, vx1, height, vz1).color(red, green, blue, alpha).tex(u1, v1).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, n2.getX(), n2.getY(), n2.getZ()).endVertex();
			bufferIn.pos(transform, vx2, height, vz2).color(red, green, blue, alpha).tex(u2, v2).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, n2.getX(), n2.getY(), n2.getZ()).endVertex();
		}
		
		// Top and bottom 'quads'
		bufferIn.pos(transform, -width, height, 0).color(red, green, blue, alpha).tex(0f, 0f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.pos(transform, 0, height, width).color(red, green, blue, alpha).tex(1f, 0f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.pos(transform, width, height, 0).color(red, green, blue, alpha).tex(1f, 1f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		//
		bufferIn.pos(transform, width, height, 0).color(red, green, blue, alpha).tex(1f, 1f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.pos(transform, 0, height, -width).color(red, green, blue, alpha).tex(0f, 1f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.pos(transform, -width, height, 0).color(red, green, blue, alpha).tex(0f, 0f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		
		bufferIn.pos(transform, -width, -height, 0).color(red, green, blue, alpha).tex(1f, 0f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.pos(transform, 0, -height, -width).color(red, green, blue, alpha).tex(1f, 1f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.pos(transform, width, -height, 0).color(red, green, blue, alpha).tex(0f, 1f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		//
		bufferIn.pos(transform, width, -height, 0).color(red, green, blue, alpha).tex(0f, 1f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.pos(transform, 0, -height, width).color(red, green, blue, alpha).tex(0f, 0f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.pos(transform, -width, -height, 0).color(red, green, blue, alpha).tex(1f, 0f).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		
		matrixStackIn.pop();
	}
	
	@Override
	public void setLivingAnimations(EntitySwitchTrigger trigger, float p_78086_2_, float age, float partialTickTime) {
//			
	}
}
