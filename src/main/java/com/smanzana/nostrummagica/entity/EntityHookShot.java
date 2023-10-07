package com.smanzana.nostrummagica.entity;


import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.HookshotItem;
import com.smanzana.nostrummagica.items.HookshotItem.HookshotType;
import com.smanzana.nostrummagica.serializers.HookshotTypeDataSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.utils.Entities;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityHookShot extends Entity {
	
	public static final String ID = "nostrum_hookshot";
	
	private double maxLength;
	
	public double velocityX;
	public double velocityY;
	public double velocityZ;
	
	// Sound and other mechanical cache stuff
	private int tickHooked;
	private Vec3d posLastPlayed;
	
	private static final String NBT_CASTER_ID = "caster_uuid";
	private static final String NBT_ATTACHED_ID = "attached_uuid";
	private static final String NBT_HOOKED = "hooked";
	private static final String NBT_MAX_LENGTH = "max_length";
	private static final String NBT_VELOCITYX = "velocity_x";
	private static final String NBT_VELOCITYY = "velocity_y";
	private static final String NBT_VELOCITYZ = "velocity_z";
	
	protected static final DataParameter<HookshotType> DATA_TYPE = EntityDataManager.<HookshotType>createKey(EntityHookShot.class, HookshotTypeDataSerializer.instance);
	protected static final DataParameter<Boolean> DATA_HOOKED = EntityDataManager.<Boolean>createKey(EntityHookShot.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<Boolean> DATA_FETCHING = EntityDataManager.<Boolean>createKey(EntityHookShot.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<Optional<UUID>> DATA_CASTING_ENTITY = EntityDataManager.<Optional<UUID>>createKey(EntityHookShot.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	protected static final DataParameter<Optional<UUID>> DATA_HOOKED_ENTITY = EntityDataManager.<Optional<UUID>>createKey(EntityHookShot.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	
	public EntityHookShot(EntityType<? extends EntityHookShot> type, World worldIn) {
		super(type, worldIn);
		
		this.setNoGravity(true);
		this.setInvulnerable(true);
	}
	
	public EntityHookShot(EntityType<? extends EntityHookShot> entType, World worldIn, LivingEntity caster, double maxLength, Vec3d direction, HookshotType type) {
		this(entType, worldIn);
		setCaster(caster);
		setMaxLength(maxLength);
		this.setPosition(caster.posX, caster.posY + (caster.getEyeHeight()), caster.posZ);
		this.velocityX = direction.x;
		this.velocityY = direction.y;
		this.velocityZ = direction.z;
		this.setType(type);
	}
	
	public void setCaster(LivingEntity caster) {
		dataManager.set(DATA_CASTING_ENTITY, Optional.of(caster.getUniqueID()));
	}
	
	public void setMaxLength(double max) {
		this.maxLength = max;
	}
	
	protected void setType(HookshotType type) {
		this.dataManager.set(DATA_TYPE, type);
	}
	
	public HookshotType getHookshotType() {
		return dataManager.get(DATA_TYPE);
	}
	
	@Nullable
	protected UUID getCasterID() {
		return dataManager.get(DATA_CASTING_ENTITY).orElse(null);
	}
	
	@Nullable
	public LivingEntity getCaster() {
		LivingEntity ret = null;
		UUID id = getCasterID();
		
		if (id != null) {
			Entity ent = Entities.FindEntity(world, id);
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
		this.dataManager.set(DATA_HOOKED_ENTITY, Optional.of(entity.getUniqueID()));
		setHookedInPlace();
	}
	
	protected void setHookedInPlace() {
		this.dataManager.set(DATA_HOOKED, Boolean.TRUE);
	}
	
	public boolean isHooked() {
		return dataManager.get(DATA_HOOKED);
	}
	
	protected void setIsFetch(boolean isFetch) {
		dataManager.set(DATA_FETCHING, isFetch);
	}
	
	public boolean isFetch() {
		return dataManager.get(DATA_FETCHING);
	}
	
	public boolean isPulling() {
		// TODO isFetch and type are not on client, but client uses it to render.
		return isHooked()
				&& !isFetch()
				&& (this.getHookshotType() != HookshotType.CLAW || this.getCaster().getDistanceSq(this) > 8);
	}
	
	@Nullable
	public Entity getHookedEntity() {
		Entity ret = null;
		
		if (this.isHooked()) {
			Optional<UUID> id = dataManager.get(DATA_HOOKED_ENTITY);
			if (id.isPresent()) {
				ret = Entities.FindEntity(world, id.get());
			}
		}
		
		return ret;
	}
	
	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		return true;
	}
	
	@Override
	public boolean canBePushed() {
		return false;
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return false;
	}
	
	@Override
	public void applyEntityCollision(Entity entityIn) {
		return;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return true;
	}
	
	protected float getMovementFactor() {
		return 0.95f;
	}
	
	protected void onFlightUpdate() {
		LivingEntity caster = this.getCaster();
		if (this.world.isRemote || (caster == null || !caster.isDead) && this.world.isBlockLoaded(new BlockPos(this)))
		{
			super.tick();

			RayTraceResult raytraceresult = RayTrace.forwardsRaycast(this, true, true, this.ticksExisted >= 5, caster);

			if (raytraceresult != null) {
				this.onImpact(raytraceresult);
			}

			this.posX += this.getMotion().x;
			this.posY += this.getMotion().y;
			this.posZ += this.getMotion().z;
			ProjectileHelper.rotateTowardsMovement(this, 0.2F);
			float f = getMovementFactor();

			if (this.isInWater())
			{
				for (int i = 0; i < 4; ++i)
				{
					this.world.addParticle(ParticleTypes.WATER_BUBBLE, this.posX - this.getMotion().x * 0.25D, this.posY - this.getMotion().y * 0.25D, this.posZ - this.getMotion().z * 0.25D, this.getMotion().x, this.getMotion().y, this.getMotion().z, new int[0]);
				}

				f = 0.8F;
			}

			this.getMotion().x = this.velocityX;
			this.getMotion().y = this.velocityY;
			this.getMotion().z = this.velocityZ;
			this.getMotion().x *= (double)f;
			this.getMotion().y *= (double)f;
			this.getMotion().z *= (double)f;
			//this.world.addParticle(this.getParticleType(), this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D, new int[0]);
			this.setPosition(this.posX, this.posY, this.posZ);
		}
		else
		{
			this.setDead();
		}
	}
	
	protected void onHookedUpdate() {
		LivingEntity caster = this.getCaster();
		Entity attachedEntity = this.getHookedEntity();
		if (attachedEntity != null) {
			if (attachedEntity.isDead) {
				this.isDead = true;
				return;
			}
		}
		
		if (caster != null) {
			final double dist = caster.getDistanceSq(this);
			if (dist < 8) {
				if (this.getHookshotType() != HookshotType.CLAW || caster.isSneaking()) {
					this.setDead();
					return;
				}
			}
		}
		
		this.getMotion().x = this.getMotion().y = this.getMotion().z = 0;
		
		if (this.isFetch()) {
			// Bring the hooked entity (and ourselves) back to the shooter
			if (attachedEntity == null) {
				this.setDead();
				return;
			}
			
			if (caster == null) {
				this.setDead();
				return;
			}
			
			if (attachedEntity.isSneaking()) {
				this.setDead();
				return;
			}
			
			caster.getMotion().x = /*caster.getMotion().y = */ caster.getMotion().z
					= attachedEntity.getMotion().x = attachedEntity.getMotion().y = attachedEntity.getMotion().z = 0;
			caster.velocityChanged = true;
			attachedEntity.velocityChanged = true;
			
			Vec3d diff = caster.getPositionVector().addVector(0, caster.getEyeHeight(), 0).subtract(this.getPositionVector());
			Vec3d velocity = diff.normalize().scale(0.75);
			this.posX += velocity.x;
			this.posY += velocity.y;
			this.posZ += velocity.z;
			
			if (attachedEntity instanceof ItemEntity) {
				attachedEntity.getMotion().x = velocity.x;
				attachedEntity.getMotion().y = velocity.y;
				attachedEntity.getMotion().z = velocity.z;
				attachedEntity.velocityChanged = true;
			}
			this.setPositionAndUpdate(posX, posY, posZ);
			
			attachedEntity.setPositionAndUpdate(this.posX, this.posY - (attachedEntity.getHeight() / 2), this.posZ);
		} else {
			// Bring the shooter to the hooked entity
			
			if (attachedEntity != null) {
				if (attachedEntity.isDead) {
					this.setDead();
					return;
				}
				this.setPositionAndUpdate(attachedEntity.posX, attachedEntity.posY + (attachedEntity.getHeight() / 2), attachedEntity.posZ);
			}
			
			if (caster != null) {
				if ((ticksExisted - tickHooked) > 6 && caster.onGround) {
					this.setDead();
					return;
				}
				
				Vec3d diff = this.getPositionVector().subtract(caster.getPositionVector());
				Vec3d velocity = diff.normalize().scale(0.75);
				caster.getMotion().x = velocity.x;
				caster.getMotion().y = velocity.y;
				caster.getMotion().z = velocity.z;
				caster.fallDistance = 0;
				//caster.onGround = true;
				caster.velocityChanged = true;
			}
			
			// Check about playing sounds
			if (caster != null) {
				if (posLastPlayed == null || (posLastPlayed.subtract(caster.getPositionVector()).lengthSquared() > 3)) {
					NostrumMagicaSounds.HOOKSHOT_TICK.play(world, 
							caster.posX + caster.getMotion().x, caster.posY + caster.getMotion().y, caster.posZ + caster.getMotion().z);
					posLastPlayed = caster.getPositionVector();
				}
			}
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (!this.isDead) {
			if (!world.isRemote) {
				LivingEntity caster = this.getCaster();
				
				// Make sure caster still exists
				if (caster != null && caster.isDead) {
					this.setDead();
					return;
				}
				
				// Check length
				if (caster != null) {
					final double dist = caster.getDistanceSq(this);
					if (dist > (maxLength*maxLength)) {
						this.setDead();
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
			wantsFetch = caster.isSneaking();
		}
		
		if (result.typeOfHit == Type.ENTITY && result.entityHit != null && HookshotItem.CanBeHooked(getHookshotType(), result.entityHit) && (caster == null || caster != result.entityHit)) {
			tickHooked = this.ticksExisted;
			
			// Large entities cannot be fetched, and instead we'll override and force the play er to go to them.
			// So if you try to pull a large enemy, you get pulled to them instead hilariously.
			// Non-living entities are ignored... except for ItemEntity which are always fetched.
			if (result.entityHit instanceof ItemEntity) {
				this.setIsFetch(true);
			} else if (!result.entityHit.canBeCollidedWith()) {
				// ignore the entity for like arrows and stuff
				return;
			} else if (result.entityHit instanceof MultiPartEntityPart) {
				return;
			} else if (result.entityHit.getWidth > 1.5 || result.entityHit.getHeight() > 2.5) {
				this.setIsFetch(false);
			} else {
				this.setIsFetch(wantsFetch);
			}
			
			// Have to do this before officially being 'hooked'
			if (!this.isFetch() && caster != null) {
				// Pulling player towards them. Go ahead and move the player up since they're in elytra mode now
				caster.setPositionAndUpdate(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);
			}
			
			setHookedEntity(result.entityHit);
		} else if (result.typeOfHit == Type.BLOCK) {
			// If shooter wants fetch, don't hook to blocks
			
			// Make sure type of hookshot supports material
			BlockState state = world.getBlockState(result.getBlockPos());
			if (wantsFetch || state == null || !HookshotItem.CanBeHooked(getHookshotType(), state)) {
				this.setDead();
				return;
			}
			
			// Can't be fetch
			
			tickHooked = this.ticksExisted;
			this.setPositionAndUpdate(result.hitVec.x, result.hitVec.y, result.hitVec.z);
			
			// Have to do this before officially being 'hooked'
			if (caster != null) {
				// Pulling player towards them. Go ahead and move the player up since they're in elytra mode now
				caster.setPositionAndUpdate(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);
			}
			setHookedInPlace();
		} else {
			return;
		}
	}
	
	@Override
	public boolean writeToNBTOptional(CompoundNBT compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
    }

	@Override
	protected void registerData() { int unused; // TODO
		dataManager.register(DATA_HOOKED, Boolean.FALSE);
		dataManager.register(DATA_HOOKED_ENTITY, Optional.<UUID>absent());
		dataManager.register(DATA_CASTING_ENTITY, Optional.<UUID>absent());
		dataManager.register(DATA_FETCHING, Boolean.FALSE);
		dataManager.register(DATA_TYPE, HookshotType.WEAK);
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		maxLength = compound.getDouble(NBT_MAX_LENGTH);
		velocityX = compound.getDouble(NBT_VELOCITYX);
		velocityY = compound.getDouble(NBT_VELOCITYY);
		velocityZ = compound.getDouble(NBT_VELOCITYZ);
		
		UUID id = compound.getUniqueId(NBT_CASTER_ID);
		if (id != null) {
			dataManager.set(DATA_CASTING_ENTITY, Optional.of(id));
		}
		
		boolean hooked = compound.getBoolean(NBT_HOOKED);
		if (hooked) {
			id = compound.getUniqueId(NBT_ATTACHED_ID);
			if (id != null) {
				Entity hookedEnt = null;
				for (Entity ent : this.world.loadedEntityList) {
					if (ent.getUniqueID().equals(id)) {
						hookedEnt = ent;
						break;
					}
				}
				
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
	protected void writeAdditional(CompoundNBT compound) {
		LivingEntity caster = getCaster();
		if (caster != null) {
			compound.setUniqueId(NBT_CASTER_ID, caster.getUniqueID());
		}
		
		compound.setDouble(NBT_MAX_LENGTH, maxLength);
		compound.setDouble(NBT_VELOCITYX, velocityX);
		compound.setDouble(NBT_VELOCITYY, velocityY);
		compound.setDouble(NBT_VELOCITYZ, velocityZ);
		
		Optional<UUID> id = dataManager.get(DATA_HOOKED_ENTITY);
		if (id.isPresent()) {
			compound.setUniqueId(NBT_ATTACHED_ID, id.get());
		}
		
		if (dataManager.get(DATA_HOOKED)) {
			compound.putBoolean(NBT_HOOKED, true);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}
}
