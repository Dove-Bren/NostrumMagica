package com.smanzana.nostrummagica.entity;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticleData;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;

/**
 * A projectile flies around an arrow, and then hits what the arrow hits.
 * @author Skyler
 *
 */
public class ArrowShardProjectile extends MagicDamageProjectileEntity {
	
	public static final String ID = "arrow_shard_projectile";
	
	protected static final EntityDataAccessor<Integer> FOLLOW_ENT_ID = SynchedEntityData.<Integer>defineId(ArrowShardProjectile.class, EntityDataSerializers.INT);
	
	protected float animOffset;
	
	protected @Nullable AbstractArrow parentArrow;
	protected @Nullable LivingEntity targetEntity;
	protected boolean hasParentLanded;
	protected boolean seeking;
	protected int expireTicks;
	protected float speed;
	protected float correctionRate = .15f;
	
	protected Vec3 lastArrowPos;
	
	public ArrowShardProjectile(EntityType<? extends AbstractHurtingProjectile> type, Level world) {
		super(type, world);
		damage = 2f;
		speed = .4f + NostrumMagica.rand.nextFloat() * .4f;
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::handleProjectile);
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(FOLLOW_ENT_ID, 0);
	}
	
	protected int getFollowEntID() {
		return entityData.get(FOLLOW_ENT_ID);
	}
	
	protected void setFollowEntID(int arrowID) {
		entityData.set(FOLLOW_ENT_ID, arrowID);
	}
	
	public ArrowShardProjectile setParentArrow(AbstractArrow arrow) {
		this.setFollowEntID(arrow.getId());
		this.parentArrow = arrow;
		return this;
	}
	
	public ArrowShardProjectile setRotationOffset(float offset) {
		this.animOffset = offset;
		return this;
	}

	@Override
	protected ParticleOptions getTrailParticle() {
		return new NostrumParticleData(NostrumParticles.WARD.getType(), new SpawnParams(1, 0, 0, 0, 0, 10, 5, new TargetLocation(Vec3.ZERO)));
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
	
	protected Vec3 getDesiredMotion(LivingEntity target) {
		Vec3 targetPos = target.position().add(0, target.getBbHeight()/2f, 0);
		Vec3 diff = targetPos.subtract(this.position());
		return diff.normalize().scale(this.speed);
	}
	
	protected Vec3 getNewMotion(LivingEntity target, double dist) {
		final Vec3 desiredMotion = getDesiredMotion(target).normalize();
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
		if (level.isClientSide()) {
			if (firstTick) {
				NostrumParticles.GLOW_TRAIL.spawn(level, new SpawnParams(1, getX(), getY() + this.getBbHeight() / 2, getZ(), 0, 300, 0,
						new TargetLocation(this, true)).setTargetBehavior(TargetBehavior.ATTACH).color(RenderFuncs.ARGBFade(this.getElement().getColor(), .7f)));
			}
		}
		
		firstTick = false;
		
		//super.tick();
		
		if (level.isClientSide()) {
			doClientEffect();
			return;
		}
		
		if (!hasParentLanded && (this.parentArrow == null || !this.parentArrow.isAlive())) {
			if (this.tickCount < 2) {
				; // just wait
			} else {
				this.discard();
			}
			return;
		}
		
		if (hasParentLanded) {
			if (targetEntity != null) {
				if (seeking && !targetEntity.isRemoved()) {
					// adjust motion
					final double dist = targetEntity.position().add(0, targetEntity.getBbHeight()/2f, 0).subtract(this.position()).length();
					if (dist < 1) {
						this.seeking = false;
						this.expireTicks = this.tickCount +5;
					}
					
					this.setDeltaMovement(getNewMotion(targetEntity, dist));
					super.tick();
				} else if (expireTicks < this.tickCount) {
					this.discard();
					return;
				}
			} else {
				this.discard();
				return;
			}
		} else {
			// follow arrow
			if (lastArrowPos == null) {
				if (parentArrow.getOwner() != null) {
					lastArrowPos = parentArrow.getOwner().position();
				} else {
					lastArrowPos = parentArrow.position();
				}
			}
			
			final Vec3 diff = parentArrow.position().subtract(lastArrowPos);
			final Vec3 n1 = new Vec3(1, 1, -(diff.x + diff.y)).normalize();
			final Vec3 n2 = diff.cross(n1).normalize();
			
			final double radius = 1;
			
			final double prog = (this.animOffset + ((float) this.tickCount / 30f)) * Math.PI * 2; // TODO
			
			final double x = (radius * Math.cos(prog) * n1.x) + (radius * Math.sin(prog) * n2.x);
			final double y = (radius * Math.cos(prog) * n1.y) + (radius * Math.sin(prog) * n2.y);
			final double z = (radius * Math.cos(prog) * n1.z) + (radius * Math.sin(prog) * n2.z);
			
			this.setPos(parentArrow.position().add(x, y, z));
			this.setDeltaMovement(position().subtract(this.xo, yo, zo)); // so that first seeking tick will carry movement
			
			lastArrowPos = parentArrow.position();
			
			if (parentArrow.isOnGround() || parentArrow.shakeTime > 0) {
				this.hasParentLanded = true;
			}
		}
		
//		else if (this.getDeltaMovement().lengthSqr() < .025) {
//			// Stalled out. Gravity?
//			this.setDeltaMovement(0, -.12, 0);
//		}
	}
	
	@Override
	protected boolean canHitEntity(Entity entity) {
		return this.canImpact(entity);
	}
	
	public boolean canImpact(Entity entity) {
		return this.getShooter() == null || ((!entity.equals(getShooter()) && !getShooter().isPassengerOfSameVehicle(entity)));
	}
	
	@Override
	protected void onHitEntity(EntityHitResult result) {
		if (!level.isClientSide()) {
			Entity entityHit = result.getEntity();
			boolean canImpact = this.canImpact(entityHit);
			if (canImpact && entityHit instanceof LivingEntity) {
				this.damageEntity((LivingEntity) entityHit);
				this.discard();
			}
		}
	}
	
	@Override
	protected void onHitBlock(BlockHitResult result) {
//		if (!level.isClientSide()) {
//			NostrumMagicaSounds.CAST_FAIL.play(this);
//			this.discard();
//		}
	}
	
	@Override
	public boolean isPickable() {
		return false;
	}
	
	protected void handleProjectile(LivingAttackEvent event) {
		Entity projectile = event.getSource().getDirectEntity();
		if (this.parentArrow != null && projectile == this.parentArrow) {
			this.hasParentLanded = true;
			this.targetEntity = event.getEntityLiving();
			this.seeking = true;
		}
	}
}
