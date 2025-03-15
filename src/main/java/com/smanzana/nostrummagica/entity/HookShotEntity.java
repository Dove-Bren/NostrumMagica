package com.smanzana.nostrummagica.entity;


import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.equipment.HookshotItem;
import com.smanzana.nostrummagica.item.equipment.HookshotItem.HookshotType;
import com.smanzana.nostrummagica.serializer.HookshotTypeDataSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.Entities;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class HookShotEntity extends Entity {
	
	public static final String ID = "nostrum_hookshot";
	
	private double maxLength;
	
	public double velocityX;
	public double velocityY;
	public double velocityZ;
	
	// Sound and other mechanical cache stuff
	private int tickHooked;
	private Vector3d posLastPlayed;
	
	private static final String NBT_CASTER_ID = "caster_uuid";
	private static final String NBT_ATTACHED_ID = "attached_uuid";
	private static final String NBT_HOOKED = "hooked";
	private static final String NBT_MAX_LENGTH = "max_length";
	private static final String NBT_VELOCITYX = "velocity_x";
	private static final String NBT_VELOCITYY = "velocity_y";
	private static final String NBT_VELOCITYZ = "velocity_z";
	
	protected static final DataParameter<HookshotType> DATA_TYPE = EntityDataManager.<HookshotType>defineId(HookShotEntity.class, HookshotTypeDataSerializer.instance);
	protected static final DataParameter<Boolean> DATA_HOOKED = EntityDataManager.<Boolean>defineId(HookShotEntity.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<Boolean> DATA_FETCHING = EntityDataManager.<Boolean>defineId(HookShotEntity.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<Optional<UUID>> DATA_CASTING_ENTITY = EntityDataManager.<Optional<UUID>>defineId(HookShotEntity.class, DataSerializers.OPTIONAL_UUID);
	protected static final DataParameter<Optional<UUID>> DATA_HOOKED_ENTITY = EntityDataManager.<Optional<UUID>>defineId(HookShotEntity.class, DataSerializers.OPTIONAL_UUID);
	
	public HookShotEntity(EntityType<? extends HookShotEntity> type, World worldIn) {
		super(type, worldIn);
		
		this.setNoGravity(true);
		this.setInvulnerable(true);
	}
	
	public HookShotEntity(EntityType<? extends HookShotEntity> entType, World worldIn, LivingEntity caster, double maxLength, Vector3d direction, HookshotType type) {
		this(entType, worldIn);
		setCaster(caster);
		setMaxLength(maxLength);
		this.setPos(caster.getX(), caster.getY() + (caster.getEyeHeight()), caster.getZ());
		this.velocityX = direction.x;
		this.velocityY = direction.y;
		this.velocityZ = direction.z;
		this.setType(type);
	}
	
	public void setCaster(LivingEntity caster) {
		entityData.set(DATA_CASTING_ENTITY, Optional.of(caster.getUUID()));
	}
	
	public void setMaxLength(double max) {
		this.maxLength = max;
	}
	
	protected void setType(HookshotType type) {
		this.entityData.set(DATA_TYPE, type);
	}
	
	public HookshotType getHookshotType() {
		return entityData.get(DATA_TYPE);
	}
	
	@Nullable
	protected UUID getCasterID() {
		return entityData.get(DATA_CASTING_ENTITY).orElse(null);
	}
	
	@Nullable
	public LivingEntity getCaster() {
		LivingEntity ret = null;
		UUID id = getCasterID();
		
		if (id != null) {
			Entity ent = Entities.FindEntity(level, id);
			if (ent instanceof LivingEntity) {
				ret = (LivingEntity) ent;
			}
		}
		
		return ret;
	}
	
	public double getMaxLength() {
		return this.maxLength;
	}
	
	protected void setHookedEntity(Entity entity) {
		this.entityData.set(DATA_HOOKED_ENTITY, Optional.of(entity.getUUID()));
		setHookedInPlace();
	}
	
	protected void setHookedInPlace() {
		this.entityData.set(DATA_HOOKED, Boolean.TRUE);
	}
	
	public boolean isHooked() {
		return entityData.get(DATA_HOOKED);
	}
	
	protected void setIsFetch(boolean isFetch) {
		entityData.set(DATA_FETCHING, isFetch);
	}
	
	public boolean isFetch() {
		return entityData.get(DATA_FETCHING);
	}
	
	public boolean isPulling() {
		// TODO isFetch and type are not on client, but client uses it to render.
		return isHooked()
				&& !isFetch()
				&& (this.getHookshotType() != HookshotType.CLAW || this.getCaster().distanceToSqr(this) > 8);
	}
	
	@Nullable
	public Entity getHookedEntity() {
		Entity ret = null;
		
		if (this.isHooked()) {
			Optional<UUID> id = entityData.get(DATA_HOOKED_ENTITY);
			if (id.isPresent()) {
				ret = Entities.FindEntity(level, id.get());
			}
		}
		
		return ret;
	}
	
	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		return true;
	}
	
	@Override
	public boolean isPushable() {
		return false;
	}
	
	@Override
	public boolean isPickable() {
		return false;
	}
	
	@Override
	public void push(Entity entityIn) {
		return;
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		return true;
	}
	
	protected float getMovementFactor() {
		return 0.95f;
	}
	
	protected void onFlightUpdate() {
		LivingEntity caster = this.getCaster();
		if (this.level.isClientSide || (caster == null || caster.isAlive()) && NostrumMagica.isBlockLoaded(level, this.blockPosition()))
		{
			super.tick();

			RayTraceResult raytraceresult = RayTrace.forwardsRaycast(this, true, true, this.tickCount >= 5, caster);

			if (raytraceresult != null) {
				this.onImpact(raytraceresult);
			}

			final Vector3d motion = this.getDeltaMovement();
			this.setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
			ProjectileHelper.rotateTowardsMovement(this, 0.2F);
			float f = getMovementFactor();

			if (this.isInWater())
			{
				for (int i = 0; i < 4; ++i)
				{
					this.level.addParticle(ParticleTypes.BUBBLE, this.getX() - this.getDeltaMovement().x * 0.25D, this.getY() - this.getDeltaMovement().y * 0.25D, this.getZ() - this.getDeltaMovement().z * 0.25D, this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
				}

				f = 0.8F;
			}

			this.setDeltaMovement(
					this.velocityX * f,
					this.velocityY * f,
					this.velocityZ * f
					);
			//this.world.addParticle(this.getParticleType(), this.getPosX(), this.getPosY() + 0.5D, this.getPosZ(), 0.0D, 0.0D, 0.0D);
			//this.setPosition(this.getPosX(), this.getPosY(), this.getPosZ());
		}
		else
		{
			this.remove();
		}
	}
	
	protected void onHookedUpdate() {
		LivingEntity caster = this.getCaster();
		Entity attachedEntity = this.getHookedEntity();
		if (attachedEntity != null) {
			if (!attachedEntity.isAlive()) {
				remove();
				return;
			}
		}
		
		if (caster != null) {
			final double dist = caster.distanceToSqr(this);
			if (dist < 8) {
				if (this.getHookshotType() != HookshotType.CLAW || caster.isShiftKeyDown()) {
					this.remove();
					return;
				}
			}
		}
		
		this.setDeltaMovement(0, 0, 0);
		
		if (this.isFetch()) {
			// Bring the hooked entity (and ourselves) back to the shooter
			if (attachedEntity == null) {
				this.remove();
				return;
			}
			
			if (caster == null) {
				this.remove();
				return;
			}
			
			if (attachedEntity.isShiftKeyDown()) {
				this.remove();
				return;
			}
			
			caster.setDeltaMovement(0, caster.getDeltaMovement().y, 0); // Caster can't move
			attachedEntity.setDeltaMovement(0, 0, 0);
			caster.hurtMarked = true;
			attachedEntity.hurtMarked = true;
			
			Vector3d diff = caster.position().add(0, caster.getEyeHeight(), 0).subtract(this.position());
			Vector3d velocity = diff.normalize().scale(0.75);
			
			if (attachedEntity instanceof ItemEntity) {
				attachedEntity.setDeltaMovement(velocity.x, velocity.y, velocity.z);
				attachedEntity.hurtMarked = true;
			}
			this.teleportTo(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);
			
			attachedEntity.teleportTo(this.getX(), this.getY() - (attachedEntity.getBbHeight() / 2), this.getZ());
		} else {
			// Bring the shooter to the hooked entity
			
			if (attachedEntity != null) {
				if (!attachedEntity.isAlive()) {
					this.remove();
					return;
				}
				this.teleportTo(attachedEntity.getX(), attachedEntity.getY() + (attachedEntity.getBbHeight() / 2), attachedEntity.getZ());
			}
			
			if (caster != null) {
				if ((tickCount - tickHooked) > 6 && caster.isOnGround()) {
					this.remove();
					return;
				}
				
				Vector3d diff = this.position().subtract(caster.position());
				Vector3d velocity = diff.normalize().scale(0.75);
				caster.setDeltaMovement(velocity.x, velocity.y, velocity.z);
				caster.fallDistance = 0;
				//caster.onGround = true;
				caster.hurtMarked = true;
			}
			
			// Check about playing sounds
			if (caster != null) {
				if (posLastPlayed == null || (posLastPlayed.subtract(caster.position()).lengthSqr() > 3)) {
					NostrumMagicaSounds.HOOKSHOT_TICK.play(level, 
							caster.getX() + caster.getDeltaMovement().x, caster.getY() + caster.getDeltaMovement().y, caster.getZ() + caster.getDeltaMovement().z);
					posLastPlayed = caster.position();
				}
			}
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.isAlive()) {
			if (!level.isClientSide) {
				LivingEntity caster = this.getCaster();
				
				// Make sure caster still exists
				if (caster != null && !caster.isAlive()) {
					this.remove();
					return;
				}
				
				// Check length
				if (caster != null) {
					final double dist = caster.distanceToSqr(this);
					if (dist > (maxLength*maxLength)) {
						this.remove();
					}
				}
				
				if (isHooked()) {
					onHookedUpdate();
				} else {
					onFlightUpdate();
				}
			}
		}
	}

	protected void onImpact(RayTraceResult result) {
		if (isHooked()) {
			return;
		}
		
		boolean wantsFetch = false;
		
		LivingEntity caster = getCaster();
		
		if (caster != null) {
			wantsFetch = caster.isShiftKeyDown();
		}
		
		if (result.getType() == Type.ENTITY && ((EntityRayTraceResult) result).getEntity() != null && HookshotItem.CanBeHooked(getHookshotType(), ((EntityRayTraceResult) result).getEntity()) && (caster == null || caster != ((EntityRayTraceResult) result).getEntity())) {
			tickHooked = this.tickCount;
			
			// Large entities cannot be fetched, and instead we'll override and force the play er to go to them.
			// So if you try to pull a large enemy, you get pulled to them instead hilariously.
			// Non-living entities are ignored... except for ItemEntity which are always fetched.
			if (((EntityRayTraceResult) result).getEntity() instanceof ItemEntity) {
				this.setIsFetch(true);
			} else if (!((EntityRayTraceResult) result).getEntity().isPickable()) {
				// ignore the entity for like arrows and stuff
				return;
			} else if (((EntityRayTraceResult) result).getEntity() instanceof MultiPartEntityPart) {
				return;
			} else if (((EntityRayTraceResult) result).getEntity().getBbWidth() > 1.5 || ((EntityRayTraceResult) result).getEntity().getBbHeight() > 2.5) {
				this.setIsFetch(false);
			} else {
				this.setIsFetch(wantsFetch);
			}
			
			// Have to do this before officially being 'hooked'
			if (!this.isFetch() && caster != null) {
				// Pulling player towards them. Go ahead and move the player up since they're in elytra mode now
				caster.teleportTo(caster.getX(), caster.getY() + caster.getEyeHeight(), caster.getZ());
			}
			
			setHookedEntity(((EntityRayTraceResult) result).getEntity());
		} else if (result.getType() == Type.BLOCK) {
			// If shooter wants fetch, don't hook to blocks
			
			// Make sure type of hookshot supports material
			BlockState state = level.getBlockState(RayTrace.blockPosFromResult(result));
			if (wantsFetch || state == null || !HookshotItem.CanBeHooked(getHookshotType(), state)) {
				this.remove();
				return;
			}
			
			// Can't be fetch
			
			tickHooked = this.tickCount;
			this.teleportTo(result.getLocation().x, result.getLocation().y, result.getLocation().z);
			
			// Have to do this before officially being 'hooked'
			if (caster != null) {
				// Pulling player towards them. Go ahead and move the player up since they're in elytra mode now
				caster.teleportTo(caster.getX(), caster.getY() + caster.getEyeHeight(), caster.getZ());
			}
			setHookedInPlace();
		} else {
			return;
		}
	}
	
	@Override
	public boolean saveAsPassenger(CompoundNBT compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(DATA_HOOKED, Boolean.FALSE);
		entityData.define(DATA_HOOKED_ENTITY, Optional.<UUID>empty());
		entityData.define(DATA_CASTING_ENTITY, Optional.<UUID>empty());
		entityData.define(DATA_FETCHING, Boolean.FALSE);
		entityData.define(DATA_TYPE, HookshotType.WEAK);
	}

	@Override
	protected void readAdditionalSaveData(CompoundNBT compound) {
		maxLength = compound.getDouble(NBT_MAX_LENGTH);
		velocityX = compound.getDouble(NBT_VELOCITYX);
		velocityY = compound.getDouble(NBT_VELOCITYY);
		velocityZ = compound.getDouble(NBT_VELOCITYZ);
		
		UUID id = compound.getUUID(NBT_CASTER_ID);
		if (id != null) {
			entityData.set(DATA_CASTING_ENTITY, Optional.of(id));
		}
		
		boolean hooked = compound.getBoolean(NBT_HOOKED);
		if (hooked) {
			id = compound.getUUID(NBT_ATTACHED_ID);
			if (id != null) {
				Entity hookedEnt = Entities.FindEntity(level, id);
				
				if (hookedEnt == null) {
					NostrumMagica.logger.warn("Lost hooked entity on deserialization. Client load hook before caster?");
				} else {
					this.setHookedEntity(hookedEnt);
				}
			} else {
				this.setHookedInPlace();
			}
		}
		
	}

	@Override
	protected void addAdditionalSaveData(CompoundNBT compound) {
		LivingEntity caster = getCaster();
		if (caster != null) {
			compound.putUUID(NBT_CASTER_ID, caster.getUUID());
		}
		
		compound.putDouble(NBT_MAX_LENGTH, maxLength);
		compound.putDouble(NBT_VELOCITYX, velocityX);
		compound.putDouble(NBT_VELOCITYY, velocityY);
		compound.putDouble(NBT_VELOCITYZ, velocityZ);
		
		Optional<UUID> id = entityData.get(DATA_HOOKED_ENTITY);
		if (id.isPresent()) {
			compound.putUUID(NBT_ATTACHED_ID, id.get());
		}
		
		if (entityData.get(DATA_HOOKED)) {
			compound.putBoolean(NBT_HOOKED, true);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderAtSqrDistance(double distance) {
		return true;
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
