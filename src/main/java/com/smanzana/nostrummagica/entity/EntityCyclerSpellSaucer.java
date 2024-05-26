package com.smanzana.nostrummagica.entity;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.smanzana.nostrummagica.spells.components.shapes.MagicCyclerShape.MagicCyclerShapeInstance;
import com.smanzana.nostrummagica.utils.Entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityCyclerSpellSaucer extends EntitySpellSaucer {
	
	public static final String ID = "entity_internal_spellsaucer_cycler";
	
	protected static final DataParameter<Optional<UUID>> SHOOTER = EntityDataManager.<Optional<UUID>>createKey(EntityCyclerSpellSaucer.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	
	// Cycler:
	private int duration;
	private boolean onBlocks;
	
	public EntityCyclerSpellSaucer(EntityType<? extends EntityCyclerSpellSaucer> type, World world) {
		super(type, world);
	}
	
	public EntityCyclerSpellSaucer(EntityType<? extends EntityCyclerSpellSaucer> type, World world, LivingEntity shooter, MagicCyclerShapeInstance trigger, float speed) {
		super(type, world, shooter, trigger, speed);
        this.duration = 10; // Long neough to flash so I know things are going on
        this.onBlocks = true;
        
        // Set up shooter as data parameter to communicate to client
        this.dataManager.set(SHOOTER, Optional.ofNullable(shooter.getUniqueID()));
	}
	
	public EntityCyclerSpellSaucer(EntityType<? extends EntityCyclerSpellSaucer> type, 
			MagicCyclerShapeInstance trigger, LivingEntity shooter,
			World world,
			double fromX, double fromY, double fromZ,
			float speedFactor, int durationTicks, boolean onBlocks) {
		this(type, world, shooter, trigger, speedFactor);
		
		this.setLocationAndAngles(fromX, fromY, fromZ, this.rotationYaw, this.rotationPitch);
        this.setPosition(fromX, fromY, fromZ);
        
        duration = durationTicks;
        this.onBlocks = onBlocks;
	}

	public EntityCyclerSpellSaucer(EntityType<? extends EntityCyclerSpellSaucer> type, 
			MagicCyclerShapeInstance trigger,
			LivingEntity shooter, float speedFactor, int durationTicks, boolean onBlocks) {
		this(type,
				trigger,
				shooter,
				shooter.world,
				shooter.getPosX(), shooter.getPosY() + shooter.getEyeHeight(), shooter.getPosZ(),
				speedFactor, durationTicks, onBlocks
				);
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(SHOOTER, Optional.<UUID>empty());
	}
	
	private Vector _getInstantVelocityVec;
	
	public Vector getTargetOffsetLoc(float partialTicks) {
		if (this._getInstantVelocityVec == null) {
			this._getInstantVelocityVec = new Vector();
		}
		
		// Get shooter position
		if (this.shootingEntity == null) {
			// Try and do a fixup
			UUID shooterID = this.dataManager.get(SHOOTER).orElse(null);
			if (shooterID != null) {
				Entity entity = Entities.FindEntity(world, shooterID);
				
				if (entity != null) {
					this.shootingEntity = (LivingEntity) entity;
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
			UUID shooterID = this.dataManager.get(SHOOTER).orElse(null);
			if (shooterID != null) {
				Entity entity = Entities.FindEntity(world, shooterID);
				
				if (entity != null) {
					this.shootingEntity = (LivingEntity) entity;
				}
			}
		}
		
		
		if (this.shootingEntity != null) {
			// Calc where around the entity we want to be
			_getInstantVelocityVec.set(this.shootingEntity.getPositionVec());
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
	public void tick() {
		super.tick();
		
		if (!world.isRemote) {
			
			if (this.shootingEntity == null || this.ticksExisted >= duration) {
				// Expired, or got loaded!
				this.remove();
				return;
			}
			
			Vector pos = this.getTargetLoc(0f);
			this.setPosition(pos.x, pos.y, pos.z);
			
//			Vector accel = this.getInstantVelocity();
//	        
//	        // Add accel to motionX for raytracing
//	        this.getMotion().x += accel.x;
//	        this.getMotion().y += accel.y;
//	        this.getMotion().z += accel.z;
			
			List<Entity> collidedEnts = world.getEntitiesInAABBexcluding(this, this.getBoundingBox(), (ent) -> {
				return ent instanceof LivingEntity;
			});
			if (!collidedEnts.isEmpty()) {
				Entity ent = null;
				
				for (Entity e : collidedEnts) {
					if (e == this.shootingEntity) {
						continue;
					}
					
					if (!e.isAlive() || e.noClip || !e.canBeCollidedWith()) {
						continue;
					}
					
					ent = e;
					break;
				}
				
				if (ent != null) {
					RayTraceResult bundledResult = new EntityRayTraceResult(collidedEnts.get(0));
					this.onImpact(bundledResult);
				}
			}
			
			// Also check for blocks, if we contact blocks
			if (this.onBlocks) {
				if (_lastBlockVector == null ||
						((int)_lastBlockVector.x == (int) this.getPosX()
						&& (int) _lastBlockVector.y == (int) this.getPosY()
						&& (int) _lastBlockVector.z == (int) this.getPosZ())) {
					
					// Only trigger on non-air
					BlockPos blockPos = new BlockPos(getPosX(), getPosY(), getPosZ()); // not using getPosition() since it adds .5 y 
					if (this.canImpact(blockPos)) {
						RayTraceResult bundledResult = new BlockRayTraceResult(
								this.getPositionVec(), Direction.UP, blockPos, false);
						
						if (_lastBlockVector == null) {
							_lastBlockVector = new Vector();
						}
						_lastBlockVector.set(getPosX(), getPosY(), getPosZ());
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
//			this.getMotion().x -= accel.x;
//	        this.getMotion().y -= accel.y;
//	        this.getMotion().z -= accel.z;

//	        if (raytraceresult != null)
//	        {
//	            this.onImpact(raytraceresult);
//	        }
	        
//	        this.getPosX() += this.getMotion().x;
//	        this.getPosY() += this.getMotion().y;
//	        this.getPosZ() += this.getMotion().z;
//	        this.getPosX() += accel.x;
//	        this.getPosY() += accel.y;
//	        this.getPosZ() += accel.z;
//	        
//	        // Apply air-friction, making motion's sort-of our initial motion
//	        this.getMotion().x *= 0.8;
//	        this.getMotion().y *= 0.8;
//	        this.getMotion().z *= 0.8;
//			
////				// Can't avoid a SQR; tracking motion would require SQR, too to get path length
////				if (this.getPositionVec().squareDistanceTo(origin) > maxDistance) {
////					trigger.onFizzle(this.getPosition());
////					this.remove();
////				}
//			
//			this.setPosition(this.getPosX(), this.getPosY(), this.getPosZ());
		}
	}
	
	@Override
	public boolean canImpact(BlockPos pos) {
		return onBlocks && !this.world.isAirBlock(pos) && this.world.getBlockState(pos).isOpaqueCube(world, pos);
	}
	
	static final AxisAlignedBB _BoundingBox = new AxisAlignedBB(-.5, -.1, -.5, .5, .1, .5);
	
	public AxisAlignedBB getCollisionBoundingBox() {
		return _BoundingBox;
	}
	
}
