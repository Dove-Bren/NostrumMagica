package com.smanzana.nostrummagica.entity;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.util.Entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class MultiPartEntityPart<T extends IMultiPartEntity> extends Entity implements IMultiPartEntityPart<T> {
	
	protected static final DataParameter<Optional<UUID>> PARENT_ID = EntityDataManager.<Optional<UUID>>defineId(MultiPartEntityPart.class, DataSerializers.OPTIONAL_UUID);
	protected static final DataParameter<String> PART_NAME = EntityDataManager.defineId(MultiPartEntityPart.class, DataSerializers.STRING);

	protected EntitySize size;
	protected int orphanTicks;
	protected @Nullable T parentCache = null;
	
	public MultiPartEntityPart(EntityType<?> type, @Nonnull T parent, String name, float width, float height) {
		super(type, parent.getWorld());
		
		this.init(parent, name);
		
		this.setSize(width, height);
	}
	
	public MultiPartEntityPart(EntityType<?> type, World world, String name, float width, float height) {
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
						this.remove();
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
	protected void readAdditionalSaveData(CompoundNBT compound) {
		// This handles the initial spawn if the server entity has already changed values
		final String name = compound.getString("name");
		final UUID parentID = compound.hasUUID("parentID") ? compound.getUUID("parentID") : null;
		this.entityData.set(PARENT_ID, Optional.of(parentID));
		this.entityData.set(PART_NAME, name);
	}

	@Override
	protected void addAdditionalSaveData(CompoundNBT compound) {
		compound.putString("name", this.getPartName());
		compound.putUUID("parentID", getParentID());
	}
	
	@Override
	public boolean saveAsPassenger(CompoundNBT compound) {
		// Don't save! We generate each time
		return false;
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
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
	public EntitySize getDimensions(Pose poseIn) {
		return this.size;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.getParent() == null) {
			if (++orphanTicks > 10) {
				this.remove();
			}
		} else {
			orphanTicks = 0;
			Entity parent = (Entity) this.getParent();
			if (!parent.isAlive()) {
				this.remove();
			}
		}
	}
	
	public void setSize(EntitySize size) {
		this.size = size;
		this.refreshDimensions();
	}
	
	public void setSize(float width, float height) {
		this.setSize(EntitySize.scalable(width, height));
	}
}
