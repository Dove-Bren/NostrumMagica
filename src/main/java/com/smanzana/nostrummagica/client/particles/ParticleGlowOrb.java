package com.smanzana.nostrummagica.client.particles;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleGlowOrb extends BatchRenderParticle {
	
	private static final ResourceLocation TEX_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/effects/glow_orb.png");
	
	protected final float maxAlpha;
	
	public ParticleGlowOrb(World worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime) {
		super(worldIn, x, y, z, 0, 0, 0);
		
		particleRed = red;
		particleGreen = green;
		particleBlue = blue;
		particleAlpha = 0f;
		this.maxAlpha = alpha;
		particleMaxAge = lifetime;
	}
	
	public ParticleGlowOrb setFloats(boolean floats) {
		return setFloatStrength(floats ? -.01f : 0);
	}
	
	public ParticleGlowOrb setFloatStrength(float strength) {
		particleGravity = strength;
		return this;
	}
	
	public ParticleGlowOrb setMotion(Vec3d motion) {
		return this.setMotion(motion.xCoord, motion.yCoord, motion.zCoord);
	}
	
	public ParticleGlowOrb setMotion(double xVelocity, double yVelocity, double zVelocity) {
		this.motionX = xVelocity;
		this.motionY = yVelocity;
		this.motionZ = zVelocity;
		return this;
	}
	
	@Override
	public ResourceLocation getTexture() {
		return TEX_LOC;
	}

	@Override
	public void setupRender() {
		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableAlpha();
		GlStateManager.enableAlpha();
		GlStateManager.enableLighting();
		GlStateManager.disableLighting();
		GlStateManager.alphaFunc(516, 0);
		GlStateManager.color(1f, 1f, 1f, .75f);
		GlStateManager.depthMask(false);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
	}
	
	@Override
	public int hashCode() {
		return TEX_LOC.hashCode() * 419 + 5119;
	}

	@Override
	public int compareTo(BatchRenderParticle o) {
		return hashCode() - o.hashCode();
	}

	@Override
	public void renderBatched(VertexBuffer buffer, float partialTicks) {
		BatchRenderParticle.RenderQuad(buffer, this, renderParams, partialTicks, .1f);
		BatchRenderParticle.RenderQuad(buffer, this, renderParams, partialTicks, .05f);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (this.particleAge < 20) {
			// fade in in first second
			this.particleAlpha = ((float) particleAge / 20f);
		} else if (this.particleAge >= this.particleMaxAge - 20f) {
			// Fade out in last second
			this.particleAlpha = ((float) (particleMaxAge - particleAge) / 20f);
		} else {
			this.particleAlpha = 1f;
		}
		
		this.particleAlpha *= maxAlpha;
	}

}
