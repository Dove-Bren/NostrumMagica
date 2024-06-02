package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelHookShot;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.entity.EntityHookShot;
import com.smanzana.nostrummagica.util.Projectiles;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class RenderHookShot extends EntityRenderer<EntityHookShot> {
	
	public static final ResourceLocation HOOK_TEXTURE = new ResourceLocation(NostrumMagica.MODID, "textures/block/dungeon_dark.png");
	public static final ResourceLocation CHAIN_TEXTURE = NostrumMagica.Loc("textures/block/spawner.png");

	private ModelHookShot model;
	
	public RenderHookShot(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
		
		this.model = new ModelHookShot();
	}

	@Override
	public ResourceLocation getEntityTexture(EntityHookShot entity) {
		return HOOK_TEXTURE;
	}
	
	@Override
	public boolean shouldRender(EntityHookShot livingEntity, ClippingHelper camera, double camX, double camY, double camZ) {
		return true;
	}
	
	private void renderChain(MatrixStack matrixStackIn, IVertexBuilder wr, Vector3f cordOffset, float segments, Vector3f perSeg) {
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
		final Matrix4f transform = matrixStackIn.getLast().getMatrix();
		
//		// Define first two vertices
//		// Note: UV is always the top here, but when going through the list, flips back and forth.
//		wr.pos(0 - (cordOffset.x / 2.0), 0 - (cordOffset.y / 2), 0 - (cordOffset.z / 2)).tex(0, 0).endVertex();
//		wr.pos(0 + (cordOffset.x / 2.0), 0 + (cordOffset.y / 2), 0 + (cordOffset.z / 2)).tex(1, 0).endVertex();
//		texFlip = true;

		// Broke down into two cases instead of generalizing to make common case easier. Is this slower or faster?
		for (int i = 0; i < wholeSegments + 1; i++) {
			vl = (texFlip ? 0 : 1);
			rlx = ((i) * perSeg.getX());
			rly = ((i) * perSeg.getY());
			rlz = ((i) * perSeg.getZ());
			
			if (i == wholeSegments) {
				// last piece which is likely a partial piece
				vh = (float) (texFlip ? partialSegment : (1.0 - partialSegment));
				rhx = ((i + partialSegment) * perSeg.getX());
				rhy = ((i + partialSegment) * perSeg.getY());
				rhz = ((i + partialSegment) * perSeg.getZ());
			} else {
				vh = (texFlip ? 1 : 0);
				rhx = ((i + 1) * perSeg.getX());
				rhy = ((i + 1) * perSeg.getY());
				rhz = ((i + 1) * perSeg.getZ());
			}
			
			wr.pos(transform, rlx - (cordOffset.getX() / 2), rly - (cordOffset.getY() / 2), rlz - (cordOffset.getZ() / 2)).tex(0, vl).endVertex();
			wr.pos(transform, rlx + (cordOffset.getX() / 2), rly + (cordOffset.getY() / 2), rlz + (cordOffset.getZ() / 2)).tex(1, vl).endVertex();
			wr.pos(transform, rhx + (cordOffset.getX() / 2), rhy + (cordOffset.getY() / 2), rhz + (cordOffset.getZ() / 2)).tex(1, vh).endVertex();
			wr.pos(transform, rhx - (cordOffset.getX() / 2), rhy - (cordOffset.getY() / 2), rhz - (cordOffset.getZ() / 2)).tex(0, vh).endVertex();
			texFlip = !texFlip;
		}
	}
	
	@Override
	public void render(EntityHookShot entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		final float texLen = .2f;
		final float chainWidth = .1f;
		final LivingEntity shooter = entity.getCaster();
		
		matrixStackIn.push();
		
		// First, render chain
		if (shooter != null) {
			Vector3d offset = Projectiles.getVectorForRotation(shooter.rotationPitch - 90f, shooter.rotationYawHead + 90f).scale(.1);
			final Vector3d diff = shooter.getEyePosition(partialTicks).add(offset).subtract(entity.getEyePosition(partialTicks));
			final float totalLength = (float) diff.distanceTo(new Vector3d(0,0,0));
			final float segments = totalLength / texLen;
			final Vector3d perSegD = diff.scale(1.0/segments);
			final Vector3f cordOffset = new Vector3f(perSegD.normalize().scale(chainWidth).rotateYaw(90f));
			final Vector3f cordVOffset = new Vector3f(perSegD.normalize().scale(chainWidth).rotatePitch(90f));
			final Vector3f perSeg = new Vector3f(perSegD);
			
			// Want some sort of width
			// TODO rotate depending on the direction chain is instead of always horizontal + updown
			
			// Our texture is symmetric up and down, so we'll cheat and use a quad strip and just flip
			// UVs depending on where we're at in the chain
			IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.HOOKSHOT_CHAIN);
			renderChain(matrixStackIn, buffer, cordOffset, segments, perSeg);
			renderChain(matrixStackIn, buffer, cordVOffset, segments, perSeg);
		}
		
		// then, render hook
		IVertexBuilder buffer = bufferIn.getBuffer(model.getRenderType(this.getEntityTexture(entity)));
		model.render(matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		
		matrixStackIn.pop();
	}
	
}
