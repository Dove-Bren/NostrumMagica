package com.smanzana.nostrummagica.client.particles;

import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonEmitter.EmitterData;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonEmitter.ISegment;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonEmitter.SegmentData;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonRenderTypes.LitColorRibbonRenderType;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonSegmentTypes.CameraFacingSegmentMixin;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonSegmentTypes.StaticSegment;
import com.smanzana.nostrummagica.util.Color;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.Curves;
import com.smanzana.nostrummagica.util.Curves.ICurve3d;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.phys.Vec3;

/**
 * An instant short-lived particle that renders a lightning-like jittery ribbon between two positions.
 */
public class LightningChainParticle extends RibbonParticle {
	
	protected final TargetLocation firstLocation;
	protected final TargetLocation secondLocation;
	protected final float arcJitter; // in block units
	protected final int arcCount;
	
	private boolean generating;

	public LightningChainParticle(ClientLevel worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime, int arcCount, float arcJitter) {
		this(worldIn, new TargetLocation(new Vec3(x, y, z)),
				new TargetLocation(new Vec3(x, y, z).add((NostrumMagica.rand.nextFloat() - .5f) * 3, 0, (NostrumMagica.rand.nextFloat() - .5f) * 3)), // random spot near start
				red, green, blue, alpha, lifetime, arcCount, arcJitter);
	}
	
	public LightningChainParticle(ClientLevel worldIn, TargetLocation firstLocation, TargetLocation secondLocation, float red, float green, float blue, float alpha, int lifetime, int arcCount, float arcJitter) {
		super(worldIn, firstLocation.getLocation().x, firstLocation.getLocation().y, firstLocation.getLocation().z, red, green, blue, alpha, lifetime, LightningChainParticle::MakeSegment);
		this.friction = 1f;
		this.hasPhysics = false;
		this.firstLocation = firstLocation;
		this.secondLocation = secondLocation;
		this.arcJitter = arcJitter;
		this.arcCount = arcCount;
		
		generatePoints();
	}
	
	@Override
	public ParticleRenderType getRenderType() {
		return LitColorRibbonRenderType.INSTANCE;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.age > this.lifetime) {
			emitter.clearSegments();
		}
	}
	
	protected void generatePoints() {
		final Vec3 pos1 = this.firstLocation.getLocation();
		final Vec3 pos2 = this.secondLocation.getLocation();
		final Vec3 halfway = pos1.add(pos2.subtract(pos1).scale(.5));
		final Random rand = new Random(this.fixedRandom);
		final ICurve3d curve = new Curves.Bezier(pos1,
				halfway.add((-.5 + rand.nextDouble()) * 2.5, (0 + rand.nextDouble()) * 1.25, (-.5 + rand.nextDouble()) * 2.5), // EXTRA jitter on top of per-segment jitter
				pos2);
		
		generating = true;
		emitter.attemptNewSegment(0f);
		for (int i = 0; i < this.arcCount; i++) {
			final float prog = (float)(i+1) / (float)(arcCount+2);
			Vec3 pos = curve.getPosition(prog);
			emitter.tick(pos.x, pos.y, pos.z, pos.x, pos.y, pos.z);
			emitter.attemptNewSegment(0f);
		}
		emitter.attemptNewSegment(0f);
		generating = false;
	}
	
	protected static @Nullable ISegment MakeSegment(RibbonParticle particle, ClientLevel worldIn, Vec3 worldPos, EmitterData emitter, double distanceFromLast, float ticksFromLast) {
		return ((LightningChainParticle) particle).makeSegment(worldIn, worldPos, emitter, distanceFromLast, ticksFromLast);
	}
	
	protected @Nullable ISegment makeSegment(ClientLevel worldIn, Vec3 worldPos, EmitterData emitter, double distanceFromLast, float ticksFromLast) {
		final float width = .025f;
		final float v = 0;
		
		// All particles are spawned in first tick
		if (generating) {
			if (emitter.segmentSpawnCount == 0) {
				// first!
				return new LightningRibbonSegment(this.firstLocation, emitter.emitterColor, width, v, arcJitter);
			} else if (emitter.segmentSpawnCount == this.arcCount + 1) {
				// last!
				return new LightningRibbonSegment(this.secondLocation, emitter.emitterColor, width, v, arcJitter);
			} // else
//			final double jitterX = (-.5f + NostrumMagica.rand.nextFloat()) * arcJitter;
//			final double jitterY = (-.5f + NostrumMagica.rand.nextFloat()) * arcJitter;
//			final double jitterZ = (-.5f + NostrumMagica.rand.nextFloat()) * arcJitter;
			//.add(jitterX, jitterY, jitterZ)
			return new LightningRibbonSegment(worldPos, emitter.emitterColor, width, v, this.arcJitter);
		}
		return null;
	}
	
	protected static final class LightningRibbonSegment implements ISegment, CameraFacingSegmentMixin {

		private final StaticSegment segmentWrapper;
		private final @Nullable TargetLocation anchor;
		private final float jitter;
		
		public LightningRibbonSegment(Vec3 position, Color color, float width, float v, float jitter) {
			this.segmentWrapper = new StaticSegment(position, Vector3f.ZERO, color, width, v);
			this.anchor = null;
			this.jitter = jitter;
		}
		
		public LightningRibbonSegment(TargetLocation anchor, Color color, float width, float v, float jitter) {
			this.segmentWrapper = new StaticSegment(Vec3.ZERO, Vector3f.ZERO, color, width, v);
			this.anchor = anchor;
			this.jitter = jitter;
		}

		@Override
		public Vec3 getPosition(Camera camera, EmitterData emitter) {
			if (this.anchor != null) {
				return anchor.getLocation(emitter.partialTicks);
			} // else 
			final double jitterX = (-.5f + NostrumMagica.rand.nextFloat()) * jitter;
			final double jitterY = (-.5f + NostrumMagica.rand.nextFloat()) * jitter;
			final double jitterZ = (-.5f + NostrumMagica.rand.nextFloat()) * jitter;
			
			return segmentWrapper.getPosition(camera, emitter).add(jitterX, jitterY, jitterZ);
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
		public Color getColor(Camera camera, EmitterData emitter, SegmentData ribbonData) {
			return segmentWrapper.color();
		}

		@Override
		public float getWidth(Camera camera, EmitterData emitter, SegmentData ribbonData) {
			return segmentWrapper.width();
		}
	}
	
	public static final LightningChainParticle MakeParticle(ClientLevel world, SpriteSet spites, SpawnParams params) {
		final float[] colors = (params.color == null
				? new float[] {.2f, .4f, 1f, .3f}
				: ColorUtil.ARGBToColor(params.color));
		final int lifetime = params.lifetime + (params.lifetimeJitter > 0 ? NostrumMagica.rand.nextInt(params.lifetimeJitter) : 0);
		final float arcJitter = .125f; // params.range/radius/size ?
		final int arcCount = params.count;
		final TargetLocation firstLocation = params.target.apply(world::getEntity);
		final TargetLocation secondLocation = params.extraTarget.apply(world::getEntity);
		
		LightningChainParticle particle = new LightningChainParticle(world, firstLocation, secondLocation, colors[0], colors[1], colors[2], colors[3], lifetime, arcCount, arcJitter);
			
		return particle;
	}

}
