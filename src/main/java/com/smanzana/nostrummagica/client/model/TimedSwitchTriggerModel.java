package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.entity.SwitchTriggerEntity;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

public class TimedSwitchTriggerModel extends SwitchTriggerModel {
	
	private static final float width = .45f;
	private static final float height = .7f;
	
	public TimedSwitchTriggerModel() {
		;
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		final Matrix4f transform = matrixStackIn.last().pose();
		final Matrix3f normal = matrixStackIn.last().normal();
		final Vector3f[] normals = {new Vector3f(0.5774f, -0.5774f, 0.5774f), new Vector3f(-0.5774f, -0.5774f, 0.5774f), new Vector3f(-0.5774f, -0.5774f, -0.5774f), new Vector3f(0.5774f, -0.5774f, -0.5774f)}; 
		
		matrixStackIn.pushPose();
		
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
			bufferIn.vertex(transform, 0, (height/4f), 0).color(red, green, blue, alpha).uv(.5f, .5f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, n1.x(), n1.y(), n1.z()).endVertex();
			bufferIn.vertex(transform, vx2, -height, vz2).color(red, green, blue, alpha).uv(u2, v2).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, n1.x(), n1.y(), n1.z()).endVertex();
			bufferIn.vertex(transform, vx1, -height, vz1).color(red, green, blue, alpha).uv(u1, v1).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, n1.x(), n1.y(), n1.z()).endVertex();
			
			// for ynegative, add in YN, LOW ANGLE, HIGH ANGLE
			bufferIn.vertex(transform, 0, -(height/4f), 0).color(red, green, blue, alpha).uv(.5f, .5f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, n2.x(), n2.y(), n2.z()).endVertex();
			bufferIn.vertex(transform, vx1, height, vz1).color(red, green, blue, alpha).uv(u1, v1).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, n2.x(), n2.y(), n2.z()).endVertex();
			bufferIn.vertex(transform, vx2, height, vz2).color(red, green, blue, alpha).uv(u2, v2).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, n2.x(), n2.y(), n2.z()).endVertex();
		}
		
		// Top and bottom 'quads'
		bufferIn.vertex(transform, -width, height, 0).color(red, green, blue, alpha).uv(0f, 0f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.vertex(transform, 0, height, width).color(red, green, blue, alpha).uv(1f, 0f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.vertex(transform, width, height, 0).color(red, green, blue, alpha).uv(1f, 1f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		//
		bufferIn.vertex(transform, width, height, 0).color(red, green, blue, alpha).uv(1f, 1f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.vertex(transform, 0, height, -width).color(red, green, blue, alpha).uv(0f, 1f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.vertex(transform, -width, height, 0).color(red, green, blue, alpha).uv(0f, 0f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		
		bufferIn.vertex(transform, -width, -height, 0).color(red, green, blue, alpha).uv(1f, 0f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.vertex(transform, 0, -height, -width).color(red, green, blue, alpha).uv(1f, 1f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.vertex(transform, width, -height, 0).color(red, green, blue, alpha).uv(0f, 1f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		//
		bufferIn.vertex(transform, width, -height, 0).color(red, green, blue, alpha).uv(0f, 1f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.vertex(transform, 0, -height, width).color(red, green, blue, alpha).uv(0f, 0f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		bufferIn.vertex(transform, -width, -height, 0).color(red, green, blue, alpha).uv(1f, 0f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 1f, 0).endVertex();
		
		matrixStackIn.popPose();
	}
	
	@Override
	public void prepareMobModel(SwitchTriggerEntity trigger, float p_78086_2_, float age, float partialTickTime) {
//			
	}
}
