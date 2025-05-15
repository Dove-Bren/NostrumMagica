package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Effect made up of multiple copies of another effect
 * @author Skyler
 *
 */
public class ClientEffectBeam extends ClientEffect {
		
	public static final ResourceLocation TEX_BEAM = BeaconRenderer.BEAM_LOCATION;

	private static class BeamForm implements ClientEffectForm {

		private Vec3 end;
		
		public BeamForm(Vec3 end) {
			this.setEnd(end);
		}
		
		public void setEnd(Vec3 end) {
			this.end = end;
		}
		
		@Override
		public void draw(PoseStack matrixStackIn, Minecraft mc, MultiBufferSource buffersIn, float partialTicks, int color, float progress) {
			VertexConsumer buffer = buffersIn.getBuffer(RenderType.beaconBeam(TEX_BEAM, false));//NostrumRenderTypes.SPELL_BEAM_SOLID);
			
			final float beamVPeriod = 4f; // in ticks
			final float beamVProg = ((float)(mc.level.getGameTime() % beamVPeriod) + partialTicks) / beamVPeriod;
			
			final double dist = end.length();
			
			// We want radius to be .125f at peak, but grow into it and fade out of it.
			final float radius = Mth.sin(progress * Mth.PI) * .125f; // note only doing first half of circle for 0->1->0
			// We want the texture to last for .25[radius]*4 intervals which matches vanilla beacon (stretched 4x in one direction)
			final float vMax = (float) (dist / (double)(radius * 4));
			final float vMin = beamVProg * (radius * 4);
			
			drawBeamInner(matrixStackIn, buffer, Vec3.ZERO, end, -vMin, -vMin + vMax, radius, color);
		}
		
		protected void drawBeamInner(PoseStack matrixStackIn, VertexConsumer buffer, Vec3 minPos, Vec3 maxPos, float vMin, float vMax, float radius, int color) {
			Matrix4f transform = matrixStackIn.last().pose();
			Matrix3f normal = matrixStackIn.last().normal();
			
			// We want to draw the quads a certain distance out instead of directly at minPos to maxPos (or it would be a line)
			Vec3 normalRay1 = maxPos.subtract(minPos).cross(new Vec3(0, 0, 1)).normalize().scale(radius);
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
		
		protected void drawBeamQuad(Matrix4f transform, Matrix3f normal, VertexConsumer buffer, Vector3f pos1, Vector3f pos2, Vector3f pos3, Vector3f pos4, float vMin, float vMax, int color) {
			Vector3f normVect = new Vector3f(0, 1, 0); // I think I know how to calculate the face vector here if that ends up being useful?
			
			addVertex(transform, normal, buffer, pos1, 0, vMin, normVect, color);
			addVertex(transform, normal, buffer, pos2, 1, vMin, normVect, color);
			addVertex(transform, normal, buffer, pos3, 1, vMax, normVect, color);
			addVertex(transform, normal, buffer, pos4, 0, vMax, normVect, color);
		}
		
		protected void addVertex(Matrix4f transform, Matrix3f normal, VertexConsumer buffer, Vector3f pos, float u, float v, Vector3f normVect, int color) {
			buffer.vertex(transform, pos.x(), pos.y(), pos.z())
					.color(color)
					.uv(u, v)
					.overlayCoords(OverlayTexture.NO_OVERLAY)
					.uv2(15728880) // bright cause emmissive
					.normal(normal, normVect.x(), normVect.y(), normVect.z())
				.endVertex();
		}
		
	}
	
	public ClientEffectBeam(Vec3 origin, Vec3 end, int ticks) {
		super(origin, new BeamForm(end.subtract(origin)), ticks);
	}
	
	public ClientEffectBeam(Vec3 origin, Vec3 end, long ms) {
		super(origin, new BeamForm(end.subtract(origin)), ms);
	}
	
}
