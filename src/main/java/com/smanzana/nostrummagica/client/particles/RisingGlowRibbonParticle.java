package com.smanzana.nostrummagica.client.particles;

import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetMotion.MotionUpdate;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonEmitter;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonEmitter.EmitterData;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonEmitter.ISegment;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonEmitter.SegmentData;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonRenderTypes.TexturedRibbonRenderType;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonSegmentTypes.CameraFacingSegmentMixin;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonSegmentTypes.FadingSegmentMixin;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonSegmentTypes.LifetimeSegment;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonSegmentTypes.StaticSegment;
import com.smanzana.nostrummagica.util.Color;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// ribbon trails that circle around at an offset
public class RisingGlowRibbonParticle extends Particle implements IMotionParticle<RisingGlowRibbonParticle> {
	
	protected final int fixedRandom;
	protected final ParticleTargetMotion motion;

	protected final RibbonEmitter<RisingGlowRibbonParticle> emitter;
	protected final int tails;
	
	protected float tailDistance;
	protected float period;
	protected Vec3 tickOffset;
	protected Vec3 lastTickOffset;
	
	public RisingGlowRibbonParticle(ClientLevel worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime, int tails, float distance, float period) {
		super(worldIn, x, y, z, 0, 0, 0);

		this.fixedRandom = NostrumMagica.rand.nextInt();
		final Random rand = new Random(this.fixedRandom);
		
		this.lifetime = lifetime;
		this.xd = 0;
		this.yd = 0;
		this.zd = 0;
		this.motion = new ParticleTargetMotion(rand.nextFloat(), .2f);
		this.friction = 1f;
		this.tails = tails;
		this.tailDistance = distance + rand.nextFloat() * .1f;
		this.period = period;
		this.tickOffset = this.getTickOffset(0f);
		this.emitter = new RibbonEmitter<>(worldIn, tickOffset.x, tickOffset.y, tickOffset.z, red, green, blue, alpha, lifetime, fixedRandom, this::makeSegment, this);
		
		
	}
	
	protected @Nullable ISegment makeSegment(RisingGlowRibbonParticle particle, ClientLevel worldIn, Vec3 worldPos, EmitterData emitter, double distanceFromLast, float ticksFromLast) {
		// Always spawn on first spawn attempt. Otherwise check if we should spawn
		if (emitter.segmentSpawnCount != 0) {
			// Don't spawn anything if distance hasn't changed
			if (distanceFromLast < 0.1) {
				return null;
			}
		}
		
		return new GlowRibbonSegment(worldPos, emitter.emitterColor, .125f, (float)emitter.ribbonSpawnLength, emitter.emitterAge, emitter.partialTicks, 20, emitter.emitterLifetime);
	}
	
	@Override
	public ParticleTargetMotion getMotion() {
		return this.motion;
	}
	
	protected float getAngleOffset(float partialTicks) {
		final Random rand = new Random(this.fixedRandom);
		return Mth.PI * 2 * (((this.age + partialTicks) / period) + rand.nextFloat());
	}
	
	protected float getAnglePer() {
		return (Mth.PI * 2) / (this.tails);
	}
	
	protected float getAngleForTail(int idx, float partialTicks) {
		return getAngleOffset(partialTicks) + (idx * getAnglePer()); 
	}
	
	protected Vec3 getTickOffset(float partialTicks) {
		final float angle = getAngleForTail(0, partialTicks);
		return new Vec3(
				Mth.cos(angle) * tailDistance,
				((age + partialTicks) * .045),
				Mth.sin(angle) * tailDistance
				);
	}
	
