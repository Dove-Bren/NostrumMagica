package com.smanzana.nostrummagica.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class BeamRenderer {
	
	public static final ResourceLocation TEX_BEAM = BeaconRenderer.BEAM_LOCATION;
	
	public static void renderToBuffer(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, int packedOverlayIn, int color, Vec3 start, Vec3 end, float radius, double ageTicks) {
		BeamRenderer.renderToBuffer(matrixStackIn, bufferIn.getBuffer(RenderType.beaconBeam(TEX_BEAM, true)), packedLightIn, packedOverlayIn, color, start, end, radius, ageTicks);
		
		BeamRenderer.renderToBuffer(matrixStackIn, bufferIn.getBuffer(RenderType.beaconBeam(TEX_BEAM, false)), packedLightIn, packedOverlayIn, color, start, end, radius * .5f, ageTicks);
	}

	public static void renderToBuffer(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, int color, Vec3 start, Vec3 end, float radius, double ageTicks) {
		final float beamVPeriod = 4f; // in ticks
		final float beamVProg = (float) (ageTicks / beamVPeriod);
		
		final double dist = end.subtract(start).length();
		
		// We want the texture to last for .25[radius]*4 intervals which matches vanilla beacon (stretched 4x in one direction)
		final float vMax = (float) (dist / (double)(radius * 4));
		final float vMin = beamVProg * (radius * 4);
		
		drawBeamInner(matrixStackIn, buffer, start, end, -vMin, -vMin + vMax, radius, color);
	}
	
	protected static void drawBeamInner(PoseStack matrixStackIn, VertexConsumer buffer, Vec3 minPos, Vec3 maxPos, float vMin, float vMax, float radius, int color) {
		Matrix4f transform = matrixStackIn.last().pose();
		Matrix3f normal = matrixStackIn.last().normal();
		
		// We want to draw the quads a certain distance out instead of directly at minPos to maxPos (or it would be a line)
		Vec3 normalRay1 = maxPos.subtract(minPos).cross(new Vec3(0, 1, 1).normalize()).normalize().scale(radius);
		Vec3 normalRay2 = maxPos.subtract(minPos).cross(normalRay1).normalize().scale(radius);
		
		Vector3f minPos1 = new Vector3f(minPos.add(normalRay1.scale(1)).add(normalRay2.scale(1)));
		Vector3f minPos2 = new Vector3f(minPos.add(normalRay1.scale(-1)).add(normalRay2.scale(1)));
		Vector3f minPos3 = new Vector3f(minPos.add(normalRay1.scale(-1)).add(normalRay2.scale(-1)));
		Vector3f minPos4 = new Vector3f(minPos.add(normalRay1.scale(1)).add(normalRay2.scale(-1)));
		
		Vector3f maxPos1 = new Vector3f(maxPos.add(normalRay1.scale(1)).add(normalRay2.scale(1)));
		Vector3f maxPos2 = new Vector3f(maxPos.add(normalRay1.scale(-1)).add(normalRay2.scale(1)));
		Vector3f maxPos3 = new Vector3f(maxPos.add(normalRay1.scale(-1)).add(normalRay2.scale(-1)));
		Vector3f maxPos4 = new Vector3f(maxPos.add(normalRay1.scale(1)).add(normalRay2.scale(-1)));
		
		drawBeamQuad(transform, normal, buffer, minPos1, minPos2, maxPos2, maxPos1, vMin, vMax, color);
		drawBeamQuad(transform, normal, buffer, minPos2, minPos3, maxPos3, maxPos2, vMin, vMax, color);
		drawBeamQuad(transform, normal, buffer, minPos3, minPos4, maxPos4, maxPos3, vMin, vMax, color);
		drawBeamQuad(transform, normal, buffer, minPos4, minPos1, maxPos1, maxPos4, vMin, vMax, color);
		
		// draw caps
		drawBeamQuad(transform, normal, buffer, minPos4, minPos3, minPos2, minPos1, 0, 1, color);
		drawBeamQuad(transform, normal, buffer, maxPos1, maxPos2, maxPos3, maxPos4, 0, 1, color);
	}
	
	protected static void drawBeamQuad(Matrix4f transform, Matrix3f normal, VertexConsumer buffer, Vector3f pos1, Vector3f pos2, Vector3f pos3, Vector3f pos4, float vMin, float vMax, int color) {
		Vector3f normVect = new Vector3f(0, 1, 0); // I think I know how to calculate the face vector here if that ends up being useful?
		
		addVertex(transform, normal, buffer, pos1, 0, vMin, normVect, color);
		addVertex(transform, normal, buffer, pos2, 1, vMin, normVect, color);
		addVertex(transform, normal, buffer, pos3, 1, vMax, normVect, color);
		addVertex(transform, normal, buffer, pos4, 0, vMax, normVect, color);
	}
	
	protected static void addVertex(Matrix4f transform, Matrix3f normal, VertexConsumer buffer, Vector3f pos, float u, float v, Vector3f normVect, int color) {
		buffer.vertex(transform, pos.x(), pos.y(), pos.z())
				.color(color)
				.uv(u, v)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(15728880) // bright cause emmissive
				.normal(normal, normVect.x(), normVect.y(), normVect.z())
			.endVertex();
	}
	
}
