package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger.MagicCutterTriggerInstance;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class EntityChakramSpellSaucer extends EntitySpellSaucer {
	
	public static final String ID = "entity_internal_spellsaucer_chakram";
	
	// Chakram:
	private Vector3d origin;
	private Vector3d target;
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
			double fromX, double fromY, double fromZ, Vector3d direction,
			float speedFactor, double maxDistance, boolean piercing, int maxTrips) {
		this(type, world, shooter, trigger, speedFactor);
		
		this.origin = new Vector3d(fromX, fromY, fromZ);
		direction = direction.normalize();
		
		this.setLocationAndAngles(fromX, fromY, fromZ, this.rotationYaw, this.rotationPitch);
        this.setPosition(fromX, fromY, fromZ);
        
        // Set initial motion perpendicular to where we're going to add some cross
        Vector3d tilt = direction.rotateYaw(90f * (this.rand.nextBoolean() ? 1 : -1)).scale(2);
        this.setMotion(tilt.x, tilt.y, tilt.z);
        
        // Raytrace at hit point, or just go max distance.
        // If piercing, only cap to raytrace if we hit an entity
        
        RayTraceResult trace = RayTrace.raytrace(world, this, this.getPositionVec(), direction, (float) maxDistance, new RayTrace.OtherLiving(shootingEntity));
        if (trace != null && trace.getType() != RayTraceResult.Type.MISS) {
        	if (trace.getType() == RayTraceResult.Type.ENTITY) {
        		Entity entityHit = ((EntityRayTraceResult) trace).getEntity();
        		this.target = entityHit.getPositionVec().add(0D, entityHit.getHeight() / 2.0, 0D);
        	} else if (!piercing) { // !piercing cause piercing just wants max dist if no entity being looked at
        		this.target = trace.getHitVec();
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
				shooter.getPosX(), shooter.getPosY() + shooter.getEyeHeight(), shooter.getPosZ(),
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
		
		Vector3d to;
		
		if (this.returning) {
			to = origin;
		} else {
			to = target;
		}
		
		final double moveScale = 0.15d * this.speed;
		Vector3d diff = to.subtract(this.getPositionVec()).normalize().scale(moveScale);
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
			this.setMotion(this.getMotion().add(accel.x, accel.y, accel.z));
	        
			RayTraceResult raytraceresult = this.ticksInAir >= 25
						? ProjectileHelper.func_234618_a_(this, (ent) -> ent != this.shootingEntity)
						: null;
			
			// Also calc pitch and yaw
			final Vector3d prevMotion = this.getMotion();
			float f = MathHelper.sqrt(prevMotion.x * prevMotion.x + prevMotion.z * prevMotion.z);
            this.rotationPitch = (float)(MathHelper.atan2(prevMotion.y, (double)f) * (180D / Math.PI));
            this.rotationYaw = (float)(MathHelper.atan2(prevMotion.x, prevMotion.z) * (180D / Math.PI));
			
            this.setMotion(this.getMotion().add(-accel.x, -accel.y, -accel.z));

	        if (raytraceresult != null)
	        {
	            this.onImpact(raytraceresult);
	        }
	        
	        {
	        	final Vector3d motion = this.getMotion();
	        	this.setPosition(getPosX() + motion.x + accel.x, getPosY() + motion.y + accel.y, getPosZ() + motion.z + accel.z);
	        }
	        
	        // Apply air-friction, making motion's sort-of our initial motion
	        this.setMotion(this.getMotion().scale(0.8));
			
//				// Can't avoid a SQR; tracking motion would require SQR, too to get path length
//				if (this.getPositionVec().squareDistanceTo(origin) > maxDistance) {
//					trigger.onFizzle(this.getPosition());
//					this.remove();
//				}
			
			// Check for motion boundaries
			if (this.returning) {
				if (Math.abs(this.getPositionVec().distanceTo(this.origin)) <= 0.5) {
					if (++trips >= maxTrips) {
						this.remove();
					} else {
						returning = false;
						// Capture motion to get boomerang-effect
						Vector3d motion = new Vector3d(accel.x, accel.y, accel.z).normalize().scale(1);
						motion = motion.rotateYaw(30f * (this.rand.nextBoolean() ? 1 : -1));
						this.setMotion(this.getMotion().add(motion));
					}
				}
			} else {
				if (Math.abs(this.getPositionVec().distanceTo(this.target)) <= 0.5) {
					this.returning = true;
					
					// Capture motion to get boomerang-effect
					Vector3d motion = new Vector3d(accel.x, accel.y, accel.z).normalize().scale(1);
					motion = motion.rotateYaw(30f * (this.rand.nextBoolean() ? 1 : -1));
					this.setMotion(this.getMotion().add(motion));
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
		this.origin = new Vector3d(xStart, yStart, zStart);
		this.target = new Vector3d(xTo, yTo, zTo);
	}


}
