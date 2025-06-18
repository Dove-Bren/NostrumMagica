package com.smanzana.nostrummagica.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.Entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class WhirlwindEntity extends Entity {
	
	public static final String ID = "whirlwind";
	
	protected static final EntityDataAccessor<Float> DATA_SIZE = SynchedEntityData.<Float>defineId(WhirlwindEntity.class, EntityDataSerializers.FLOAT);
	protected static final EntityDataAccessor<Optional<UUID>> DATA_ATTACHTO = SynchedEntityData.<Optional<UUID>>defineId(WhirlwindEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	
	protected @Nullable Predicate<Entity> filter;
	protected int duration;
	
	private final Map<Entity, Integer> affectedEnts; // map from ent to ticksExisted of when we last affected them
	
	protected WhirlwindEntity(EntityType<? extends WhirlwindEntity> type, Level level) {
		super(type, level);
		this.duration = 60;
		this.affectedEnts = new HashMap<>();
	}
	
	public WhirlwindEntity(Level level, int duration, float size, @Nullable Entity attachToEntity, @Nullable Predicate<Entity> filter) {
		this(NostrumEntityTypes.whirlwind, level);
		this.duration = duration;
		this.filter = filter;
		this.getEntityData().set(DATA_SIZE, size);
		this.getEntityData().set(DATA_ATTACHTO, Optional.ofNullable(attachToEntity == null ? null : attachToEntity.getUUID()));
	}
	
	public WhirlwindEntity(Level level, int duration, @Nullable Entity attachToEntity, @Nullable Predicate<Entity> filter) {
		this(level, duration, 1f, attachToEntity, filter);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_SIZE, 1f);
		this.entityData.define(DATA_ATTACHTO, Optional.empty());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	@Override
	public boolean saveAsPassenger(CompoundTag compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
	}
	
	public float getWhirlwindSize() {
		return this.getEntityData().get(DATA_SIZE);
	}
	
	public @Nullable Entity getAttachToEntity() {
		Optional<UUID> id = this.getEntityData().get(DATA_ATTACHTO);
		if (id.isPresent()) {
			return Entities.FindEntity(level, id.get());
		}
		return null;
	}
	
	protected void clearAttachToEntity() {
		this.getEntityData().set(DATA_ATTACHTO, Optional.empty());
	}
	
	protected boolean affectsEntity(Entity ent) {
		return filter == null || filter.test(ent);
	}
	
	protected boolean canAffect(Entity ent) {
		return affectsEntity(ent)
				&& ent != getAttachToEntity()
				&& this.tickCount - affectedEnts.getOrDefault(ent, -20) >= 20;
	}
	
	protected float getPushForce(Entity ent) {
		return this.getWhirlwindSize();
	}
	
	protected void doWhirlwindPush(Entity ent) {
		final float force = getPushForce(ent);
		Vec3 dir = ent.position().subtract(position()).add(0, 1, 0);
		ent.setDeltaMovement(ent.getDeltaMovement().add(dir.normalize().scale(force)));
		ent.hasImpulse = true;
		
		NostrumMagicaSounds.DAMAGE_WIND.play(ent);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		@Nullable Entity attachToEnt = this.getAttachToEntity();
		
		if (!this.level.isClientSide()) {
			// Check for ents that need pushing
			final float size = getWhirlwindSize() * (attachToEnt != null && attachToEnt.isAlive() ? 3f : 1f);
			AABB box = AABB.ofSize(position().add(0, size / 2, 0), size, size, size);
			for (Entity ent : this.level.getEntities(this, box, this::canAffect)) {
				doWhirlwindPush(ent);
				this.affectedEnts.put(ent, this.tickCount);
			}
			
			if (this.tickCount >= this.duration) {
				this.remove(RemovalReason.DISCARDED);
				return;
			}
		}
		
		// Note: outside server-only ocde so client can do it too
		if (attachToEnt == null) {
			if (this.getEntityData().get(DATA_ATTACHTO).isPresent()) {
				// failed lookup
				this.clearAttachToEntity();
			}
		} else {
			if (attachToEnt.isAlive()) {
				this.moveTo(attachToEnt.position());
			} else {
				this.clearAttachToEntity();
			}
		}
		
	}

}
