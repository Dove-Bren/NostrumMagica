package com.smanzana.nostrummagica.entity;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.utils.Entities;

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
	
	protected static final DataParameter<Optional<UUID>> PARENT_ID = EntityDataManager.<Optional<UUID>>createKey(MultiPartEntityPart.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	protected static final DataParameter<String> PART_NAME = EntityDataManager.createKey(MultiPartEntityPart.class, DataSerializers.STRING);

	protected EntitySize size;
	protected int orphanTicks;
	
	public MultiPartEntityPart(EntityType<?> type, @Nonnull IMultiPartEntity parent, String name, float width, float height) {
		super(type, parent.getWorld());
		
		this.init(parent, name);
		
		this.setSize(width, height);
	}
	
	public MultiPartEntityPart(EntityType<?> type, World world, String name, float width, float height) {
		super(type, world);
		
		this.setSize(width, height);
	}
	
	public void init(@Nonnull IMultiPartEntity parent, String partName) {
		this.dataManager.set(PARENT_ID, Optional.of(((Entity) parent).getUniqueID()));
		this.dataManager.set(PART_NAME, partName);
	}
	
	public String getPartName() {
		return this.dataManager.get(PART_NAME);
	}
	
	public @Nullable UUID getParentID() {
		return this.dataManager.get(PARENT_ID).orElse(null);
	}
	
	@SuppressWarnings("unchecked")
	public @Nullable T getParent() {
		T parent = null;
		UUID parentID = getParentID();
		if (parentID != null && this.world != null) {
			Entity e = Entities.FindEntity(this.world, parentID);
			try {
				parent = (T) e;
			} catch (Exception exception) {
				parent = null;
			}
		}
		
		return parent;
	}
	
	@Override
	public boolean isEntityEqual(Entity entityIn) {
		return entityIn == this || (getParent() != null && entityIn == this.getParent());
	}
	
	@Override
	protected void registerData() {
		this.dataManager.register(PARENT_ID, Optional.empty());
		this.dataManager.register(PART_NAME, "ENTITY_PART");
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		// This handles the initial spawn if the server entity has already changed values
		final String name = compound.getString("name");
		final UUID parentID = compound.getUniqueId("parentID");
		this.dataManager.set(PARENT_ID, Optional.of(parentID));
		this.dataManager.set(PART_NAME, name);
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.putString("name", this.getPartName());
		compound.putUniqueId("parentID", getParentID());
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return true;
	}
	
	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.getParent() == null) {
			return false;
		}
		return this.isInvulnerableTo(source) ? false : getParent().attackEntityFromPart(this, source, amount);
	}
	
	@Override
	public EntitySize getSize(Pose poseIn) {
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
		this.recalculateSize();
	}
	
	public void setSize(float width, float height) {
		this.setSize(EntitySize.flexible(width, height));
	}
}
