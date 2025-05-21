package com.smanzana.nostrummagica.client.particles;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ParticleTargetMotion {
	protected TargetLocation target;
	protected final ParticleTargetBehavior targetBehavior;
	
	protected final float fixedRandom;
	protected float impulse;
	protected Vec3 offsetPos;
	protected boolean autoRandomOffset;
	
	protected int age;
	
	public ParticleTargetMotion(float fixedRandom, float impulse) {
		this.fixedRandom = fixedRandom;
		this.targetBehavior = new ParticleTargetBehavior();
		this.offsetPos = Vec3.ZERO;
		this.age = 0;
		this.impulse = impulse;
	}
	
	public ParticleTargetMotion setAutoRandomOffset() {
		this.autoRandomOffset = true;
		return this;
	}
	
	public ParticleTargetMotion setTarget(TargetLocation target) {
		this.target = target;
		if (autoRandomOffset && target != null) {
			this.setRandomTargetOffset();
		}
		return this;
	}
	
	public ParticleTargetMotion setTargetOffset(Vec3 offset) {
		this.offsetPos = offset;
		return this;
	}
	
	public ParticleTargetMotion setRandomTargetOffset() {
		final double wRad = target.getTargetWidth() * 2; // double width
		final double hRad = target.getTargetHeight();
		final Random rand = new Random((int) (this.fixedRandom * 16777216));
		return this.setTargetOffset(new Vec3(
				wRad * (rand.nextDouble() - .5),
				hRad * (rand.nextDouble() - .5),
				wRad * (rand.nextDouble() - .5)
				));
	}
	
	public ParticleTargetMotion joinMode(boolean dieOnTarget) {
		this.targetBehavior.entityBehavior = TargetBehavior.JOIN;
		this.targetBehavior.dieWithTarget = dieOnTarget;
		return this;
	}
	
	public ParticleTargetMotion orbitMode(float radius, boolean lazy) {
		this.targetBehavior.entityBehavior = lazy ? TargetBehavior.ORBIT_LAZY : TargetBehavior.ORBIT;
		this.targetBehavior.orbitRadius = radius;
		return this;
	}
	
	public ParticleTargetMotion orbitMode(float radius) {
		return orbitMode(radius, true);
	}
	
	public ParticleTargetMotion orbitMode(boolean lazy) {
		if (this.target != null) {
			return orbitMode(target.getTargetWidth() * 2);
		}
		return orbitMode(1f, lazy);
	}
	
	public ParticleTargetMotion orbitMode() {
		return orbitMode(true);
	}
	
	public ParticleTargetMotion attachMode() {
		this.targetBehavior.entityBehavior = TargetBehavior.ATTACH;
		return this;
	}
	
	protected float fixedRandom() {
		return this.fixedRandom;
	}
	
	protected Vec3 smoothMotionTo(Vec3 desiredPosition, Vec3 particlePosition, Vec3 particleMotion) {
		Vec3 posDelta = desiredPosition.subtract(particlePosition);
		Vec3 idealVelocity = posDelta.normalize().scale(.3);
		return particleMotion.scale(1-impulse).add(idealVelocity.scale(impulse));
	}
	
	protected @Nullable MotionUpdate updateOrbit(Vec3 baseTarget, Vec3 particlePosition, Vec3 particleMotion, boolean lazy) {
		final float orbitRadius = this.targetBehavior.orbitRadius;
		final float orbitPeriod = this.targetBehavior.orbitPeriod;
		final float randomOrbitOffset = this.fixedRandom() * 2 * Mth.PI;
		
		final float rot = Mth.PI * 2 * (((float) age % orbitPeriod) / orbitPeriod) + randomOrbitOffset;
		
		final Vec3 target = new Vec3(orbitRadius, 0, 0).yRot(rot).add(baseTarget);
		if (lazy) {
			return new MotionUpdate(null, smoothMotionTo(target, particlePosition, particleMotion));
		} // else
		return new MotionUpdate(target, null);
	}
	
	protected @Nullable MotionUpdate updateJoin(Vec3 baseTarget, Vec3 particlePosition, Vec3 particleMotion) {
		if (this.targetBehavior.dieWithTarget && baseTarget.distanceToSqr(particlePosition) < 0.01) {
			return null;
		}
		
		return new MotionUpdate(null, smoothMotionTo(baseTarget, particlePosition, particleMotion));
	}
	
	protected @Nullable MotionUpdate updateAttach(Vec3 baseTarget, Vec3 particlePosition, Vec3 particleMotion) {
		if (this.targetBehavior.dieWithTarget && !this.target.isValid()) {
			return null;
		}
		return new MotionUpdate(baseTarget, Vec3.ZERO);
	}
	
	protected @Nullable MotionUpdate updatePosition(Vec3 particlePosition, Vec3 particleMotion) {
		Vec3 baseTarget = this.target == null ? null : this.target.getLocation();
		
		if (baseTarget == null) {
			return null; // Don't bother continuing
		}
		
		// Adjust base target based on offset position if we have one
		if (this.offsetPos != null) {
			baseTarget = baseTarget.add(this.offsetPos);
		}
		
		switch (this.targetBehavior.entityBehavior) {
		case JOIN:
			return updateJoin(baseTarget, particlePosition, particleMotion);
		case ORBIT:
		case ORBIT_LAZY:
			return updateOrbit(baseTarget, particlePosition, particleMotion, this.targetBehavior.entityBehavior == TargetBehavior.ORBIT_LAZY);
		case ATTACH:
			return updateAttach(baseTarget, particlePosition, particleMotion);
		}
		
		return null;
	}
	
	public static final record MotionUpdate(@Nullable Vec3 desiredPosition, @Nullable Vec3 desiredMotion) {}
	
	public @Nullable MotionUpdate update(Vec3 particlePosition, Vec3 particleMotion) {
		final MotionUpdate result = updatePosition(particlePosition, particleMotion);
		age++;
		return result;
	}
	
	public boolean shouldUpdate() {
		return this.target != null;
	}
	
	public static final <T extends IMotionParticle<?>> void ApplyUpdate(@Nonnull MotionUpdate update, IMotionParticle<T> particle) {
		if (update.desiredPosition() != null) {
			particle.setPosition(update.desiredPosition());
		}
		if (update.desiredMotion() != null) {
			particle.setMotion(update.desiredMotion());
		}
	}
}
