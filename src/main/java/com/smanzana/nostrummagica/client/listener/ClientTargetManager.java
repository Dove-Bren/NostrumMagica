package com.smanzana.nostrummagica.client.listener;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.petcommand.util.RayTrace;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Provides ease-of-use for clients to select targets. Especially helpful for raycasts where the moment-to-moment
 * can be difficult to stay directly over the target.
 * Client side only.
 */
public class ClientTargetManager {
	
	public static final float STANDARD_VIEW = .01f;
	public static final float LARGE_VIEW = .025f;

	protected @Nullable LivingEntity lastTarget;
	
	public ClientTargetManager() {
		
	}
	
	/**
	 * Quickly return whatever the last selected target was, if it's still within an allowable distance
	 * @return
	 */
	public @Nullable LivingEntity getLastTarget(float viewTolerance) {
		return this.fetchOrResetLastTarget(viewTolerance);
	}
	
	public @Nullable LivingEntity traceOrLastTarget(Supplier<HitResult> tracer, float viewTolerance) {
		HitResult result = tracer.get();
		LivingEntity ent = RayTrace.livingFromRaytrace(result);
		if (ent == null) {
			ent = fetchOrResetLastTarget(viewTolerance);
		} else {
			this.lastTarget = ent;
		}
		return ent;
	}
	
	protected @Nullable LivingEntity fetchOrResetLastTarget(float viewTolerance) {
		if (lastTarget != null) {
			final Vec3 diff = lastTarget.position().add(0, lastTarget.getBbHeight() / 2, 0).subtract(this.getCameraPos()).normalize();
			final Vec3 look = this.getCameraLook().normalize();
			
			final float deviation = (float) look.dot(diff);
			if (1f - deviation > viewTolerance) {
				clearTarget();
			}
		}
		return lastTarget;
	}
	
	private Vec3 getCameraPos() {
		final Minecraft mc = Minecraft.getInstance();
		return mc.gameRenderer.getMainCamera().getPosition();
	}
	
	private Vec3 getCameraLook() {
		final Minecraft mc = Minecraft.getInstance();
		return new Vec3(mc.gameRenderer.getMainCamera().getLookVector());
	}

	public void clearTarget() {
		this.lastTarget = null;
	}
	
}
