package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger.MagicCutterTriggerInstance;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityChakramSpellSaucer extends EntitySpellSaucer {
	
	public static final String ID = "entity_internal_spellsaucer_chakram";
	
	// Chakram:
	private Vec3d origin;
	private Vec3d target;
	private boolean returning;
	private int trips = 0;
	
	private boolean piercing; // Configurable by the player\
	private int maxTrips;
	
	public EntityChakramSpellSaucer(EntityType<? extends EntityChakramSpellSaucer> type, World world) {
		super(type, world);
	}
	
	public EntityChakramSpellSaucer(EntityType<? extends EntityChakramSpellSaucer> type, World world, LivingEntity shooter, MagicCutterTriggerInstance trigger, float speed) {
		super(type, world, shooter, trigger, speed);
        this.returning = false;
	}
	
	public EntityChakramSpellSaucer(EntityType<? extends EntityChakramSpellSaucer> type,
			MagicCutterTriggerInstance trigger, LivingEntity shooter,
			World world,
			double fromX, double fromY, double fromZ, Vec3d direction,
			float speedFactor, double maxDistance, boolean piercing, int maxTrips) {
		this(type, world, shooter, trigger, speedFactor);
		
		this.origin = new Vec3d(fromX, fromY, fromZ);
		direction = direction.normalize();
		
		this.setLocationAndAngles(fromX, fromY, fromZ, this.rotationYaw, this.rotationPitch);
        this.setPosition(fromX, fromY, fromZ);
        
        // Set initial motion perpendicular to where we're going to add some cross
        Vec3d tilt = direction.rotateYaw(90f * (this.rand.nextBoolean() ? 1 : -1)).scale(2);
        this.getMotion().x = tilt.x;
        this.getMotion().y = tilt.y;
        this.getMotion().z = tilt.z;
        
        // Raytrace at hit point, or just go max distance.
        // If piercing, only cap to raytrace if we hit an entity
        
        RayTraceResult trace = RayTrace.raytrace(world, this.getPositionVector(), direction, (float) maxDistance, new RayTrace.OtherLiving(shootingEntity));
        if (trace != null && trace.typeOfHit != RayTraceResult.Type.MISS) {
        	if (trace.typeOfHit == RayTraceResult.Type.ENTITY) {
        		this.target = trace.hitVec.add(0D, trace.entityHit.getHeight() / 2.0, 0D);
        	} else if (!piercing) { // !piercing cause piercing just wants max dist if no entity being looked at
        		this.target = trace.hitVec;
        	}
        }
        
        if (target == null){
        	this.target = origin.add(direction.scale(maxDistance));
        }
        
        this.piercing = piercing;
	}

	public EntityChakramSpellSaucer(EntityType<? extends EntityChakramSpellSaucer> type,
			MagicCutterTriggerInstance trigger,
			LivingEntity shooter, float speedFactor, double maxDistance, boolean piercing, int maxTrips) {
		this(type,
				trigger,
				shooter,
				shooter.world,
				shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ,
				shooter.getLookVec(),
				speedFactor, maxDistance, piercing, maxTrips
				);
	}
	
	private Vector _getInstantVelocityVec;
	
	// Get the velocity the entity should have given the current time
	private Vector getInstantVelocity() {
		if (this._getInstantVelocityVec == null) {
			this._getInstantVelocityVec = new Vector();
		}
		
		Vec3d to;
		
		if (this.returning) {
			to = origin;
		} else {
			to = target;
		}
		
		final double moveScale = 0.15d * this.speed;
		Vec3d diff = to.subtract(this.getPositionVector()).normalize().scale(moveScale);
		this._getInstantVelocityVec.set(diff);
		
		return this._getInstantVelocityVec;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (!world.isRemote) {
			
			if (origin == null) {
				// We got loaded...
				this.remove();
				return;
			}
			
			Vector accel = this.getInstantVelocity();
	        
	        // Add accel to motionX for raytracing
	        this.getMotion().x += accel.x;
	        this.getMotion().y += accel.y;
	        this.getMotion().z += accel.z;
	        
			RayTraceResult raytraceresult = ProjectileHelper.forwardsRaycast(this, true, this.ticksInAir >= 25, this.shootingEntity);
			
			// Also calc pitch and yaw
			float f = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
            this.rotationPitch = (float)(MathHelper.atan2(motionY, (double)f) * (180D / Math.PI));
            this.rotationYaw = (float)(MathHelper.atan2(motionX, motionZ) * (180D / Math.PI));
			
			this.getMotion().x -= accel.x;
	        this.getMotion().y -= accel.y;
	        this.getMotion().z -= accel.z;

	        if (raytraceresult != null)
	        {
	            this.onImpact(raytraceresult);
	        }
	        
	        this.posX += this.getMotion().x;
	        this.posY += this.getMotion().y;
	        this.posZ += this.getMotion().z;
	        this.posX += accel.x;
	        this.posY += accel.y;
	        this.posZ += accel.z;
	        
	        // Apply air-friction, making motion's sort-of our initial motion
	        this.getMotion().x *= 0.8;
	        this.getMotion().y *= 0.8;
	        this.getMotion().z *= 0.8;
			
//				// Can't avoid a SQR; tracking motion would require SQR, too to get path length
//				if (this.getPositionVector().squareDistanceTo(origin) > maxDistance) {
//					trigger.onFizzle(this.getPosition());
//					this.remove();
//				}
			
			this.setPosition(this.posX, this.posY, this.posZ);
			
			// Check for motion boundaries
			if (this.returning) {
				if (Math.abs(this.getPositionVector().distanceTo(this.origin)) <= 0.5) {
					if (++trips >= maxTrips) {
						this.remove();
					} else {
						returning = false;
						// Capture motion to get boomerang-effect
						Vec3d motion = new Vec3d(accel.x, accel.y, accel.z).normalize().scale(1);
						motion = motion.rotateYaw(30f * (this.rand.nextBoolean() ? 1 : -1));
						this.getMotion().x += motion.x;
						this.getMotion().y += motion.y;
						this.getMotion().z += motion.z;
					}
				}
			} else {
				if (Math.abs(this.getPositionVector().distanceTo(this.target)) <= 0.5) {
					this.returning = true;
					
					// Capture motion to get boomerang-effect
					Vec3d motion = new Vec3d(accel.x, accel.y, accel.z).normalize().scale(1);
					motion = motion.rotateYaw(30f * (this.rand.nextBoolean() ? 1 : -1));
					this.getMotion().x += motion.x;
					this.getMotion().y += motion.y;
					this.getMotion().z += motion.z;
				}
			}
		}
	}
	
	@Override
	public boolean canImpact(BlockPos pos) {
		return this.piercing;
	}
	
	@Override
	public boolean dieOnImpact(BlockPos pos) {
		return false; // !this.piercing; used to only be with piercing
	}
	
	@Override
	public boolean dieOnImpact(LivingEntity entity) {
		return !this.piercing;
	}

	@Override
	protected void shoot(double xStart, double yStart, double zStart, double xTo, double yTo, double zTo, float velocity, float inaccuracy) {
		super.shoot(xStart, yStart, zStart, xTo, yTo, zTo, velocity, inaccuracy);
		this.origin = new Vec3d(xStart, yStart, zStart);
		this.target = new Vec3d(xTo, yTo, zTo);
	}


}
