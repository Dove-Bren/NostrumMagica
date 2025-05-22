package com.smanzana.nostrummagica.client.particles;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class LightningStaticParticle extends TextureSheetParticle {
	
	protected final float maxAlpha;
	protected Vec3 targetPos;
	protected Entity targetEntity;
	protected boolean dieOnTarget;
	
	protected final SpriteSet sprites;
	
	public LightningStaticParticle(ClientLevel worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime,
			SpriteSet sprites) {
		super(worldIn, x, y, z, 0, 0, 0);
		
		rCol = red;
		gCol = green;
		bCol = blue;
		this.alpha = 0f;
		this.maxAlpha = alpha;
		this.lifetime = lifetime;
		this.sprites = sprites;
		
		this.setSpriteFromAge(sprites);
		
		this.quadSize = .05f;
	}
	
	public LightningStaticParticle setGravity(boolean gravity) {
		return setGravityStrength(gravity ? .01f : 0);
	}
	
	public LightningStaticParticle setGravityStrength(float strength) {
		gravity = strength;
		return this;
	}
	
	public LightningStaticParticle setMotion(Vec3 motion) {
		return this.setMotion(motion.x, motion.y, motion.z);
	}
	
	public LightningStaticParticle setMotion(double xVelocity, double yVelocity, double zVelocity) {
		return this.setMotion(xVelocity, yVelocity, zVelocity, 0, 0, 0);
	}
	
	public LightningStaticParticle setMotion(Vec3 motion, Vec3 jitter) {
		return this.setMotion(motion.x, motion.y, motion.z, jitter.x, jitter.y, jitter.z);
	}
	
	public LightningStaticParticle setMotion(double xVelocity, double yVelocity, double zVelocity,
			double xJitter, double yJitter, double zJitter) {
		this.xd = xVelocity * (1.0 + (NostrumMagica.rand.nextDouble() * 2 - 1) * xJitter); // 1 +- jitter
		this.yd = yVelocity * (1.0 + (NostrumMagica.rand.nextDouble() * 2 - 1) * yJitter);
		this.zd = zVelocity * (1.0 + (NostrumMagica.rand.nextDouble() * 2 - 1) * zJitter);
		return this;
	}
	
	public LightningStaticParticle setTarget(Entity ent) {
		targetEntity = ent;
		return this;
	}
	
	public LightningStaticParticle setTarget(Vec3 targetPos) {
		this.targetPos = targetPos;
		return this;
	}
	
	public void dieOnTarget(boolean die) {
		this.dieOnTarget = die;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		this.setSpriteFromAge(sprites);
		
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
			Vec3 curVelocity = new Vec3(this.xd, this.yd, this.zd);
			Vec3 posDelta = targetEntity.position().add(0, targetEntity.getBbHeight()/2, 0).subtract(x, y, z);
			Vec3 idealVelocity = posDelta.normalize().scale(.3);
			this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
		} else if (targetPos != null) {
			Vec3 curVelocity = new Vec3(this.xd, this.yd, this.zd);
			Vec3 posDelta = targetPos.subtract(x, y, z);
			Vec3 idealVelocity = posDelta.normalize().scale(.3);
			this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
		}
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	public static final LightningStaticParticle MakeParticle(ClientLevel world, SpriteSet sprites, SpawnParams params) {
		LightningStaticParticle particle = null;
		for (int i = 0; i < params.count; i++) {
			final double spawnX = params.spawnX + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final double spawnY = params.spawnY + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final double spawnZ = params.spawnZ + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final float[] colors = (params.color == null
					? new float[] {.2f, .4f, 1f, .3f}
					: ColorUtil.ARGBToColor(params.color));
			final int lifetime = params.lifetime + (params.lifetimeJitter > 0 ? NostrumMagica.rand.nextInt(params.lifetimeJitter) : 0);
			particle = new LightningStaticParticle(world, spawnX, spawnY, spawnZ, colors[0], colors[1], colors[2], colors[3], lifetime, sprites);
			
//			if (params.target != null) {
//				TargetLocation target = params.target.apply(world::getEntity);
//				if (target.)
//				particle.setTarget(world.getEntity(params.targetEntID));
//			}
//			if (params.targetPos != null) {
//				particle.setTarget(params.targetPos);
//			}
			int unused; // convert to motion particle
			if (params.velocity != null) {
				particle.setMotion(params.velocity, params.velocityJitter == null ? Vec3.ZERO : params.velocityJitter);
			}
			if (params.gravityStrength != 0f) {
				particle.setGravityStrength(params.gravityStrength);
			}
			particle.dieOnTarget(params.dieWithTarget);
			Minecraft mc = Minecraft.getInstance();
			mc.particleEngine.add(particle);
		}
		return particle;
	}

}
