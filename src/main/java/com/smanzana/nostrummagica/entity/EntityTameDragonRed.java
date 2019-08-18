package com.smanzana.nostrummagica.entity;

import java.util.Random;
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
import net.minecraft.entity.ai.EntityAISwimming;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityTameDragonRed extends EntityDragonRedBase implements IEntityTameable {

	protected static final DataParameter<Boolean> TAMED = EntityDataManager.<Boolean>createKey(EntityTameDragonRed.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.<Optional<UUID>>createKey(EntityTameDragonRed.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected static final DataParameter<Boolean> SITTING = EntityDataManager.<Boolean>createKey(EntityTameDragonRed.class, DataSerializers.BOOLEAN);
    
    protected static final DataParameter<Boolean> CAPABILITY_FLY = EntityDataManager.<Boolean>createKey(EntityTameDragonRed.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Byte> CAPABILITY_JUMP = EntityDataManager.<Byte>createKey(EntityTameDragonRed.class, DataSerializers.BYTE);
    protected static final DataParameter<Float> CAPABILITY_JUMP_HEIGHT = EntityDataManager.<Float>createKey(EntityTameDragonRed.class, DataSerializers.FLOAT);
    protected static final DataParameter<Float> CAPABILITY_SPEED = EntityDataManager.<Float>createKey(EntityTameDragonRed.class, DataSerializers.FLOAT);
    protected static final DataParameter<Integer> CAPABILITY_MANA = EntityDataManager.<Integer>createKey(EntityTameDragonRed.class, DataSerializers.VARINT);
    protected static final DataParameter<Boolean> CAPABILITY_MAGIC = EntityDataManager.<Boolean>createKey(EntityTameDragonRed.class, DataSerializers.BOOLEAN);
    
    protected static final String NBT_TAMED = "Tamed";
    protected static final String NBT_OWNER_ID = "OwnerUUID";
    protected static final String NBT_SITTING = "Sitting";
    protected static final String NBT_CAP_FLY = "CapableFly";
    protected static final String NBT_CAP_JUMP = "CapableJump";
    protected static final String NBT_CAP_JUMPBOOST = "CapableJumpBonus";
    protected static final String NBT_CAP_SPEED = "CapableSpeedBonus";
    protected static final String NBT_CAP_MANA = "CapableMana";
    protected static final String NBT_CAP_MAGIC = "CapableMagic";
    
    // AI tasks to swap when tamed
    private DragonAINearestAttackableTarget<EntityPlayer> aiPlayerTarget;
    private EntityAIHurtByTarget aiRevengeTarget;
    
    // Internal timers for controlling while riding
    private int jumpCount; // How many times we've jumped
    
	public EntityTameDragonRed(World worldIn) {
		super(worldIn);
		
		this.setSize(6F * .4F, 4.6F * .6F);
        this.stepHeight = 2;
        this.isImmuneToFire = true;
	}
	
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(TAMED, false);
		this.dataManager.register(OWNER_UNIQUE_ID, Optional.<UUID>absent());
		this.dataManager.register(SITTING, false);
		this.dataManager.register(CAPABILITY_FLY, Boolean.FALSE);
		this.dataManager.register(CAPABILITY_JUMP, (byte) 0);
		this.dataManager.register(CAPABILITY_JUMP_HEIGHT, 0f);
		this.dataManager.register(CAPABILITY_SPEED, 0f);
		this.dataManager.register(CAPABILITY_MANA, 0);
		this.dataManager.register(CAPABILITY_MAGIC, Boolean.FALSE);
		
		aiPlayerTarget = new DragonAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true);
		aiRevengeTarget = new EntityAIHurtByTarget(this, false, new Class[0]);
	}
	
	protected void setupTamedAI() {
		this.targetTasks.removeTask(aiPlayerTarget);
	}
	
	protected void setupBaseAI() {
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAISitGeneric<EntityTameDragonRed>(this));
		this.tasks.addTask(2, new DragonMeleeAttackTask(this, 1.0D, true));
		this.tasks.addTask(3, new EntityAIFollowOwnerGeneric<EntityTameDragonRed>(this, 1.0D, 16.0F, 4.0F));
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
					this.setSitting(false);
				}
				
				//donotcheckin
				{
					System.out.println("Can fly: " + this.getCanFly());
					System.out.println("Jumps: " + this.getBonusJumps());
					System.out.println("Jump Height Bonus: " + this.getJumpHeightBonus());
					System.out.println("Speed Bonus: " + this.getSpeedBonus());
					System.out.println("Mana: " + this.getDragonMana());
					System.out.println("Has Magic: " + this.getCanUseMagic());
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
			compound.setString(NBT_OWNER_ID, "");
		} else {
			compound.setString(NBT_OWNER_ID, this.getOwnerId().toString());
		}
		
		compound.setBoolean(NBT_SITTING, this.isSitting());
		
		compound.setBoolean(NBT_CAP_FLY, this.getCanFly());
		compound.setByte(NBT_CAP_JUMP, (byte) this.getBonusJumps());
		compound.setFloat(NBT_CAP_JUMPBOOST, this.getJumpHeightBonus());
		compound.setFloat(NBT_CAP_SPEED, this.getSpeedBonus());
		compound.setInteger(NBT_CAP_MANA, this.getDragonMana());
		compound.setBoolean(NBT_CAP_MAGIC, this.getCanUseMagic());
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
		
		this.setSitting(compound.getBoolean(NBT_SITTING));
		
		boolean canFly = compound.getBoolean(NBT_CAP_FLY);
		byte jumps = compound.getByte(NBT_CAP_JUMP);
		float jumpboost = compound.getFloat(NBT_CAP_JUMPBOOST);
		float speed = compound.getFloat(NBT_CAP_SPEED);
		int mana = compound.getInteger(NBT_CAP_MANA);
		boolean canCast = compound.getBoolean(NBT_CAP_MAGIC);
		
		this.setStats(canFly, jumps, jumpboost, speed, mana, canCast);
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
	
	protected boolean getCanFly() {
		return this.dataManager.get(CAPABILITY_FLY);
	}
	
	protected int getBonusJumps() {
		return (int) this.dataManager.get(CAPABILITY_JUMP);
	}
	
	protected float getJumpHeightBonus() {
		return this.dataManager.get(CAPABILITY_JUMP_HEIGHT);
	}
	
	protected float getSpeedBonus() {
		return this.dataManager.get(CAPABILITY_SPEED);
	}
	
	protected int getDragonMana() {
		return this.dataManager.get(CAPABILITY_MANA);
	}
	
	protected boolean getCanUseMagic() {
		return this.dataManager.get(CAPABILITY_MAGIC);
	}
	
	private void setStats(
			boolean canFly,
			int bonusJumps,
			float bonusJumpHeight, // relative. 1 is double the height!
			float bonusSpeed, // ""
			int mana,
			boolean hasMagic
			) {
		
		this.dataManager.set(CAPABILITY_FLY, canFly);
		this.dataManager.set(CAPABILITY_JUMP, (byte) bonusJumps);
		this.dataManager.set(CAPABILITY_JUMP_HEIGHT, bonusJumpHeight);
		this.dataManager.set(CAPABILITY_SPEED, bonusSpeed);
		this.dataManager.set(CAPABILITY_MANA, mana);
		this.dataManager.set(CAPABILITY_MAGIC, hasMagic);
		
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.31D * (1D + (double) bonusSpeed));
	}
	
	public void rollRandomStats() {
		Random rand = this.getRNG();
		boolean canFly = false;
		int bonusJumps = 0;
		
		// Red dragons are 40/60 oon flying.
		// If they can fly, they usually have good jumps.
		// If not, they have more random jump ability
		canFly = rand.nextInt(10) < 4;
		if (canFly) {
			bonusJumps = 1 + rand.nextInt(2);
		} else {
			bonusJumps = rand.nextInt(4);
		}
		
		// 50% of the time, no jump height bonus.
		// Otherwise, random between -15 and 15
		float bonusJumpHeight = 0.0f;
		if (rand.nextBoolean()) {
			bonusJumpHeight = (rand.nextFloat() - .5f) * .3f;
		}
		
		// Speed just happens all the time.
		float bonusSpeed = (rand.nextFloat() - .5f) * .15f;
		
		// Nearly all red dragons have a mana pool. 90% of them.
		int mana = 0;
		if (rand.nextInt(10) < 9) {
			// If they have mana, they have a lot! base 2000, rand 1000
			// in intervals of 100
			mana = 2000 + (100 * rand.nextInt(10));
		}
		
		// Baby dragons may or may not have magic.
		// If it can fly, it's more likely to be supreme and
		// have magic (40%). Otherwise, 25%;
		boolean hasMagic;
		if (canFly) {
			hasMagic = rand.nextInt(5) < 2;
		} else {
			hasMagic = rand.nextInt(4)  == 0;
		}
		
		this.setStats(canFly, bonusJumps, bonusJumpHeight, bonusSpeed, mana, hasMagic);
	}
	
	public void rollInheritedStats(EntityTameDragonRed par1, EntityTameDragonRed par2) {
		// TODO
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
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.31D);
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
	
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		
		if (this.worldObj.isRemote) {
			if (this.considerFlying()) {
				if (this.motionY < 0.0D) {
					double relief = Math.min(1.0D, this.motionX * this.motionX + this.motionZ * this.motionZ);
					this.motionY *= (1D - (0.9D * relief));				
				}
			}
		}
	}
	
