package com.smanzana.nostrummagica.entity.boss.plantboss;

import java.util.List;

import com.smanzana.nostrummagica.effect.NostrumEffects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class PlantBossBrambleEntity extends Entity {
	
	public static final String ID = "entity_plant_boss.bramble";

	protected static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.<Float>defineId(PlantBossBrambleEntity.class, EntityDataSerializers.FLOAT);
	protected static final EntityDataAccessor<Float> DEPTH = SynchedEntityData.<Float>defineId(PlantBossBrambleEntity.class, EntityDataSerializers.FLOAT);
	protected static final EntityDataAccessor<Float> HEIGHT = SynchedEntityData.<Float>defineId(PlantBossBrambleEntity.class, EntityDataSerializers.FLOAT);
	protected static final EntityDataAccessor<Direction> FACING = SynchedEntityData.<Direction>defineId(PlantBossBrambleEntity.class, EntityDataSerializers.DIRECTION);
	
	protected PlantBossEntity plant;
	protected float distance;
	protected BlockPos startPos;
	
	protected AABB entityBBOverride;
	
	public PlantBossBrambleEntity(EntityType<PlantBossBrambleEntity> type, Level worldIn) {
		super(type, worldIn);
	}
	
	public PlantBossBrambleEntity(EntityType<PlantBossBrambleEntity> type, Level worldIn, PlantBossEntity plant, float length) {
		this(type, worldIn, plant, length, .5f, .75f);
	}
	
	public PlantBossBrambleEntity(EntityType<PlantBossBrambleEntity> type, Level worldIn, PlantBossEntity plant, float width, float depth, float height) {
		this(type, worldIn);
		this.plant = plant;
		this.setDims(width, depth, height);
	}
	
	@Override
	protected void defineSynchedData() {
		//super.registerData();
		this.entityData.define(WIDTH, 5f);
		this.entityData.define(DEPTH, .5f);
		this.entityData.define(HEIGHT, 5f);
		this.entityData.define(FACING, Direction.SOUTH);
	}
	
	public float getBrambleWidth() {
		return this.entityData.get(WIDTH);
	}
	
	public float getBrambleDepth() {
		return this.entityData.get(DEPTH);
	}
	
	public float getBrambleHeight() {
		return this.entityData.get(HEIGHT);
	}
	
	public Direction getFacing() {
		return this.entityData.get(FACING);
	}
	
	protected void setDims(float width, float depth, float height) {
		
		// Server side gets this called from outside data manager and so should update data manager.
		// client calls this as data manager event and shouldn't set data manager.
		
		boolean change = false;
		
		if (level.isClientSide) {
			change = true; // Just adjust BB
		} else {
			if (this.getBrambleWidth() != width) {
				this.entityData.set(WIDTH, width);
				change = true;
			}
			if (this.getBrambleDepth() != depth) {
				this.entityData.set(DEPTH, depth);
				change = true;
			}
			if (this.getBrambleHeight() != height) {
				this.entityData.set(HEIGHT, height);
				change = true;
			}
		}
		
		if (change) {
//			AxisAlignedBB old = this.getBoundingBox();
//			this.setEntityBoundingBox(new AxisAlignedBB(
//					old.minX, old.minY, old.minZ,
//					old.minX + width,
//					old.minY + height,
//					old.minZ + depth
//					));
			this.refreshDimensions();
		}
	}
	
	public void setMotion(Direction direction, float distance) {
		this.entityData.set(FACING, direction);
		this.distance = distance;
		
		this.startPos = this.blockPosition();
	}
	
	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (this.level != null && this.level.isClientSide) {
			if (key == WIDTH
					|| key == HEIGHT
					|| key == DEPTH) {
				this.setDims(this.getBrambleWidth(), this.getBrambleDepth(), this.getBrambleHeight());
			} else if (key == FACING && this.level.isClientSide) {
				// Adjust width/depth to rotate if not moving n/s
				final Direction dir = this.entityData.get(FACING);
				if (dir == Direction.WEST || dir == Direction.EAST) {
					final float w = this.getBrambleWidth();
					final float d = this.getBrambleDepth();
					this.setDims(d, w, this.getBrambleHeight());
				}
			}
		}
	}
	
	@Override
	public boolean saveAsPassenger(CompoundTag compound) {
		return false;
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		//super.readEntityFromNBT(compound);
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
    	//super.writeEntityToNBT(compound);
	}
	
	@Override
	public boolean isPickable() {
		return true;
	}
	
	@Override
	public float getPickRadius() {
		return 1f;
	}
	
	protected void onImpact(LivingEntity entity) {
		if (entity != this.plant && !entity.equals(this.plant)) {
			entity.removeEffect(NostrumEffects.rooted);
			entity.doHurtTarget(this);
			entity.hurt(new BrambleDamageSource(this.plant), 6f);
			entity.knockback(1f, (double)Mth.sin(this.getYRot() * 0.017453292F), (double)(-Mth.cos(this.getYRot() * 0.017453292F)));
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.getFacing() != null) {
			this.setRot(this.getFacing().toYRot(), 0f);
		}
		
		checkBoundingBox();
		
		if (!level.isClientSide) {
			// Move if given a direction
			if (this.getFacing() != null) {
				if (!startPos.closerToCenterThan(this.position(), distance)) {
					this.discard();
				}
				
				Vec3 motion = Vec3.atLowerCornerOf(this.getFacing().getNormal())
						.scale(.2)
						;
				this.teleportTo(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
			}
			
			List<Entity> collidedEnts = level.getEntities(this, this.getBoundingBox(), (ent) -> {
				return ent instanceof LivingEntity;
			});
			
			for (Entity ent : collidedEnts) {
				onImpact((LivingEntity) ent);
			}
		}
	}
	
	protected void checkBoundingBox() {
		// When moving E/W, w should be d
		final boolean turned = this.getFacing() == Direction.EAST || this.getFacing() == Direction.WEST;
		
		final float w = turned ? this.getBrambleDepth() : this.getBrambleWidth();
		final float h = this.getBrambleHeight();
		final float d = turned ? this.getBrambleWidth() : this.getBrambleDepth();
		this.setBoundingBox(new AABB(
				this.getX() - (w/2f),
				this.getY(),
				this.getZ() - (d/2f),
				this.getX() + (w/2f),
				this.getY() + h,
				this.getZ() + (d/2f)
				)); // TODO this is different. Used to set override variable and force that to return when getting bounding box, which is now final.
	}
	
//	@Override
//	public AABB getBoundingBox() {
//		checkBoundingBox();
//		return this.entityBBOverride;
//	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	public static class BrambleDamageSource extends EntityDamageSource {
		
		public BrambleDamageSource(PlantBossEntity parent) {
			super("plantboss.bramble", parent);
		}
		
		@Override
		public Component getLocalizedDeathMessage(LivingEntity entityLivingBaseIn) {
	        String untranslated = "death.attack.plantboss.bramble";
	        return new TranslatableComponent(untranslated, new Object[] {entityLivingBaseIn.getDisplayName(), this.entity.getDisplayName()});
	    }
	}
}
