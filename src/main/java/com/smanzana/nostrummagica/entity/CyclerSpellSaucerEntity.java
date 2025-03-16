package com.smanzana.nostrummagica.entity;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.smanzana.nostrummagica.spell.component.shapes.MagicCyclerShape.MagicCyclerShapeInstance;
import com.smanzana.nostrummagica.util.Entities;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class CyclerSpellSaucerEntity extends SpellSaucerEntity {
	
	public static final String ID = "entity_internal_spellsaucer_cycler";
	protected static final AABB _BoundingBox = new AABB(-.5, -.1, -.5, .5, .1, .5);
	public static final double CYCLER_RADIUS = 1;
	
	protected static final EntityDataAccessor<Optional<UUID>> SHOOTER = SynchedEntityData.<Optional<UUID>>defineId(CyclerSpellSaucerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	
	// Cycler:
	private final int duration;
	private final boolean onBlocks;
	private final boolean dieOnImpact;
	
	public CyclerSpellSaucerEntity(EntityType<? extends CyclerSpellSaucerEntity> type, Level world) {
		super(type, world);
		this.duration = 5;
		this.onBlocks = false;
		this.dieOnImpact = false;
        this.setNoGravity(true);
        this.setDeltaMovement(0, 0, 0);
        this.xPower = this.yPower = this.zPower = 0;
	}
	
	protected CyclerSpellSaucerEntity(EntityType<? extends CyclerSpellSaucerEntity> type, MagicCyclerShapeInstance trigger, Level world, LivingEntity shooter, float speed,
			int duration, boolean onBlocks, boolean dieOnImpact) {
		super(type, trigger, world, shooter, speed, 1000, 20);
        this.duration = duration; // Long neough to flash so I know things are going on
        this.onBlocks = onBlocks;
        this.dieOnImpact = dieOnImpact;
        this.setNoGravity(true);
        this.setDeltaMovement(0, 0, 0);
        this.xPower = this.yPower = this.zPower = 0;
        
        this.moveTo(shooter.getX(), shooter.getY(), shooter.getZ(), 0, 0);
        this.setPos(shooter.getX(), shooter.getY(), shooter.getZ());
        
        // Set up shooter as data parameter to communicate to client
        this.entityData.set(SHOOTER, Optional.ofNullable(shooter.getUUID()));
	}
	
	public CyclerSpellSaucerEntity(Level world, LivingEntity shooter, MagicCyclerShapeInstance trigger, float speed,
			int duration, boolean onBlocks, boolean dieOnImpact) {
		this(NostrumEntityTypes.cyclerSpellSaucer, trigger, world, shooter, speed, duration, onBlocks, dieOnImpact);
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(SHOOTER, Optional.<UUID>empty());
	}
	
	
	protected Vec3 getTargetOffsetLoc(float partialTicks) {
		// Get shooter position
		if (this.shootingEntity == null) {
			// Try and do a fixup
			UUID shooterID = this.entityData.get(SHOOTER).orElse(null);
			if (shooterID != null) {
				Entity entity = Entities.FindEntity(level, shooterID);
				
				if (entity != null) {
					this.shootingEntity = (LivingEntity) entity;
				}
			}
		}
		
		final double x;
		final double y;
		final double z;
		if (this.shootingEntity != null) {
			// Center vertically on the entity
			y = (this.shootingEntity.getEyeHeight() / 2f);
			final int ticksAround = 40;
			float progress = (((float) (this.tickCount % ticksAround)) + partialTicks) / (float) ticksAround;
			double radians = progress * 2D * Math.PI;
			
			final double rotateDist = CYCLER_RADIUS; 
			x = Math.cos(radians) * rotateDist;
			z = Math.sin(radians) * rotateDist;
		} else {
			x = y = z = 0;
		}
		
		return new Vec3(x, y, z);
	}
	
	public Vec3 getTargetLoc(float partialTicks) {
		// Get shooter position
		if (this.shootingEntity == null) {
			// Try and do a fixup
			UUID shooterID = this.entityData.get(SHOOTER).orElse(null);
			if (shooterID != null) {
				Entity entity = Entities.FindEntity(level, shooterID);
				
				if (entity != null) {
					this.shootingEntity = (LivingEntity) entity;
				}
			}
		}
		
		if (this.shootingEntity != null) {
			return this.shootingEntity.position().add(this.getTargetOffsetLoc(partialTicks));
		}
		
		return this.position();
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (!level.isClientSide) {
			
			if (this.shootingEntity == null || this.tickCount >= duration) {
				// Expired, or got loaded!
				this.remove();
				return;
			}
			
			Vec3 pos = this.getTargetLoc(0f);
			this.setPos(pos.x, pos.y, pos.z);
			
//			Vector accel = this.getInstantVelocity();
//	        
//	        // Add accel to motionX for raytracing
//	        this.getMotion().x += accel.x;
//	        this.getMotion().y += accel.y;
//	        this.getMotion().z += accel.z;
			
			List<Entity> collidedEnts = level.getEntities(this, this.getBoundingBox(), (ent) -> {
				return ent instanceof LivingEntity;
			});
			if (!collidedEnts.isEmpty()) {
				Entity ent = null;
				
				for (Entity e : collidedEnts) {
					if (e == this.shootingEntity) {
						continue;
					}
					
					if (!e.isAlive() || e.noPhysics || !e.isPickable()) {
						continue;
					}
					
					ent = e;
					break;
				}
				
				if (ent != null) {
					HitResult bundledResult = new EntityHitResult(collidedEnts.get(0));
					this.onHit(bundledResult);
				}
			}
			
			// Also check for blocks, if we contact blocks
			if (this.onBlocks) {
				// Only trigger on non-air
				BlockPos blockPos = new BlockPos(getX(), getY(), getZ()); // not using getPosition() since it adds .5 y 
				HitResult bundledResult = new BlockHitResult(
							this.position(), Direction.UP, blockPos, false);
					
				this.onHit(bundledResult);
			}
		}
	}
	
	@Override
	public boolean canImpact(BlockPos pos) {
		return onBlocks && !this.level.isEmptyBlock(pos) && this.level.getBlockState(pos).isSolidRender(level, pos) && super.canImpact(pos);
	}
	
	@Override
	public boolean canImpact(Entity entity) {
		return super.canImpact(entity);
	}
	
	public AABB getCollisionBoundingBox() {
		return _BoundingBox;
	}
	
	@Override
	public boolean dieOnImpact(BlockPos pos) {
		return this.onBlocks && this.dieOnImpact;
	}
	
	@Override
	public boolean dieOnImpact(Entity ent) {
		return this.dieOnImpact;
	}
	
}
