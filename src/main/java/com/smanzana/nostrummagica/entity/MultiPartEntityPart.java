package com.smanzana.nostrummagica.entity;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.util.Entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class MultiPartEntityPart<T extends IMultiPartEntity> extends Entity implements IMultiPartEntityPart<T> {
	
	protected static final EntityDataAccessor<Optional<UUID>> PARENT_ID = SynchedEntityData.<Optional<UUID>>defineId(MultiPartEntityPart.class, EntityDataSerializers.OPTIONAL_UUID);
	protected static final EntityDataAccessor<String> PART_NAME = SynchedEntityData.defineId(MultiPartEntityPart.class, EntityDataSerializers.STRING);

	protected EntityDimensions size;
	protected int orphanTicks;
	protected @Nullable T parentCache = null;
	
	public MultiPartEntityPart(EntityType<?> type, @Nonnull T parent, String name, float width, float height) {
		super(type, parent.getWorld());
		
		this.init(parent, name);
		
		this.setSize(width, height);
	}
	
	public MultiPartEntityPart(EntityType<?> type, Level world, String name, float width, float height) {
		super(type, world);
		
		this.setSize(width, height);
	}
	
	public void init(@Nonnull T parent, String partName) {
		this.entityData.set(PARENT_ID, Optional.of(((Entity) parent).getUUID()));
		this.entityData.set(PART_NAME, partName);
		
		parentCache = parent;
	}
	
	public String getPartName() {
		return this.entityData.get(PART_NAME);
	}
	
	public @Nullable UUID getParentID() {
		return this.entityData.get(PARENT_ID).orElse(null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public @Nullable T getParent() {
		if (parentCache == null) {
			UUID parentID = getParentID();
			if (parentID != null && this.level != null) {
				Entity e = Entities.FindEntity(this.level, parentID);
				try {
					parentCache = (T) e;
				} catch (Exception exception) {
					parentCache = null;
				}
			}
			
			if (parentCache != null) {
				// Found parent for the first time!
				if (level.isClientSide()) {
					// Try to attach to parent
					if (!parentCache.attachClientEntity(this)) {
						// Parent claims we're not theirs.
						NostrumMagica.logger.warn("MultiPartEntity part failed to attach. Removing " + this);
						parentCache = null;
						this.discard();
					}
				}
			}
		}
		
		return parentCache;
	}
	
	@Override
	public boolean is(Entity entityIn) {
		return entityIn == this || (getParent() != null && entityIn == this.getParent());
	}
	
	@Override
	protected void defineSynchedData() {
		this.entityData.define(PARENT_ID, Optional.empty());
		this.entityData.define(PART_NAME, "ENTITY_PART");
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {
		// This handles the initial spawn if the server entity has already changed values
		final String name = compound.getString("name");
		final UUID parentID = compound.hasUUID("parentID") ? compound.getUUID("parentID") : null;
		this.entityData.set(PARENT_ID, Optional.of(parentID));
		this.entityData.set(PART_NAME, name);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		compound.putString("name", this.getPartName());
		compound.putUUID("parentID", getParentID());
	}
	
	@Override
	public boolean saveAsPassenger(CompoundTag compound) {
		// Don't save! We generate each time
		return false;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	@Override
	public boolean isPickable() {
		return true;
	}
	
	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (this.getParent() == null) {
			return false;
		}
		return this.isInvulnerableTo(source) ? false : getParent().attackEntityFromPart(this, source, amount);
	}
	
	@Override
	public EntityDimensions getDimensions(Pose poseIn) {
		return this.size;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.getParent() == null) {
			if (++orphanTicks > 10) {
				this.discard();
			}
		} else {
			orphanTicks = 0;
			Entity parent = (Entity) this.getParent();
			if (!parent.isAlive()) {
				this.discard();
			}
		}
	}
	
	public void setSize(EntityDimensions size) {
		this.size = size;
		this.refreshDimensions();
	}
	
	public void setSize(float width, float height) {
		this.setSize(EntityDimensions.scalable(width, height));
	}
}