	protected void updateEmitter(double offsetX, double offsetY, double offsetZ, double lastX, double lastY, double lastZ) {
		this.emitter.tick(offsetX,
				offsetY,
				offsetZ,
				lastX,
				lastY,
				lastZ
				);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		// Update tick offsets
		this.lastTickOffset = this.tickOffset;
		this.tickOffset = this.getTickOffset(0f);
		
		if (this.removed) {
			this.emitter.disable();
		}
		
		this.updateEmitter(tickOffset.x, tickOffset.y, tickOffset.z, lastTickOffset.x, lastTickOffset.y, lastTickOffset.z);
		
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
	public boolean isAlive() {
		return super.isAlive() || this.emitter.isAlive();
	}
	
	@Override
	public boolean shouldCull() {
		return false;
	}
	
	@Override
	public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
		// render multiple times for each tail
		PoseStack stack = new PoseStack();
		
		// Rotate relative to camera
		Vec3 cameraPos = camera.getPosition();
		
		double offX = (float) -cameraPos.x();
		double offY = (float) -cameraPos.y();
		double offZ = (float) -cameraPos.z();
		stack.translate(offX, offY, offZ);
		
		stack.translate(
				Mth.lerp(partialTicks, xo, x),
				Mth.lerp(partialTicks, yo, y),
				Mth.lerp(partialTicks, zo, z)
		);
		
		for (int i = 0; i < tails; i++) {
			stack.pushPose();
			stack.mulPose(Vector3f.YP.rotation((i * getAnglePer())));
			this.emitter.render(stack, buffer, camera, partialTicks);
			stack.popPose();
		}
	}
	
	protected static final class GlowRibbonSegment extends LifetimeSegment implements CameraFacingSegmentMixin, FadingSegmentMixin {

		private final StaticSegment segmentWrapper;
		
		public GlowRibbonSegment(Vec3 position, Color color, float width, float v, int startTicks, float partialTicks, int lifetimeTicks, int particleLifetime) {
			super(startTicks, lifetimeTicks, particleLifetime, partialTicks, false);
			
			this.segmentWrapper = new StaticSegment(position, Vector3f.ZERO, color, width, v);
		}

		@Override
		public Vec3 getPosition(Camera camera, EmitterData emitter) {
			return segmentWrapper.getPosition(camera, emitter)
					//.add(0, this.getLifeProgress(emitter) * 1.5, 0)
					;
		}

		@Override
		public float getV(Camera camera, EmitterData emitter, SegmentData ribbonData) {
			return segmentWrapper.getV(camera, emitter, ribbonData) + (emitter.emitterAge + emitter.partialTicks) * .025f;
		}
		
		@Override
		public float getU(Camera camera, EmitterData emitter, SegmentData ribbonData, boolean leftEdge) {
			return leftEdge ? 0 : 1;
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
			return .4f;
		}
	}

	@Override
	public RisingGlowRibbonParticle setGravityStrength(float strength) {
		gravity = strength;
		return this;
	}

	@Override
	public RisingGlowRibbonParticle setMotion(double xVelocity, double yVelocity, double zVelocity) {
		this.xd = xVelocity;
		this.yd = yVelocity;
		this.zd = zVelocity;
		return this;
	}
	
	@Override
	public RisingGlowRibbonParticle setPosition(double x, double y, double z) {
		this.setPos(x, y, z);
		return this;
	}
	
	private static final ParticleRenderType RenderType = new TexturedRibbonRenderType(NostrumMagica.Loc("textures/particle/glow_stream.png"));
	
	@Override
	public ParticleRenderType getRenderType() {
		return RenderType;
	}
	
	public static final RisingGlowRibbonParticle MakeParticle(ClientLevel world, SpriteSet spites, SpawnParams params) {
		RisingGlowRibbonParticle particle = null;
		
		final double spawnX = params.spawnX + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
		final double spawnY = params.spawnY + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
		final double spawnZ = params.spawnZ + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
		final float[] colors = (params.color == null
				? new float[] {.2f, .4f, 1f, .3f}
				: ColorUtil.ARGBToColor(params.color));
		final int lifetime = params.lifetime + (params.lifetimeJitter > 0 ? NostrumMagica.rand.nextInt(params.lifetimeJitter) : 0);
		
		particle = new RisingGlowRibbonParticle(world, spawnX, spawnY, spawnZ, colors[0], colors[1], colors[2], colors[3], lifetime, params.count, 1.25f, lifetime);
		particle.setFromParams(params, world::getEntity);
		
		int unused; // is caller-determined now
//		if (params.targetEntID != null) {
//			particle.setTarget(new TargetLocation(world.getEntity(params.targetEntID), false));
//		}
		
		return particle;
	}
}
