package com.smanzana.nostrummagica.client.render.entity;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityHookShot;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class RenderHookShot extends Render<EntityHookShot> {

	private ModelHookShot model;
	
	public RenderHookShot(RenderManager renderManagerIn) {
		super(renderManagerIn);
		
		this.model = new ModelHookShot();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityHookShot entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/blocks/dungeon_dark.png"
				);
	}
	
	@Override
	public boolean shouldRender(EntityHookShot livingEntity, ICamera camera, double camX, double camY, double camZ) {
		return true;
	}
	
	private void renderChain(BufferBuilder wr, Vec3d cordOffset, double segments, Vec3d perSeg) {
		final int wholeSegments = (int) segments;
		final double partialSegment = segments - wholeSegments;
		double v;
		double rx;
		double ry;
		double rz;
		boolean texFlip;
		
		wr.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_TEX);
		
		// Do first two vertices
		// Note: UV is always the top here, but when going through the list, flips back and forth.
		wr.pos(0 - (cordOffset.x / 2.0), 0 - (cordOffset.y / 2), 0 - (cordOffset.z / 2)).tex(0, 0).endVertex();
		wr.pos(0 + (cordOffset.x / 2.0), 0 + (cordOffset.y / 2), 0 + (cordOffset.z / 2)).tex(1, 0).endVertex();
		texFlip = true;

		// Broke down into two cases instead of generalizing to make common case easier. Is this slower or faster?
		for (int i = 0; i < wholeSegments + 1; i++) {
			if (i == wholeSegments) {
				// last piece which is likely a partial piece
				v = (texFlip ? partialSegment : (1.0 - partialSegment));
				rx = ((i + partialSegment) * perSeg.x);
				ry = ((i + partialSegment) * perSeg.y);
				rz = ((i + partialSegment) * perSeg.z);
			} else {
				v = (texFlip ? 1 : 0);
				rx = ((i + 1) * perSeg.x);
				ry = ((i + 1) * perSeg.y);
				rz = ((i + 1) * perSeg.z);
			}
			
			wr.pos(rx - (cordOffset.x / 2), ry - (cordOffset.y / 2), rz - (cordOffset.z / 2)).tex(0, v).endVertex();
			wr.pos(rx + (cordOffset.x / 2), ry + (cordOffset.y / 2), rz + (cordOffset.z / 2)).tex(1, v).endVertex();
			texFlip = !texFlip;
		}
		Tessellator.getInstance().draw();
	}
	
	@Override
	public void doRender(EntityHookShot entity, double x, double y, double z, float entityYaw, float partialTicks) {
		
		final double texLen = .2;
		final double chainWidth = .1;
		
		BufferBuilder wr = Tessellator.getInstance().getBuffer();
		GlStateManager.pushMatrix();
		
		GlStateManager.translate(x, y, z);
		//GlStateManager.enableAlpha();
		
		// First, render chain
		EntityLivingBase shooter = entity.getCaster();
		if (shooter != null) {
			Vec3d offset = ProjectileTrigger.getVectorForRotation(shooter.rotationPitch - 90f, shooter.rotationYawHead + 90f).scale(.1);
			final Vec3d diff = shooter.getPositionEyes(partialTicks).add(offset).subtract(entity.getPositionEyes(partialTicks));
			final double totalLength = diff.distanceTo(new Vec3d(0,0,0));
			final double segments = totalLength / texLen;
			final Vec3d perSeg = diff.scale(1.0/segments);
			final Vec3d cordOffset = perSeg.normalize().scale(chainWidth).rotateYaw(90f);
			final Vec3d cordVOffset = perSeg.normalize().scale(chainWidth).rotatePitch(90f);
			
			// Want some sort of width
			// TODO rotate depending on the direction chain is instead of always horizontal + updown
			
			// Our texture is symmetric up and down, so we'll cheat and use a quad strip and just flip
			// UVs depending on where we're at in the chain
		
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(NostrumMagica.MODID,
					"textures/blocks/spawner.png"
					));
			
			//GlStateManager.disableDepth();
			GlStateManager.color(1f, 1f, 1f, 1f);
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableBlend();
			GlStateManager.disableCull();
			GlStateManager.disableLighting();
			renderChain(wr, cordOffset, segments, perSeg);
			renderChain(wr, cordVOffset, segments, perSeg);
			
			GlStateManager.enableLighting();
			GlStateManager.enableCull();
			Minecraft.getMinecraft().getTextureManager().bindTexture(getEntityTexture(entity));
		}
		
		GlStateManager.translate(0, -.5, 0);
		// then, render hook
		model.render(entity, partialTicks, 0f, 0f, 0f, 0f, 1f);
		
		GlStateManager.popMatrix();
	}
	
}
