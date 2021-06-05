package com.smanzana.nostrummagica.client.particles;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public abstract class BatchRenderParticle extends Particle implements Comparable<BatchRenderParticle> {

	protected final RenderParams renderParams;
	
	public BatchRenderParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
		this.renderParams = new RenderParams();
	}
	
	public BatchRenderParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn);
		this.renderParams = new RenderParams();
	}

	/**
	 * Render to the provided vertex buffer in a nice cached manner.
	 * Note: rotation should be saved from original render call
	 */
	public abstract void renderBatched(VertexBuffer wr, float partialTicks);
	
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
	public abstract void setupRender();
	
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
	public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationXZ, float rotationZ, float rotationYZ, float rotationXY) {
		renderParams.rotX = rotationX;
		renderParams.rotXZ = rotationXZ;
		renderParams.rotZ = rotationZ;
		renderParams.rotYZ = rotationYZ;
		renderParams.rotXY = rotationXY;
		
		ParticleBatchRenderer.instance().queueParticle(this);
	}
	
	/**
	 * Utility class for particles to stash their render params
	 */
	public static final class RenderParams {
		public float rotX;
		public float rotXZ;
		public float rotZ;
		public float rotYZ;
		public float rotXY;
	}
	
	public static void RenderQuad(VertexBuffer buffer, BatchRenderParticle particle, RenderParams params, float partialTicks, float scale) {
		final float offsetX = (float)(particle.prevPosX + (particle.posX - particle.prevPosX) * partialTicks - Particle.interpPosX);
		final float offsetY = (float)(particle.prevPosY + (particle.posY - particle.prevPosY) * partialTicks - Particle.interpPosY);
		final float offsetZ = (float)(particle.prevPosZ + (particle.posZ - particle.prevPosZ) * partialTicks - Particle.interpPosZ);
		final float rX = params.rotX;
		final float rXZ = params.rotXZ;
		final float rZ = params.rotZ;
		final float rYZ = params.rotYZ;
		final float rXY = params.rotXY;
		final float radius = particle.particleScale * scale;
		
		
		buffer.pos(offsetX - (rX * radius) - (rYZ * radius), offsetY - (rXZ * radius), offsetZ - (rZ * radius) - (rXY * radius))
			.tex(0, 0)
			.color(particle.particleRed, particle.particleGreen, particle.particleBlue, particle.particleAlpha)
			.normal(0, 0, 1).endVertex();
		buffer.pos(offsetX - (rX * radius) + (rYZ * radius), offsetY + (rXZ * radius), offsetZ - (rZ * radius) + (rXY * radius))
			.tex(0, 1)
			.color(particle.particleRed, particle.particleGreen, particle.particleBlue, particle.particleAlpha)
			.normal(0, 0, 1).endVertex();
		buffer.pos(offsetX + (rX * radius) + (rYZ * radius), offsetY + (rXZ * radius), offsetZ + (rZ * radius) + (rXY * radius))
			.tex(1, 1)
			.color(particle.particleRed, particle.particleGreen, particle.particleBlue, particle.particleAlpha)
			.normal(0, 0, 1).endVertex();
		buffer.pos(offsetX + (rX * radius) - (rYZ * radius), offsetY - (rXZ * radius), offsetZ + (rZ * radius) - (rXY * radius))
			.tex(1, 0)
			.color(particle.particleRed, particle.particleGreen, particle.particleBlue, particle.particleAlpha)
			.normal(0, 0, 1).endVertex();
	}
	
}
