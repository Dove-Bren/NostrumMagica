package com.smanzana.nostrummagica.client.particles;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ParticleFilledOrb extends TextureSheetParticle {
	
	
	protected final float maxAlpha;
	protected Vec3 targetPos; // Absolute position to move to (if targetEntity == null) or offset from entity to go to
	protected Entity targetEntity;
	protected boolean dieOnTarget;
	protected final SpriteSet sprite;
	
	public ParticleFilledOrb(ClientLevel worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime, SpriteSet sprite) {
		super(worldIn, x, y, z, 0, 0, 0);
		
		rCol = red;
		gCol = green;
		bCol = blue;
		this.alpha = 0f;
		this.maxAlpha = alpha;
		this.lifetime = lifetime;
		this.sprite = sprite;
		
		this.quadSize = 0.05f;
		
		this.setSpriteFromAge(sprite);
	}
	
	public ParticleFilledOrb setGravity(boolean gravity) {
		return setGravityStrength(gravity ? .01f : 0);
	}
	
	public ParticleFilledOrb setGravityStrength(float strength) {
		gravity = strength;
		return this;
	}
	
	public ParticleFilledOrb setMotion(Vec3 motion) {
		return this.setMotion(motion.x, motion.y, motion.z);
	}
	
	public ParticleFilledOrb setMotion(double xVelocity, double yVelocity, double zVelocity) {
		return this.setMotion(xVelocity, yVelocity, zVelocity, 0, 0, 0);
	}
	
	public ParticleFilledOrb setMotion(Vec3 motion, Vec3 jitter) {
		return this.setMotion(motion.x, motion.y, motion.z, jitter.x, jitter.y, jitter.z);
	}
	
	public ParticleFilledOrb setMotion(double xVelocity, double yVelocity, double zVelocity,
			double xJitter, double yJitter, double zJitter) {
		this.xd = xVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * xJitter; // +- jitter
		this.yd = yVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * yJitter;
		this.zd = zVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * zJitter;
		return this;
	}
	
	public ParticleFilledOrb setTarget(Entity ent) {
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
	
	public ParticleFilledOrb setTarget(Vec3 targetPos) {
		this.targetPos = targetPos;
		return this;
	}
	
	public void dieOnTarget(boolean die) {
		this.dieOnTarget = die;
	}
	
	@Override
	public int hashCode() {
		return 29 * 419 + 5119;
	}

	@Override
	public void tick() {
		super.tick();
		
		this.setSpriteFromAge(sprite);
		
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
		
		if (targetEntity != null) {
			if (targetEntity.isAlive()) {
				final float period = 20f;
				Vec3 offset = targetPos == null ? new Vec3(0,0,0) : targetPos.yRot((float) (Math.PI * 2 * ((float) age % period) / period));
				Vec3 curVelocity = new Vec3(xd, yd, zd);
				Vec3 posDelta = targetEntity.position()
						.add(offset.x, offset.y + targetEntity.getBbHeight()/2, offset.z)
						.subtract(x, y, z);
				Vec3 idealVelocity = posDelta.normalize().scale(.3);
				this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
			}
			// Else just do nothing
		} else if (targetPos != null) {
			Vec3 curVelocity = new Vec3(xd, yd, zd);
			Vec3 posDelta = targetPos.subtract(x, y, z);
			Vec3 idealVelocity = posDelta.normalize().scale(.3);
			this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
		}
	}
	
	public static final ParticleFilledOrb MakeParticle(ClientLevel world, SpriteSet sprites, SpawnParams params) {
		ParticleFilledOrb particle = null;
		for (int i = 0; i < params.count; i++) {
			final double spawnX = params.spawnX + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final double spawnY = params.spawnY + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final double spawnZ = params.spawnZ + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final float[] colors = (params.color == null
					? new float[] {.2f, .4f, 1f, .3f}
					: ColorUtil.ARGBToColor(params.color));
			final int lifetime = params.lifetime + (params.lifetimeJitter > 0 ? NostrumMagica.rand.nextInt(params.lifetimeJitter) : 0);
			particle = new ParticleFilledOrb(world, spawnX, spawnY, spawnZ, colors[0], colors[1], colors[2], colors[3], lifetime, sprites);
			
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
			Minecraft mc = Minecraft.getInstance();
			mc.particleEngine.add(particle);
		}
		return particle;
	}
		

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}
	
}
