package com.smanzana.nostrummagica.entity;

import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.pet.IPetWithSoul;
import com.smanzana.nostrummagica.pet.PetInfo;
import com.smanzana.nostrummagica.pet.PetInfo.PetAction;
import com.smanzana.nostrummagica.pet.PetInfo.SecondaryFlavor;
import com.smanzana.nostrummagica.serializers.ArcaneWolfElementalTypeSerializer;
import com.smanzana.nostrummagica.serializers.MagicElementDataSerializer;
import com.smanzana.nostrummagica.serializers.PetJobSerializer;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class EntityArcaneWolf extends EntityWolf implements IEntityTameable, IEntityPet, IPetWithSoul, IStabbableEntity {
	
	public static enum ArcaneWolfElementalType {
		NONELEMENTAL("nonelemental", null),
		FIRE_ONLY("fire", EMagicElement.FIRE),
		ICE_ONLY("ice", EMagicElement.ICE),
		WIND_ONLY("wind", EMagicElement.WIND),
		EARTH_ONLY("earth", EMagicElement.EARTH),
		ENDER_ONLY("ender", EMagicElement.ENDER),
		LIGHTNING_ONLY("lightning", EMagicElement.LIGHTNING),
		
		// Composites
		BARRIER("barrier", EMagicElement.EARTH, EMagicElement.ICE), // Earth + Ice
		STORM("storm", EMagicElement.WIND, EMagicElement.LIGHTNING), // Wind + Lightning
		ELDRICH("eldrich", EMagicElement.ENDER, EMagicElement.FIRE), // Ender + Fire
		MYSTIC("mystic", EMagicElement.ICE, EMagicElement.ENDER), // Ice + Ender
		NATURE("nature", EMagicElement.LIGHTNING, EMagicElement.EARTH), // Lightning + Earth
		HELL("hell", EMagicElement.FIRE, EMagicElement.WIND); // Fire + Wind
		
		private final String key;
		private final @Nullable EMagicElement primary; // Only nonelemental has null though so we pretendit's not nullable
		private final @Nullable EMagicElement secondary;
		
		private ArcaneWolfElementalType(String key, EMagicElement primary) {
			this(key, primary, null);
		}
		
		private ArcaneWolfElementalType(String key, EMagicElement primary, @Nullable EMagicElement secondary) {
			this.primary = primary;
			this.secondary = secondary;
			this.key = key;
		}
		
		public static @Nullable ArcaneWolfElementalType Match(EMagicElement first, EMagicElement second) {
			for (ArcaneWolfElementalType type: values()) {
				if (type.primary == first && type.secondary == second) {
					return type;
				}
				if (type.secondary == first && type.primary == second) {
					return type;
				}
			}
			return null;
		}
		
		public static ArcaneWolfElementalType Match(EMagicElement element) {
			return Match(element, null);
		}
		
		public @Nullable EMagicElement getPrimary() {
			return this.primary;
		}
		
		public @Nullable EMagicElement getSecondary() {
			return this.secondary;
		}
		
		public String getNameKey() {
			return key;
		}
	}
	
	protected static final DataParameter<Boolean> SOULBOUND = EntityDataManager.<Boolean>createKey(EntityArcaneWolf.class, DataSerializers.BOOLEAN);
	
	protected static final DataParameter<Integer> ATTRIBUTE_XP  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    protected static final DataParameter<Integer> ATTRIBUTE_LEVEL  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    protected static final DataParameter<Float> ATTRIBUTE_BOND  = EntityDataManager.<Float>createKey(EntityArcaneWolf.class, DataSerializers.FLOAT);
    
    protected static final DataParameter<Float> SYNCED_MAX_HEALTH  = EntityDataManager.<Float>createKey(EntityArcaneWolf.class, DataSerializers.FLOAT);
    protected static final DataParameter<Integer> MANA  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    protected static final DataParameter<Integer> MAX_MANA  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    protected static final DataParameter<PetAction> DATA_PET_ACTION = EntityDataManager.<PetAction>createKey(EntityArcaneWolf.class, PetJobSerializer.instance);
    protected static final DataParameter<Integer> RUNE_COLOR = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    
    protected static final DataParameter<ArcaneWolfElementalType> ELEMENTAL_TYPE = EntityDataManager.<ArcaneWolfElementalType>createKey(EntityArcaneWolf.class, ArcaneWolfElementalTypeSerializer.instance);
    protected static final DataParameter<EMagicElement> TRAINING_ELEMENT = EntityDataManager.<EMagicElement>createKey(EntityArcaneWolf.class, MagicElementDataSerializer.instance);
    protected static final DataParameter<Integer> TRAINING_XP  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    
    private static final String NBT_SOUL_BOUND = "SoulBound";
    private static final String NBT_ATTR_XP = "AttrXP";
    private static final String NBT_ATTR_LEVEL = "AttrLevel";
    private static final String NBT_ATTR_BOND = "AttrBond";
    private static final String NBT_MANA = "Mana";
    private static final String NBT_MAX_MANA = "MaxMana";
    private static final String NBT_SOUL_ID = "SoulID";
    private static final String NBT_SOUL_WORLDID = "SoulWorldID";
    private static final String NBT_RUNE_COLOR = "RuneColor";
    private static final String NBT_TRAINING_ELEMENT = "TrainingElement";
    private static final String NBT_TRAINING_XP = "TrainingElement";
    private static final String NBT_ELEMENTAL_TYPE = "ElementType";
    
    private static final float ARCANE_WOLF_WARN_HEALTH = 10.0f;
    private static final float BOND_LEVEL_ALLOW_RIDE = .75f;
    
    private UUID soulID;
    private UUID worldID;
    private int jumpCount;
    
	public EntityArcaneWolf(World worldIn) {
		super(worldIn);
		this.setSize(0.7F, 0.95F);
        
        soulID = UUID.randomUUID();
        worldID = null;
        jumpCount = 0;
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(SOULBOUND, false);
		dataManager.register(ATTRIBUTE_XP, 0);
		dataManager.register(ATTRIBUTE_LEVEL, 0);
		dataManager.register(ATTRIBUTE_BOND, 0f);
		dataManager.register(SYNCED_MAX_HEALTH, 50f);
		dataManager.register(DATA_PET_ACTION, PetAction.WAITING);
		dataManager.register(MANA, 0);
		dataManager.register(MAX_MANA, 1);
		dataManager.register(RUNE_COLOR, 0xFFFF00FF);
		dataManager.register(ELEMENTAL_TYPE, ArcaneWolfElementalType.NONELEMENTAL);
		dataManager.register(TRAINING_ELEMENT, EMagicElement.PHYSICAL);
		dataManager.register(TRAINING_XP, 0);
	}
		
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(10.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(60.0);
		this.getEntityAttribute(AttributeMagicResist.instance()).setBaseValue(20.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
	}
	
	@Override
	public void setTamed(boolean tamed) {
		// Parent resets max health. Reset after that
		super.setTamed(tamed);
		setMaxHealth(dataManager.get(SYNCED_MAX_HEALTH));
	}
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (key == SYNCED_MAX_HEALTH) {
			setMaxHealth(this.dataManager.get(SYNCED_MAX_HEALTH).floatValue());
		}
	}
	
	@Override
	protected void initEntityAI() {
		super.initEntityAI();
	}
	
	@Override
	public void onEntityUpdate() {
		super.onEntityUpdate();
		
		if (world != null && !world.isRemote) {
			if (!this.isSitting()) {
				if (this.getAttackTarget() == null) {
					setPetAction(PetAction.WAITING);
				} else {
					setPetAction(PetAction.ATTACKING);
				}
			}
		}
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
	}
	
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		
	}
	
	@Override
	public void travel(float strafe, float vertical, float forward) {
		//super.travel(strafe, vertical, forward);
		
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
			strafe = entitylivingbase.moveStrafing * 0.45F;
			forward = entitylivingbase.moveForward * .7f;

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
				super.travel(strafe, vertical, forward);
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
			float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;

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
			super.travel(strafe, vertical, forward);
		}
	}
	
	@Override
	public boolean isBreedingItem(@Nonnull ItemStack stack) {
		return false;
	}
	
	public boolean isHungerItem(@Nonnull ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		
		Item item = stack.getItem();
		return (
				item == Items.BONE
			||	item == Items.CHICKEN
			||	item == Items.MUTTON
				);
	}
	
	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		// Shift-right click toggles sitting.
		// When not sitting, right-click mounts the wolf
		// When sitting, will eventually open a GUI
		final @Nonnull ItemStack stack = player.getHeldItem(hand);
		if (this.isTamed() && player == this.getOwner()) {
			if (player.isSneaking()) {
				if (!this.world.isRemote) {
					this.setSitting(!this.isSitting());
					if (player.isCreative()) {
						this.setBond(1f);
					}
				}
				return true;
			} else if (this.getHealth() < this.getMaxHealth() && isHungerItem(stack)) {
				if (!this.world.isRemote) {
					this.heal(5f);
					this.addBond(.2f);
					
					if (!player.isCreative()) {
						player.getHeldItem(hand).shrink(1);
					}
				}
				return true;
			} else if (this.isSitting() && stack.isEmpty()) {
				if (!this.world.isRemote) {
					//player.openGui(NostrumMagica.instance, NostrumGui.dragonID, this.world, (int) this.posX, (int) this.posY, (int) this.posZ);
					//NostrumMagica.proxy.openDragonGUI(player, this);
					// TODO
					int unused;
				}
				return true;
//			} else if (isBreedingItem(stack) && this.getBond() > BOND_LEVEL_BREED && this.getEgg() == null) {
//				if (!this.world.isRemote) {
//					layEgg();
//					if (!player.isCreative()) {
//						stack.shrink(1);
//					}
//				}
//				return true;
			} else if (stack.isEmpty()) {
				if (!this.world.isRemote) {
					if (this.getBond() >= BOND_LEVEL_ALLOW_RIDE) {
						if (this.getHealth() < ARCANE_WOLF_WARN_HEALTH) {
							player.sendMessage(new TextComponentTranslation("info.tamed_arcane_wolf.low_health", this.getName()));
						} else {
							player.startRiding(this);
						}
					} else {
						player.sendMessage(new TextComponentTranslation("info.tamed_arcane_wolf.no_ride", this.getName()));
					}
				}
				return true;
			}
			else {
				; // fall through; we didn't handle it
			}
		} else if (!this.isTamed() && player.isCreative() && hand == EnumHand.MAIN_HAND && player.isSneaking()) {
			if (!world.isRemote) {
				this.setTamedBy(player);
			}
			return true;
		} else {
			// Someone other than the owner clicked
			if (!this.world.isRemote) {
				player.sendMessage(new TextComponentTranslation("info.tamed_arcane_wolf.not_yours", this.getName()));
			}
			return true;
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
	public double getMountedYOffset() {
		// Dragons go from 60% to 100% height.
		// This is synced with the rendering code.
		return (this.height * 0.6D) - ((0.4f * this.height) * (1f-getGrowingAge()));
	}
	
	@Override
	protected float getJumpUpwardsMotion() {
		return super.getJumpUpwardsMotion();
	}
	
	@Override
	protected void jump() {
		super.jump();
	}
	
	@Override
	 public boolean canBeLeashedTo(EntityPlayer player) {
		return !isSitting() && player == getOwner();
	}
	
	public boolean isSoulBound() {
		return this.dataManager.get(SOULBOUND);
	}
	
	public void setSoulBound(boolean soulBound) {
		this.dataManager.set(SOULBOUND, soulBound);
	}
	
	@Override
	public void setSitting(boolean sitting) {
		super.setSitting(sitting);
		this.aiSit.setSitting(sitting); // Idk why this isn't in the base class
		setPetAction(PetAction.SITTING);
	}
	
	public int getLevel() {
		return this.dataManager.get(ATTRIBUTE_LEVEL);
	}
	
	protected void setLevel(int level) {
		this.dataManager.set(ATTRIBUTE_LEVEL, Math.max(1, level));
	}
	
	protected int getMaxXP(int level) {
		return (int) (100D * Math.pow(1.5, level));
	}

	public int getXP() {
		return this.dataManager.get(ATTRIBUTE_XP);
	}

	public int getMaxXP() {
		return this.getMaxXP(getLevel());
	}

	public int getMana() {
		return this.dataManager.get(MANA);
	}
	
	protected void setMana(int mana) {
		this.dataManager.set(MANA, Math.max(0, Math.min(mana, this.getMaxMana())));
	}

	public int getMaxMana() {
		return dataManager.get(MAX_MANA);
	}
	
	protected void setMaxMana(int maxMana) {
		this.dataManager.set(MAX_MANA, Math.max(0, maxMana));
	}

	public float getBond() {
		if (this.isSoulBound()) {
			return 1f;
		}
		return this.dataManager.get(ATTRIBUTE_BOND);
	}
	
	protected void setBond(float bond) {
		this.dataManager.set(ATTRIBUTE_BOND, bond);
	}
	
	/**
	 * Has a chance of adding some amount of bonding to the dragon.
	 * Change is random and not in your control.
	 * Rate influences how much is added. 1f is a normal kind-deed's amount. 2f is double that.
	 * @param rate
	 */
	public void addBond(float rate) {
		if (getRNG().nextBoolean() && getRNG().nextBoolean()) {
			float amt = 0.025f;
			float current = getBond();
			float mod = rate * (1f + (getRNG().nextFloat() - 0.5f) * .5f); // 100% +- 25% of rate
			
			amt *= mod;
			
			amt = (current + amt);
			amt = Math.max(0f, Math.min(1f, amt));
			
			setBond(amt);
		}
	}
	
	public void removeBond(float rate) {
		float amt = -0.01f;
		float current = getBond();
		float mod = rate * (1f + (getRNG().nextFloat() - 0.5f) * .5f); // 100% +- 25% of rate
		
		amt *= mod;
		
		amt = (current + amt);
		amt = Math.max(0f, Math.min(1f, amt));
		
		setBond(amt);
	}
	
	/**
	 * Adds some amount of XP to the dragon.
	 * Note the xp curve before writing an amount. >.<
	 * @param rate
	 */
	public void addXP(int xp) {
		int newXP = this.getXP() + xp;
		if (newXP < 0) {
			newXP = 0;
		}
		
		while (newXP > this.getMaxXP()) {
			newXP -= this.getMaxXP();
			this.levelup();
		}
		
		this.setXP(newXP);
	}
	
	/**
	 * Hard set current xp. Does not do level-up mechanics.
	 * @param xp
	 */
	protected void setXP(int xp) {
		this.dataManager.set(ATTRIBUTE_XP, xp);
	}
	
	public void addMana(int mana) {
		this.setMana(mana + this.getMana());
	}
	
	protected void setMaxHealth(float maxHealth) {
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);
	}
	
	public int getRuneColor() {
		return dataManager.get(RUNE_COLOR);
	}
	
	public void setRuneColor(int ARGB) {
		dataManager.set(RUNE_COLOR, ARGB);
	}
	
	public ArcaneWolfElementalType getElementalType() {
		return dataManager.get(ELEMENTAL_TYPE);
	}
	
	protected void setElementalType(ArcaneWolfElementalType type) {
		this.dataManager.set(ELEMENTAL_TYPE, type);
	}
	
	public int getTrainingXP() {
		return dataManager.get(TRAINING_XP);
	}
	
	protected void setTrainingXP(int xp) {
		dataManager.set(TRAINING_XP, xp);
	}
	
	public void addTrainingXP(int xp) {
		if (this.getTrainingElement() == null) {
			return;
		}
		
		xp += this.getTrainingXP();
		if (xp >= getMaxTrainingXP()) {
			xp = 0;
			this.finishTrainingElement(this.getTrainingElement());
		}
		this.setTrainingXP(xp);
	}
	
	public int getMaxTrainingXP() {
		final ArcaneWolfElementalType type = this.getElementalType();
		final int maxXP;
		switch (type) {
		case BARRIER:
		case ELDRICH:
		case HELL:
		case MYSTIC:
		case NATURE:
		case STORM:
			maxXP = 1000;
			break;
		case EARTH_ONLY:
		case ENDER_ONLY:
		case FIRE_ONLY:
		case ICE_ONLY:
		case LIGHTNING_ONLY:
		case WIND_ONLY:
			maxXP = 500;
			break;
		case NONELEMENTAL:
		default:
			maxXP = 250;
			break;
		}
		return maxXP;
	}
	
	public @Nullable EMagicElement getTrainingElement() {
		final EMagicElement elem = dataManager.get(TRAINING_ELEMENT);
		if (elem == EMagicElement.PHYSICAL) {
			return null;
		}
		return elem;
	}
	
	public void setTrainingElement(@Nullable EMagicElement element) {
		if (element == null) {
			element = EMagicElement.PHYSICAL;
		}
		dataManager.set(TRAINING_ELEMENT, element);
	}
	
	public void setPetAction(PetAction action) {
		dataManager.set(DATA_PET_ACTION, action);
	}

	public PetAction getPetAction() {
		return dataManager.get(DATA_PET_ACTION);
	}
	
	@Override
	public PetInfo getPetSummary() {
		return PetInfo.claim(getHealth(), getMaxHealth(), getXP(), getMaxXP(), SecondaryFlavor.PROGRESS, getPetAction());
	}
	
	public int getBonusJumps() {
		int unused;
		return 0; // TODO
	}
	
	public void wolfJump() {
		if (this.jumpCount == 0 && !this.onGround) {
			// Lose first jump if you didn't jump from the ground
			jumpCount = 1;
		}
		
		if (this.jumpCount < 1 + this.getBonusJumps()) {
			this.jumpCount++;
			this.jump();
//			if (this.considerFlying()) {
//				this.setFlyState(FlyState.FLYING);
//			}
		}
	}
	
	@Override
	public void fall(float distance, float damageMulti) {
		super.fall(distance, damageMulti);
		this.jumpCount = 0;
	}
	
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		
		compound.setBoolean(NBT_SOUL_BOUND, this.isSoulBound());
		compound.setInteger(NBT_ATTR_XP, this.getXP());
		compound.setInteger(NBT_ATTR_LEVEL, this.getLevel());
		compound.setFloat(NBT_ATTR_BOND, this.getBond());
		// Ignore max health; already saved
		compound.setInteger(NBT_MANA, this.getMana());
		compound.setInteger(NBT_MAX_MANA, this.getMaxMana());
		compound.setUniqueId(NBT_SOUL_ID, soulID);
		if (worldID != null) {
			compound.setUniqueId(NBT_SOUL_WORLDID, worldID);
		}
		compound.setInteger(NBT_RUNE_COLOR, this.getRuneColor());
		if (this.getTrainingElement() != null) {
			compound.setString(NBT_TRAINING_ELEMENT, this.getTrainingElement().name());
		}
		compound.setInteger(NBT_TRAINING_XP, this.getTrainingXP());
		compound.setString(NBT_ELEMENTAL_TYPE, this.getElementalType().name());
		
		// TODO inventory...
		int unused;
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		
		this.setSoulBound(compound.getBoolean(NBT_SOUL_BOUND));
		this.setXP(compound.getInteger(NBT_ATTR_XP));
		this.setLevel(compound.getInteger(NBT_ATTR_LEVEL));
		this.setBond(compound.getFloat(NBT_ATTR_BOND));
		// Continue ignoring max health
		this.setMaxMana(compound.getInteger(NBT_MAX_MANA)); // before setting mana
		this.setMana(compound.getInteger(NBT_MANA));
		
		// Summon command passes empty NBT to parse. Don't overwrite random UUID if not present.
		if (compound.hasUniqueId(NBT_SOUL_ID)) {
			this.soulID = compound.getUniqueId(NBT_SOUL_ID);
		}
		
		if (compound.hasUniqueId(NBT_SOUL_WORLDID)) {
			this.worldID = compound.getUniqueId(NBT_SOUL_WORLDID);
		} else {
			this.worldID = null;
		}
		
		this.setRuneColor(compound.getInteger(NBT_RUNE_COLOR));
		this.setTrainingElement(null);
		if (compound.hasKey(NBT_TRAINING_ELEMENT)) {
			try {
				this.setTrainingElement(EMagicElement.valueOf(compound.getString(NBT_TRAINING_ELEMENT).toUpperCase()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.setTrainingXP(compound.getInteger(NBT_TRAINING_XP));
		try {
			this.setElementalType(ArcaneWolfElementalType.valueOf(compound.getString(NBT_ELEMENTAL_TYPE).toUpperCase()));
		} catch (Exception e) {
			e.printStackTrace();
			this.setElementalType(ArcaneWolfElementalType.NONELEMENTAL);
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
	
	protected void dropInventory() {
		;
	}
	
	@Override
	public void onDeath(DamageSource cause) {
		dropInventory();

		super.onDeath(cause);
	}
	
	@Override
	protected boolean canDespawn() {
		return false;
	}
	
	@Override
	public boolean hitByEntity(Entity entityIn) {
		if (this.isRidingOrBeingRiddenBy(entityIn)) {
			return true;
		}
		
		return super.hitByEntity(entityIn);
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		boolean hurt = super.attackEntityFrom(source, amount);
		
		if (hurt && source.getTrueSource() != null) {
			if (this.isRidingOrBeingRiddenBy(source.getTrueSource())) {
				hurt = false;
			}
		}
		
		if (hurt && this.isTamed()) {
			EntityLivingBase owner = this.getOwner();
			float health = this.getHealth();
			if (health > 0f && health < ARCANE_WOLF_WARN_HEALTH) {
				if (owner != null && owner instanceof EntityPlayer) {
					((EntityPlayer) this.getOwner()).sendMessage(new TextComponentTranslation("info.tamed_arcane_wolf.hurt", this.getName()));
				}
				this.dismountRidingEntity();
			} else if (health > 0f) {
				if (source.getTrueSource() == owner) {
					// Hurt by the owner!
					if (this.getRNG().nextBoolean()) {
						// Remove bond!
						this.removeBond(0.75f);
					}
				}
			}
		}
		
		return hurt;
	}
	
	@Override
	public UUID getPetSoulID() {
		return soulID;
	}

	@Override
	public UUID getWorldID() {
		return worldID;
	}

	public void setWorldID(UUID worldID) {
		this.worldID = worldID;
	}
	
	@Override
	public boolean onSoulStab(EntityLivingBase stabber, ItemStack stabbingItem) {
		
		if (this.isOwner(stabber) && !isSoulBound() && this.getBond() >= 1f) {
			// Die and scream and drop a soul ember
			this.setSoulBound(true);
			
			// Drop inventory before snapshotting
			dropInventory();
			
//			final ItemStack stack = DragonSoulItem.MakeSoulItem(this, true);
//			if (!stack.isEmpty()) {
//				this.entityDropItem(stack, 1f);
//				this.attackEntityFrom(DamageSource.GENERIC, 1000000f);
//			}
//			
//			// Award lore about soul bonding
//			INostrumMagic attr = NostrumMagica.getMagicWrapper(stabber);
//			if (attr != null) {
//				attr.giveBasicLore(SoulBoundDragonLore.instance);
//			}
			
			return true;
		}
		return false;
	}
	
	public void levelup() {
		int level = this.getLevel();
		
		// 
		Random rand = getRNG();
		
		float health = 0;
		int mana = 0;
		
//		// Jump height:
//		float jumpHeight = 0f;
//		if (rand.nextBoolean()) {
//			jumpHeight = rand.nextFloat() * 0.05f; // 0-5%
//		}
//		
//		// Speed:
//		float speed = 0f;
//		if (rand.nextBoolean() && rand.nextBoolean()) {
//			speed = rand.nextFloat() * 0.05f; // 0-5%
//		}
//		
//		// Magic Memory:
//		int memory = 0;
//		if (this.getCanUseMagic() && rand.nextBoolean()) {
//			memory = rand.nextInt(2);
//		}
//		
//		// Health
//		// 50% 0, 37.5% 10, 12.5% 20
//		float health = (float) (rand.nextInt(2) * 10);
//		if (health > 0 && rand.nextBoolean() && rand.nextBoolean()) {
//			health += 10.0f;
//		}
//		
//		// Mana + regen
//		int mana = 0;
//		float regen = 0f;
//		if (this.getDragonMana() > 0) {
//			if (rand.nextBoolean()) {
//				mana = (rand.nextInt(30) + 1) * 10;
//			}
//			
//			if (rand.nextBoolean() && rand.nextBoolean()) {
//				regen = rand.nextFloat() * .2f;
//			}
//		}
		
		health += this.getMaxHealth();
		mana += this.getMana();
		
		this.setMaxHealth(health);
		this.setHealth(this.getMaxHealth());
		this.setMaxMana(mana);
		this.setMana(mana);
		
//		this.setStats(this.getCanFly(), this.getBonusJumps(), this.getJumpHeightBonus() + jumpHeight, this.getSpeedBonus() + speed,
//				health, health, mana, mana, regen + this.getManaRegen(), this.getCanUseMagic(), this.getMagicMemorySize() + memory, this.getXP(), level + 1, this.getBond());
		//canFly, jumps, jumpboost, speed, this.getMaxHealth(), this.getHealth(), maxmana, mana, canCast, magicMemory, xp, level, bond
		
		EntityLivingBase owner = this.getOwner();
		if (owner != null) {
			this.playSound(SoundEvents.ENTITY_WOLF_AMBIENT, 1f, 1f);
			owner.sendMessage(new TextComponentString(this.getName() + " leveled up!"));
		}
	}
	
	/**
	 * Checks whether this wolf can start training the provided element.
	 * Returns false if an element is already training, pup is maxed out on elements, or elements are
	 * incompatible.
	 * @param element
	 * @return
	 */
	public boolean canTrainElement(EMagicElement element) {
		// Already training?
		if (this.getTrainingElement() != null) {
			return false;
		}
		
		// Already full secondary type?
		final ArcaneWolfElementalType currentType = this.getElementalType();
		if (currentType.getSecondary() != null) {
			return false;
		}
		
		// Primary incompatible with new element?
		if (currentType.getPrimary() != null
				&& ArcaneWolfElementalType.Match(currentType.getPrimary(), element) == null) {
			return false;
		}
		
		return true;
	}
	
	protected void playTrainingFinishEffects() {
		int unused;
	}
	
	protected void finishTrainingElement(EMagicElement element) {
		if (element == null || element == EMagicElement.PHYSICAL) {
			return;
		}
		
		final ArcaneWolfElementalType currentType = this.getElementalType();
		final @Nullable ArcaneWolfElementalType result;
		if (currentType.getPrimary() != null) {
			if (currentType.secondary != null) {
				result = null;
			} else {
				result = ArcaneWolfElementalType.Match(currentType.primary, element);
			}
		} else {
			result = ArcaneWolfElementalType.Match(element);
		}
		
		if (result != null) {
			this.setElementalType(result);
			playTrainingFinishEffects();
		}
	}
	
}
