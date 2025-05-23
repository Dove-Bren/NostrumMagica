package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumSet;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Movement AI to orbit around the owner.
 * Doesn't support pathfinding and will make things try to move into the air. Intended for flying entities.
 * @author Skyler
 *
 * @param <T>
 */
public class OrbitEntityGenericGoal<T extends Mob> extends Goal {
	
	private static final double DEFAULT_WOBBLE = 5 * 20;
	private static final int DEFAULT_BUMPS = 1;
	
	protected final T ent;
	protected LivingEntity orbitTarget;
	protected Level theWorld;
	protected final double orbitDistance;
	protected final double assembleSpeed;
	protected final double orbitPeriod;
	protected final double ringWobbleSpeed;
	protected final int ringWobbleBumps;
	protected final MoveControl entMovementHelper;
	protected final Predicate<? super T> startPred;
	protected final Predicate<? super T> continuePred;
	
	private double offsetYaw;
	private final MutableVector3d cursor;

	public OrbitEntityGenericGoal(T orbiter, LivingEntity target, double orbitDistance, double orbitPeriod) {
		this(orbiter, target, orbitDistance, orbitPeriod, 1.0, DEFAULT_WOBBLE, DEFAULT_BUMPS, null, null);
	}
	
	public OrbitEntityGenericGoal(T orbiter, LivingEntity target, double orbitDistance, double orbitPeriod, double assembleSpeed,
			double ringWobbleSpeed, int ringWobbleBumps, Predicate<? super T> startFilter, Predicate<? super T> continueFilter) {
		this.ent = orbiter;
		this.theWorld = orbiter.level;
		this.entMovementHelper = orbiter.getMoveControl();
		this.orbitTarget = target;
		this.orbitDistance = orbitDistance;
		this.orbitPeriod = orbitPeriod;
		this.assembleSpeed = assembleSpeed;
		this.ringWobbleSpeed = ringWobbleSpeed;
		this.ringWobbleBumps = ringWobbleBumps;
		this.startPred = startFilter;
		this.continuePred = continueFilter;
		
		cursor = new MutableVector3d(0, 0, 0);
		
		this.setFlags(EnumSet.of(Goal.Flag.JUMP));
	}
	
	protected LivingEntity getOrbitTarget() {
		return this.orbitTarget;
	}

	/**
	 * Returns whether the Goal should begin execution.
	 */
	public boolean canUse() {
		LivingEntity entitylivingbase = this.getOrbitTarget();

		if (entitylivingbase == null) {
			return false;
		} else if (entitylivingbase instanceof Player && ((Player)entitylivingbase).isSpectator()) {
			return false;
//		} else if (this.ent.getMoveHelper().isUpdating()
//				&& ent.getDistanceSq(ent.getMoveHelper().getX(), ent.getMoveHelper().getY(), ent.getMoveHelper().getZ()) > 1.0) {
//			return false;
		} else if (!ent.level.equals(entitylivingbase.getCommandSenderWorld())) {
			return false;
		} else if (ent.distanceToSqr(entitylivingbase) > 1024) {
			return false;
		} else if (startPred != null && !startPred.apply(ent)) {
			return false;
		} else {
			this.orbitTarget = entitylivingbase; // In case getOrbitTarget is overriden
			this.theWorld = entitylivingbase.level;
			return true;
		}
	}

	/**
	 * Returns whether an in-progress Goal should continue executing
	 */
	public boolean canContinueToUse() {
		// Only stop if something died, something changed worlds, or a continue pred was provided and returns false.
		
		if (		!ent.isAlive()
				|| !orbitTarget.isAlive()
				|| !ent.level.equals(orbitTarget.level)
				|| (continuePred != null && !continuePred.apply(ent))) {
			return false;
		}
		
		return true;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		// Figure out what our starting yaw is
		offsetYaw = NostrumMagica.rand.nextFloat();
	}

	/**
	 * Resets the task
	 */
	public void stop() {
		;
	}

	protected static MutableVector3d getPoint(MutableVector3d posOut, double radius, double periodTicks, double elapsedTicks, double periodOffset,
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
			posOut = new MutableVector3d(x, y, z);
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
	public void tick() {
		getPoint(cursor, orbitDistance, orbitPeriod, ent.tickCount, offsetYaw, ringWobbleBumps, ringWobbleSpeed);
		if (ent.distanceToSqr(cursor.xCoord + orbitTarget.getX(), cursor.yCoord + orbitTarget.getY() + orbitTarget.getEyeHeight() + .75, cursor.zCoord + orbitTarget.getZ())
				> 512) {
			ent.setPos(cursor.xCoord + orbitTarget.getX(), cursor.yCoord + orbitTarget.getY() + orbitTarget.getEyeHeight() + .75, cursor.zCoord + orbitTarget.getZ());
		} else {
			ent.getMoveControl().setWantedPosition(cursor.xCoord + orbitTarget.getX(), cursor.yCoord + orbitTarget.getY() + orbitTarget.getEyeHeight() + .75, cursor.zCoord + orbitTarget.getZ(), 2);
		}
	}
	
	private static final class MutableVector3d {
		public double xCoord;
		public double yCoord;
		public double zCoord;
		
		public MutableVector3d(double x, double y, double z) {
			this.xCoord = x;
			this.yCoord = y;
			this.zCoord = z;
		}
	}
}
