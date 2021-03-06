package com.smanzana.nostrummagica.entity;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger.ProjectileTriggerInstance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntitySpellProjectile extends EntityFireball {
	
	private ProjectileTriggerInstance trigger;
	private double maxDistance; // Squared distance so no sqrt
	private Vec3d origin;
	
	private @Nullable Predicate<Entity> filter;

	public EntitySpellProjectile(ProjectileTriggerInstance trigger,
			EntityLivingBase shooter, float speedFactor, double maxDistance) {
		this(trigger,
				shooter,
				shooter.worldObj,
				shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ,
				shooter.getLookVec(),
				speedFactor, maxDistance
				);
	}
	
	public EntitySpellProjectile(World world) {
		super(world);
        this.setSize(0.3125F, 0.3125F);
	}
	
	public EntitySpellProjectile(ProjectileTriggerInstance trigger, EntityLivingBase shooter,
			World world,
			double fromX, double fromY, double fromZ, Vec3d direction,
			float speedFactor, double maxDistance) {
		super(world, fromX, fromY, fromZ, 0, 0, 0);
        this.setSize(0.3125F, 0.3125F);
		Vec3d accel = getAccel(direction, speedFactor);
		this.accelerationX = accel.xCoord;
		this.accelerationY = accel.yCoord;
		this.accelerationZ = accel.zCoord;
		this.shootingEntity = shooter;
		
		this.trigger = trigger;
		this.maxDistance = Math.pow(maxDistance, 2);
		this.origin = new Vec3d(fromX, fromY, fromZ);
	}
	
	public void setFilter(@Nullable Predicate<Entity> filter) {
		this.filter = filter;
	}
	
	private Vec3d getAccel(Vec3d direction, double scale) {
		Vec3d base = direction.normalize();
		final double tickScale = .05;
		
		return new Vec3d(base.xCoord * scale * tickScale, base.yCoord * scale * tickScale, base.zCoord * scale * tickScale);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		// if client
//		if (this.ticksExisted % 5 == 0) {
//			this.worldObj.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
//					posX, posY, posZ, 0, 0, 0);
//		}
		
		if (!worldObj.isRemote) {
			if (origin == null) {
				// We got loaded...
				this.setDead();
				return;
			}
			// Can't avoid a SQR; tracking motion would require SQR, too to get path length
			if (this.getPositionVector().squareDistanceTo(origin) > maxDistance) {
				trigger.onFizzle(this.getPosition());
				this.setDead();
			}
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (worldObj.isRemote)
			return;
		
		if (result.typeOfHit == RayTraceResult.Type.MISS) {
			; // Do nothing
		} else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			trigger.onProjectileHit(new BlockPos(result.hitVec));
			this.setDead();
		} else if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
			if (filter == null || filter.apply(result.entityHit)) {
				if ((result.entityHit != shootingEntity && !shootingEntity.isRidingOrBeingRiddenBy(result.entityHit))
						|| this.ticksExisted > 20) {
					trigger.onProjectileHit(result.entityHit);
					this.setDead();
				}
			}
		}
	}
	
	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
    }
	
	@Override
	protected boolean isFireballFiery() {
		return false;
	}
	
	@Override
	protected EnumParticleTypes getParticleType() {
		return EnumParticleTypes.SPELL_MOB;
	}
}
