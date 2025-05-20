package com.smanzana.nostrummagica.client.particles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ParticleTargetMotion {
	protected Vec3 targetPos;
	protected Entity targetEntity;
	protected final ParticleTargetBehavior targetBehavior;
	
	protected final float fixedRandom;
	protected float impulse;
	protected Vec3 offsetPos;
	
	protected int age;
	
	public ParticleTargetMotion(float fixedRandom, float impulse) {
		this.fixedRandom = fixedRandom;
		this.targetBehavior = new ParticleTargetBehavior();
		this.offsetPos = Vec3.ZERO;
		this.age = 0;
		this.impulse = impulse;
	}
	
	public ParticleTargetMotion setTarget(Entity ent) {
		this.targetEntity = ent;
		this.targetPos = null;
		if (this.targetPos == null && ent != null) {
			final double wRad = ent.getBbWidth() * 2; // double width
			final double hRad = ent.getBbHeight();
			this.targetPos = new Vec3(wRad * (NostrumMagica.rand.nextDouble() - .5),
					hRad * (NostrumMagica.rand.nextDouble() - .5),
					wRad * (NostrumMagica.rand.nextDouble() - .5));
		}
		return this;
	}
	
	public ParticleTargetMotion setTarget(Vec3 targetPos) {
		this.targetEntity = null;
		this.targetPos = targetPos;
		return this;
	}
	
	public ParticleTargetMotion setTargetOffset(Vec3 offset) {
		this.offsetPos = offset;
		return this;
	}
	
	public ParticleTargetMotion setRandomTargetOffset(Entity ent) {
		final double wRad = ent.getBbWidth() * 2; // double width
		final double hRad = ent.getBbHeight();
		return this.setTargetOffset(new Vec3(
				wRad * (NostrumMagica.rand.nextDouble() - .5),
				hRad * (NostrumMagica.rand.nextDouble() - .5),
				wRad * (NostrumMagica.rand.nextDouble() - .5)
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
		if (this.targetEntity != null) {
			return orbitMode(targetEntity.getBbWidth() * 2);
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
		if (this.targetBehavior.dieWithTarget && this.targetEntity != null && !this.targetEntity.isAlive()) {
			return null;
		}
		return new MotionUpdate(baseTarget, Vec3.ZERO);
	}
	
	protected @Nullable MotionUpdate updatePosition(Vec3 particlePosition, Vec3 particleMotion) {
		Vec3 baseTarget = null;
		if (this.targetEntity != null) {
			// Could check if they're alive but I don't think it matters. They have a position!
			baseTarget = this.targetEntity.position().add(0, this.targetEntity.getBbHeight() / 2, 0);
		} else if (this.targetPos != null) {
			baseTarget = targetPos;
		}
		
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
		return this.targetPos != null || this.targetEntity != null;
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
