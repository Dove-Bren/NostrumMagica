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
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
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
		if (this.world.isRemote || (caster == null || caster.isAlive()) && NostrumMagica.isBlockLoaded(world, new BlockPos(this)))
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
					this.world.addParticle(ParticleTypes.BUBBLE, this.posX - this.getMotion().x * 0.25D, this.posY - this.getMotion().y * 0.25D, this.posZ - this.getMotion().z * 0.25D, this.getMotion().x, this.getMotion().y, this.getMotion().z);
				}

				f = 0.8F;
			}

			this.setMotion(
					this.velocityX * f,
					this.velocityY * f,
					this.velocityZ * f
					);
			//this.world.addParticle(this.getParticleType(), this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
			this.setPosition(this.posX, this.posY, this.posZ);
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
			final double dist = caster.getDistanceSq(this);
			if (dist < 8) {
				if (this.getHookshotType() != HookshotType.CLAW || caster.isSneaking()) {
					this.remove();
					return;
				}
			}
		}
		
		this.setMotion(0, 0, 0);
		
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
			
			if (attachedEntity.isSneaking()) {
				this.remove();
				return;
			}
			
			caster.setMotion(0, caster.getMotion().y, 0); // Caster can't move
			attachedEntity.setMotion(0, 0, 0);
			caster.velocityChanged = true;
			attachedEntity.velocityChanged = true;
			
			Vec3d diff = caster.getPositionVector().add(0, caster.getEyeHeight(), 0).subtract(this.getPositionVector());
			Vec3d velocity = diff.normalize().scale(0.75);
			this.posX += velocity.x;
			this.posY += velocity.y;
			this.posZ += velocity.z;
			
			if (attachedEntity instanceof ItemEntity) {
				attachedEntity.setMotion(velocity.x, velocity.y, velocity.z);
				attachedEntity.velocityChanged = true;
			}
			this.setPositionAndUpdate(posX, posY, posZ);
			
			attachedEntity.setPositionAndUpdate(this.posX, this.posY - (attachedEntity.getHeight() / 2), this.posZ);
		} else {
			// Bring the shooter to the hooked entity
			
			if (attachedEntity != null) {
				if (!attachedEntity.isAlive()) {
					this.remove();
					return;
				}
				this.setPositionAndUpdate(attachedEntity.posX, attachedEntity.posY + (attachedEntity.getHeight() / 2), attachedEntity.posZ);
			}
			
			if (caster != null) {
				if ((ticksExisted - tickHooked) > 6 && caster.onGround) {
					this.remove();
					return;
				}
				
				Vec3d diff = this.getPositionVector().subtract(caster.getPositionVector());
				Vec3d velocity = diff.normalize().scale(0.75);
				caster.setMotion(velocity.x, velocity.y, velocity.z);
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
		
		if (this.isAlive()) {
			if (!world.isRemote) {
				LivingEntity caster = this.getCaster();
				
				// Make sure caster still exists
				if (caster != null && !caster.isAlive()) {
					this.remove();
					return;
				}
				
				// Check length
				if (caster != null) {
					final double dist = caster.getDistanceSq(this);
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
			wantsFetch = caster.isSneaking();
		}
		
		if (result.getType() == Type.ENTITY && ((EntityRayTraceResult) result).getEntity() != null && HookshotItem.CanBeHooked(getHookshotType(), ((EntityRayTraceResult) result).getEntity()) && (caster == null || caster != ((EntityRayTraceResult) result).getEntity())) {
			tickHooked = this.ticksExisted;
			
			// Large entities cannot be fetched, and instead we'll override and force the play er to go to them.
			// So if you try to pull a large enemy, you get pulled to them instead hilariously.
			// Non-living entities are ignored... except for ItemEntity which are always fetched.
			if (((EntityRayTraceResult) result).getEntity() instanceof ItemEntity) {
				this.setIsFetch(true);
			} else if (!((EntityRayTraceResult) result).getEntity().canBeCollidedWith()) {
				// ignore the entity for like arrows and stuff
				return;
			} else if (((EntityRayTraceResult) result).getEntity() instanceof MultiPartEntityPart) {
				return;
			} else if (((EntityRayTraceResult) result).getEntity().getWidth() > 1.5 || ((EntityRayTraceResult) result).getEntity().getHeight() > 2.5) {
				this.setIsFetch(false);
			} else {
				this.setIsFetch(wantsFetch);
			}
			
			// Have to do this before officially being 'hooked'
			if (!this.isFetch() && caster != null) {
				// Pulling player towards them. Go ahead and move the player up since they're in elytra mode now
				caster.setPositionAndUpdate(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);
			}
			
			setHookedEntity(((EntityRayTraceResult) result).getEntity());
		} else if (result.getType() == Type.BLOCK) {
			// If shooter wants fetch, don't hook to blocks
			
			// Make sure type of hookshot supports material
			BlockState state = world.getBlockState(new BlockPos(result.getHitVec()));
			if (wantsFetch || state == null || !HookshotItem.CanBeHooked(getHookshotType(), state)) {
				this.remove();
				return;
			}
			
			// Can't be fetch
			
			tickHooked = this.ticksExisted;
			this.setPositionAndUpdate(result.getHitVec().x, result.getHitVec().y, result.getHitVec().z);
			
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
	public boolean writeUnlessRemoved(CompoundNBT compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
    }

	@Override
	protected void registerData() {
		dataManager.register(DATA_HOOKED, Boolean.FALSE);
		dataManager.register(DATA_HOOKED_ENTITY, Optional.<UUID>empty());
		dataManager.register(DATA_CASTING_ENTITY, Optional.<UUID>empty());
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
				Entity hookedEnt = Entities.FindEntity(world, id);
				
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
			compound.putUniqueId(NBT_CASTER_ID, caster.getUniqueID());
		}
		
		compound.putDouble(NBT_MAX_LENGTH, maxLength);
		compound.putDouble(NBT_VELOCITYX, velocityX);
		compound.putDouble(NBT_VELOCITYY, velocityY);
		compound.putDouble(NBT_VELOCITYZ, velocityZ);
		
		Optional<UUID> id = dataManager.get(DATA_HOOKED_ENTITY);
		if (id.isPresent()) {
			compound.putUniqueId(NBT_ATTACHED_ID, id.get());
		}
		
		if (dataManager.get(DATA_HOOKED)) {
			compound.putBoolean(NBT_HOOKED, true);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return new SSpawnObjectPacket(this);
	}
}
