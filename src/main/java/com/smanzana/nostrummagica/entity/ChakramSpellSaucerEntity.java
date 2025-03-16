package com.smanzana.nostrummagica.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class ChakramSpellSaucerEntity extends SpellSaucerEntity {
	
	public static final String ID = "entity_internal_spellsaucer_chakram";
	
	public ChakramSpellSaucerEntity(EntityType<? extends ChakramSpellSaucerEntity> type, Level world) {
		super(type, world);
	}
	
	protected ChakramSpellSaucerEntity(EntityType<? extends ChakramSpellSaucerEntity> type, ISpellProjectileShape trigger, Level world, LivingEntity shooter, float speed, double maxDistance) {
		super(type, trigger, world, shooter, speed, maxDistance);
	}
	
	protected ChakramSpellSaucerEntity(EntityType<? extends ChakramSpellSaucerEntity> type, ISpellProjectileShape trigger, Level world, LivingEntity shooter,
			Vec3 from, Vec3 direction,
			float speedFactor, double maxDistance) {
		super(type, trigger, shooter.level, shooter, from, direction, speedFactor, maxDistance, -1);
//		direction = direction.normalize();
//		
//		this.setLocationAndAngles(fromX, fromY, fromZ, this.rotationYaw, this.rotationPitch);
//        this.setPosition(fromX, fromY, fromZ);
//        
//        // Set initial motion perpendicular to where we're going to add some cross
//        Vector3d tilt = direction.rotateYaw(90f * (this.rand.nextBoolean() ? 1 : -1)).scale(2);
//        this.setMotion(tilt.x, tilt.y, tilt.z);
//        
//        // Raytrace at hit point, or just go max distance.
//        // If piercing, only cap to raytrace if we hit an entity
//        
//        RayTraceResult trace = RayTrace.raytrace(world, this, this.getPositionVec(), direction, (float) maxDistance, new RayTrace.OtherLiving(shooter));
//        if (trace != null && trace.getType() != RayTraceResult.Type.MISS) {
//        	if (trace.getType() == RayTraceResult.Type.ENTITY) {
//        		Entity entityHit = ((EntityRayTraceResult) trace).getEntity();
//        		this.target = entityHit.getPositionVec().add(0D, entityHit.getHeight() / 2.0, 0D);
//        	} else if (!piercing) { // !piercing cause piercing just wants max dist if no entity being looked at
//        		this.target = trace.getHitVec();
//        	}
//        }
//        
//        if (target == null){
//        	this.target = origin.add(direction.scale(maxDistance));
//        }
	}
	
	public ChakramSpellSaucerEntity(ISpellProjectileShape trigger, Level world, LivingEntity shooter, float speed, double maxDistance) {
		super(NostrumEntityTypes.chakramSpellSaucer, trigger, world, shooter, speed, maxDistance);
	}
	
	public ChakramSpellSaucerEntity(ISpellProjectileShape trigger, Level world, LivingEntity shooter,
			Vec3 from, Vec3 direction,
			float speedFactor, double maxDistance) {
		super(NostrumEntityTypes.chakramSpellSaucer, trigger, shooter.level, shooter, from, direction, speedFactor, maxDistance, -1);
	}

	@Override
	public void tick() {
		super.tick();
		
		if (!level.isClientSide) {
			
			if (origin == null) {
				// We got loaded...
				this.remove();
				return;
			}
			
//			Vector accel = this.getInstantVelocity();
//	        
//	        // Add accel to motionX for raytracing
//			this.setMotion(this.getMotion().add(accel.x, accel.y, accel.z));
//	        
//			RayTraceResult raytraceresult = this.ticksInAir >= 25
//						? ProjectileHelper.getHitResult(this, (ent) -> ent != this.shootingEntity)
//						: null;
//			
//			// Also calc pitch and yaw
//			final Vector3d prevMotion = this.getMotion();
//			float f = MathHelper.sqrt(prevMotion.x * prevMotion.x + prevMotion.z * prevMotion.z);
//            this.rotationPitch = (float)(MathHelper.atan2(prevMotion.y, (double)f) * (180D / Math.PI));
//            this.rotationYaw = (float)(MathHelper.atan2(prevMotion.x, prevMotion.z) * (180D / Math.PI));
//			
//            this.setMotion(this.getMotion().add(-accel.x, -accel.y, -accel.z));
//
//	        if (raytraceresult != null)
//	        {
//	            this.onImpact(raytraceresult);
//	        }
//	        
//	        {
//	        	final Vector3d motion = this.getMotion();
//	        	this.setPosition(getPosX() + motion.x + accel.x, getPosY() + motion.y + accel.y, getPosZ() + motion.z + accel.z);
//	        }
//	        
//	        // Apply air-friction, making motion's sort-of our initial motion
//	        this.setMotion(this.getMotion().scale(0.8));
//			
////				// Can't avoid a SQR; tracking motion would require SQR, too to get path length
////				if (this.getPositionVec().squareDistanceTo(origin) > maxDistance) {
////					trigger.onFizzle(this.getPosition());
////					this.remove();
////				}
//			
//			// Check for motion boundaries
//			if (this.returning) {
//				if (Math.abs(this.getPositionVec().distanceTo(this.origin)) <= 0.5) {
//					if (++trips >= maxTrips) {
//						this.remove();
//					} else {
//						returning = false;
//						// Capture motion to get boomerang-effect
//						Vector3d motion = new Vector3d(accel.x, accel.y, accel.z).normalize().scale(1);
//						motion = motion.rotateYaw(30f * (this.rand.nextBoolean() ? 1 : -1));
//						this.setMotion(this.getMotion().add(motion));
//					}
//				}
//			} else {
//				if (Math.abs(this.getPositionVec().distanceTo(this.target)) <= 0.5) {
//					this.returning = true;
//					
//					// Capture motion to get boomerang-effect
//					Vector3d motion = new Vector3d(accel.x, accel.y, accel.z).normalize().scale(1);
//					motion = motion.rotateYaw(30f * (this.rand.nextBoolean() ? 1 : -1));
//					this.setMotion(this.getMotion().add(motion));
//				}
//			}
		}
	}
	
	@Override
	public boolean canImpact(BlockPos pos) {
		return true;
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
}
