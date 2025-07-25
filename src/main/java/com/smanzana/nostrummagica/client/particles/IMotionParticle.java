package com.smanzana.nostrummagica.client.particles;

import java.util.function.Function;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Largely a lazy defaults-implemented view of things with ParticleTargetMotion components
 */
public interface IMotionParticle<T extends IMotionParticle<?>> {
	
	public ParticleTargetMotion getMotion();
	
	@SuppressWarnings("unchecked")
	private static <T extends IMotionParticle<?>> T Self(IMotionParticle<?> self) {
		return (T) self;
	}
	
	public default T setTarget(TargetLocation target) {
		getMotion().setTarget(target);
		return Self(this);
	}
	
	public default T setTargetOffset(Vec3 offset) {
		getMotion().setTargetOffset(offset);
		return Self(this);
	}
	
	public default T setRandomTargetOffset() {
		getMotion().setRandomTargetOffset();
		return Self(this);
	}
	
	public default T setBehavior(ParticleTargetBehavior behavior) {
		getMotion().setBehavior(behavior);
		return Self(this);
	}
	
	public default T setGravity(boolean gravity) {
		return setGravityStrength(gravity ? .01f : 0);
	}
	
	public default T setMotion(Vec3 motion) {
		return this.setMotion(motion.x, motion.y, motion.z);
	}
	
	public default T setMotion(Vec3 motion, Vec3 jitter) {
		return this.setMotion(motion.x, motion.y, motion.z, jitter.x, jitter.y, jitter.z);
	}
	
	public default T setMotion(double xVelocity, double yVelocity, double zVelocity,
			double xJitter, double yJitter, double zJitter) {
		return this.setMotion(
				xVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * xJitter, // +- jitter
				yVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * yJitter,
				zVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * zJitter
		);
	}
	
	public default T setPosition(Vec3 position) {
		return setPosition(position.x(), position.y(), position.z());
	}
	
	public default T setFromParams(SpawnParams params, Function<Integer, Entity> entityLookup) {
		if (params.target != null) {
			this.setTarget(params.target.apply(entityLookup));
		}
		if (params.velocity != null) {
			this.setMotion(params.velocity, params.velocityJitter == null ? Vec3.ZERO : params.velocityJitter);
		}
		if (params.gravityStrength != 0f) {
			this.setGravityStrength(params.gravityStrength);
		}
		if (params.targetBehavior != null) {
//			switch (params.targetBehavior) {
//			case JOIN:
//				this.joinMode(params.dieWithTarget);
//				break;
//			case ORBIT:
//			case ORBIT_LAZY:
//				if (params.orbitRadius > 0) {
//					this.orbitMode(params.orbitRadius, params.targetBehavior == TargetBehavior.ORBIT_LAZY);
//				} else {
//					this.orbitMode(params.targetBehavior == TargetBehavior.ORBIT_LAZY);
//				}
//				break;
//			case ATTACH:
//				this.attachMode();
//				break;
//			}
			this.setBehavior(params.targetBehavior);
		}
		
		return Self(this);
	}
	
	
	///////////////////////////////////////////////////////////
	//// Required implementations to power everything else ////
	///////////////////////////////////////////////////////////
	
	public T setGravityStrength(float strength);
	
	public T setMotion(double xVelocity, double yVelocity, double zVelocity);

	public T setPosition(double x, double y, double z);
	
}
