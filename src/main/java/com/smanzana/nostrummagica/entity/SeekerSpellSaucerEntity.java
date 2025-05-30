package com.smanzana.nostrummagica.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SeekerSpellSaucerEntity extends SpellSaucerEntity {
	
	public static final String ID = "entity_internal_spellsaucer_seeker";
	
	protected TargetLocation target;
	protected float correctionRate = .15f;
	protected float speed;
	protected boolean seeking;
	protected int expireTicks;
	
	// Animation variables for clients
	protected int ticksPerSegment = 1;
	protected final List<Section> animationSegments;
	
	public SeekerSpellSaucerEntity(EntityType<? extends SeekerSpellSaucerEntity> type, Level world) {
		super(type, world);
		this.animationSegments = new ArrayList<>();
	}
	
	protected SeekerSpellSaucerEntity(EntityType<? extends SeekerSpellSaucerEntity> type, ISpellProjectileShape trigger, Level world, LivingEntity shooter, Vec3 pos, Vec3 direction, float speed, TargetLocation target) {
		super(type, trigger, world, shooter, pos, direction, speed, 1000, -1);
		this.target = target;
		this.speed = speed;
		this.animationSegments = new ArrayList<>();
		this.seeking = true;
		
		// Override parent behavior of using velocity as power and set instant velocity
		this.xPower = 0;
		this.yPower = 0;
		this.zPower = 0;
		this.setDeltaMovement(direction.normalize().scale(speed));
		
	}
	
	public SeekerSpellSaucerEntity(ISpellProjectileShape trigger, Level world, LivingEntity shooter, Vec3 pos, Vec3 direction, float speed, TargetLocation target) {
		this(NostrumEntityTypes.seekerSpellSaucer, trigger, world, shooter, pos, direction, speed, target);
	}
	
	protected float getYRotForDiff(Vec3 diff) {
		return (float) Mth.atan2(diff.x, diff.z);
	}
	
	protected float getXRotForDiff(Vec3 diff) {
		return (float) Mth.atan2(diff.y, diff.horizontalDistance());
	}
	
	protected float getCorrectionRateForDistance(double distance) {
		final double threshold = 16;
		
		// deduct from perceived distance as age increases to make it more accurate over time
		distance = Math.max(0, distance - this.tickCount*.01);
		
		if (distance >= threshold) {
			// return base
			return correctionRate;
		} else {
			// ramp it up to 1f as it gets closer to 0
			return Mth.lerp((float) ((threshold - distance) / threshold), correctionRate, 1f);
		}
	}
	
	protected Vec3 getDesiredMotion() {
		if (target == null) {
			return Vec3.ZERO;
		}
		Vec3 targetPos = this.target.getLocation();
		Vec3 diff = targetPos.subtract(this.position());
		return diff.normalize().scale(this.speed);
	}
	
	protected Vec3 getNewMotion(double dist) {
		final Vec3 desiredMotion = getDesiredMotion().normalize();
		final float TO_DEG = 180f / Mth.PI;
		final float desiredYaw = TO_DEG * getYRotForDiff(desiredMotion);
		final float desiredPitch = TO_DEG * getXRotForDiff(desiredMotion);
		final float curYaw = TO_DEG * getYRotForDiff(this.getDeltaMovement());
		final float curPitch = TO_DEG * getXRotForDiff(this.getDeltaMovement());
		
//		final double desiredY = desiredMotion.y;
//		final double currentY = this.getDeltaMovement().y;
		
		// Lerp between yaw for x/z, and y component seperately for y
		float newYaw = Mth.approachDegrees(curYaw, desiredYaw, getCorrectionRateForDistance(dist) * 20f);
		float newPitch = Mth.approachDegrees(curPitch, desiredPitch, getCorrectionRateForDistance(dist) * 5f);
		
		final Vec3 newMotion = Vec3.directionFromRotation(-newPitch, -newYaw).scale(speed);
		
		return newMotion;
	}

	@Override
	public void tick() {
		super.tick();
		
		if (!level.isClientSide) {
			if (origin == null) {
				// We got loaded...
				this.discard();
				return;
			}
			
			if (target != null && seeking) {
				// adjust motion
				final double dist = target.getLocation().subtract(this.position()).length();
				if (dist < 1) {
					this.seeking = false;
					this.expireTicks = this.tickCount +5;
				}
				
				this.setDeltaMovement(getNewMotion(dist));
			} else if (expireTicks < this.tickCount) {
				this.discard();
				return;
			}
			
		} else {
			if ((this.tickCount-1) % ticksPerSegment == 0) {
				this.recordAnimationFrame();
			}
		}
	}
	
	@Override
	public boolean canImpact(BlockPos pos) {
		return false;
	}
	
	@Override
	public boolean dieOnImpact(BlockPos pos) {
		return false;
	}
	
	@Override
	public boolean canImpact(Entity entity) {
		return super.canImpact(entity);
	}
	
	@Override
	public boolean dieOnImpact(Entity entity) {
		return false;
	}
	
	@Override
	protected void doImpact(Entity entity) {
		super.doImpact(entity);
	}
	
	protected void cleanupAnimationFrames() {
		Iterator<Section> it = this.animationSegments.iterator();
		final int lifetime = this.getRenderSectionLifetime();
		while (it.hasNext()) {
			Section section = it.next();
			if (this.tickCount > section.startTicks + lifetime) {
				it.remove();
			}
		}
	}
	
	private Vec3 lastCapturePos = null;
	private float lastYRotDef = 0f;
	
	protected Quaternion captureRotation() {
		if (lastCapturePos == null) {
			lastCapturePos = new Vec3(xo, yo, zo);
			lastYRotDef = this.yRotO;
		}
		
		// pitch translates cleanly, I think. Yaw changes we want to turn into roll
		final Vec3 motionDiff = this.position().subtract(lastCapturePos).normalize();
		final float yRotDeg = getYRotForDiff(motionDiff) * (180f / Mth.PI);
		final float xRotDeg = getXRotForDiff(motionDiff) * (180f / Mth.PI);
		
		float yawDiff = Mth.degreesDifference(lastYRotDef, yRotDeg);
		Quaternion quat = Vector3f.YP.rotationDegrees(yRotDeg + 0f);
		quat.mul(Vector3f.XP.rotationDegrees(-xRotDeg));
		quat.mul(Vector3f.ZP.rotationDegrees(-yawDiff));
		lastCapturePos = this.position();
		lastYRotDef = yRotDeg;
		return quat;
	}
	
	protected void recordAnimationFrame() {
		this.cleanupAnimationFrames();
		if (lastCapturePos == null || this.position().distanceTo(lastCapturePos) > 0.25) {
			this.animationSegments.add(new Section(this.position(), this.tickCount, captureRotation()));
		}
	}
	
	public static record Section(Vec3 pos, int startTicks, Quaternion rotation) {}

	public List<Section> getRenderSections() {
		return this.animationSegments;
	}

	public int getRenderSectionLifetime() {
		return 40;
	}
}
