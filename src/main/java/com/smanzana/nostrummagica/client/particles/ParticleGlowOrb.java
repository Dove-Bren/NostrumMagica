package com.smanzana.nostrummagica.client.particles;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams.TargetBehavior;
import com.smanzana.nostrummagica.utils.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class ParticleGlowOrb extends BatchRenderParticle {
	
	private static final ResourceLocation TEX_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/effects/glow_orb.png");
	
	protected final float maxAlpha;
	protected Vector3d targetPos;
	protected Entity targetEntity;
	protected boolean dieOnTarget;
	protected TargetBehavior entityBehavior;
	protected float orbitRadius;
	private float fixedRandom; // Only generated when needed, hopefully. -1 means not generated
	
	public ParticleGlowOrb(World worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime) {
		super(worldIn, x, y, z, 0, 0, 0);
		
		particleRed = red;
		particleGreen = green;
		particleBlue = blue;
		particleAlpha = 0f;
		this.maxAlpha = alpha;
		maxAge = lifetime;
		fixedRandom = -1f;
	}
	
	public ParticleGlowOrb setGravity(boolean gravity) {
		return setGravityStrength(gravity ? .01f : 0);
	}
	
	public ParticleGlowOrb setGravityStrength(float strength) {
		particleGravity = strength;
		return this;
	}
	
	public ParticleGlowOrb setMotion(Vector3d motion) {
		return this.setMotion(motion.x, motion.y, motion.z);
	}
	
	public ParticleGlowOrb setMotion(double xVelocity, double yVelocity, double zVelocity) {
		return this.setMotion(xVelocity, yVelocity, zVelocity, 0, 0, 0);
	}
	
	public ParticleGlowOrb setMotion(Vector3d motion, Vector3d jitter) {
		return this.setMotion(motion.x, motion.y, motion.z, jitter.x, jitter.y, jitter.z);
	}
	
	public ParticleGlowOrb setMotion(double xVelocity, double yVelocity, double zVelocity,
			double xJitter, double yJitter, double zJitter) {
		this.motionX = xVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * xJitter; // +- jitter
		this.motionY = yVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * yJitter;
		this.motionZ = zVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * zJitter;
		return this;
	}
	
	public ParticleGlowOrb setTarget(Entity ent) {
		targetEntity = ent;
		if (this.targetPos == null && ent != null) {
			final double wRad = ent.getWidth() * 2; // double width
			final double hRad = ent.getHeight();
			this.targetPos = new Vector3d(wRad * (NostrumMagica.rand.nextDouble() - .5),
					hRad * (NostrumMagica.rand.nextDouble() - .5),
					wRad * (NostrumMagica.rand.nextDouble() - .5));
		}
		return this;
	}
	
	public ParticleGlowOrb setTarget(Vector3d targetPos) {
		this.targetPos = targetPos;
		return this;
	}
	
	public void dieOnTarget(boolean die) {
		this.dieOnTarget = die;
	}
	
	public void setEntityBehavior(TargetBehavior behavior) {
		this.entityBehavior = behavior;
	}
	
	public void setOrbitRadius(float radius) {
		this.orbitRadius = radius;
	}
	
	@Override
	public ResourceLocation getTexture() {
		return TEX_LOC;
	}
	
	protected float fixedRandom() {
		if (this.fixedRandom == -1f) {
			fixedRandom = NostrumMagica.rand.nextFloat();
		}
		
		return this.fixedRandom;
	}

	@Override
	public void setupRender() {
		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableAlphaTest();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableLighting();
		GlStateManager.disableLighting();
		GlStateManager.alphaFunc(516, 0);
		GlStateManager.color4f(1f, 1f, 1f, .75f);
		GlStateManager.depthMask(false);
		//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
	}
	
	@Override
	public int hashCode() {
		return 37 * 419 + 5119;
	}

	@Override
	public int compareTo(BatchRenderParticle o) {
		return hashCode() - o.hashCode();
	}

	@Override
	public void renderBatched(BufferBuilder buffer, float partialTicks) {
		BatchRenderParticle.RenderQuad(buffer, this, renderParams, partialTicks, .1f);
		BatchRenderParticle.RenderQuad(buffer, this, renderParams, partialTicks, .05f);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.age < 20) {
			// fade in in first second
			this.particleAlpha = ((float) age / 20f);
		} else if (this.age >= this.maxAge - 20f) {
			// Fade out in last second
			this.particleAlpha = ((float) (maxAge - age) / 20f);
		} else {
			this.particleAlpha = 1f;
		}
		
		this.particleAlpha *= maxAlpha;
		
		// If we orbit, this is the radius
		final float orbitRadius = this.orbitRadius != 0 ? this.orbitRadius
				: (this.targetEntity != null
					? this.targetEntity.getWidth() * 2
					: 1);
		final double randomOrbitOffset = this.fixedRandom() * 2 * Math.PI;
		
		if (targetEntity != null) {
			if (targetEntity.isAlive()) {
				final float period;
				Vector3d offset;
				if (this.entityBehavior == TargetBehavior.JOIN) {
					period = 20f;
					//offset = targetPos == null ? Vector3d.ZERO : targetPos.rotateYaw((float) (Math.PI * 2 * ((float) age % period) / period))
					offset = Vector3d.ZERO
							.add(0, targetEntity.getHeight()/2, 0);
				} else if (this.entityBehavior == TargetBehavior.ORBIT) {
					period = 20f;
					final float rot = (float) ((Math.PI * 2 * ((float) age % period) / period) + randomOrbitOffset);
					offset = (new Vector3d(orbitRadius, 0, 0)).rotateYaw((float) rot)
							.add(0, (targetEntity.getHeight()/2) + (targetPos == null ? 0 : targetPos.y), 0);
					
					// do this better
					if (this.particleGravity != 0f) {
						if (targetPos == null) {
							targetPos = Vector3d.ZERO;
						}
						targetPos = targetPos.add(0, -this.particleGravity, 0);
					}
				} else {
					throw new RuntimeException("Unsupported particle behavior");
				}
				Vector3d curVelocity = new Vector3d(this.motionX, this.motionY, this.motionZ);
				Vector3d posDelta = targetEntity.getPositionVec()
						.add(offset.x, offset.y, offset.z)
						.subtract(posX, posY, posZ);
				Vector3d idealVelocity = posDelta.normalize().scale(.3);
				this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
			}
		} else if (targetPos != null) {
			final float yOffset = fixedRandom();
			final Vector3d finalTargetPos;
			if (this.entityBehavior == TargetBehavior.JOIN) {
				finalTargetPos = targetPos.add(0, yOffset, 0);
			} else {
				// Make this rotate around based on offset
				if (targetPos.subtract(posX, posY, posZ).length() <= orbitRadius) {
					// Close enough to orbit
					final double period = 20 * Math.max(1, orbitRadius / .5); // slow down the bigger the radius
					final double rot = (Math.PI * 2 * ((float) age % period) / period) + randomOrbitOffset;
					finalTargetPos = targetPos.add(new Vector3d(orbitRadius, yOffset, 0).rotateYaw((float) rot));
				} else {
					finalTargetPos = targetPos.add(0, yOffset, 0);
				}
			}
			
			Vector3d curVelocity = new Vector3d(this.motionX, this.motionY, this.motionZ);
			Vector3d posDelta = finalTargetPos.subtract(posX, posY, posZ);
			Vector3d idealVelocity = posDelta.normalize().scale(.3);
			this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
		}
	}
	
	public static final class Factory implements INostrumParticleFactory<ParticleGlowOrb> {

		@Override
		public ParticleGlowOrb createParticle(World world, SpawnParams params) {
			ParticleGlowOrb particle = null;
			for (int i = 0; i < params.count; i++) {
				final double spawnX = params.spawnX + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
				final double spawnY = params.spawnY + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
				final double spawnZ = params.spawnZ + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
				final float[] colors = (params.color == null
						? new float[] {.2f, .4f, 1f, .3f}
						: ColorUtil.ARGBToColor(params.color));
				final int lifetime = params.lifetime + (params.lifetimeJitter > 0 ? NostrumMagica.rand.nextInt(params.lifetimeJitter) : 0);
				particle = new ParticleGlowOrb(world, spawnX, spawnY, spawnZ, colors[0], colors[1], colors[2], colors[3], lifetime);
				
				if (params.targetEntID != null) {
					particle.setTarget(world.getEntityByID(params.targetEntID));
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
				particle.setEntityBehavior(params.targetBehavior);
				particle.setOrbitRadius(params.orbitRadius);
				Minecraft mc = Minecraft.getInstance();
				mc.particles.addEffect(particle);
			}
			return particle;
		}
		
	}
}
