package com.smanzana.nostrummagica.client.particles;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.utils.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleLightningStatic extends BatchRenderParticle {
	
	private static final ResourceLocation[] TEX_1_LOCS = new ResourceLocation[] {
			new ResourceLocation(NostrumMagica.MODID, "textures/effects/lightning1_0.png"),
			new ResourceLocation(NostrumMagica.MODID, "textures/effects/lightning1_1.png"),
			new ResourceLocation(NostrumMagica.MODID, "textures/effects/lightning1_2.png"),
			new ResourceLocation(NostrumMagica.MODID, "textures/effects/lightning1_3.png"),
	};
	private static final ResourceLocation[] TEX_2_LOCS = new ResourceLocation[] {
			new ResourceLocation(NostrumMagica.MODID, "textures/effects/lightning2_0.png"),
			new ResourceLocation(NostrumMagica.MODID, "textures/effects/lightning2_1.png"),
			new ResourceLocation(NostrumMagica.MODID, "textures/effects/lightning2_2.png"),
			new ResourceLocation(NostrumMagica.MODID, "textures/effects/lightning2_3.png"),
	};
	
	protected final float maxAlpha;
	protected Vec3d targetPos;
	protected Entity targetEntity;
	
	protected int type;
	protected int ticksExisted;
	
	public ParticleLightningStatic(World worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime) {
		super(worldIn, x, y, z, 0, 0, 0);
		
		particleRed = red;
		particleGreen = green;
		particleBlue = blue;
		particleAlpha = 0f;
		this.maxAlpha = alpha;
		particleMaxAge = lifetime;
		
		type = NostrumMagica.rand.nextInt(2);
	}
	
	public ParticleLightningStatic setFloats(boolean floats) {
		return setFloatStrength(floats ? -.01f : 0);
	}
	
	public ParticleLightningStatic setFloatStrength(float strength) {
		particleGravity = strength;
		return this;
	}
	
	public ParticleLightningStatic setMotion(Vec3d motion) {
		return this.setMotion(motion.xCoord, motion.yCoord, motion.zCoord);
	}
	
	public ParticleLightningStatic setMotion(double xVelocity, double yVelocity, double zVelocity) {
		this.motionX = xVelocity;
		this.motionY = yVelocity;
		this.motionZ = zVelocity;
		return this;
	}
	
	public ParticleLightningStatic setTarget(Entity ent) {
		targetEntity = ent;
		return this;
	}
	
	public ParticleLightningStatic setTarget(Vec3d targetPos) {
		this.targetPos = targetPos;
		return this;
	}
	
	protected float getDisplayProgress() {
		return (float) particleAge / (float) particleMaxAge;
	}
	
	protected static int GetDisplayFrame(float progress, int count) {
		return progress < 1f ? (int) (progress * count) : count-1;
	}
	
	protected ResourceLocation[] getAnimationTextures(int type) {
		switch (type) {
		default:
		case 0: return TEX_1_LOCS;
		case 1: return TEX_2_LOCS;
		}
	}
	
	@Override
	public ResourceLocation getTexture() {
		final ResourceLocation[] textures = getAnimationTextures(type);
		return textures[GetDisplayFrame(getDisplayProgress(), textures.length)];
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
		final ResourceLocation[] textures = getAnimationTextures(type);
		final int idx = GetDisplayFrame(getDisplayProgress(), textures.length);
		return type * 419 + 311 * idx + 19;
	}

	@Override
	public int compareTo(BatchRenderParticle o) {
		return hashCode() - o.hashCode();
	}

	@Override
	public void renderBatched(VertexBuffer buffer, float partialTicks) {
		BatchRenderParticle.RenderQuad(buffer, this, renderParams, partialTicks, .05f);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (this.particleAge < 20) {
			// fade in in first second
			this.particleAlpha = ((float) particleAge / 20f);
//		} else if (this.particleAge >= this.particleMaxAge - 20f) {
//			// Fade out in last second
//			this.particleAlpha = ((float) (particleMaxAge - particleAge) / 20f);
		} else {
			this.particleAlpha = 1f;
		}
		
		this.particleAlpha *= maxAlpha;
		
		if (targetEntity != null && !targetEntity.isDead) {
			Vec3d curVelocity = new Vec3d(this.motionX, this.motionY, this.motionZ);
			Vec3d posDelta = targetEntity.getPositionVector().addVector(0, targetEntity.height/2, 0).subtract(posX, posY, posZ);
			Vec3d idealVelocity = posDelta.normalize().scale(.3);
			this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
		} else if (targetPos != null) {
			Vec3d curVelocity = new Vec3d(this.motionX, this.motionY, this.motionZ);
			Vec3d posDelta = targetPos.subtract(posX, posY, posZ);
			Vec3d idealVelocity = posDelta.normalize().scale(.3);
			this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
		}
	}
	
	public static final class Factory implements INostrumParticleFactory<ParticleLightningStatic> {

		@Override
		public ParticleLightningStatic createParticle(World world, SpawnParams params) {
			ParticleLightningStatic particle = null;
			for (int i = 0; i < params.count; i++) {
				final double spawnX = params.spawnX + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
				final double spawnY = params.spawnY + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
				final double spawnZ = params.spawnZ + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
				final float[] colors = (params.color == null
						? new float[] {.2f, .4f, 1f, .3f}
						: ColorUtil.ARGBToColor(params.color));
				final int lifetime = params.lifetime + (params.lifetimeJitter > 0 ? NostrumMagica.rand.nextInt(params.lifetimeJitter) : 0);
				particle = new ParticleLightningStatic(world, spawnX, spawnY, spawnZ, colors[0], colors[1], colors[2], colors[3], lifetime);
				
				if (params.targetEntID != null) {
					particle.setTarget(world.getEntityByID(params.targetEntID));
				}
				if (params.targetPos != null) {
					particle.setTarget(params.targetPos);
				}
				if (params.velocity != null) {
					particle.setMotion(params.velocity);
				}
				Minecraft.getMinecraft().effectRenderer.addEffect(particle);
			}
			return particle;
		}
		
	}

}
