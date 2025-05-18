package com.smanzana.nostrummagica.client.particles;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class GlowOrbParticle extends TextureSheetParticle {
	
	protected final float maxAlpha;
	protected Vec3 targetPos;
	protected Entity targetEntity;
	protected boolean dieOnTarget;
	protected TargetBehavior entityBehavior;
	protected float orbitRadius;
	private float fixedRandom; // Only generated when needed, hopefully. -1 means not generated
	
	protected final SpriteSet sprites;
	
	public GlowOrbParticle(ClientLevel worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime, SpriteSet sprites) {
		super(worldIn, x, y, z, 0, 0, 0);
		
		this.rCol = red;
		this.gCol = green;
		this.bCol = blue;
		this.alpha = 0f;
		this.maxAlpha = alpha;
		this.lifetime = lifetime;
		this.fixedRandom = -1f;
		
		this.quadSize = .1f;
		this.sprites = sprites;
		this.setSpriteFromAge(sprites);
	}
	
	public GlowOrbParticle setGravity(boolean gravity) {
		return setGravityStrength(gravity ? .01f : 0);
	}
	
	public GlowOrbParticle setGravityStrength(float strength) {
		gravity = strength;
		return this;
	}
	
	public GlowOrbParticle setMotion(Vec3 motion) {
		return this.setMotion(motion.x, motion.y, motion.z);
	}
	
	public GlowOrbParticle setMotion(double xVelocity, double yVelocity, double zVelocity) {
		return this.setMotion(xVelocity, yVelocity, zVelocity, 0, 0, 0);
	}
	
	public GlowOrbParticle setMotion(Vec3 motion, Vec3 jitter) {
		return this.setMotion(motion.x, motion.y, motion.z, jitter.x, jitter.y, jitter.z);
	}
	
	public GlowOrbParticle setMotion(double xVelocity, double yVelocity, double zVelocity,
			double xJitter, double yJitter, double zJitter) {
		this.xd = xVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * xJitter; // +- jitter
		this.yd = yVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * yJitter;
		this.zd = zVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * zJitter;
		return this;
	}
	
	public GlowOrbParticle setTarget(Entity ent) {
		targetEntity = ent;
		if (this.targetPos == null && ent != null) {
			final double wRad = ent.getBbWidth() * 2; // double width
			final double hRad = ent.getBbHeight();
			this.targetPos = new Vec3(wRad * (NostrumMagica.rand.nextDouble() - .5),
					hRad * (NostrumMagica.rand.nextDouble() - .5),
					wRad * (NostrumMagica.rand.nextDouble() - .5));
		}
		return this;
	}
	
	public GlowOrbParticle setTarget(Vec3 targetPos) {
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
	
	protected float fixedRandom() {
		if (this.fixedRandom == -1f) {
			fixedRandom = NostrumMagica.rand.nextFloat();
		}
		
		return this.fixedRandom;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return GlowOrbRenderType;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		this.setSpriteFromAge(sprites);
		
		if (this.age < 20) {
			// fade in in first second
			this.alpha = ((float) age / 20f);
		} else if (this.age >= this.lifetime - 20f) {
			// Fade out in last second
			this.alpha = ((float) (lifetime - age) / 20f);
		} else {
			this.alpha = 1f;
		}
		
		this.alpha *= maxAlpha;
		
		// If we orbit, this is the radius
		final float orbitRadius = this.orbitRadius != 0 ? this.orbitRadius
				: (this.targetEntity != null
					? this.targetEntity.getBbWidth() * 2
					: 1);
		final double randomOrbitOffset = this.fixedRandom() * 2 * Math.PI;
		
		if (targetEntity != null) {
			if (targetEntity.isAlive()) {
				final float period;
				Vec3 offset;
				if (this.entityBehavior == TargetBehavior.JOIN) {
					period = 20f;
					//offset = targetPos == null ? Vector3d.ZERO : targetPos.rotateYaw((float) (Math.PI * 2 * ((float) age % period) / period))
					offset = Vec3.ZERO
							.add(0, targetEntity.getBbHeight()/2, 0);
				} else if (this.entityBehavior == TargetBehavior.ORBIT) {
					period = 20f;
					final float rot = (float) ((Math.PI * 2 * ((float) age % period) / period) + randomOrbitOffset);
					offset = (new Vec3(orbitRadius, 0, 0)).yRot((float) rot)
							.add(0, (targetEntity.getBbHeight()/2) + (targetPos == null ? 0 : targetPos.y), 0);
					
					// do this better
					if (this.gravity != 0f) {
						if (targetPos == null) {
							targetPos = Vec3.ZERO;
						}
						targetPos = targetPos.add(0, -this.gravity, 0);
					}
				} else {
					throw new RuntimeException("Unsupported particle behavior");
				}
				Vec3 curVelocity = new Vec3(this.xd, this.yd, this.zd);
				Vec3 posDelta = targetEntity.position()
						.add(offset.x, offset.y, offset.z)
						.subtract(x, y, z);
				Vec3 idealVelocity = posDelta.normalize().scale(.3);
				this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
			}
		} else if (targetPos != null) {
			final float yOffset = fixedRandom();
			final Vec3 finalTargetPos;
			if (this.entityBehavior == TargetBehavior.JOIN) {
				finalTargetPos = targetPos.add(0, yOffset, 0);
			} else {
				// Make this rotate around based on offset
				if (targetPos.subtract(x, y, z).length() <= orbitRadius) {
					// Close enough to orbit
					final double period = 20 * Math.max(1, orbitRadius / .5); // slow down the bigger the radius
					final double rot = (Math.PI * 2 * ((float) age % period) / period) + randomOrbitOffset;
					finalTargetPos = targetPos.add(new Vec3(orbitRadius, yOffset, 0).yRot((float) rot));
				} else {
					finalTargetPos = targetPos.add(0, yOffset, 0);
				}
			}
			
			Vec3 curVelocity = new Vec3(this.xd, this.yd, this.zd);
			Vec3 posDelta = finalTargetPos.subtract(x, y, z);
			Vec3 idealVelocity = posDelta.normalize().scale(.3);
			this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
		}
	}
	
	@Override
	public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
		//BatchRenderParticle.RenderQuad(matrixStackIn, buffer, this, renderInfo, partialTicks, .1f);
		//BatchRenderParticle.RenderQuad(matrixStackIn, buffer, this, renderInfo, partialTicks, .05f);
		this.quadSize = .1f;
		super.render(buffer, camera, partialTicks);
//		this.quadSize = .05f;
//		super.render(buffer, camera, partialTicks);
	}
	
	protected static ParticleRenderType GlowOrbRenderType = new ParticleRenderType() {

		@SuppressWarnings("deprecation")
		@Override
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.depthMask(false);
			RenderSystem.enableDepthTest();
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			//RenderSystem.alphaFunc(GL11.GL_GREATER, 0);
			//RenderSystem.disableLighting();
			// Texture set up by batch renderer but would need to be here if this were a real particlerendertype
			RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
		}

		@Override
		public void end(Tesselator tessellator) {
			tessellator.end();
		}
		
	};
	
	public static final GlowOrbParticle MakeParticle(ClientLevel world, SpriteSet sprites, SpawnParams params) {
		GlowOrbParticle particle = null;
		for (int i = 0; i < params.count; i++) {
			final double spawnX = params.spawnX + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final double spawnY = params.spawnY + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final double spawnZ = params.spawnZ + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final float[] colors = (params.color == null
					? new float[] {.2f, .4f, 1f, .3f}
					: ColorUtil.ARGBToColor(params.color));
			final int lifetime = params.lifetime + (params.lifetimeJitter > 0 ? NostrumMagica.rand.nextInt(params.lifetimeJitter) : 0);
			particle = new GlowOrbParticle(world, spawnX, spawnY, spawnZ, colors[0], colors[1], colors[2], colors[3], lifetime, sprites);
			
			if (params.targetEntID != null) {
				particle.setTarget(world.getEntity(params.targetEntID));
			}
			if (params.targetPos != null) {
				particle.setTarget(params.targetPos);
			}
			if (params.velocity != null) {
				particle.setMotion(params.velocity, params.velocityJitter == null ? Vec3.ZERO : params.velocityJitter);
			}
			if (params.gravityStrength != 0f) {
				particle.setGravityStrength(params.gravityStrength);
			}
			particle.dieOnTarget(params.dieOnTarget);
			particle.setEntityBehavior(params.targetBehavior);
			particle.setOrbitRadius(params.orbitRadius);
			Minecraft mc = Minecraft.getInstance();
			mc.particleEngine.add(particle);
		}
		return particle;
	}
}
