package com.smanzana.nostrummagica.entity;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.smanzana.nostrummagica.spell.component.shapes.MagicCyclerShape.MagicCyclerShapeInstance;
import com.smanzana.nostrummagica.util.Entities;

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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class EntityCyclerSpellSaucer extends EntitySpellSaucer {
	
	public static final String ID = "entity_internal_spellsaucer_cycler";
	protected static final AxisAlignedBB _BoundingBox = new AxisAlignedBB(-.5, -.1, -.5, .5, .1, .5);
	public static final double CYCLER_RADIUS = 1;
	
	protected static final DataParameter<Optional<UUID>> SHOOTER = EntityDataManager.<Optional<UUID>>createKey(EntityCyclerSpellSaucer.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	
	// Cycler:
	private final int duration;
	private final boolean onBlocks;
	private final boolean dieOnImpact;
	
	public EntityCyclerSpellSaucer(EntityType<? extends EntityCyclerSpellSaucer> type, World world) {
		super(type, world);
		this.duration = 5;
		this.onBlocks = false;
		this.dieOnImpact = false;
        this.setNoGravity(true);
        this.setMotion(0, 0, 0);
        this.accelerationX = this.accelerationY = this.accelerationZ = 0;
	}
	
	protected EntityCyclerSpellSaucer(EntityType<? extends EntityCyclerSpellSaucer> type, MagicCyclerShapeInstance trigger, World world, LivingEntity shooter, float speed,
			int duration, boolean onBlocks, boolean dieOnImpact) {
		super(type, trigger, world, shooter, speed, 1000, 20);
        this.duration = duration; // Long neough to flash so I know things are going on
        this.onBlocks = onBlocks;
        this.dieOnImpact = dieOnImpact;
        this.setNoGravity(true);
        this.setMotion(0, 0, 0);
        this.accelerationX = this.accelerationY = this.accelerationZ = 0;
        
        this.setLocationAndAngles(shooter.getPosX(), shooter.getPosY(), shooter.getPosZ(), 0, 0);
        this.setPosition(shooter.getPosX(), shooter.getPosY(), shooter.getPosZ());
        
        // Set up shooter as data parameter to communicate to client
        this.dataManager.set(SHOOTER, Optional.ofNullable(shooter.getUniqueID()));
	}
	
	public EntityCyclerSpellSaucer(World world, LivingEntity shooter, MagicCyclerShapeInstance trigger, float speed,
			int duration, boolean onBlocks, boolean dieOnImpact) {
		this(NostrumEntityTypes.cyclerSpellSaucer, trigger, world, shooter, speed, duration, onBlocks, dieOnImpact);
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(SHOOTER, Optional.<UUID>empty());
	}
	
	
	protected Vector3d getTargetOffsetLoc(float partialTicks) {
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
		
		final double x;
		final double y;
		final double z;
		if (this.shootingEntity != null) {
			// Center vertically on the entity
			y = (this.shootingEntity.getEyeHeight() / 2f);
			final int ticksAround = 40;
			float progress = (((float) (this.ticksExisted % ticksAround)) + partialTicks) / (float) ticksAround;
			double radians = progress * 2D * Math.PI;
			
			final double rotateDist = CYCLER_RADIUS; 
			x = Math.cos(radians) * rotateDist;
			z = Math.sin(radians) * rotateDist;
		} else {
			x = y = z = 0;
		}
		
		return new Vector3d(x, y, z);
	}
	
	public Vector3d getTargetLoc(float partialTicks) {
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
			return this.shootingEntity.getPositionVec().add(this.getTargetOffsetLoc(partialTicks));
		}
		
		return this.getPositionVec();
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (!world.isRemote) {
			
			if (this.shootingEntity == null || this.ticksExisted >= duration) {
				// Expired, or got loaded!
				this.remove();
				return;
			}
			
			Vector3d pos = this.getTargetLoc(0f);
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
				// Only trigger on non-air
				BlockPos blockPos = new BlockPos(getPosX(), getPosY(), getPosZ()); // not using getPosition() since it adds .5 y 
				RayTraceResult bundledResult = new BlockRayTraceResult(
							this.getPositionVec(), Direction.UP, blockPos, false);
					
				this.onImpact(bundledResult);
			}
		}
	}
	
	@Override
	public boolean canImpact(BlockPos pos) {
		return onBlocks && !this.world.isAirBlock(pos) && this.world.getBlockState(pos).isOpaqueCube(world, pos) && super.canImpact(pos);
	}
	
	@Override
	public boolean canImpact(Entity entity) {
		return super.canImpact(entity);
	}
	
	public AxisAlignedBB getCollisionBoundingBox() {
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
