package com.smanzana.nostrummagica.entity.tasks;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/**
 * Movement AI to orbit around the owner.
 * Doesn't support pathfinding and will make things try to move into the air. Intended for flying entities.
 * @author Skyler
 *
 * @param <T>
 */
public class EntityAIOrbitEntityGeneric<T extends MobEntity> extends Goal {
	
	private static final double DEFAULT_WOBBLE = 5 * 20;
	private static final int DEFAULT_BUMPS = 1;
	
	protected final T ent;
	protected LivingEntity orbitTarget;
	protected World theWorld;
	protected final double orbitDistance;
	protected final double assembleSpeed;
	protected final double orbitPeriod;
	protected final double ringWobbleSpeed;
	protected final int ringWobbleBumps;
	protected final EntityMoveHelper entMovementHelper;
	protected final Predicate<? super T> startPred;
	protected final Predicate<? super T> continuePred;
	
	private double offsetYaw;
	private final MutableVec3d cursor;

	public EntityAIOrbitEntityGeneric(T orbiter, LivingEntity target, double orbitDistance, double orbitPeriod) {
		this(orbiter, target, orbitDistance, orbitPeriod, 1.0, DEFAULT_WOBBLE, DEFAULT_BUMPS, null, null);
	}
	
	public EntityAIOrbitEntityGeneric(T orbiter, LivingEntity target, double orbitDistance, double orbitPeriod, double assembleSpeed,
			double ringWobbleSpeed, int ringWobbleBumps, Predicate<? super T> startFilter, Predicate<? super T> continueFilter) {
		this.ent = orbiter;
		this.theWorld = orbiter.world;
		this.entMovementHelper = orbiter.getMoveHelper();
		this.orbitTarget = target;
		this.orbitDistance = orbitDistance;
		this.orbitPeriod = orbitPeriod;
		this.assembleSpeed = assembleSpeed;
		this.ringWobbleSpeed = ringWobbleSpeed;
		this.ringWobbleBumps = ringWobbleBumps;
		this.startPred = startFilter;
		this.continuePred = continueFilter;
		
		cursor = new MutableVec3d(0, 0, 0);
		
		this.setMutexBits(4);
	}
	
	protected LivingEntity getOrbitTarget() {
		return this.orbitTarget;
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean shouldExecute() {
		LivingEntity entitylivingbase = this.getOrbitTarget();

		if (entitylivingbase == null) {
			return false;
		} else if (entitylivingbase instanceof PlayerEntity && ((PlayerEntity)entitylivingbase).isSpectator()) {
			return false;
//		} else if (this.ent.getMoveHelper().isUpdating()
//				&& ent.getDistanceSq(ent.getMoveHelper().getX(), ent.getMoveHelper().getY(), ent.getMoveHelper().getZ()) > 1.0) {
//			return false;
		} else if (!ent.world.equals(entitylivingbase.getEntityWorld())) {
			return false;
		} else if (ent.getDistanceSq(entitylivingbase) > 1024) {
			return false;
		} else if (startPred != null && !startPred.apply(ent)) {
			return false;
		} else {
			this.orbitTarget = entitylivingbase; // In case getOrbitTarget is overriden
			this.theWorld = entitylivingbase.world;
			return true;
		}
	}

	/**
	 * Returns whether an in-progress Goal should continue executing
	 */
	public boolean shouldContinueExecuting() {
		// Only stop if something died, something changed worlds, or a continue pred was provided and returns false.
		
		if (		!ent.isAlive()
				|| !orbitTarget.isAlive()
				|| !ent.world.equals(orbitTarget.world)
				|| (continuePred != null && !continuePred.apply(ent))) {
			return false;
		}
		
		return true;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		// Figure out what our starting yaw is
		offsetYaw = NostrumMagica.rand.nextFloat();
	}

	/**
	 * Resets the task
	 */
	public void resetTask() {
		;
	}

	protected static MutableVec3d getPoint(MutableVec3d posOut, double radius, double periodTicks, double elapsedTicks, double periodOffset,
			int ringBumps, double ringWobblePeriod) {
		// x/z come from one regular circle with radius [radius] an period [periodTicks].
		// On top of that, y offset comes from the ring wobble params.
		final double perc = ((elapsedTicks / periodTicks) + periodOffset) % 1.0;
		final double radians = perc * Math.PI * 2;
		final double x = Math.cos(radians) * radius;
		final double z = Math.sin(radians) * radius;
		
		final double wobblePerc = ((elapsedTicks / ringWobblePeriod) + periodOffset) % 1.0;
		final double wobbleRadians = ringBumps * ((wobblePerc * 2 * Math.PI) + radians); // 0 for consistent Y if no bumps. Otherwise
		final double y = Math.sin(wobbleRadians) * .5;
		
		if (posOut == null) {
			posOut = new MutableVec3d(x, y, z);
		} else {
			posOut.xCoord = x;
			posOut.yCoord = y;
			posOut.zCoord = z;
		}
		
		return posOut;
	}

	/**
	 * Updates the task
	 */
	public void updateTask() {
		getPoint(cursor, orbitDistance, orbitPeriod, ent.ticksExisted, offsetYaw, ringWobbleBumps, ringWobbleSpeed);
		if (ent.getDistanceSq(cursor.xCoord + orbitTarget.posX, cursor.yCoord + orbitTarget.posY + orbitTarget.getEyeHeight() + .75, cursor.zCoord + orbitTarget.posZ)
				> 512) {
			ent.setPosition(cursor.xCoord + orbitTarget.posX, cursor.yCoord + orbitTarget.posY + orbitTarget.getEyeHeight() + .75, cursor.zCoord + orbitTarget.posZ);
		} else {
			ent.getMoveHelper().setMoveTo(cursor.xCoord + orbitTarget.posX, cursor.yCoord + orbitTarget.posY + orbitTarget.getEyeHeight() + .75, cursor.zCoord + orbitTarget.posZ, 2);
		}
	}
	
	private static final class MutableVec3d {
		public double xCoord;
		public double yCoord;
		public double zCoord;
		
		public MutableVec3d(double x, double y, double z) {
			this.xCoord = x;
			this.yCoord = y;
			this.zCoord = z;
		}
	}
}
