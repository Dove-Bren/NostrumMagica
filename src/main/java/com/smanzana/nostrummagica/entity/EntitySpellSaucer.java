package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger.MagicCutterTriggerInstance;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntitySpellSaucer extends Entity implements IProjectile {
	
	private EntityLivingBase shootingEntity;
	private MagicCutterTriggerInstance trigger;
	private Vec3d origin;
	private Vec3d target;
	private float speed;
	
	private int ticksInAir;
	private boolean returning;
	
	public EntitySpellSaucer(World world) {
		super(world);
        this.setSize(1F, .2F);
        this.returning = false;
	}
	
	public EntitySpellSaucer(MagicCutterTriggerInstance trigger, EntityLivingBase shooter,
			World world,
			double fromX, double fromY, double fromZ, Vec3d direction,
			float speedFactor, double maxDistance) {
		this(world);
		this.shootingEntity = shooter;
		
		this.trigger = trigger;
		this.origin = new Vec3d(fromX, fromY, fromZ);
		direction = direction.normalize();
		this.speed = speedFactor;
		
		this.setLocationAndAngles(fromX, fromY, fromZ, this.rotationYaw, this.rotationPitch);
        this.setPosition(fromX, fromY, fromZ);
        
        // Set initial motion perpendicular to where we're going to add some cross
        Vec3d tilt = direction.rotateYaw(90f * (this.rand.nextBoolean() ? 1 : -1)).scale(2);
        this.motionX = tilt.xCoord;
        this.motionY = tilt.yCoord;
        this.motionZ = tilt.zCoord;
        
        // Raytrace at hit point, or just go max distance
        
        RayTraceResult trace = RayTrace.raytrace(worldObj, this.getPositionVector(), direction, (float) maxDistance, new RayTrace.OtherLiving(shootingEntity));
        if (trace != null && trace.typeOfHit != RayTraceResult.Type.MISS) {
        	if (trace.typeOfHit == RayTraceResult.Type.ENTITY) {
        		this.target = trace.hitVec.addVector(0D, trace.entityHit.height / 2.0, 0D);
        	} else {
        		this.target = trace.hitVec;
        	}
        } else {
        	this.target = origin.add(direction.scale(maxDistance));
        }
	}

	public EntitySpellSaucer(MagicCutterTriggerInstance trigger,
			EntityLivingBase shooter, float speedFactor, double maxDistance) {
		this(trigger,
				shooter,
				shooter.worldObj,
				shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ,
				shooter.getLookVec(),
				speedFactor, maxDistance
				);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance <= 64 * 64 * 64;
	}
	
//	private Vec3d scaleVelocity(Vec3d direction, double scale) {
//		Vec3d base = direction.normalize();
//		final double tickScale = .05;
//		
//		return new Vec3d(base.xCoord * scale * tickScale, base.yCoord * scale * tickScale, base.zCoord * scale * tickScale);
//	}
	
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
	public void onUpdate() {
		super.onUpdate();
		ticksInAir++;
		ticksExisted++;
		
		//System.out.println(this);
		
		if (this.ticksExisted % 5 == 0) {
			this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
					posX, posY, posZ, 0, 0, 0);
		}
		
		if (!worldObj.isRemote) {
			
			if (origin == null) {
				// We got loaded...
				this.setDead();
				return;
			}
			
			Vector accel = this.getInstantVelocity();
	        
	        // Add accel to motionX for raytracing
	        this.motionX += accel.x;
	        this.motionY += accel.y;
	        this.motionZ += accel.z;
	        
			RayTraceResult raytraceresult = ProjectileHelper.forwardsRaycast(this, true, this.ticksInAir >= 25, this.shootingEntity);
			
			// Also calc pitch and yaw
			float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
            this.rotationPitch = (float)(MathHelper.atan2(motionY, (double)f) * (180D / Math.PI));
            this.rotationYaw = (float)(MathHelper.atan2(motionX, motionZ) * (180D / Math.PI));
			
			this.motionX -= accel.x;
	        this.motionY -= accel.y;
	        this.motionZ -= accel.z;

	        if (raytraceresult != null)
	        {
	            this.onImpact(raytraceresult);
	        }
	        
	        this.posX += this.motionX;
	        this.posY += this.motionY;
	        this.posZ += this.motionZ;
	        this.posX += accel.x;
	        this.posY += accel.y;
	        this.posZ += accel.z;
	        
	        // Apply air-friction, making motion's sort-of our initial motion
	        this.motionX *= 0.8;
	        this.motionY *= 0.8;
	        this.motionZ *= 0.8;
			
//				// Can't avoid a SQR; tracking motion would require SQR, too to get path length
//				if (this.getPositionVector().squareDistanceTo(origin) > maxDistance) {
//					trigger.onFizzle(this.getPosition());
//					this.setDead();
//				}
			
			this.setPosition(this.posX, this.posY, this.posZ);
			
			// Check for motion boundaries
			if (this.returning) {
				if (Math.abs(this.getPositionVector().distanceTo(this.origin)) <= 0.5) {
					this.setDead();
				}
			} else {
				if (Math.abs(this.getPositionVector().distanceTo(this.target)) <= 0.5) {
					this.returning = true;
					
					// Capture motion to get boomerang-effect
					Vec3d motion = new Vec3d(accel.x, accel.y, accel.z).normalize().scale(2);
					this.motionX += motion.xCoord;
					this.motionY += motion.yCoord;
					this.motionZ += motion.zCoord;
				}
			}
		}
	}

	protected void onImpact(RayTraceResult result) {
		if (worldObj.isRemote)
			return;
		
		if (result.typeOfHit == RayTraceResult.Type.MISS) {
			; // Do nothing
		} else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			trigger.onProjectileHit(new BlockPos(result.hitVec));
			this.setDead();
		} else if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
			if (result.entityHit instanceof EntitySpellSaucer) {
				
			} else if (result.entityHit != shootingEntity && !shootingEntity.isRidingOrBeingRiddenBy(result.entityHit)) {
				trigger.onProjectileHit(result.entityHit);
				this.setDead();
			}
		}
	}
	
	private void setThrowableHeadingFrom(double xStart, double yStart, double zStart, double xTo, double yTo, double zTo, float velocity, float inaccuracy) {
		this.origin = new Vec3d(xStart, yStart, zStart);
		this.target = new Vec3d(xTo, yTo, zTo);
		this.ticksExisted = 0;
	}

	@Override
	public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {
		this.setThrowableHeadingFrom(posX, posY, posZ, x, y, z, velocity, inaccuracy);
	}

	@Override
	protected void entityInit() {
		
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.ticksExisted = compound.getInteger("existed");
		this.posX = compound.getDouble("posX");
		this.posY = compound.getDouble("posY");
		this.posZ = compound.getDouble("posZ");
		this.origin = new Vec3d(
				compound.getDouble("originX"),
				compound.getDouble("originY"),
				compound.getDouble("originZ"));
		this.target = new Vec3d(
				compound.getDouble("targetX"),
				compound.getDouble("targetY"),
				compound.getDouble("targetZ"));
		this.speed = compound.getFloat("speed");
	}
	
	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {
		return false; // This makes us not save and persist!!
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("existed", this.ticksExisted);
		compound.setDouble("posX", this.posX);
		compound.setDouble("posY", this.posY);
		compound.setDouble("posZ", this.posZ);
		compound.setDouble("originX", this.origin.xCoord);
		compound.setDouble("originY", this.origin.yCoord);
		compound.setDouble("originZ", this.origin.zCoord);
		compound.setDouble("targetX", this.target.xCoord);
		compound.setDouble("targetY", this.target.yCoord);
		compound.setDouble("targetZ", this.target.zCoord);
		compound.setFloat("speed", this.speed);
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return true;
	}
	
	@Override
	public float getCollisionBorderSize() {
		return 1f;
	}
	
	private static final class Vector {
		public double x;
		public double y;
		public double z;
		
		public Vector() {
			
		}
		
		public void set(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public void set(Vec3d vec) {
			this.set(vec.xCoord, vec.yCoord, vec.zCoord);
		}
	}
}
