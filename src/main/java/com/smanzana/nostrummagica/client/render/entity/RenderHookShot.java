package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelHookShot;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.entity.HookShotEntity;
import com.smanzana.nostrummagica.util.Projectiles;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class RenderHookShot extends EntityRenderer<HookShotEntity> {
	
	public static final ResourceLocation HOOK_TEXTURE = new ResourceLocation(NostrumMagica.MODID, "textures/block/dungeon_dark.png");
	public static final ResourceLocation CHAIN_TEXTURE = NostrumMagica.Loc("textures/block/spawner.png");

	private ModelHookShot model;
	
	public RenderHookShot(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
		
		this.model = new ModelHookShot();
	}

	@Override
	public ResourceLocation getTextureLocation(HookShotEntity entity) {
		return HOOK_TEXTURE;
	}
	
	@Override
	public boolean shouldRender(HookShotEntity livingEntity, Frustum camera, double camX, double camY, double camZ) {
		return true;
	}
	
	private void renderChain(PoseStack matrixStackIn, VertexConsumer wr, Vector3f cordOffset, float segments, Vector3f perSeg) {
		final int wholeSegments = (int) segments;
		final float partialSegment = segments - wholeSegments;
		float vh;
		float rhx;
		float rhy;
		float rhz;
		float vl;
		float rlx;
		float rly;
		float rlz;
		boolean texFlip = false;
		final Matrix4f transform = matrixStackIn.last().pose();
		
//		// Define first two vertices
//		// Note: UV is always the top here, but when going through the list, flips back and forth.
//		wr.pos(0 - (cordOffset.x / 2.0), 0 - (cordOffset.y / 2), 0 - (cordOffset.z / 2)).tex(0, 0).endVertex();
//		wr.pos(0 + (cordOffset.x / 2.0), 0 + (cordOffset.y / 2), 0 + (cordOffset.z / 2)).tex(1, 0).endVertex();
//		texFlip = true;

		// Broke down into two cases instead of generalizing to make common case easier. Is this slower or faster?
		for (int i = 0; i < wholeSegments + 1; i++) {
			vl = (texFlip ? 0 : 1);
			rlx = ((i) * perSeg.x());
			rly = ((i) * perSeg.y());
			rlz = ((i) * perSeg.z());
			
			if (i == wholeSegments) {
				// last piece which is likely a partial piece
				vh = (float) (texFlip ? partialSegment : (1.0 - partialSegment));
				rhx = ((i + partialSegment) * perSeg.x());
				rhy = ((i + partialSegment) * perSeg.y());
				rhz = ((i + partialSegment) * perSeg.z());
			} else {
				vh = (texFlip ? 1 : 0);
				rhx = ((i + 1) * perSeg.x());
				rhy = ((i + 1) * perSeg.y());
				rhz = ((i + 1) * perSeg.z());
			}
			
			wr.vertex(transform, rlx - (cordOffset.x() / 2), rly - (cordOffset.y() / 2), rlz - (cordOffset.z() / 2)).uv(0, vl).endVertex();
			wr.vertex(transform, rlx + (cordOffset.x() / 2), rly + (cordOffset.y() / 2), rlz + (cordOffset.z() / 2)).uv(1, vl).endVertex();
			wr.vertex(transform, rhx + (cordOffset.x() / 2), rhy + (cordOffset.y() / 2), rhz + (cordOffset.z() / 2)).uv(1, vh).endVertex();
			wr.vertex(transform, rhx - (cordOffset.x() / 2), rhy - (cordOffset.y() / 2), rhz - (cordOffset.z() / 2)).uv(0, vh).endVertex();
			texFlip = !texFlip;
		}
	}
	
	@Override
	public void render(HookShotEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		final float texLen = .2f;
		final float chainWidth = .1f;
		final LivingEntity shooter = entity.getCaster();
		
		matrixStackIn.pushPose();
		
		// First, render chain
		if (shooter != null) {
			Vec3 offset = Projectiles.getVectorForRotation(shooter.getXRot() - 90f, shooter.yHeadRot + 90f).scale(.1);
			final Vec3 diff = shooter.getEyePosition(partialTicks).add(offset).subtract(entity.getEyePosition(partialTicks));
			final float totalLength = (float) diff.distanceTo(new Vec3(0,0,0));
			final float segments = totalLength / texLen;
			final Vec3 perSegD = diff.scale(1.0/segments);
			final Vector3f cordOffset = new Vector3f(perSegD.normalize().scale(chainWidth).yRot(90f));
			final Vector3f cordVOffset = new Vector3f(perSegD.normalize().scale(chainWidth).xRot(90f));
			final Vector3f perSeg = new Vector3f(perSegD);
			
			// Want some sort of width
			// TODO rotate depending on the direction chain is instead of always horizontal + updown
			
			// Our texture is symmetric up and down, so we'll cheat and use a quad strip and just flip
			// UVs depending on where we're at in the chain
			VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.HOOKSHOT_CHAIN);
			renderChain(matrixStackIn, buffer, cordOffset, segments, perSeg);
			renderChain(matrixStackIn, buffer, cordVOffset, segments, perSeg);
		}
		
		// then, render hook
		VertexConsumer buffer = bufferIn.getBuffer(model.renderType(this.getTextureLocation(entity)));
		model.renderToBuffer(matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		
		matrixStackIn.popPose();
	}
	
}