//	@Override
//	public void moveEntityWithHeading(float strafe, float forward) {
//		if (this.isBeingRidden() && this.canBeSteered()) {
//			
//		}
//	}
	
	@Override
	public void moveEntityWithHeading(float strafe, float forward) {
		
		if (this.onGround && this.motionY <= 0) {
			this.jumpCount = 0;
		}
		
		if (this.isBeingRidden() && this.canBeSteered()) {
			EntityLivingBase entitylivingbase = (EntityLivingBase)this.getControllingPassenger();
			this.rotationYaw = entitylivingbase.rotationYaw;
			this.prevRotationYaw = this.rotationYaw;
			this.rotationPitch = entitylivingbase.rotationPitch * 0.5F;
			this.setRotation(this.rotationYaw, this.rotationPitch);
			this.renderYawOffset = this.rotationYaw;
			this.rotationYawHead = this.renderYawOffset;
			strafe = entitylivingbase.moveStrafing * 0.5F;
			forward = entitylivingbase.moveForward;

			if (forward < 0.0F)
			{
				forward *= 0.5F;
			}
			
			this.jumpMovementFactor = this.getAIMoveSpeed() * 0.33F;

			if (this.canPassengerSteer())
			{
//				if (this.setJump) {
//					this.setJump = false;
//					this.motionY = (double)this.getJumpUpwardsMotion();
//					
//					if (this.isPotionActive(MobEffects.JUMP_BOOST)) {
//						this.motionY += (double)((float)(this.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
//					}
//					
//					this.isAirBorne = true;
//					net.minecraftforge.common.ForgeHooks.onLivingJump(this);
//				}
				
				this.setAIMoveSpeed((float)this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
				super.moveEntityWithHeading(strafe, forward);
			}
			else if (entitylivingbase instanceof EntityPlayer)
			{
				this.motionX = 0.0D;
				this.motionY = 0.0D;
				this.motionZ = 0.0D;
			}

			this.prevLimbSwingAmount = this.limbSwingAmount;
			double d1 = this.posX - this.prevPosX;
			double d0 = this.posZ - this.prevPosZ;
			float f2 = MathHelper.sqrt_double(d1 * d1 + d0 * d0) * 4.0F;

			if (f2 > 1.0F)
			{
				f2 = 1.0F;
			}

			this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
			this.limbSwing += this.limbSwingAmount;
		}
		else
		{
			this.jumpMovementFactor = 0.02F;
			super.moveEntityWithHeading(strafe, forward);
		}
	}
	
	@Override
	public double getMountedYOffset() {
		return this.height * 0.6D;
	}
	
	@Override
	public void dragonJump() {
		if (this.jumpCount < 1 + this.getBonusJumps()) {
			this.jumpCount++;
			this.jump();
		}
	}
	
	@Override
	public void fall(float distance, float damageMulti) {
		super.fall(distance, damageMulti);
		this.jumpCount = 0;
	}
	
	@Override
	protected float getJumpUpwardsMotion() {
		return 0.75f * (1f + this.getJumpHeightBonus());
	}
	
	@Override
	public boolean isFlying() {
		return considerFlying();
	}
	
	private boolean considerFlying() {
		return this.getCanFly() && jumpCount >= 2;
	}

}
