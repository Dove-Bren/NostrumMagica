package com.smanzana.nostrummagica.client.particles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public abstract class BatchRenderParticle extends Particle implements Comparable<BatchRenderParticle> {

	private int unused; // Remove this whole class and replace with one of the other IParticleRenderTypes (sprite one?)
	
	public BatchRenderParticle(ClientWorld worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
	}
	
	public BatchRenderParticle(ClientWorld worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn);
	}

	/**
	 * Render to the provided vertex buffer in a nice cached manner.
	 * Note: rotation should be saved from original render call
	 * @param renderInfo TODO
	 */
	public abstract void renderBatched(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks);
	
	/**
	 * Return the texture to use when rendering this particle
	 * Note: only called and used once every time the compare function with the last
	 * particle returns non-zero. Not intended to be unique texture per _instance_ of a particle.
	 */
	public abstract ResourceLocation getTexture();
	
	/**
	 * Set up the GL stack to render this type of particle correctly.
	 * Note: only called and used once every time the compare function with the last
	 * particle returns non-zero. Not intended to be unique texture per _instance_ of a particle.
	 */
	public abstract void setupBatchedRender();
	
	/**
	 * Tear down the GL stack to render this type of particle after all of this type have been rendered.
	 * Note: only called and used once every time the compare function with the last
	 * particle returns non-zero. Not intended to be unique texture per _instance_ of a particle.
	 */
	public abstract void teardownBatchedRender();
	
	/**
	 * Check if two particles have the same texture and render setup.
	 * Particle _types_ should return 0 when compared to eachother and non-zero otherwise.
	 * Rendering order is determined by this and shortcuts with texture and render setup
	 * are taken based on the sort order. Any run of particles that this function says are equal
	 * are rendered with the same texture (one call to getTexture()) and  the same render setup
	 * in the same draw call.
	*/
	@Override
	public abstract int compareTo(BatchRenderParticle o);
	
	@Override
	public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
		
		// Just don't render if too far away
		final double maxDistSQ = 60 * 60;
		if (renderInfo.getProjectedView().squareDistanceTo(posX, posY, posZ) < maxDistSQ) {
//			renderParams.rotX = rotationX;
//			renderParams.rotXZ = rotationXZ;
//			renderParams.rotZ = rotationZ;
//			renderParams.rotYZ = rotationYZ;
//			renderParams.rotXY = rotationXY;
//			
//			ParticleBatchRenderer.instance().queueParticle(this);
		}
	}
	
	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.CUSTOM;
	}
	
	public double getPosX() {
		return this.posX;
	}
	
	public double getPosY() {
		return this.posY;
	}
	
	public double getPosZ() {
		return this.posZ;
	}
	
	public static void RenderQuad(IVertexBuilder buffer, BatchRenderParticle particle, ActiveRenderInfo renderInfo, float partialTicks, float scale) {
		Vector3d originPos = renderInfo.getProjectedView();
		final float offsetX = (float)(particle.prevPosX + (particle.getPosX() - particle.prevPosX) * partialTicks - originPos.getX()); // could use MathHelper.lerp
		final float offsetY = (float)(particle.prevPosY + (particle.getPosY() - particle.prevPosY) * partialTicks - originPos.getY());
		final float offsetZ = (float)(particle.prevPosZ + (particle.getPosZ() - particle.prevPosZ) * partialTicks - originPos.getZ());
		final float radius = /*particle.particleScale*/1 * scale;
		final int lightmap = particle.getBrightnessForRender(partialTicks);
		
		final MatrixStack stack = new MatrixStack();
		stack.translate(offsetX, offsetY, offsetZ);
		
		RenderFuncs.renderSpaceQuadFacingCamera(stack, buffer, renderInfo, radius, lightmap, OverlayTexture.NO_OVERLAY, particle.particleRed, particle.particleGreen, particle.particleBlue, particle.particleAlpha);
		
//		buffer.pos(offsetX - (rX * radius) - (rXY * radius), offsetY - (rZ * radius), offsetZ - (rYZ * radius) - (rXZ * radius))
//			.tex(0, 0)
//			.color(particle.particleRed, particle.particleGreen, particle.particleBlue, particle.particleAlpha)
//			.normal(0, 0, 1).endVertex();
//		buffer.pos(offsetX - (rX * radius) + (rXY * radius), offsetY + (rZ * radius), offsetZ - (rYZ * radius) + (rXZ * radius))
//			.tex(0, 1)
//			.color(particle.particleRed, particle.particleGreen, particle.particleBlue, particle.particleAlpha)
//			.normal(0, 0, 1).endVertex();
//		buffer.pos(offsetX + (rX * radius) + (rXY * radius), offsetY + (rZ * radius), offsetZ + (rYZ * radius) + (rXZ * radius))
//			.tex(1, 1)
//			.color(particle.particleRed, particle.particleGreen, particle.particleBlue, particle.particleAlpha)
//			.normal(0, 0, 1).endVertex();
//		buffer.pos(offsetX + (rX * radius) - (rXY * radius), offsetY - (rZ * radius), offsetZ + (rYZ * radius) - (rXZ * radius))
//			.tex(1, 0)
//			.color(particle.particleRed, particle.particleGreen, particle.particleBlue, particle.particleAlpha)
//			.normal(0, 0, 1).endVertex();
		
//		buffer.pos(offsetX - (rX * radius) - (rYZ * radius), offsetY - (rXZ * radius), offsetZ - (rZ * radius) - (rXY * radius))
//			.tex(0, 0)
//			.color(particle.particleRed, particle.particleGreen, particle.particleBlue, particle.particleAlpha)
//			.normal(0, 0, 1).endVertex();
//		buffer.pos(offsetX - (rX * radius) + (rYZ * radius), offsetY + (rXZ * radius), offsetZ - (rZ * radius) + (rXY * radius))
//			.tex(0, 1)
//			.color(particle.particleRed, particle.particleGreen, particle.particleBlue, particle.particleAlpha)
//			.normal(0, 0, 1).endVertex();
//		buffer.pos(offsetX + (rX * radius) + (rYZ * radius), offsetY + (rXZ * radius), offsetZ + (rZ * radius) + (rXY * radius))
//			.tex(1, 1)
//			.color(particle.particleRed, particle.particleGreen, particle.particleBlue, particle.particleAlpha)
//			.normal(0, 0, 1).endVertex();
//		buffer.pos(offsetX + (rX * radius) - (rYZ * radius), offsetY - (rXZ * radius), offsetZ + (rZ * radius) - (rXY * radius))
//			.tex(1, 0)
//			.color(particle.particleRed, particle.particleGreen, particle.particleBlue, particle.particleAlpha)
//			.normal(0, 0, 1).endVertex();
	}
	
}
