package com.smanzana.nostrummagica.entity.dragon;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.dragongui.RedDragonBondInfoSheet;
import com.smanzana.nostrummagica.client.gui.dragongui.RedDragonInfoSheet;
import com.smanzana.nostrummagica.client.gui.dragongui.RedDragonInventorySheet;
import com.smanzana.nostrummagica.client.gui.dragongui.RedDragonSpellSheet;
import com.smanzana.nostrummagica.client.gui.dragongui.TamedDragonGUI.DragonContainer;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.IEntityTameable;
import com.smanzana.nostrummagica.entity.dragon.IDragonSpawnData.IDragonSpawnFactory;
import com.smanzana.nostrummagica.entity.tasks.DragonAINearestAttackableTarget;
import com.smanzana.nostrummagica.entity.tasks.DragonGambittedSpellAttackTask;
import com.smanzana.nostrummagica.entity.tasks.DragonMeleeAttackTask;
import com.smanzana.nostrummagica.entity.tasks.EntityAIFollowEntityGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAIFollowOwnerGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAIOwnerHurtByTargetGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAIOwnerHurtTargetGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAIPanicGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAISitGeneric;
import com.smanzana.nostrummagica.items.NostrumRoseItem;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityTameDragonRed extends EntityDragonRedBase implements IEntityTameable, ITameDragon {

	protected static final DataParameter<Boolean> TAMED = EntityDataManager.<Boolean>createKey(EntityTameDragonRed.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.<Optional<UUID>>createKey(EntityTameDragonRed.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected static final DataParameter<Boolean> SITTING = EntityDataManager.<Boolean>createKey(EntityTameDragonRed.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Float> AGE = EntityDataManager.<Float>createKey(EntityTameDragonRed.class, DataSerializers.FLOAT);
    protected static final DataParameter<Optional<UUID>> EGG_UNIQUE_ID = EntityDataManager.<Optional<UUID>>createKey(EntityTameDragonRed.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    
    protected static final DataParameter<Boolean> CAPABILITY_FLY = EntityDataManager.<Boolean>createKey(EntityTameDragonRed.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Byte> CAPABILITY_JUMP = EntityDataManager.<Byte>createKey(EntityTameDragonRed.class, DataSerializers.BYTE);
    protected static final DataParameter<Float> CAPABILITY_JUMP_HEIGHT = EntityDataManager.<Float>createKey(EntityTameDragonRed.class, DataSerializers.FLOAT);
    protected static final DataParameter<Float> CAPABILITY_SPEED = EntityDataManager.<Float>createKey(EntityTameDragonRed.class, DataSerializers.FLOAT);
    protected static final DataParameter<Integer> CAPABILITY_MAXMANA = EntityDataManager.<Integer>createKey(EntityTameDragonRed.class, DataSerializers.VARINT);
    protected static final DataParameter<Integer> CAPABILITY_MANA = EntityDataManager.<Integer>createKey(EntityTameDragonRed.class, DataSerializers.VARINT);
    protected static final DataParameter<Float> CAPABILITY_MANA_REGEN  = EntityDataManager.<Float>createKey(EntityTameDragonRed.class, DataSerializers.FLOAT);
    protected static final DataParameter<Boolean> CAPABILITY_MAGIC = EntityDataManager.<Boolean>createKey(EntityTameDragonRed.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Integer> CAPABILITY_MAGIC_SIZE  = EntityDataManager.<Integer>createKey(EntityTameDragonRed.class, DataSerializers.VARINT);
    
    
    protected static final DataParameter<Integer> ATTRIBUTE_XP  = EntityDataManager.<Integer>createKey(EntityTameDragonRed.class, DataSerializers.VARINT);
    protected static final DataParameter<Integer> ATTRIBUTE_LEVEL  = EntityDataManager.<Integer>createKey(EntityTameDragonRed.class, DataSerializers.VARINT);
    protected static final DataParameter<Float> ATTRIBUTE_BOND  = EntityDataManager.<Float>createKey(EntityTameDragonRed.class, DataSerializers.FLOAT);
    
    protected static final DataParameter<Float> SYNCED_MAX_HEALTH  = EntityDataManager.<Float>createKey(EntityTameDragonRed.class, DataSerializers.FLOAT);
    
    protected static final String NBT_TAMED = "Tamed";
    protected static final String NBT_OWNER_ID = "OwnerUUID";
    protected static final String NBT_SITTING = "Sitting";
    protected static final String NBT_AGE = "Age";
    protected static final String NBT_EGG_ID = "EggID";
    protected static final String NBT_CAP_FLY = "CapableFly";
    protected static final String NBT_CAP_JUMP = "CapableJump";
    protected static final String NBT_CAP_JUMPBOOST = "CapableJumpBonus";
    protected static final String NBT_CAP_SPEED = "CapableSpeedBonus";
    protected static final String NBT_CAP_MANA = "CapableMana";
    protected static final String NBT_CAP_MANAREGEN = "CapableManaRegen";
    protected static final String NBT_CAP_MAXMANA = "CapableMaxMana";
    protected static final String NBT_CAP_MAGIC = "CapableMagic";
    protected static final String NBT_CAP_MAGIC_SIZE = "CapableMagicSize";
    protected static final String NBT_ATTR_XP = "AttrXP";
    protected static final String NBT_ATTR_LEVEL = "AttrLevel";
    protected static final String NBT_ATTR_BOND = "AttrBond";
    protected static final String NBT_INVENTORY = "DRInventory";
    protected static final String NBT_SPELL_INVENTORY = "DRSpellInventory";
    
    public static final float BOND_LEVEL_FOLLOW = 0.05f;
    public static final float BOND_LEVEL_PLAYERS = 0.15f;
    public static final float BOND_LEVEL_CHEST = 0.20f;
    public static final float BOND_LEVEL_ALLOW_RIDE = 0.50f;
    public static final float BOND_LEVEL_MAGIC = 0.60f;
    public static final float BOND_LEVEL_MANA = 0.95f;
    public static final float BOND_LEVEL_BREED = 0.999f;
    
    private static final float DRAGON_MIN_HEALTH = 10.0f;
    private static final int DRAGON_INV_SIZE = 27;
    
    public static void init() {
    	IDragonSpawnData.register(EntityTameDragonRed.RedDragonSpawnData.SPAWN_KEY, new IDragonSpawnFactory() {
			@Override
			public IDragonSpawnData<?> create(NBTTagCompound nbt) {
				return EntityTameDragonRed.RedDragonSpawnData.fromNBT(nbt);
			}
		});
    }
    
    // AI tasks to swap when tamed
    private DragonAINearestAttackableTarget<EntityPlayer> aiPlayerTarget;
    private EntityAIHurtByTarget aiRevengeTarget;
    
    private IInventory inventory;
    private RedDragonSpellInventory spellInventory;
    
    // Internal timers for controlling while riding
    private int jumpCount; // How many times we've jumped
    
	public EntityTameDragonRed(World worldIn) {
		super(worldIn);
		
		this.setSize(6F * .4F, 4.6F * .6F);
        this.stepHeight = 2;
        this.isImmuneToFire = true;
        
        this.inventory = new InventoryBasic("Dragon Inventory", true, DRAGON_INV_SIZE);
        this.spellInventory = new RedDragonSpellInventory("Dragon Spell Inventory", true);
	}
	
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(TAMED, false);
		this.dataManager.register(OWNER_UNIQUE_ID, Optional.<UUID>absent());
		this.dataManager.register(SITTING, false);
		this.dataManager.register(AGE, 0f);
		this.dataManager.register(EGG_UNIQUE_ID, Optional.<UUID>absent());
		this.dataManager.register(CAPABILITY_FLY, Boolean.FALSE);
		this.dataManager.register(CAPABILITY_JUMP, (byte) 0);
		this.dataManager.register(CAPABILITY_JUMP_HEIGHT, 0f);
		this.dataManager.register(CAPABILITY_SPEED, 0f);
		this.dataManager.register(CAPABILITY_MANA, 0);
		this.dataManager.register(CAPABILITY_MANA_REGEN, 1.0f);
		this.dataManager.register(CAPABILITY_MAXMANA, 0);
		this.dataManager.register(CAPABILITY_MAGIC, Boolean.FALSE);
		this.dataManager.register(CAPABILITY_MAGIC_SIZE, 0);
		this.dataManager.register(ATTRIBUTE_XP, 0);
		this.dataManager.register(ATTRIBUTE_LEVEL, 0);
		this.dataManager.register(ATTRIBUTE_BOND, 0f);
		this.dataManager.register(SYNCED_MAX_HEALTH, 100.0f);
		
		final EntityTameDragonRed dragon = this;
		aiPlayerTarget = new DragonAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true, new Predicate<EntityPlayer>() {
			@Override
			public boolean apply(EntityPlayer input) {
				float bond = dragon.isTamed() ? dragon.getBond() : 0f;
				
				if (bond > BOND_LEVEL_PLAYERS) {
					// Have a _chance_ of attacking for a while... about 5%
					bond -= BOND_LEVEL_PLAYERS;
					if (bond <= .05f) {
						return (dragon.getRNG().nextFloat() <= bond);
					} else {
						return false;
					}
				}
				
				return true;
			}
		});
		aiRevengeTarget = new EntityAIHurtByTarget(this, false, new Class[0]);
	}
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (key == SYNCED_MAX_HEALTH) {
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(
					this.dataManager.get(SYNCED_MAX_HEALTH).floatValue()
					);
		}
	}
	
	protected void setupTamedAI() {
		//this.targetTasks.removeTask(aiPlayerTarget);
	}
	
	protected void setupBaseAI() {
		int priority = 0;
		this.tasks.addTask(priority++, new EntityAISwimming(this));
		this.tasks.addTask(priority++, new EntityAISitGeneric<EntityTameDragonRed>(this));
		this.tasks.addTask(priority++, new EntityAIPanicGeneric<EntityTameDragonRed>(this, 1.0D, new Predicate<EntityTameDragonRed>() {
			@Override
			public boolean apply(EntityTameDragonRed input) {
				return !input.isTamed() && input.getHealth() <= DRAGON_MIN_HEALTH;
			}
		}));
		// Target gambits
		final EntityTameDragonRed selfDragon = this;
		this.tasks.addTask(priority++, new DragonGambittedSpellAttackTask<EntityTameDragonRed>(this, 20, 4) {

			@Override
			public EntityDragonGambit[] getGambits() {
				if (!selfDragon.isTamed() || !selfDragon.getCanUseMagic()) {
					return null;
				}
				
				Spell[] spells = getSpells(); // To reuse the login in there
				
				return Arrays.copyOf(selfDragon.spellInventory.getTargetGambits(), spells.length);
			}

			@Override
			public Spell[] getSpells() {
				if (!selfDragon.getCanUseMagic()) {
					return null;
				}
				
				ItemStack[] scrolls = selfDragon.spellInventory.getTargetSpells();
				Spell[] spells = new Spell[scrolls.length];
				
				for (int i = 0; i < spells.length; i++) {
					// We odn't check for null here cause we sanitize input on placement
					spells[i] = SpellScroll.getSpell(scrolls[i]);
				}
				
				return spells;
			}

			@Override
			public EntityLivingBase getTarget(EntityTameDragonRed dragon) {
				if (!selfDragon.getCanUseMagic()) {
					return null;
				}
				
				return selfDragon.getAttackTarget();
			}
			
		});
		// Self
		this.tasks.addTask(priority++, new DragonGambittedSpellAttackTask<EntityTameDragonRed>(this, 20, 4) {

			@Override
			public EntityDragonGambit[] getGambits() {
				if (!selfDragon.getCanUseMagic()) {
					return null;
				}
				
				Spell[] spells = getSpells(); // To reuse the login in there
				
				return Arrays.copyOf(selfDragon.spellInventory.getSelfGambits(), spells.length);
			}

			@Override
			public Spell[] getSpells() {
				if (!selfDragon.getCanUseMagic()) {
					return null;
				}
				
				ItemStack[] scrolls = selfDragon.spellInventory.getSelfSpells();
				Spell[] spells = new Spell[scrolls.length];
				
				for (int i = 0; i < spells.length; i++) {
					// We odn't check for null here cause we sanitize input on placement
					spells[i] = SpellScroll.getSpell(scrolls[i]);
				}
				
				return spells;
			}

			@Override
			public EntityLivingBase getTarget(EntityTameDragonRed dragon) {
				if (!selfDragon.getCanUseMagic()) {
					return null;
				}
				
				return selfDragon;
			}
			
		});
		// Ally
		this.tasks.addTask(priority++, new DragonGambittedSpellAttackTask<EntityTameDragonRed>(this, 20, 4) {

			@Override
			public EntityDragonGambit[] getGambits() {
				if (!selfDragon.getCanUseMagic()) {
					return null;
				}
				
				Spell[] spells = getSpells(); // To reuse the login in there
				
				return Arrays.copyOf(selfDragon.spellInventory.getAllyGambits(), spells.length);
			}

			@Override
			public Spell[] getSpells() {
				if (!selfDragon.getCanUseMagic()) {
					return null;
				}
				
				ItemStack[] scrolls = selfDragon.spellInventory.getAllySpells();
				Spell[] spells = new Spell[scrolls.length];
				
				for (int i = 0; i < spells.length; i++) {
					// We odn't check for null here cause we sanitize input on placement
					spells[i] = SpellScroll.getSpell(scrolls[i]);
				}
				
				return spells;
			}

			@Override
			public EntityLivingBase getTarget(EntityTameDragonRed dragon) {
				if (!selfDragon.getCanUseMagic()) {
					return null;
				}
				
				if (selfDragon.isTamed()) {
					EntityLivingBase owner = selfDragon.getOwner();
					if (owner != null) {
						List<EntityLivingBase> nearby = owner.worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
								new AxisAlignedBB(owner.posX - 8, owner.posY - 5, owner.posZ - 8, owner.posX + 8, owner.posY + 5, owner.posZ + 8),
								new Predicate<EntityLivingBase>() {

									@Override
									public boolean apply(EntityLivingBase input) {
										return input != null && (input == owner || input.isOnSameTeam(owner));
									}
							
						});
						
						if (nearby == null || nearby.isEmpty()) {
							return owner;
						}
						
						return nearby.get(getRNG().nextInt(nearby.size()));
					}
				}
				
				return null;
			}
			
		});
		this.tasks.addTask(priority++, new EntityAIFollowEntityGeneric<EntityTameDragonRed>(this, 1.0D, .5f, 1.5f, false) {
			@Override
			protected EntityLivingBase getTarget(EntityTameDragonRed entity) {
				if (selfDragon.isTamed()) {
					return selfDragon.getEgg(); // can be null
				}
				
				return null;
			}
		});
		this.tasks.addTask(priority++, new DragonMeleeAttackTask(this, 1.0D, true, 15.0D));
		this.tasks.addTask(priority++, new EntityAIFollowOwnerGeneric<EntityTameDragonRed>(this, 1.0D, 16.0F, 4.0F, new Predicate<EntityTameDragonRed>() {
			@Override
			public boolean apply(EntityTameDragonRed input) {
				// Don't follow unless we've bonded enough
				return (input.getBond() >= BOND_LEVEL_FOLLOW);
			}
		}));
		this.tasks.addTask(priority++, new EntityAIWander(this, 1.0D, 30));
		
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
	
	public boolean isBreedingItem(@Nullable ItemStack stack) {
		if (stack == null || !this.isTamed())
			return false;
		
		return stack.getItem() instanceof NostrumRoseItem;
	}
	
	public boolean isHungerItem(ItemStack stack) {
		if (stack == null) {
			return false;
		}
		
		Item item = stack.getItem();
		return (
				item == Items.BEEF
			||	item == Items.MUTTON
			||	item == Items.CHICKEN
			||	item == Items.PORKCHOP
				);
	}
	
	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand, @Nullable ItemStack stack) {
		// Shift-right click toggles the dragon sitting.
		// When not sitting, right-click mounts the dragon.
		// When sitting, right-click opens the GUI
		if (this.isTamed() && player == this.getOwner()) {
			if (hand == EnumHand.MAIN_HAND) {
				
				if (player.isSneaking()) {
					if (!this.worldObj.isRemote) {
						this.setSitting(!this.isSitting());
						if (player.isCreative()) {
							this.setBond(1f);
						}
					}
					return true;
				} else if (this.getHealth() < this.getMaxHealth() && isHungerItem(stack)) {
					if (!this.worldObj.isRemote) {
						this.heal(5f);
						this.addBond(.2f);
						
						if (!player.isCreative()) {
							player.getHeldItem(hand).stackSize--;
						}
					}
					return true;
				} else if (this.isSitting() && stack == null) {
					if (!this.worldObj.isRemote) {
						//player.openGui(NostrumMagica.instance, NostrumGui.dragonID, this.worldObj, (int) this.posX, (int) this.posY, (int) this.posZ);
						NostrumMagica.proxy.openDragonGUI(player, this);
					}
					return true;
				} else if (isBreedingItem(stack) && this.getBond() > BOND_LEVEL_BREED && this.getEgg() == null) {
					if (!this.worldObj.isRemote) {
						layEgg();
						if (!player.isCreative()) {
							stack.stackSize--;
						}
					}
					return true;
				} else if (stack == null) {
					if (!this.worldObj.isRemote) {
						if (this.getBond() >= BOND_LEVEL_ALLOW_RIDE) {
							if (this.getHealth() < DRAGON_MIN_HEALTH) {
								player.addChatComponentMessage(new TextComponentTranslation("info.tamed_dragon.low_health", this.getName()));
							} else {
								player.startRiding(this);
							}
						} else {
							player.addChatComponentMessage(new TextComponentTranslation("info.tamed_dragon.no_ride", this.getName()));
						}
					}
					return true;
				}
				else {
					; // fall through; we didn't handle it
				}
				
			}
		} else if (!this.isTamed()) {
			if (hand == EnumHand.MAIN_HAND) {
				if (!this.worldObj.isRemote) {
					this.tame(player, player.isCreative());
				}
				return true;
			}
		} else if (this.isTamed() && player.isCreative() && hand == EnumHand.MAIN_HAND && player.isSneaking()) {
			if (!this.worldObj.isRemote) {
				this.tame(player, true);
				this.setBond(1f);
			}
			return true;
		} else if (this.isTamed() && hand == EnumHand.MAIN_HAND) {
			// Someone other than the owner clicked
			if (!this.worldObj.isRemote) {
				player.addChatComponentMessage(new TextComponentTranslation("info.tamed_dragon.not_yours", this.getName()));
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
	 public boolean canBeLeashedTo(EntityPlayer player) {
		return !isSitting() && player == getOwner();
	}

	@Nullable
	public UUID getOwnerId() {
		return ((Optional<UUID>)this.dataManager.get(OWNER_UNIQUE_ID)).orNull();
	}

	public void setOwnerId(@Nullable UUID p_184754_1_) {
		this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(p_184754_1_));
	}
	
	protected UUID getEggID() {
		return this.dataManager.get(EGG_UNIQUE_ID).orNull();
	}
	
	public void setEggId(UUID id) {
		dataManager.set(EGG_UNIQUE_ID, Optional.fromNullable(id));
	}
	
	public EntityDragonEgg getEgg() {
		UUID id = getEggID();
		if (id != null) {
			for (Entity ent : worldObj.loadedEntityList) {
				if (ent.getUniqueID().equals(id) && ent instanceof EntityDragonEgg) {
					return (EntityDragonEgg) ent;
				}
			}
			
			// Means we didn't find the egg.
			// Either we've left it, so abandon :/  or it hatched or was broken.
			setEggId(null);
			return null;
		}
		
		return null;
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
		compound.setFloat(NBT_AGE, this.getGrowingAge());
		
		UUID eggID = this.getEggID();
		if (eggID != null) {
			compound.setUniqueId(NBT_EGG_ID, eggID);
		}
		
		compound.setBoolean(NBT_CAP_FLY, this.getCanFly());
		compound.setByte(NBT_CAP_JUMP, (byte) this.getBonusJumps());
		compound.setFloat(NBT_CAP_JUMPBOOST, this.getJumpHeightBonus());
		compound.setFloat(NBT_CAP_SPEED, this.getSpeedBonus());
		compound.setInteger(NBT_CAP_MANA, this.getCurrentMana());
		compound.setInteger(NBT_CAP_MAXMANA, this.getDragonMana());
		compound.setFloat(NBT_CAP_MANAREGEN, this.getManaRegen());
		compound.setBoolean(NBT_CAP_MAGIC, this.getCanUseMagic());
		compound.setInteger(NBT_CAP_MAGIC_SIZE, this.getMagicMemorySize());
		compound.setInteger(NBT_ATTR_XP, this.getXP());
		compound.setInteger(NBT_ATTR_LEVEL, this.getLevel());
		compound.setFloat(NBT_ATTR_BOND, this.getBond());
		
		// Write inventory
		{
			NBTTagList invTag = new NBTTagList();
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				NBTTagCompound tag = new NBTTagCompound();
				ItemStack stack = inventory.getStackInSlot(i);
				if (stack != null) {
					stack.writeToNBT(tag);
				}
				
				invTag.appendTag(tag);
			}
			
			compound.setTag(NBT_INVENTORY, invTag);
		}
		
		// Write spell inventory
		{
			compound.setTag(NBT_SPELL_INVENTORY, this.spellInventory.toNBT());
			
//			NBTTagList invTag = new NBTTagList();
//			for (int i = 0; i < spellInventory.getSizeInventory(); i++) {
//				NBTTagCompound tag = new NBTTagCompound();
//				ItemStack stack = spellInventory.getStackInSlot(i);
//				if (stack != null) {
//					stack.writeToNBT(tag);
//				}
//				
//				invTag.appendTag(tag);
//			}
//			
//			compound.setTag(NBT_SPELL_INVENTORY, invTag);
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
		
		this.setSitting(compound.getBoolean(NBT_SITTING));
		this.setGrowingAge(compound.getFloat(NBT_AGE));
		
		this.setEggId(compound.getUniqueId(NBT_EGG_ID));
		
		boolean canFly = compound.getBoolean(NBT_CAP_FLY);
		byte jumps = compound.getByte(NBT_CAP_JUMP);
		float jumpboost = compound.getFloat(NBT_CAP_JUMPBOOST);
		float speed = compound.getFloat(NBT_CAP_SPEED);
		int mana = compound.getInteger(NBT_CAP_MANA);
		int maxmana = compound.getInteger(NBT_CAP_MAXMANA);
		float regen = compound.getFloat(NBT_CAP_MANAREGEN);
		boolean canCast = compound.getBoolean(NBT_CAP_MAGIC);
		int magicMemory = compound.getInteger(NBT_CAP_MAGIC_SIZE);
		int xp = compound.getInteger(NBT_ATTR_XP);
		int level = compound.getInteger(NBT_ATTR_LEVEL);
		float bond = compound.getFloat(NBT_ATTR_BOND);
		
		this.setStats(canFly, jumps, jumpboost, speed, this.getMaxHealth(), this.getHealth(), maxmana, mana, regen, canCast, magicMemory, xp, level, bond);
		
		// Read inventory
		{
			NBTTagList list = compound.getTagList(NBT_INVENTORY, NBT.TAG_COMPOUND);
			this.inventory = new InventoryBasic(this.getName(), true, DRAGON_INV_SIZE);
			
			for (int i = 0; i < DRAGON_INV_SIZE; i++) {
				NBTTagCompound tag = list.getCompoundTagAt(i);
				ItemStack stack = null;
				if (tag != null) {
					stack = ItemStack.loadItemStackFromNBT(tag);
				}
				this.inventory.setInventorySlotContents(i, stack);
			}
		}
		
		// Read spell inventory
		if (canCast) {
			this.spellInventory = RedDragonSpellInventory.fromNBT(compound.getCompoundTag(NBT_SPELL_INVENTORY));
			
//			NBTTagList list = compound.getTagList(NBT_SPELL_INVENTORY, NBT.TAG_COMPOUND);
//			if (list != null) {
//				
//				for (int i = 0; i < spellInventory.getSizeInventory(); i++) {
//					NBTTagCompound tag = list.getCompoundTagAt(i);
//					ItemStack stack = null;
//					if (tag != null) {
//						stack = ItemStack.loadItemStackFromNBT(tag);
//					}
//					this.spellInventory.setInventorySlotContents(i, stack);
//				}
//			}
		} else {
			this.spellInventory = new RedDragonSpellInventory(this.getName() + " Empty Spell Inventory", true);
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
		
		if (force || this.getHealth() < DRAGON_MIN_HEALTH) {
			if (force || this.getRNG().nextInt(10) == 0) {
				player.addChatComponentMessage(new TextComponentTranslation("info.tamed_dragon.wild.tame_success", this.getName()));
				
				this.setTamed(true);
				this.navigator.clearPathEntity();
				this.setAttackTarget(null);
				this.setHealth((float) this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue());
				this.setOwnerId(player.getUniqueID());
				this.setSitting(true);
				
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				if (attr != null) {
					attr.giveFullLore(TameRedDragonLore.instance());
				}
				success = true;
			} else {
				// Failed
				player.addChatComponentMessage(new TextComponentTranslation("info.tamed_dragon.wild.tame_fail", this.getName()));
				this.heal(5.0f);
			}
		} else {
			player.addChatComponentMessage(new TextComponentTranslation("info.tamed_dragon.wild.high_health", this.getName()));
		}

		if (!this.worldObj.isRemote) {
			this.worldObj.setEntityState(this, success ? (byte) 7 : (byte) 6);
		}
		
		if (!success) {
			this.setAttackTarget(player);
		}
		
		playTameEffect(success);
	}
	
	private void playTameEffect(boolean success) {
		
		EnumParticleTypes enumparticletypes = success ? EnumParticleTypes.HEART : EnumParticleTypes.VILLAGER_ANGRY;

		for (int i = 0; i < 15; ++i) {
			double d0 = this.rand.nextGaussian() * 0.02D;
			double d1 = this.rand.nextGaussian() * 0.02D;
			double d2 = this.rand.nextGaussian() * 0.02D;
			this.worldObj.spawnParticle(enumparticletypes, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2, new int[0]);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void handleStatusUpdate(byte id) {
		if (id == 7) {
			this.playTameEffect(true);
		} else if (id == 6) {
			this.playTameEffect(false);
		} else {
			super.handleStatusUpdate(id);
		}
	}
	
	public boolean getCanFly() {
		return this.dataManager.get(CAPABILITY_FLY);
	}
	
	public int getBonusJumps() {
		return (int) this.dataManager.get(CAPABILITY_JUMP);
	}
	
	public float getJumpHeightBonus() {
		return this.dataManager.get(CAPABILITY_JUMP_HEIGHT);
	}
	
	public float getSpeedBonus() {
		return this.dataManager.get(CAPABILITY_SPEED);
	}
	
	public int getDragonMana() {
		return this.dataManager.get(CAPABILITY_MAXMANA);
	}
	
	public int getCurrentMana() {
		return this.dataManager.get(CAPABILITY_MANA);
	}
	
	public float getManaRegen() {
		return this.dataManager.get(CAPABILITY_MANA_REGEN);
	}
	
	public void addManaRegen(float regen) {
		float old = this.getManaRegen();
		this.dataManager.set(CAPABILITY_MANA_REGEN, old + regen);
	}
	
	public boolean getCanUseMagic() {
		return this.dataManager.get(CAPABILITY_MAGIC);
	}
	
	public int getMagicMemorySize() {
		return this.dataManager.get(CAPABILITY_MAGIC_SIZE);
	}
	
	private void setStats(
			boolean canFly,
			int bonusJumps,
			float bonusJumpHeight, // relative. 1 is double the height!
			float bonusSpeed, // ""
			float maxHealth,
			float health,
			int maxMana,
			int mana,
			float regen,
			boolean hasMagic,
			int magicMemory,
			int xp,
			int level,
			float bond
			) {
		
		health = Math.min(health, maxHealth);
		mana = Math.min(mana, maxMana);
		bond = Math.min(1f, Math.max(0f, bond));
		
		this.dataManager.set(CAPABILITY_FLY, canFly);
		this.dataManager.set(CAPABILITY_JUMP, (byte) bonusJumps);
		this.dataManager.set(CAPABILITY_JUMP_HEIGHT, bonusJumpHeight);
		this.dataManager.set(CAPABILITY_SPEED, bonusSpeed);
		this.dataManager.set(CAPABILITY_MAXMANA, maxMana);
		this.dataManager.set(CAPABILITY_MANA, mana);
		this.dataManager.set(CAPABILITY_MANA_REGEN, regen);
		this.dataManager.set(CAPABILITY_MAGIC, hasMagic);
		this.dataManager.set(CAPABILITY_MAGIC_SIZE, magicMemory);
		this.dataManager.set(ATTRIBUTE_XP, xp);
		this.dataManager.set(ATTRIBUTE_LEVEL, level);
		this.dataManager.set(ATTRIBUTE_BOND, bond);
		
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.31D * (1D + (double) bonusSpeed));
		this.dataManager.set(SYNCED_MAX_HEALTH, maxHealth);
		//this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth); Synced thr ough data manager
		this.setHealth(health);
	}
	
	public static RedDragonSpawnData rollRandomStats() {
		Random rand = new Random();
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
		
		// Dragons that can't fly are hardier. It's just evolution!
		float health;
		if (canFly) {
			health = 20 + (10 * rand.nextInt(4));
		} else {
			health = 50 + (10 * rand.nextInt(3));
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
		
		// If we have mana, we have some sort of mana regen
		float regen = 0f;
		if (mana > 0) {
			// Dragons regen kinda slowly. Red dragons have .75 +- .25 regen
			regen = (rand.nextFloat() * .5f) + .5f;
		}
		
		// Baby dragons may or may not have magic.
		// Dragons can't have magic if they don't have mana.
		// If it can fly, it's more likely to be supreme and
		// have magic (40%). Otherwise, 25%;
		boolean hasMagic = false;
		if (mana > 0) {
			if (canFly) {
				hasMagic = rand.nextInt(5) < 2;
			} else {
				hasMagic = rand.nextInt(4)  == 0;
			}
		}
		
		int magicMemory = 0;
		if (hasMagic) {
			magicMemory = rand.nextInt(2) + 1;
		}
		
		//this.setStats(canFly, bonusJumps, bonusJumpHeight, bonusSpeed, health, health, mana, mana, regen, hasMagic, magicMemory, this.getXP(), this.getLevel(), this.getBond());
		return new RedDragonSpawnData(
				canFly,
				bonusJumps,
				bonusJumpHeight,
				bonusSpeed,
				health,
				mana,
				regen,
				hasMagic,
				magicMemory
				);
	}
	
	public RedDragonSpawnData rollInheritedStats() {
		Random random = this.getRNG();
		
		boolean canFly = this.getCanFly();
		if (random.nextInt(5) == 0) {
			canFly = !canFly;
		}
		
		int bonusJumps = this.getBonusJumps();
		if (random.nextBoolean()) {
			bonusJumps += -1 + random.nextInt(3);
			// cap at 5 and min at 0 or 1 depending on if you can fly
			bonusJumps = Math.max(canFly ? 1 : 0, Math.min(5, bonusJumps));
		}
		
		// Cap at -30% to 30%. Jitter by +- 5%
		float bonusJumpHeight = Math.max(-.3f, Math.min(.3f, this.getJumpHeightBonus() + (-.05f + random.nextFloat() * .1f)));
		// Cap at -20% to 20%. Jitter by +- 5%
		float bonusSpeed = Math.max(-.2f, Math.min(.2f, this.getSpeedBonus() + (-.05f + random.nextFloat() * .1f)));
		// Bounded between 10 and 120. Step in either direction
		int health = Math.max(10, Math.min(120, (int) this.getMaxHealth() + 10 * (random.nextBoolean() ? 1 : -1)));
		// Mana, if they have it, bounded between 1000 and 5000.
		// Change (25%) to swap between having some and none.
		// If swapping to have some, regular roll
		//int mana = Math.max(10, Math.min(120, (int) this.getDragonMana() + 10 * (random.nextBoolean() ? 1 : -1)));
		int mana = this.getDragonMana();
		if (mana > 0) {
			if (random.nextBoolean() && random.nextBoolean()) {
				mana = 0;
			} else {
				mana = mana + 100 * (random.nextBoolean() ? 1 : -1);
			}
		} else if (random.nextBoolean() && random.nextBoolean()) {
			mana = 2000 + (100 * rand.nextInt(10)); // Same as generating random
		}
		
		if (mana > 0) {
			mana = Math.max(1000, Math.min(5000, mana));
		}
		
		float regen = 0f;
		if (mana > 0) {
			// Bounded between .5 and 2. Jitter +-.05.
			regen = Math.max(.5f, Math.min(2f, this.getManaRegen() + (-.05f + random.nextFloat() * .1f)));
		}
		
		boolean hasMagic = false;
		if (mana > 0) {
			// If already had magic, keep it.
			// Otherwise, do like random roll chances.
			if (this.getCanUseMagic()) {
				hasMagic = true;
			} else {
				if (canFly) {
					hasMagic = rand.nextInt(5) < 2;
				} else {
					hasMagic = rand.nextInt(4)  == 0;
				}
			}
		}
		
		int magicMemory = 0;
		if (hasMagic) {
			// Always re-roll, but with better odds if parents were magic.
			if (this.getCanUseMagic()) {
				magicMemory = rand.nextInt(3) + 2;
			} else {
				magicMemory = rand.nextInt(2) + 1;
			}
		}
		
		return new RedDragonSpawnData(
				canFly,
				bonusJumps,
				bonusJumpHeight,
				bonusSpeed,
				health,
				mana,
				regen,
				hasMagic,
				magicMemory
				);
	}
	
	public void rollStats() {
		rollRandomStats().apply(this);
	}
	
	public void levelup() {
		int level = this.getLevel();
		
		// Red dragons possible gain jump height, max Health, max Mana, and Speed.
		Random rand = getRNG();
		
		// Jump height:
		float jumpHeight = 0f;
		if (rand.nextBoolean()) {
			jumpHeight = rand.nextFloat() * 0.05f; // 0-5%
		}
		
		// Speed:
		float speed = 0f;
		if (rand.nextBoolean() && rand.nextBoolean()) {
			speed = rand.nextFloat() * 0.05f; // 0-5%
		}
		
		// Magic Memory:
		int memory = 0;
		if (this.getCanUseMagic() && rand.nextBoolean()) {
			memory = rand.nextInt(2);
		}
		
		// Health
		// 50% 0, 37.5% 10, 12.5% 20
		float health = (float) (rand.nextInt(1) * 10);
		if (health > 0 && rand.nextBoolean() && rand.nextBoolean()) {
			health += 10.0f;
		}
		
		// Mana + regen
		int mana = 0;
		float regen = 0f;
		if (this.getDragonMana() > 0) {
			if (rand.nextBoolean()) {
				mana = (rand.nextInt(30) + 1) * 10;
			}
			
			if (rand.nextBoolean() && rand.nextBoolean()) {
				regen = rand.nextFloat() * .2f;
			}
		}
		
		health += this.getMaxHealth();
		mana += this.getCurrentMana();
		
		this.setStats(this.getCanFly(), this.getBonusJumps(), this.getJumpHeightBonus() + jumpHeight, this.getSpeedBonus() + speed,
				health, health, mana, mana, regen + this.getManaRegen(), this.getCanUseMagic(), this.getMagicMemorySize() + memory, this.getXP(), level + 1, this.getBond());
		//canFly, jumps, jumpboost, speed, this.getMaxHealth(), this.getHealth(), maxmana, mana, canCast, magicMemory, xp, level, bond
		
		EntityLivingBase owner = this.getOwner();
		if (owner != null) {
			NostrumMagicaSounds.DRAGON_DEATH.play(owner);
			owner.addChatMessage(new TextComponentString(this.getName() + " leveled up!"));
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
		if (this.getOwner() != null && !this.worldObj.isRemote && this.worldObj.getGameRules().getBoolean("showDeathMessages") && this.getOwner() instanceof EntityPlayerMP) {
			this.getOwner().addChatMessage(this.getCombatTracker().getDeathMessage());
		}
		
		if (!this.worldObj.isRemote && this.inventory != null) {
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				ItemStack stack = inventory.getStackInSlot(i);
				if (stack != null && stack.stackSize != 0) {
					EntityItem item = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, stack);
					this.worldObj.spawnEntityInWorld(item);
				}
			}
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
	
	public float getGrowingAge() {
		return this.dataManager.get(AGE);
	}
	
	protected void setGrowingAge(float age) {
		this.dataManager.set(AGE, Math.max(0, Math.min(1f, age)));
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
		} else {
			if (this.ticksExisted % 20 == 0) {
				if (this.getDragonMana() > 0 && this.getCurrentMana() < this.getDragonMana()) {
					float amt = this.getManaRegen();
					int mana = (int) (amt);
					amt = amt - (int) amt;
					if (amt > 0f && NostrumMagica.rand.nextFloat() < amt)
						mana++;
					
					this.addMana(mana);
				}
			}
			if (this.ticksExisted % (20 * 30) == 0) {
				this.age();
			}
			
			EntityDragonEgg egg = this.getEgg();
			if (egg != null) {
				if (egg.getDistanceSqToEntity(this) < 4.0) {
					egg.heatUp();
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
		// Dragons go from 60% to 100% height.
		// This is synced with the rendering code.
		return (this.height * 0.6D) - ((0.4f * this.height) * (1f-getGrowingAge()));
	}
	
	@Override
	public void dragonJump() {
		if (this.jumpCount < 1 + this.getBonusJumps()) {
			this.jumpCount++;
			this.jump();
			
			if (this.considerFlying()) {
				this.setFlyState(FlyState.FLYING);
			}
		}
	}
	
	@Override
	public void fall(float distance, float damageMulti) {
		super.fall(distance, damageMulti);
		this.jumpCount = 0;
		this.setFlyState(FlyState.LANDED);
	}
	
	@Override
	protected void entityStartFlying() {
		; // We handle flying (gliding) ourselves
	}
	
	@Override
	protected void entityStopFlying() {
		; // We handle flying (gliding) ourselves
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
		
		if (hurt && source.getSourceOfDamage() != null) {
			if (this.isRidingOrBeingRiddenBy(source.getSourceOfDamage())) {
				hurt = false;
			}
		}
		
		if (hurt && this.isTamed()) {
			EntityLivingBase owner = this.getOwner();
			float health = this.getHealth();
			if (health > 0f && health < DRAGON_MIN_HEALTH) {
				if (owner != null && owner instanceof EntityPlayer) {
					((EntityPlayer) this.getOwner()).addChatComponentMessage(new TextComponentTranslation("info.tamed_dragon.hurt", this.getName()));
				}
				this.dismountRidingEntity();
			} else if (health > 0f) {
				if (source instanceof EntityDamageSource) {
					if (((EntityDamageSource) source).getEntity() == owner) {
						// Hurt by the owner!
						if (this.getRNG().nextBoolean()) {
							// Remove bond!
							this.removeBond(0.75f);
						}
					}
				}
			}
		}
		
		return hurt;
	}
	
	@Override
	protected float getJumpUpwardsMotion() {
		return 0.75f * (1f + this.getJumpHeightBonus() + (this.considerFlying() ? 1 : 0));
	}
	
	@Override
	public boolean isFlying() {
		return considerFlying();
	}
	
	private boolean considerFlying() {
		return this.getCanFly() && jumpCount >= 2;
	}

	@Override
	public DragonContainer getGUIContainer(EntityPlayer player) {
		return new DragonContainer(this, player,
				new RedDragonInfoSheet(this),
				new RedDragonBondInfoSheet(this),
				new RedDragonInventorySheet(this),
				new RedDragonSpellSheet(this));
	}
	
	public int getLevel() {
		return this.dataManager.get(ATTRIBUTE_LEVEL);
	}
	
	protected int getMaxXP(int level) {
		return (int) (100D * Math.pow(1.5, level));
	}

	@Override
	public int getXP() {
		return this.dataManager.get(ATTRIBUTE_XP);
	}

	@Override
	public int getMaxXP() {
		return this.getMaxXP(getLevel());
	}

	@Override
	public int getMana() {
		return this.getCurrentMana();
	}

	@Override
	public int getMaxMana() {
		return this.getDragonMana();
	}

	@Override
	public float getBond() {
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
		
		if (newXP > this.getMaxXP()) {
			newXP -= this.getMaxXP();
			this.levelup();
		}
		
		this.dataManager.set(ATTRIBUTE_XP, newXP);
	}
	
	public void slash(EntityLivingBase target) {
		super.slash(target);
		
		if (this.isTamed()) {
			Random rand = getRNG();
			int xp = 0;
			
			if (rand.nextBoolean() && rand.nextBoolean()) {
				xp = rand.nextInt(2);
			}
			
			if (target.isDead || target.getHealth() < 0f) {
				xp += 1;
			}
			
			this.addXP(xp);
		}
		
		if (getRNG().nextInt(10) == 0) {
			this.age();
		}
	}
	
	public void bite(EntityLivingBase target) {
		super.bite(target);
		
		if (this.isTamed()) {
			Random rand = getRNG();
			int xp = 0;
			
			if (rand.nextBoolean()) {
				xp = rand.nextInt(3);
			}
			
			if (target.isDead || target.getHealth() < 0f) {
				xp += 2;
			}
			
			this.addXP(xp);
			
			EntityLivingBase owner = this.getOwner();
			if (owner != null && owner.equals(this.getControllingPassenger())) {
				// Owner is riding
				this.addBond(.5f);
			}
		}
		
		if (target instanceof EntityZombie || target instanceof EntityPlayer) {
			this.heal(2);
		}
		
		if (getRNG().nextInt(20) == 0) {
			this.age();
		}
	}
	
	// Advances the dragon's age a small, random amount.
	private void age() {
		float currentAge = this.getGrowingAge();
		if (currentAge < 1f) {
			// setter already does bound checkin. So be lazy and just add here!
			currentAge += 0.001f + (getRNG().nextFloat() * 0.001f);
			this.setGrowingAge(currentAge);
		}
	}
	
	private void layEgg() {
		EntityPlayer player = null;
		EntityLivingBase owner = this.getOwner();
		if (owner != null && owner instanceof EntityPlayer) {
			player = (EntityPlayer) owner;
		}
		EntityDragonEgg egg = new EntityDragonEgg(worldObj, player, this.rollInheritedStats());
		egg.setPosition((int) posX + .5, (int) posY, (int) posZ + .5);
		if (worldObj.spawnEntityInWorld(egg)) {
			this.setEggId(egg.getUniqueID());
			
			if (player != null) {
				player.addChatComponentMessage(new TextComponentTranslation("info.egg.lay", this.getDisplayName()));
			}
			
			this.setBond(this.getBond() - .5f);
		}
	}
	
	public boolean canUseInventory() {
		return this.isTamed() && this.getBond() >= BOND_LEVEL_CHEST;
	}
	
	public IInventory getInventory() {
		return this.inventory;
	}
	
	public boolean canManageSpells() {
		return this.isTamed() && this.getCanUseMagic() && this.getBond() >= BOND_LEVEL_MAGIC;
	}
	
	public RedDragonSpellInventory getSpellInventory() {
		return this.spellInventory;
	}

	@Override
	public void addMana(int mana) {
		this.dataManager.set(CAPABILITY_MANA, Math.max(0, Math.min(this.getCurrentMana() + mana, this.getDragonMana())));
	}

	@Override
	public boolean sharesMana(EntityPlayer player) {
		return player != null && player.isEntityEqual(this.getOwner()) && this.getBond() >= BOND_LEVEL_MANA;
	}
	
	public static class RedDragonSpellInventory extends InventoryBasic {
		
		public static final int MaxSpellsPerCategory = 5;
		private static final int TargetSpellIndex = 0;
		private static final int SelfSpellIndex = MaxSpellsPerCategory;
		private static final int AllySpellIndex = MaxSpellsPerCategory + MaxSpellsPerCategory;
		
		private static final String NBT_ITEMS = "items";
		private static final String NBT_GAMBITS = "predicates";
		
		// Items kept in super inventory
		// Gambits we keep here
		private EntityDragonGambit gambits[];

		public RedDragonSpellInventory(String title, boolean customName) {
			super(title, customName, MaxSpellsPerCategory * 3);
			gambits = new EntityDragonGambit[MaxSpellsPerCategory * 3];
			Arrays.fill(gambits, EntityDragonGambit.ALWAYS);
		}
		
		public ItemStack[] getTargetSpells() {
			ItemStack array[] = new ItemStack[MaxSpellsPerCategory];
			for (int i = 0; i < MaxSpellsPerCategory; i++) {
				array[i] = this.getStackInSlot(i + TargetSpellIndex);
			}
			return array;
		}
		
		public ItemStack[] getSelfSpells() {
			ItemStack array[] = new ItemStack[MaxSpellsPerCategory];
			for (int i = 0; i < MaxSpellsPerCategory; i++) {
				array[i] = this.getStackInSlot(i + SelfSpellIndex);
			}
			return array;
		}
		
		public ItemStack[] getAllySpells() {
			ItemStack array[] = new ItemStack[MaxSpellsPerCategory];
			for (int i = 0; i < MaxSpellsPerCategory; i++) {
				array[i] = this.getStackInSlot(i + AllySpellIndex);
			}
			return array;
		}
		
		@Nullable
		public ItemStack setStackInTargetSlot(ItemStack stack, int slotIndex) {
			if (slotIndex <  TargetSpellIndex || slotIndex >= TargetSpellIndex + MaxSpellsPerCategory) {
				return stack;
			}
			
			int fixedIndex = slotIndex + TargetSpellIndex;
			ItemStack ret = this.getStackInSlot(fixedIndex);
			this.setInventorySlotContents(fixedIndex, stack);
			return ret;
		}
		
		@Nullable
		public ItemStack setStackInSelfSlot(ItemStack stack, int slotIndex) {
			if (slotIndex <  SelfSpellIndex || slotIndex >= SelfSpellIndex + MaxSpellsPerCategory) {
				return stack;
			}
			
			int fixedIndex = slotIndex + SelfSpellIndex;
			ItemStack ret = this.getStackInSlot(fixedIndex);
			this.setInventorySlotContents(fixedIndex, stack);
			return ret;
		}
		
		@Nullable
		public ItemStack setStackInAllySlot(ItemStack stack, int slotIndex) {
			if (slotIndex <  AllySpellIndex || slotIndex >= AllySpellIndex + MaxSpellsPerCategory) {
				return stack;
			}
			
			int fixedIndex = slotIndex + AllySpellIndex;
			ItemStack ret = this.getStackInSlot(fixedIndex);
			this.setInventorySlotContents(fixedIndex, stack);
			return ret;
		}
		
		public int getUsedSlots() {
			int count = 0;
			for (int i = 0; i < this.getSizeInventory(); i++) {
				if (this.getStackInSlot(i) != null) {
					count++;
				}
			}
			return count;
		}
		
		public EntityDragonGambit getTargetGambit(int index) {
			EntityDragonGambit gambit = EntityDragonGambit.ALWAYS;
			
			if (index >=  TargetSpellIndex && index < TargetSpellIndex + MaxSpellsPerCategory) {
				gambit = this.gambits[index + TargetSpellIndex];
			}
			
			return gambit;
		}
		
		public EntityDragonGambit getSelfGambit(int index) {
			EntityDragonGambit gambit = EntityDragonGambit.ALWAYS;
			
			if (index >=  SelfSpellIndex && index < SelfSpellIndex + MaxSpellsPerCategory) {
				gambit = this.gambits[index + SelfSpellIndex];
			}
			
			return gambit;		
		}
		
		public EntityDragonGambit getAllyGambit(int index) {
			EntityDragonGambit gambit = EntityDragonGambit.ALWAYS;
			
			if (index >=  AllySpellIndex && index < AllySpellIndex + MaxSpellsPerCategory) {
				gambit = this.gambits[index + AllySpellIndex];
			}
			
			return gambit;
		}
		
		public EntityDragonGambit[] getTargetGambits() {
			return Arrays.copyOfRange(this.gambits, TargetSpellIndex, TargetSpellIndex + MaxSpellsPerCategory);
		}
		
		public EntityDragonGambit[] getSelfGambits() {
			return Arrays.copyOfRange(this.gambits, SelfSpellIndex, SelfSpellIndex + MaxSpellsPerCategory);
		}

		public EntityDragonGambit[] getAllyGambits() {
			return Arrays.copyOfRange(this.gambits, AllySpellIndex, AllySpellIndex + MaxSpellsPerCategory);
		}
		
		public NBTTagCompound toNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			
			// Write item inventory
			{
				NBTTagList list = new NBTTagList();
				
				for (int i = 0; i < this.getSizeInventory(); i++) {
					NBTTagCompound tag = new NBTTagCompound();
					
					ItemStack stack = this.getStackInSlot(i);
					if (stack != null) {
						stack.writeToNBT(tag);
					}
					
					list.appendTag(tag);
				}
				
				nbt.setTag(NBT_ITEMS, list);
			}
			
			// Write gambits
			{
				NBTTagList list = new NBTTagList();
				
				for (int i = 0; i < this.getSizeInventory(); i++) {
					EntityDragonGambit gambit = gambits[i];
					if (gambit == null) {
						gambit = EntityDragonGambit.ALWAYS;
					}
					
					list.appendTag(new NBTTagString(gambit.name()));
				}
				
				nbt.setTag(NBT_GAMBITS, list);
			}
			
			return nbt;
		}
		
		public static RedDragonSpellInventory fromNBT(NBTTagCompound nbt) {
			RedDragonSpellInventory inv = new RedDragonSpellInventory("Red Dragon Spell Inventory", true);
			
			// Item inventory
			{
				NBTTagList list = nbt.getTagList(NBT_ITEMS, NBT.TAG_COMPOUND);
				if (list != null) {
					for (int i = 0; i < inv.getSizeInventory(); i++) {
						NBTTagCompound tag = list.getCompoundTagAt(i);
						ItemStack stack = null;
						if (tag != null) {
							stack = ItemStack.loadItemStackFromNBT(tag);
						}
						inv.setInventorySlotContents(i, stack);
					}
				}
			}
			
			// Gambits
			{
				NBTTagList list = nbt.getTagList(NBT_GAMBITS, NBT.TAG_STRING);
				if (list != null) {
					for (int i = 0; i < inv.getSizeInventory(); i++) {
						String name = list.getStringTagAt(i);
						EntityDragonGambit gambit;
						try {
							gambit = EntityDragonGambit.valueOf(name.toUpperCase());
						} catch (Exception e) {
							gambit = EntityDragonGambit.ALWAYS;
						}
						
						inv.gambits[i] = gambit;
					}
				}
			}
			
			return inv;
		}

		public EntityDragonGambit[] getAllGambits() {
			return gambits;
		}

		public void setGambit(int index, EntityDragonGambit gambit) {
			if (index < 0 || index >= gambits.length) {
				return;
			}
			
			gambits[index] = gambit;
		}
		
		private void cleanRow(int startIndex) {
			for (int i = 0; i < MaxSpellsPerCategory; i++) {
				int index = i + startIndex;
				// See if it's empty
				if (this.getStackInSlot(index) == null) {
					// This slot is empty. Are there any further on?
					boolean fixed = false;
					for (int j = i + 1; j < MaxSpellsPerCategory; j++) {
						int lookIndex = j + startIndex;
						ItemStack stack = this.getStackInSlot(lookIndex);
						if (stack != null) {
							// Fix gambits first, since we hook into setContents later
							gambits[index] = gambits[lookIndex];
							gambits[lookIndex] = EntityDragonGambit.ALWAYS;
							
							this.setInventorySlotContents(index, stack);
							this.setInventorySlotContents(lookIndex, null);
							
							fixed = true;
							break;
						}
					}
					
					if (!fixed) {
						// There was no later slot tlhat was good, either. AKA things look good
						break;
					}
					// else look at next I slot and fix that up, too!
				}
			}
		}
		
		// Clean up any stacks that are where they shouldn't be.
		public void clean() {
			cleanRow(TargetSpellIndex);
			cleanRow(SelfSpellIndex);
			cleanRow(AllySpellIndex);
		}
		
		@Override
		public ItemStack removeStackFromSlot(int index) {
			ItemStack stack = super.removeStackFromSlot(index);
			
			if (stack != null) {
				// Item removed!
				gambits[index] = EntityDragonGambit.ALWAYS;
				this.clean();
			}
			
			return stack;
		}
		
		@Override
		public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
			super.setInventorySlotContents(index, stack);
			
			if (stack == null) {
				gambits[index] = EntityDragonGambit.ALWAYS;
			}
		}
		
		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			if (stack == null) {
				return true;
			}
			
			if (!(stack.getItem() instanceof SpellScroll)) {
				return false;
			}
			
			if (SpellScroll.getSpell(stack) == null) {
				return false;
			}
			
			return true;
		}
	}
	
	public static final class TameRedDragonLore implements ILoreTagged {
		
		private static TameRedDragonLore instance = null;
		public static TameRedDragonLore instance() {
			if (instance == null) {
				instance = new TameRedDragonLore();
			}
			return instance;
		}

		@Override
		public String getLoreKey() {
			return "lore_tamedragon_red";
		}

		@Override
		public String getLoreDisplayName() {
			return "Baby Red Dragons";
		}

		@Override
		public Lore getBasicLore() {
			return new Lore().add("Baby red dragons are cute!", "Not only are they cute, but they're compotent fighters!");
		}

		@Override
		public Lore getDeepLore() {
			return new Lore().add("Baby red dragons!");
		}

		@Override
		public InfoScreenTabs getTab() {
			// Don't actually display! We're going to show our own page!
			return null;
		}
		
	}
	
	public static class RedDragonSpawnData extends IDragonSpawnData<EntityTameDragonRed> {

		private static final String SPAWN_KEY = "RedDragon";
		
		private static RedDragonSpawnData fromNBT(NBTTagCompound nbt) {
			return new RedDragonSpawnData(
					nbt.getBoolean("canFly"),
					nbt.getInteger("bonusJumps"),
					nbt.getFloat("bonusJumpHeight"),
					nbt.getFloat("bonusSpeed"),
					nbt.getFloat("maxHealth"),
					nbt.getInteger("maxMana"),
					nbt.getFloat("regen"),
					nbt.getBoolean("hasMagic"),
					nbt.getInteger("magicMemory")
					);
		}
		
		private boolean canFly;
		private int bonusJumps;
		private float bonusJumpHeight;
		private float bonusSpeed;
		private float maxHealth;
		private int maxMana;
		private float regen;
		private boolean hasMagic;
		private int magicMemory;
		
		public RedDragonSpawnData(
				boolean canFly,
				int bonusJumps,
				float bonusJumpHeight, // relative. 1 is double the height!
				float bonusSpeed, // ""
				float maxHealth,
				int maxMana,
				float regen,
				boolean hasMagic,
				int magicMemory
				) {
			this.canFly = canFly;
			this.bonusJumps = bonusJumps;
			this.bonusJumpHeight = bonusJumpHeight;
			this.bonusSpeed = bonusSpeed;
			this.maxHealth = maxHealth;
			this.maxMana = maxMana;
			this.regen = regen;
			this.hasMagic = hasMagic;
			this.magicMemory = magicMemory;
		}
		
		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			nbt.setBoolean("canFly", canFly);
			nbt.setInteger("bonusJumps", bonusJumps);
			nbt.setFloat("bonusJumpHeight", bonusJumpHeight);
			nbt.setFloat("bonusSpeed", bonusSpeed);
			nbt.setFloat("maxHealth", maxHealth);
			nbt.setInteger("maxMana", maxMana);
			nbt.setFloat("regen", regen);
			nbt.setBoolean("hasMagic", hasMagic);
			nbt.setInteger("magicMemory", magicMemory);
		}

		@Override
		public EntityTameDragonRed spawnDragon(World world, double x, double y, double z) {
			EntityTameDragonRed dragon = new EntityTameDragonRed(world);
			dragon.setPosition(x, y, z);
			apply(dragon);
			return dragon;
		}

		@Override
		public String getKey() {
			return SPAWN_KEY;
		}
		
		public void apply(EntityTameDragonRed dragon) {
			dragon.setStats(
					canFly,
					bonusJumps,
					bonusJumpHeight,
					bonusSpeed,
					maxHealth,
					maxHealth,
					maxMana,
					maxMana,
					regen,
					hasMagic,
					magicMemory,
					dragon.getXP(),
					dragon.getLevel(),
					dragon.getBond()
					);
		}
		
	}

}
