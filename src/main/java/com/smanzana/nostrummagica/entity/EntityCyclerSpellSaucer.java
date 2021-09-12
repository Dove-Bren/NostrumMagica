package com.smanzana.nostrummagica.entity;


import java.util.List;
import java.util.UUID;

import com.google.common.base.Optional;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCyclerTrigger.MagicCyclerTriggerInstance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityCyclerSpellSaucer extends EntitySpellSaucer {
	
	protected static final DataParameter<Optional<UUID>> SHOOTER = EntityDataManager.<Optional<UUID>>createKey(EntityCyclerSpellSaucer.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	
	// Cycler:
	private int duration;
	private boolean onBlocks;
	
	public EntityCyclerSpellSaucer(World world) {
		super(world);
	}
	
	public EntityCyclerSpellSaucer(World world, EntityLivingBase shooter, MagicCyclerTriggerInstance trigger, float speed) {
		super(world, shooter, trigger, speed);
        this.duration = 10; // Long neough to flash so I know things are going on
        this.onBlocks = true;
        
        // Set up shooter as data parameter to communicate to client
        this.dataManager.set(SHOOTER, Optional.fromNullable(shooter.getUniqueID()));
	}
	
	public EntityCyclerSpellSaucer(MagicCyclerTriggerInstance trigger, EntityLivingBase shooter,
			World world,
			double fromX, double fromY, double fromZ,
			float speedFactor, int durationTicks, boolean onBlocks) {
		this(world, shooter, trigger, speedFactor);
		
		this.setLocationAndAngles(fromX, fromY, fromZ, this.rotationYaw, this.rotationPitch);
        this.setPosition(fromX, fromY, fromZ);
        
        duration = durationTicks;
        this.onBlocks = onBlocks;
	}

	public EntityCyclerSpellSaucer(MagicCyclerTriggerInstance trigger,
			EntityLivingBase shooter, float speedFactor, int durationTicks, boolean onBlocks) {
		this(trigger,
				shooter,
				shooter.world,
				shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ,
				speedFactor, durationTicks, onBlocks
				);
	}
	
	@Override
	protected void entityInit() {
		this.dataManager.register(SHOOTER, Optional.<UUID>absent());
	}
	
	private Vector _getInstantVelocityVec;
	
	public Vector getTargetOffsetLoc(float partialTicks) {
		if (this._getInstantVelocityVec == null) {
			this._getInstantVelocityVec = new Vector();
		}
		
		// Get shooter position
		if (this.shootingEntity == null) {
			// Try and do a fixup
			UUID shooterID = this.dataManager.get(SHOOTER).orNull();
			if (shooterID != null) {
				Entity entity = world.loadedEntityList.stream()
						.filter((ent) -> { return ent.getUniqueID().equals(shooterID);})
						.findAny().orElse(null);
				
				if (entity != null) {
					this.shootingEntity = (EntityLivingBase) entity;
				}
			}
		}
		
		
		if (this.shootingEntity != null) {
			// Sub in 0's and treat shootingEntity as origin
			_getInstantVelocityVec.set(0, 0, 0);
			// Center vertically on the entity
			_getInstantVelocityVec.y += (this.shootingEntity.getEyeHeight() / 2f);
			final int ticksAround = 40;
			float progress = (((float) (this.ticksExisted % ticksAround)) + partialTicks) / (float) ticksAround;
			double radians = progress * 2D * Math.PI;
			
			final double rotateDist = 1D; 
			_getInstantVelocityVec.x += Math.cos(radians) * rotateDist;
			_getInstantVelocityVec.z += Math.sin(radians) * rotateDist;
		}
		
		return _getInstantVelocityVec;
	}
	
	public Vector getTargetLoc(float partialTicks) {
		if (this._getInstantVelocityVec == null) {
			this._getInstantVelocityVec = new Vector();
		}
		
		// Get shooter position
		if (this.shootingEntity == null) {
			// Try and do a fixup
			UUID shooterID = this.dataManager.get(SHOOTER).orNull();
			if (shooterID != null) {
				Entity entity = world.loadedEntityList.stream()
						.filter((ent) -> { return ent.getUniqueID().equals(shooterID);})
						.findAny().orElse(null);
				
				if (entity != null) {
					this.shootingEntity = (EntityLivingBase) entity;
				}
			}
		}
		
		
		if (this.shootingEntity != null) {
			// Calc where around the entity we want to be
			_getInstantVelocityVec.set(this.shootingEntity.getPositionVector());
			// Center vertically on the entity
			_getInstantVelocityVec.y += (this.shootingEntity.getEyeHeight() / 2f);
			final int ticksAround = 40;
			float progress = (((float) (this.ticksExisted % ticksAround)) + partialTicks) / (float) ticksAround;
			double radians = progress * 2D * Math.PI;
			
			final double rotateDist = 1D; 
			_getInstantVelocityVec.x += Math.cos(radians) * rotateDist;
			_getInstantVelocityVec.z += Math.sin(radians) * rotateDist;
		}
		
		return _getInstantVelocityVec;
	}
	
	private Vector _lastBlockVector;
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (!world.isRemote) {
			
			if (this.shootingEntity == null || this.ticksExisted >= duration) {
				// Expired, or got loaded!
				this.setDead();
				return;
			}
			
			Vector pos = this.getTargetLoc(0f);
			this.setPosition(pos.x, pos.y, pos.z);
			
//			Vector accel = this.getInstantVelocity();
//	        
//	        // Add accel to motionX for raytracing
//	        this.motionX += accel.x;
//	        this.motionY += accel.y;
//	        this.motionZ += accel.z;
			
			List<Entity> collidedEnts = world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), (ent) -> {
				return ent instanceof EntityLivingBase;
			});
			if (!collidedEnts.isEmpty()) {
				Entity ent = null;
				
				for (Entity e : collidedEnts) {
					if (e == this.shootingEntity) {
						continue;
					}
					
					if (e.isDead || e.noClip || !e.canBeCollidedWith()) {
						continue;
					}
					
					ent = e;
					break;
				}
				
				if (ent != null) {
					RayTraceResult bundledResult = new RayTraceResult(collidedEnts.get(0));
					this.onImpact(bundledResult);
				}
			}
			
			// Also check for blocks, if we contact blocks
			if (this.onBlocks) {
				if (_lastBlockVector == null ||
						((int)_lastBlockVector.x == (int) this.posX
						&& (int) _lastBlockVector.y == (int) this.posY
						&& (int) _lastBlockVector.z == (int) this.posZ)) {
					
					// Only trigger on non-air
					BlockPos blockPos = new BlockPos(posX, posY, posZ); // not using getPosition() since it adds .5 y 
					if (this.canImpact(blockPos)) {
						RayTraceResult bundledResult = new RayTraceResult(
								RayTraceResult.Type.BLOCK, this.getPositionVector(), EnumFacing.UP, blockPos);
						
						if (_lastBlockVector == null) {
							_lastBlockVector = new Vector();
						}
						_lastBlockVector.set(posX, posY, posZ);
						this.onImpact(bundledResult);
					}
				}
			}
	        
			//RayTraceResult raytraceresult = ProjectileHelper.forwardsRaycast(this, true, this.ticksInAir >= 25, this.shootingEntity);
			
//			// Also calc pitch and yaw
//			float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
//            this.rotationPitch = (float)(MathHelper.atan2(motionY, (double)f) * (180D / Math.PI));
//            this.rotationYaw = (float)(MathHelper.atan2(motionX, motionZ) * (180D / Math.PI));
//			
//			this.motionX -= accel.x;
//	        this.motionY -= accel.y;
//	        this.motionZ -= accel.z;

//	        if (raytraceresult != null)
//	        {
//	            this.onImpact(raytraceresult);
//	        }
	        
//	        this.posX += this.motionX;
//	        this.posY += this.motionY;
//	        this.posZ += this.motionZ;
//	        this.posX += accel.x;
//	        this.posY += accel.y;
//	        this.posZ += accel.z;
//	        
//	        // Apply air-friction, making motion's sort-of our initial motion
//	        this.motionX *= 0.8;
//	        this.motionY *= 0.8;
//	        this.motionZ *= 0.8;
//			
////				// Can't avoid a SQR; tracking motion would require SQR, too to get path length
////				if (this.getPositionVector().squareDistanceTo(origin) > maxDistance) {
////					trigger.onFizzle(this.getPosition());
////					this.setDead();
////				}
//			
//			this.setPosition(this.posX, this.posY, this.posZ);
		}
	}
	
	@Override
	public boolean canImpact(BlockPos pos) {
		return onBlocks && !this.world.isAirBlock(pos) && this.world.getBlockState(pos).isOpaqueCube();
	}
	
	static final AxisAlignedBB _BoundingBox = new AxisAlignedBB(-.5, -.1, -.5, .5, .1, .5);
	
	public AxisAlignedBB getCollisionBoundingBox() {
		return _BoundingBox;
	}
	
}
