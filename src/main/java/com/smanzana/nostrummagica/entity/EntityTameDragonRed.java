package com.smanzana.nostrummagica.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.smanzana.nostrummagica.entity.tasks.DragonAINearestAttackableTarget;
import com.smanzana.nostrummagica.entity.tasks.DragonMeleeAttackTask;
import com.smanzana.nostrummagica.entity.tasks.EntityAIFollowOwnerGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAIOwnerHurtByTargetGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAIOwnerHurtTargetGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAISitGeneric;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemSaddle;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityTameDragonRed extends EntityDragonRedBase implements IEntityTameable {

	protected static final DataParameter<Boolean> TAMED = EntityDataManager.<Boolean>createKey(EntityTameDragonRed.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.<Optional<UUID>>createKey(EntityTameDragonRed.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected static final DataParameter<Boolean> SITTING = EntityDataManager.<Boolean>createKey(EntityTameDragonRed.class, DataSerializers.BOOLEAN);
    
    private DragonAINearestAttackableTarget<EntityPlayer> aiPlayerTarget;
    private EntityAIHurtByTarget aiRevengeTarget;
    
	public EntityTameDragonRed(World worldIn) {
		super(worldIn);
		
		this.setSize(6F * .6F, 4.6F * .6F);
        this.stepHeight = 2;
        this.isImmuneToFire = true;
	}
	
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(TAMED, false);
		this.dataManager.register(OWNER_UNIQUE_ID, Optional.<UUID>absent());
		this.dataManager.register(SITTING, false);
		
		aiPlayerTarget = new DragonAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true);
		aiRevengeTarget = new EntityAIHurtByTarget(this, false, new Class[0]);
	}
	
	protected void setupTamedAI() {
		this.targetTasks.removeTask(aiPlayerTarget);
	}
	
	protected void setupBaseAI() {
		this.tasks.addTask(1, new EntityAISitGeneric<EntityTameDragonRed>(this));
		this.tasks.addTask(2, new DragonMeleeAttackTask(this, 1.0D, true));
		this.tasks.addTask(3, new EntityAIFollowOwnerGeneric<EntityTameDragonRed>(this, 1.0D, 10.0F, 2.0F));
		this.tasks.addTask(4, new EntityAIWander(this, 1.0D, 30));
		
		this.targetTasks.addTask(1, new EntityAIOwnerHurtByTargetGeneric<EntityTameDragonRed>(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTargetGeneric<EntityTameDragonRed>(this));
        this.targetTasks.addTask(3, aiRevengeTarget);
        this.targetTasks.addTask(4, aiPlayerTarget);
		this.targetTasks.addTask(5, new DragonAINearestAttackableTarget<EntityZombie>(this, EntityZombie.class, true));
		this.targetTasks.addTask(6, new DragonAINearestAttackableTarget<EntitySheep>(this, EntitySheep.class, true));
		this.targetTasks.addTask(7, new DragonAINearestAttackableTarget<EntityCow>(this, EntityCow.class, true));
		this.targetTasks.addTask(8, new DragonAINearestAttackableTarget<EntityPig>(this, EntityPig.class, true));
	}
	
	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		
		this.setupBaseAI();
		if (this.isTamed()) {
			this.setupTamedAI();
		}
	}
	
	@Override
	protected float getSoundVolume() {
		return 1F;
	}
	
	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand, @Nullable ItemStack stack) {
		if (this.worldObj.isRemote) {
			return false;
		}
		
		if (this.isTamed() && player == this.getOwner()) {
			if (hand == EnumHand.MAIN_HAND) {
				if (stack == null) {
					this.setSitting(!this.isSitting());
				} else if (stack.getItem() instanceof ItemSaddle) {
					player.startRiding(this);
				}
			}
		} else if (!this.isTamed()) {
			this.tame(player, false);
		}
		
		return false;
	}
	
	@Nullable
	public Entity getControllingPassenger() {
		return this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
	}
	
	@Override
	public boolean canBeSteered() {
		Entity entity = this.getControllingPassenger();
		return entity instanceof EntityLivingBase;
	}
	
	@Override
	public boolean isBreedingItem(@Nullable ItemStack stack) {
		return false;
	}
	
	@Override
	 public boolean canBeLeashedTo(EntityPlayer player) {
		return player == getOwner();
	}

	@Nullable
	public UUID getOwnerId() {
		return ((Optional<UUID>)this.dataManager.get(OWNER_UNIQUE_ID)).orNull();
	}

	public void setOwnerId(@Nullable UUID p_184754_1_) {
		this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(p_184754_1_));
	}

	@Nullable
	public EntityLivingBase getOwner() {
		try {
			UUID uuid = this.getOwnerId();
			return uuid == null ? null : this.worldObj.getPlayerEntityByUUID(uuid);
		} catch (IllegalArgumentException var2) {
			return null;
		}
	}

	public boolean isOwner(EntityLivingBase entityIn) {
		return entityIn == this.getOwner();
	}
	
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);

		if (this.getOwnerId() == null) {
			compound.setString("OwnerUUID", "");
		} else {
			compound.setString("OwnerUUID", this.getOwnerId().toString());
		}
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		String s;

		if (compound.hasKey("OwnerUUID", 8)) {
			s = compound.getString("OwnerUUID");
		}
		else {
			String s1 = compound.getString("Owner");
			s = PreYggdrasilConverter.convertMobOwnerIfNeeded(this.getServer(), s1);
		}

		if (!s.isEmpty()) {
			try {
				this.setOwnerId(UUID.fromString(s));
				this.setTamed(true);
			}
			catch (Throwable var4) {
				this.setTamed(false);
			}
		}

	}
	
	public boolean isTamed() {
		return this.dataManager.get(TAMED);
	}

	public void setTamed(boolean tamed) {
		this.dataManager.set(TAMED, tamed);
		if (tamed) {
			this.setupTamedAI();
		}
	}
	
	private void tame(EntityPlayer player, boolean force) {
		
		boolean success = false;
		
		if (force || this.getRNG().nextInt(10) == 0) {
			this.setTamed(true);
			this.navigator.clearPathEntity();
			this.setAttackTarget(null);
			this.setHealth((float) this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue());
			this.setOwnerId(player.getUniqueID());
			
			success = true;
		}

		if (!this.worldObj.isRemote) {
			this.worldObj.setEntityState(this, success ? (byte) 7 : (byte) 6);
		}
		
		playTameEffect(success);
	}
	
	private void playTameEffect(boolean success) {
		
		EnumParticleTypes enumparticletypes = success ? EnumParticleTypes.HEART : EnumParticleTypes.SMOKE_NORMAL;

		for (int i = 0; i < 15; ++i) {
			double d0 = this.rand.nextGaussian() * 0.02D;
			double d1 = this.rand.nextGaussian() * 0.02D;
			double d2 = this.rand.nextGaussian() * 0.02D;
			this.worldObj.spawnParticle(enumparticletypes, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2, new int[0]);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 7) {
			this.playTameEffect(true);
		} else if (id == 6) {
			this.playTameEffect(false);
		} else {
			super.handleStatusUpdate(id);
		}
	}
	
	@Override
	public Team getTeam() {
		if (this.isTamed()) {
			EntityLivingBase entitylivingbase = this.getOwner();

			if (entitylivingbase != null) {
				return entitylivingbase.getTeam();
			}
		}

		return super.getTeam();
	}

	/**
	 * Returns whether this Entity is on the same team as the given Entity.
	 */
	@Override
	public boolean isOnSameTeam(Entity entityIn) {
		if (this.isTamed()) {
			EntityLivingBase myOwner = this.getOwner();

			if (entityIn == myOwner) {
				return true;
			}

			if (myOwner != null) {
				
				if (entityIn instanceof IEntityOwnable) {
					if (((IEntityOwnable) entityIn).getOwner() == myOwner) {
						return true;
					}
				}
				
				return myOwner.isOnSameTeam(entityIn);
			}
		}

		return super.isOnSameTeam(entityIn);
	}

	/**
	 * Called when the mob's health reaches 0.
	 */
	@Override
	public void onDeath(DamageSource cause) {
		if (!this.worldObj.isRemote && this.worldObj.getGameRules().getBoolean("showDeathMessages") && this.getOwner() instanceof EntityPlayerMP) {
			this.getOwner().addChatMessage(this.getCombatTracker().getDeathMessage());
		}

		super.onDeath(cause);
	}

	@Override
	public boolean isSitting() {
		return this.dataManager.get(SITTING);
	}
	
	public void setSitting(boolean sitting) {
		this.dataManager.set(SITTING, sitting);
	}

	@Override
	public String getLoreKey() {
		return "nostrum__dragon_baby_red";
	}

	@Override
	public String getLoreDisplayName() {
		return "Baby Red Dragons";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("An adorable baby dragon!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("An adorable baby dragon!", "Those with less of a heart may find their hearts to be especially valuable...");
	}

	@Override
	protected void setFlyingAI() {
		//
	}

	@Override
	protected void setGroundedAI() {
		//
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(10.0D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64D);
    }
	
	@Override
	protected boolean canDespawn() {
		return false;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		EntityLivingBase target = this.getAttackTarget();
		if (target != null) {
			if (isOnSameTeam(target)) {
				this.setAttackTarget(null);
				aiRevengeTarget.resetTask();
			}
		}
	}
	
//	@Override
//	public void moveEntityWithHeading(float strafe, float forward) {
//		if (this.isBeingRidden() && this.canBeSteered()) {
//			EntityLivingBase entitylivingbase = (EntityLivingBase)this.getControllingPassenger();
//			this.rotationYaw = entitylivingbase.rotationYaw;
//			this.prevRotationYaw = this.rotationYaw;
//			this.rotationPitch = entitylivingbase.rotationPitch * 0.5F;
//			this.setRotation(this.rotationYaw, this.rotationPitch);
//			this.renderYawOffset = this.rotationYaw;
//			this.rotationYawHead = this.renderYawOffset;
//			strafe = entitylivingbase.moveStrafing * 0.5F;
//			forward = entitylivingbase.moveForward;
//
//			if (forward <= 0.0F)
//			{
//				forward *= 0.25F;
//			}
//
//			if (this.jumpPower > 0.0F && !this.isHorseJumping() && this.onGround)
//			{
//				this.motionY = this.getHorseJumpStrength() * (double)this.jumpPower;
//
//				if (this.isPotionActive(MobEffects.JUMP_BOOST))
//				{
//					this.motionY += (double)((float)(this.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
//				}
//
//				this.setHorseJumping(true);
//				this.isAirBorne = true;
//
//				if (forward > 0.0F)
//				{
//					float f = MathHelper.sin(this.rotationYaw * 0.017453292F);
//					float f1 = MathHelper.cos(this.rotationYaw * 0.017453292F);
//					this.motionX += (double)(-0.4F * f * this.jumpPower);
//					this.motionZ += (double)(0.4F * f1 * this.jumpPower);
//					this.playSound(SoundEvents.ENTITY_HORSE_JUMP, 0.4F, 1.0F);
//				}
//
//				this.jumpPower = 0.0F;
//				net.minecraftforge.common.ForgeHooks.onLivingJump(this);
//			}
//
//			this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
//
//			if (this.canPassengerSteer())
//			{
//				this.setAIMoveSpeed((float)this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
//				super.moveEntityWithHeading(strafe, forward);
//			}
//			else if (entitylivingbase instanceof EntityPlayer)
//			{
//				this.motionX = 0.0D;
//				this.motionY = 0.0D;
//				this.motionZ = 0.0D;
//			}
//
//			if (this.onGround)
//			{
//				this.jumpPower = 0.0F;
//				this.setHorseJumping(false);
//			}
//
//			this.prevLimbSwingAmount = this.limbSwingAmount;
//			double d1 = this.posX - this.prevPosX;
//			double d0 = this.posZ - this.prevPosZ;
//			float f2 = MathHelper.sqrt_double(d1 * d1 + d0 * d0) * 4.0F;
//
//			if (f2 > 1.0F)
//			{
//				f2 = 1.0F;
//			}
//
//			this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
//			this.limbSwing += this.limbSwingAmount;
//		}
//		else
//		{
//			this.jumpMovementFactor = 0.02F;
//			super.moveEntityWithHeading(strafe, forward);
//		}
//	}

}
