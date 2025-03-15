package com.smanzana.nostrummagica.client.particles;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

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
	protected Vector3d targetPos;
	protected Entity targetEntity;
	protected boolean dieOnTarget;
	
	protected int type;
	protected int ticksExisted;
	
	public ParticleLightningStatic(ClientWorld worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime) {
		super(worldIn, x, y, z, 0, 0, 0);
		
		rCol = red;
		gCol = green;
		bCol = blue;
		alpha = 0f;
		this.maxAlpha = alpha;
		lifetime = lifetime;
		
		type = NostrumMagica.rand.nextInt(2);
	}
	
	public ParticleLightningStatic setGravity(boolean gravity) {
		return setGravityStrength(gravity ? .01f : 0);
	}
	
	public ParticleLightningStatic setGravityStrength(float strength) {
		gravity = strength;
		return this;
	}
	
	public ParticleLightningStatic setMotion(Vector3d motion) {
		return this.setMotion(motion.x, motion.y, motion.z);
	}
	
	public ParticleLightningStatic setMotion(double xVelocity, double yVelocity, double zVelocity) {
		return this.setMotion(xVelocity, yVelocity, zVelocity, 0, 0, 0);
	}
	
	public ParticleLightningStatic setMotion(Vector3d motion, Vector3d jitter) {
		return this.setMotion(motion.x, motion.y, motion.z, jitter.x, jitter.y, jitter.z);
	}
	
	public ParticleLightningStatic setMotion(double xVelocity, double yVelocity, double zVelocity,
			double xJitter, double yJitter, double zJitter) {
		this.xd = xVelocity * (1.0 + (NostrumMagica.rand.nextDouble() * 2 - 1) * xJitter); // 1 +- jitter
		this.yd = yVelocity * (1.0 + (NostrumMagica.rand.nextDouble() * 2 - 1) * yJitter);
		this.zd = zVelocity * (1.0 + (NostrumMagica.rand.nextDouble() * 2 - 1) * zJitter);
		return this;
	}
	
	public ParticleLightningStatic setTarget(Entity ent) {
		targetEntity = ent;
		return this;
	}
	
	public ParticleLightningStatic setTarget(Vector3d targetPos) {
		this.targetPos = targetPos;
		return this;
	}
	
	public void dieOnTarget(boolean die) {
		this.dieOnTarget = die;
	}
	
	protected float getDisplayProgress() {
		return (float) age / (float) lifetime;
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
	public void setupBatchedRender() {
		RenderSystem.depthMask(false);
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.alphaFunc(GL11.GL_GREATER, 0);
		RenderSystem.disableLighting();
		// Texture set up by batch renderer but would need to be here if this were a real particlerendertype
		
	}
	
	@Override
	public void teardownBatchedRender() {
		RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F); // idk where this was copied from
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
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
	public void renderBatched(MatrixStack matrixStackIn, IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
		BatchRenderParticle.RenderQuad(matrixStackIn, buffer, this, renderInfo, partialTicks, .05f);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.age < 20) {
			// fade in in first second
			this.alpha = ((float) age / 20f);
//		} else if (this.age >= this.maxAge - 20f) {
//			// Fade out in last second
//			this.particleAlpha = ((float) (maxAge - age) / 20f);
		} else {
			this.alpha = 1f;
		}
		
		this.alpha *= maxAlpha;
		
		if (targetEntity != null && targetEntity.isAlive()) {
			Vector3d curVelocity = new Vector3d(this.xd, this.yd, this.zd);
			Vector3d posDelta = targetEntity.position().add(0, targetEntity.getBbHeight()/2, 0).subtract(x, y, z);
			Vector3d idealVelocity = posDelta.normalize().scale(.3);
			this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
		} else if (targetPos != null) {
			Vector3d curVelocity = new Vector3d(this.xd, this.yd, this.zd);
			Vector3d posDelta = targetPos.subtract(x, y, z);
			Vector3d idealVelocity = posDelta.normalize().scale(.3);
			this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
		}
	}
	
	public static final class Factory implements INostrumParticleFactory<ParticleLightningStatic> {

		@Override
		public ParticleLightningStatic createParticle(ClientWorld world, SpawnParams params) {
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
					particle.setTarget(world.getEntity(params.targetEntID));
				}
				if (params.targetPos != null) {
					particle.setTarget(params.targetPos);
				}
				if (params.velocity != null) {
					particle.setMotion(params.velocity, params.velocityJitter == null ? Vector3d.ZERO : params.velocityJitter);
				}
				if (params.gravityStrength != 0f) {
					particle.setGravityStrength(params.gravityStrength);
				}
				particle.dieOnTarget(params.dieOnTarget);
				Minecraft mc = Minecraft.getInstance();
				mc.particleEngine.add(particle);
			}
			return particle;
		}
		
	}

}
