package com.smanzana.nostrummagica.client.particles;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetMotion.MotionUpdate;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.phys.Vec3;

public class LightningStaticParticle extends TextureSheetParticle implements IMotionParticle<LightningStaticParticle> {
	
	protected final float maxAlpha;
	protected final ParticleTargetMotion motion;
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
		this.motion = new ParticleTargetMotion(NostrumMagica.rand.nextFloat(), .2f);
		
		this.setSpriteFromAge(sprites);
		
		this.quadSize = .05f;
	}
	
	@Override
	public ParticleTargetMotion getMotion() {
		return this.motion;
	}
	
	@Override
	public LightningStaticParticle setPosition(double x, double y, double z) {
		this.setPos(x, y, z);
		return this;
	}
	
	@Override
	public LightningStaticParticle setGravityStrength(float strength) {
		gravity = strength;
		return this;
	}
	
	@Override
	public LightningStaticParticle setMotion(double xVelocity, double yVelocity, double zVelocity) {
		this.xd = xVelocity;
		this.yd = yVelocity;
		this.zd = zVelocity;
		return this;
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
		
		if (this.getMotion().shouldUpdate()) {
			final @Nullable MotionUpdate update = this.getMotion().update(new Vec3(x, y, z), new Vec3(xd, yd, zd));
			if (update == null) {
				this.remove();
			} else {
				ParticleTargetMotion.ApplyUpdate(update, this);
			}
		}
		// else let super.tick do normal particle motion
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	public static final LightningStaticParticle MakeParticle(ClientLevel world, SpriteSet sprites, SpawnParams params) {
		LightningStaticParticle particle = null;
		for (int i = 0; i < params.count; i++) {
			if (particle != null) {
				Minecraft mc = Minecraft.getInstance();
				mc.particleEngine.add(particle);
			}
			final double spawnX = params.spawnX + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final double spawnY = params.spawnY + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final double spawnZ = params.spawnZ + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final float[] colors = (params.color == null
					? new float[] {.2f, .4f, 1f, .3f}
					: ColorUtil.ARGBToColor(params.color));
			final int lifetime = params.lifetime + (params.lifetimeJitter > 0 ? NostrumMagica.rand.nextInt(params.lifetimeJitter) : 0);
			particle = new LightningStaticParticle(world, spawnX, spawnY, spawnZ, colors[0], colors[1], colors[2], colors[3], lifetime, sprites);
			particle.setFromParams(params, world::getEntity);
		}
		return particle;
	}

}
