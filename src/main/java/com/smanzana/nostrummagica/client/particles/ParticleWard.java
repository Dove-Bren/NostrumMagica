package com.smanzana.nostrummagica.client.particles;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams.EntityBehavior;
import com.smanzana.nostrummagica.utils.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleWard extends BatchRenderParticle {
	
	private static final ResourceLocation TEX_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/effects/hexa_shield.png");
	
	protected final float maxAlpha;
	protected Vec3d targetPos; // Absolute position to move to (if targetEntity == null) or offset from entity to go to
	protected Entity targetEntity;
	protected boolean dieOnTarget;
	protected EntityBehavior entityBehavior;
	
	public ParticleWard(World worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime) {
		super(worldIn, x, y, z, 0, 0, 0);
		
		particleRed = red;
		particleGreen = green;
		particleBlue = blue;
		particleAlpha = 0f;
		this.maxAlpha = alpha;
		particleMaxAge = lifetime;
	}
	
	public ParticleWard setGravity(boolean gravity) {
		return setGravityStrength(gravity ? .01f : 0);
	}
	
	public ParticleWard setGravityStrength(float strength) {
		particleGravity = strength;
		return this;
	}
	
	public ParticleWard setMotion(Vec3d motion) {
		return this.setMotion(motion.x, motion.y, motion.z);
	}
	
	public ParticleWard setMotion(double xVelocity, double yVelocity, double zVelocity) {
		return this.setMotion(xVelocity, yVelocity, zVelocity, 0, 0, 0);
	}
	
	public ParticleWard setMotion(Vec3d motion, Vec3d jitter) {
		return this.setMotion(motion.x, motion.y, motion.z, jitter.x, jitter.y, jitter.z);
	}
	
	public ParticleWard setMotion(double xVelocity, double yVelocity, double zVelocity,
			double xJitter, double yJitter, double zJitter) {
		this.motionX = xVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * xJitter; // +- jitter
		this.motionY = yVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * yJitter;
		this.motionZ = zVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * zJitter;
		return this;
	}
	
	public ParticleWard setTarget(Entity ent) {
		targetEntity = ent;
		if (this.targetPos == null && ent != null) {
			final double wRad = ent.width * 2; // double width
			final double hRad = ent.height;
			this.targetPos = new Vec3d(wRad * (NostrumMagica.rand.nextDouble() - .5),
					hRad * (NostrumMagica.rand.nextDouble() - .5),
					wRad * (NostrumMagica.rand.nextDouble() - .5));
		}
		return this;
	}
	
	public ParticleWard setTarget(Vec3d targetPos) {
		this.targetPos = targetPos;
		return this;
	}
	
	public void dieOnTarget(boolean die) {
		this.dieOnTarget = die;
	}
	
	public void setEntityBehavior(EntityBehavior behavior) {
		this.entityBehavior = behavior;
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
		return 47 * 419 + 5119;
	}

	@Override
	public int compareTo(BatchRenderParticle o) {
		return hashCode() - o.hashCode();
	}

	@Override
	public void renderBatched(BufferBuilder buffer, float partialTicks) {
		BatchRenderParticle.RenderQuad(buffer, this, renderParams, partialTicks, .1f);
		//BatchRenderParticle.RenderQuad(buffer, this, renderParams, partialTicks, .05f);
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
		
		if (targetEntity != null) {
			if (!targetEntity.isDead) {
				final float period;
				Vec3d offset;
				if (this.entityBehavior == EntityBehavior.JOIN) {
					period = 20f;
					offset = targetPos == null ? Vec3d.ZERO : targetPos.rotateYaw((float) (Math.PI * 2 * ((float) particleAge % period) / period))
							.addVector(0, targetEntity.height/2, 0);
				} else if (this.entityBehavior == EntityBehavior.ORBIT) {
					period = 20f;
					//randPeriodOffset = ?
					offset = (new Vec3d(targetEntity.width * 2, 0, 0)).rotateYaw((float) (Math.PI * 2 * ((float) particleAge % period) / period))
							.addVector(0, (targetEntity.height/2) + (targetPos == null ? 0 : targetPos.y), 0);
					
					// do this better
					if (this.particleGravity != 0f) {
						if (targetPos == null) {
							targetPos = Vec3d.ZERO;
						}
						targetPos = targetPos.addVector(0, -this.particleGravity, 0);
					}
				} else {
					throw new RuntimeException("Unsupported particle behavior");
				}
				Vec3d curVelocity = new Vec3d(this.motionX, this.motionY, this.motionZ);
				Vec3d posDelta = targetEntity.getPositionVector()
						.addVector(offset.x, offset.y, offset.z)
						.subtract(posX, posY, posZ);
				Vec3d idealVelocity = posDelta.normalize().scale(.3);
				this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
			}
		} else if (targetPos != null) {
			Vec3d curVelocity = new Vec3d(this.motionX, this.motionY, this.motionZ);
			Vec3d posDelta = targetPos.subtract(posX, posY, posZ);
			Vec3d idealVelocity = posDelta.normalize().scale(.3);
			this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
		}
	}
	
	public static final class Factory implements INostrumParticleFactory<ParticleWard> {

		@Override
		public ParticleWard createParticle(World world, SpawnParams params) {
			ParticleWard particle = null;
			for (int i = 0; i < params.count; i++) {
				final double spawnX = params.spawnX + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
				final double spawnY = params.spawnY + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
				final double spawnZ = params.spawnZ + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
				final float[] colors = (params.color == null
						? new float[] {.2f, .4f, 1f, .3f}
						: ColorUtil.ARGBToColor(params.color));
				final int lifetime = params.lifetime + (params.lifetimeJitter > 0 ? NostrumMagica.rand.nextInt(params.lifetimeJitter) : 0);
				particle = new ParticleWard(world, spawnX, spawnY, spawnZ, colors[0], colors[1], colors[2], colors[3], lifetime);
				
				if (params.targetEntID != null) {
					particle.setTarget(world.getEntityByID(params.targetEntID));
				}
				if (params.targetPos != null) {
					particle.setTarget(params.targetPos);
				}
				if (params.velocity != null) {
					particle.setMotion(params.velocity, params.velocityJitter == null ? Vec3d.ZERO : params.velocityJitter);
				}
				if (params.gravityStrength != 0f) {
					particle.setGravityStrength(params.gravityStrength);
				}
				particle.dieOnTarget(params.dieOnTarget);
				particle.setEntityBehavior(params.entityBehavior);
				Minecraft.getMinecraft().effectRenderer.addEffect(particle);
			}
			return particle;
		}
		
	}

}
