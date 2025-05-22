package com.smanzana.nostrummagica.util;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Wraps up either an entity or absolute world position and lets callers not care what they have when they need
 * a world position to translate to etc.
 */
public class TargetLocation {

	protected final Vec3 targetPos; // Absolute position to move to (if targetEntity == null) or offset from entity to go to
	protected final @Nullable Entity targetEntity;
	
	protected TargetLocation(@Nullable Entity targetEntity, Vec3 targetPos) {
		this.targetPos = targetPos;
		this.targetEntity = targetEntity;
	}
	
	public TargetLocation(Entity targetEntity, boolean center) {
		this(targetEntity, center ? new Vec3(0, targetEntity.getBbHeight() / 2f, 0) : Vec3.ZERO);
	}
	
	public TargetLocation(Vec3 position) {
		this(null, position);
	}
	
	public Vec3 getLocation() {
		if (targetEntity == null) {
			return targetPos;
		} else {
			return targetEntity.position().add(targetPos);
		}
	}

	public float getTargetWidth() {
		return targetEntity == null ? 1 : targetEntity.getBbWidth();
	}

	public double getTargetHeight() {
		return targetEntity == null ? 1 : targetEntity.getBbHeight();
	}

	public boolean isValid() {
		return targetEntity == null || targetEntity.isAlive();
	}
}
