package com.smanzana.nostrummagica.entity;


import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.HookshotItem;
import com.smanzana.nostrummagica.items.HookshotItem.HookshotType;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityHookShot extends Entity {
	
	private double maxLength;
	protected HookshotType type;
	
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
	
	protected static final DataParameter<Boolean> DATA_HOOKED = EntityDataManager.<Boolean>createKey(EntityHookShot.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<Optional<UUID>> DATA_CASTING_ENTITY = EntityDataManager.<Optional<UUID>>createKey(EntityHookShot.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	protected static final DataParameter<Optional<UUID>> DATA_HOOKED_ENTITY = EntityDataManager.<Optional<UUID>>createKey(EntityHookShot.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	
	public EntityHookShot(World worldIn) {
		super(worldIn);
		this.setSize(.2f, .2f);
		
		this.setNoGravity(true);
	}
	
	public EntityHookShot(World worldIn, EntityLivingBase caster, double maxLength, Vec3d direction, HookshotType type) {
		this(worldIn);
		setCaster(caster);
		setMaxLength(maxLength);
		this.setPosition(caster.posX, caster.posY + (caster.getEyeHeight()), caster.posZ);
		this.velocityX = direction.xCoord;
		this.velocityY = direction.yCoord;
		this.velocityZ = direction.zCoord;
		this.type = type;
	}
	
	public void setCaster(EntityLivingBase caster) {
		dataManager.set(DATA_CASTING_ENTITY, Optional.of(caster.getUniqueID()));
	}
	
	public void setMaxLength(double max) {
		this.maxLength = max;
	}
	
	public HookshotType getType() {
		return this.type;
	}
	
	@Nullable
	protected UUID getCasterID() {
		return dataManager.get(DATA_CASTING_ENTITY).orNull();
	}
	
	@Nullable
	public EntityLivingBase getCaster() {
		EntityLivingBase ret = null;
		UUID id = getCasterID();
		
		if (id != null) {
			for (Entity ent : worldObj.loadedEntityList) {
				if (ent instanceof EntityLivingBase) {
					if (((EntityLivingBase) ent).getUniqueID().equals(id)) {
						ret = (EntityLivingBase) ent;
						break;
					}
				}
			}
		}
		
		return ret;
	}
	
	public double getMaxLength() {
		return this.maxLength;
	}
	
	protected void setHookedEntity(EntityLivingBase entity) {
		this.dataManager.set(DATA_HOOKED_ENTITY, Optional.of(entity.getUniqueID()));
		setHookedInPlace();
	}
	
	protected void setHookedInPlace() {
		this.dataManager.set(DATA_HOOKED, Boolean.TRUE);
	}
	
	public boolean isHooked() {
		return dataManager.get(DATA_HOOKED);
	}
	
	@Nullable
	public EntityLivingBase getHookedEntity() {
		EntityLivingBase ret = null;
		
		if (this.isHooked()) {
			Optional<UUID> id = dataManager.get(DATA_HOOKED_ENTITY);
			if (id.isPresent()) {
				for (Entity ent : worldObj.loadedEntityList) {
					if (ent instanceof EntityLivingBase) {
						if (((EntityLivingBase) ent).getUniqueID().equals(id.get())) {
							ret = (EntityLivingBase) ent;
							break;
						}
					}
				}
			}
		}
		
		return ret;
	}
	
	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
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
		EntityLivingBase caster = this.getCaster();
		if (this.worldObj.isRemote || (caster == null || !caster.isDead) && this.worldObj.isBlockLoaded(new BlockPos(this)))
		{
			super.onUpdate();

			RayTraceResult raytraceresult = ProjectileHelper.forwardsRaycast(this, true, this.ticksExisted >= 5, caster);

			if (raytraceresult != null)
			{
				this.onImpact(raytraceresult);
			}

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			ProjectileHelper.rotateTowardsMovement(this, 0.2F);
			float f = getMovementFactor();

			if (this.isInWater())
			{
				for (int i = 0; i < 4; ++i)
				{
					this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ, new int[0]);
				}

				f = 0.8F;
			}

			this.motionX = this.velocityX;
			this.motionY = this.velocityY;
			this.motionZ = this.velocityZ;
			this.motionX *= (double)f;
			this.motionY *= (double)f;
			this.motionZ *= (double)f;
			//this.worldObj.spawnParticle(this.getParticleType(), this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D, new int[0]);
			this.setPosition(this.posX, this.posY, this.posZ);
		}
		else
		{
			this.setDead();
		}
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (!this.isDead) {
			if (!worldObj.isRemote) {
				EntityLivingBase caster = this.getCaster();
				
				// Make sure caster still exists
				if (caster != null && caster.isDead) {
					this.setDead();
					return;
				}
				
				// Check length
				if (caster != null) {
					final double dist = caster.getDistanceSqToEntity(this);
					if (dist > (maxLength*maxLength)) {
						this.setDead();
					}
				}
				
				if (isHooked()) {
					this.motionX = this.motionY = this.motionZ = 0;
					
					EntityLivingBase attachedEntity = this.getHookedEntity();
					if (attachedEntity != null) {
						if (attachedEntity.isDead) {
							this.kill();
							this.isDead = true;
							return;
						}
						
						this.setPositionAndUpdate(attachedEntity.posX, attachedEntity.posY + (attachedEntity.height / 2), attachedEntity.posZ);
					}
					
					if (caster != null) {
						if ((ticksExisted - tickHooked) > 6 && caster.onGround) {
							this.setDead();
							return;
						}
						
						final double dist = caster.getDistanceSqToEntity(this);
						if (dist < 8) {
							this.setDead();
						} else {
							Vec3d diff = this.getPositionVector().subtract(caster.getPositionVector());
							Vec3d velocity = diff.normalize().scale(0.75);
							caster.motionX = velocity.xCoord;
							caster.motionY = velocity.yCoord;
							caster.motionZ = velocity.zCoord;
							caster.fallDistance = 0;
							caster.onGround = true;
							caster.velocityChanged = true;
							
							// Check about playing sounds
							if (posLastPlayed == null || (posLastPlayed.subtract(caster.getPositionVector()).lengthSquared() > 3)) {
								NostrumMagicaSounds.HOOKSHOT_TICK.play(worldObj, 
										caster.posX + caster.motionX, caster.posY + caster.motionY, caster.posZ + caster.motionZ);
								posLastPlayed = caster.getPositionVector();
							}
						}
					}
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
		
		EntityLivingBase caster = getCaster();
		if (result.typeOfHit == Type.ENTITY && result.entityHit != null && HookshotItem.CanBeHooked(type, result.entityHit) && (caster == null || caster != result.entityHit)) {
			tickHooked = this.ticksExisted;
			setHookedEntity((EntityLivingBase) result.entityHit);
		} else if (result.typeOfHit == Type.BLOCK) {
			// Make sure type of hookshot supports material
			IBlockState state = worldObj.getBlockState(result.getBlockPos());
			if (state == null || !HookshotItem.CanBeHooked(type, state)) {
				this.setDead();
				return;
			}
			
			tickHooked = this.ticksExisted;
			this.setPositionAndUpdate(result.hitVec.xCoord, result.hitVec.yCoord, result.hitVec.zCoord);
			setHookedInPlace();
		} else {
			return;
		}
	}
	
	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
    }

	@Override
	protected void entityInit() {
		dataManager.register(DATA_HOOKED, Boolean.FALSE);
		dataManager.register(DATA_HOOKED_ENTITY, Optional.<UUID>absent());
		dataManager.register(DATA_CASTING_ENTITY, Optional.<UUID>absent());
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
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
				EntityLivingBase hookedEnt = null;
				for (Entity ent : this.worldObj.loadedEntityList) {
					if (ent instanceof EntityLivingBase && ent.getUniqueID().equals(id)) {
						hookedEnt = (EntityLivingBase) ent;
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
	protected void writeEntityToNBT(NBTTagCompound compound) {
		EntityLivingBase caster = getCaster();
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
			compound.setBoolean(NBT_HOOKED, true);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}
}
