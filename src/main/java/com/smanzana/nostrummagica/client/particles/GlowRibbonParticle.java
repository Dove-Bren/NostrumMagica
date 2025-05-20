package com.smanzana.nostrummagica.client.particles;

import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetMotion.MotionUpdate;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonParticle;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonRenderTypes.LitColorRibbonRenderType;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonSegmentTypes.CameraFacingSegmentMixin;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonSegmentTypes.FadingSegmentMixin;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonSegmentTypes.LifetimeSegment;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonSegmentTypes.StaticSegment;
import com.smanzana.nostrummagica.util.Color;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.phys.Vec3;

// Has a color and fades out segments over time
public class GlowRibbonParticle extends RibbonParticle implements IMotionParticle<GlowRibbonParticle> {
	
	protected final ParticleTargetMotion motion;
	
	public GlowRibbonParticle(ClientLevel worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime) {
		super(worldIn, x, y, z, red, green, blue, alpha, lifetime, GlowRibbonParticle::MakeSegment);
		this.motion = new ParticleTargetMotion(new Random(this.fixedRandom).nextFloat(), .2f);
		this.friction = 1f;
	}
	
	protected static @Nullable ISegment MakeSegment(RibbonParticle particle, ClientLevel worldIn, Vec3 worldPos, EmitterData emitter, double distanceFromLast, float ticksFromLast) {
		return ((GlowRibbonParticle) particle).makeSegment(worldIn, worldPos, emitter, distanceFromLast, ticksFromLast);
	}
	
	protected @Nullable ISegment makeSegment(ClientLevel worldIn, Vec3 worldPos, EmitterData emitter, double distanceFromLast, float ticksFromLast) {
		// Always spawn on first spawn attempt. Otherwise check if we should spawn
		if (emitter.segmentSpawnCount != 0) {
			// Don't spawn anything if distance hasn't changed
			if (distanceFromLast < 0.1) {
				return null;
			}
		}
		
		return new GlowRibbonSegment(worldPos, emitter.emitterColor, .125f, 0, age, emitter.partialTicks, 20, emitter.emitterLifetime);
	}
	
	@Override
	public ParticleTargetMotion getMotion() {
		return this.motion;
	}
	
	@Override
	public void tick() {
		super.tick();
		
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
	
	protected static final class GlowRibbonSegment extends LifetimeSegment implements CameraFacingSegmentMixin, FadingSegmentMixin {

		private final StaticSegment segmentWrapper;
		
		public GlowRibbonSegment(Vec3 position, Color color, float width, float v, int startTicks, float partialTicks, int lifetimeTicks, int particleLifetime) {
			super(startTicks, lifetimeTicks, particleLifetime, partialTicks, false);
			
			this.segmentWrapper = new StaticSegment(position, Vector3f.ZERO, color, width, v);
		}

		@Override
		public Vec3 getPosition(Camera camera, EmitterData emitter) {
			return segmentWrapper.getPosition(camera, emitter) ;
		}

		@Override
		public float getV(Camera camera, EmitterData emitter, SegmentData ribbonData) {
			return segmentWrapper.getV(camera, emitter, ribbonData);
		}
		
		@Override
		public float getU(Camera camera, EmitterData emitter, SegmentData ribbonData, boolean leftEdge) {
			return leftEdge ? -1 : 1;
		}

		@Override
		public Color getBaseColor(Camera camera, EmitterData emitter, SegmentData ribbonData) {
			return segmentWrapper.color();
		}

		@Override
		public float getBaseWidth(Camera camera, EmitterData emitter, SegmentData ribbonData) {
			return segmentWrapper.width();
		}

		@Override
		public float getFadeInProg(EmitterData emitter, SegmentData ribbonData) {
			return .05f;
		}

		@Override
		public float getFadeOutProg(EmitterData emitter, SegmentData ribbonData) {
			return .9f;
		}
	}

	@Override
	public GlowRibbonParticle setGravityStrength(float strength) {
		gravity = strength;
		return this;
	}

	@Override
	public GlowRibbonParticle setMotion(double xVelocity, double yVelocity, double zVelocity) {
		this.xd = xVelocity;
		this.yd = yVelocity;
		this.zd = zVelocity;
		return this;
	}
	
	@Override
	public GlowRibbonParticle setPosition(double x, double y, double z) {
		this.setPos(x, y, z);
		return this;
	}
	
	@Override
	public ParticleRenderType getRenderType() {
		return LitColorRibbonRenderType.INSTANCE;
	}
	
	public static final GlowRibbonParticle MakeParticle(ClientLevel world, SpriteSet spites, SpawnParams params) {
		GlowRibbonParticle particle = null;
		final Minecraft mc = Minecraft.getInstance();
		for (int i = 0; i < params.count; i++) {
			if (particle != null) {
				mc.particleEngine.add(particle);
			}
			
			final double spawnX = params.spawnX + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final double spawnY = params.spawnY + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final double spawnZ = params.spawnZ + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final float[] colors = (params.color == null
					? new float[] {.2f, .4f, 1f, .3f}
					: ColorUtil.ARGBToColor(params.color));
			final int lifetime = params.lifetime + (params.lifetimeJitter > 0 ? NostrumMagica.rand.nextInt(params.lifetimeJitter) : 0);
			
			particle = new GlowRibbonParticle(world, spawnX, spawnY, spawnZ, colors[0], colors[1], colors[2], colors[3], lifetime);
			particle.setFromParams(params, world::getEntity);
		}
		
		return particle;
	}
}
