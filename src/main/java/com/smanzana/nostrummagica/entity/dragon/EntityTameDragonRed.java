package com.smanzana.nostrummagica.entity.dragon;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeManaRegen;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.gui.petgui.IPetGUISheet;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetGUIStatAdapter;
import com.smanzana.nostrummagica.client.gui.petgui.reddragon.RedDragonBondInfoSheet;
import com.smanzana.nostrummagica.client.gui.petgui.reddragon.RedDragonInfoSheet;
import com.smanzana.nostrummagica.client.gui.petgui.reddragon.RedDragonInventorySheet;
import com.smanzana.nostrummagica.client.gui.petgui.reddragon.RedDragonSpellSheet;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.entity.IStabbableEntity;
import com.smanzana.nostrummagica.entity.ITameableEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.dragon.EntityDragon.DragonEquipmentInventory.IChangeListener;
import com.smanzana.nostrummagica.entity.dragon.IDragonSpawnData.IDragonSpawnFactory;
import com.smanzana.nostrummagica.entity.tasks.EntityAIFollowEntityGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAIPanicGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAISitGeneric;
import com.smanzana.nostrummagica.entity.tasks.FollowOwnerGenericGoal;
import com.smanzana.nostrummagica.entity.tasks.OwnerHurtByTargetGoalGeneric;
import com.smanzana.nostrummagica.entity.tasks.OwnerHurtTargetGenericGoal;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonAINearestAttackableTarget;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonGambittedSpellAttackTask;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonMeleeAttackTask;
import com.smanzana.nostrummagica.items.DragonArmor.DragonEquipmentSlot;
import com.smanzana.nostrummagica.items.DragonSoulItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.pet.IPetWithSoul;
import com.smanzana.nostrummagica.pet.PetInfo;
import com.smanzana.nostrummagica.pet.PetInfo.PetAction;
import com.smanzana.nostrummagica.pet.PetInfo.SecondaryFlavor;
import com.smanzana.nostrummagica.serializers.PetJobSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.utils.ArrayUtil;
import com.smanzana.nostrummagica.utils.Entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class EntityTameDragonRed extends EntityDragonRedBase implements ITameableEntity, ITameDragon, IChangeListener, IPetWithSoul, IStabbableEntity {

	public static final String ID = "entity_tame_dragon_red";
	
	protected static final DataParameter<Boolean> HATCHED = EntityDataManager.<Boolean>createKey(EntityTameDragonRed.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<Boolean> TAMED = EntityDataManager.<Boolean>createKey(EntityTameDragonRed.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.<Optional<UUID>>createKey(EntityTameDragonRed.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected static final DataParameter<Boolean> SITTING = EntityDataManager.<Boolean>createKey(EntityTameDragonRed.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Float> AGE = EntityDataManager.<Float>createKey(EntityTameDragonRed.class, DataSerializers.FLOAT);
    protected static final DataParameter<Optional<UUID>> EGG_UNIQUE_ID = EntityDataManager.<Optional<UUID>>createKey(EntityTameDragonRed.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected static final DataParameter<Boolean> SOULBOUND = EntityDataManager.<Boolean>createKey(EntityTameDragonRed.class, DataSerializers.BOOLEAN);
    
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
    protected static final DataParameter<PetAction> DATA_PET_ACTION = EntityDataManager.<PetAction>createKey(EntityTameDragonRed.class, PetJobSerializer.instance);
    
    protected static final DataParameter<ItemStack> DATA_ARMOR_BODY = EntityDataManager.<ItemStack>createKey(EntityTameDragonRed.class, DataSerializers.ITEMSTACK);
    protected static final DataParameter<ItemStack> DATA_ARMOR_HELM = EntityDataManager.<ItemStack>createKey(EntityTameDragonRed.class, DataSerializers.ITEMSTACK);
    
    protected static final String NBT_HATCHED = "Hatched";
    protected static final String NBT_TAMED = "Tamed";
    protected static final String NBT_OWNER_ID = "OwnerUUID";
    protected static final String NBT_SITTING = "Sitting";
    protected static final String NBT_AGE = "Age";
    protected static final String NBT_EGG_ID = "EggID";
    protected static final String NBT_SOUL_BOUND = "SoulBound";
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
    protected static final String NBT_EQUIPMENT = "DREquipment";
    protected static final String NBT_SPELL_INVENTORY = "DRSpellInventory";
    protected static final String NBT_SOUL_ID = "SoulID";
    protected static final String NBT_SOUL_WORLDID = "SoulWorldID";
    
    public static final float BOND_LEVEL_FOLLOW = 0.05f;
    public static final float BOND_LEVEL_PLAYERS = 0.15f;
    public static final float BOND_LEVEL_CHEST = 0.20f;
    public static final float BOND_LEVEL_ALLOW_RIDE = 0.50f;
    public static final float BOND_LEVEL_MAGIC = 0.60f;
    public static final float BOND_LEVEL_MANA = 0.95f;
    public static final float BOND_LEVEL_BREED = 0.999f;
    
    private static final double DRAGON_BOND_DISTANCE_SQ = 100;
    
    private static final float DRAGON_MIN_HEALTH = 10.0f;
    private static final int DRAGON_INV_SIZE = 27;
    
    public static void init() {
    	IDragonSpawnData.register(EntityTameDragonRed.RedDragonSpawnData.SPAWN_KEY, new IDragonSpawnFactory() {
			@Override
			public IDragonSpawnData<?> create(CompoundNBT nbt) {
				return EntityTameDragonRed.RedDragonSpawnData.fromNBT(nbt);
			}
		});
    }
    
    // AI tasks to swap when tamed
    private DragonAINearestAttackableTarget<PlayerEntity> aiPlayerTarget;
    private HurtByTargetGoal aiRevengeTarget;
    
    private IInventory inventory; // Full player-accessable inventory
    private final DragonEquipmentInventory equipment;
    private RedDragonSpellInventory spellInventory;
    private UUID soulID;
    private UUID worldID;
    
    // Internal timers for controlling while riding
    private int jumpCount; // How many times we've jumped
    
	public EntityTameDragonRed(EntityType<? extends EntityTameDragonRed> type, World worldIn) {
		super(type, worldIn);
		
        this.stepHeight = 2;
        
        this.inventory = new Inventory(DRAGON_INV_SIZE);
        this.equipment = new DragonEquipmentInventory(this);
        this.spellInventory = new RedDragonSpellInventory();
        
        soulID = UUID.randomUUID();
        worldID = null;
	}
	
	protected void registerData() {
		super.registerData();
		this.dataManager.register(TAMED, false);
		this.dataManager.register(OWNER_UNIQUE_ID, Optional.<UUID>empty());
		this.dataManager.register(SITTING, false);
		this.dataManager.register(AGE, 0f);
		this.dataManager.register(EGG_UNIQUE_ID, Optional.<UUID>empty());
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
		this.dataManager.register(DATA_PET_ACTION, PetAction.WAITING);
		this.dataManager.register(HATCHED, false);
		this.dataManager.register(SOULBOUND, false);
		this.dataManager.register(DATA_ARMOR_BODY, ItemStack.EMPTY);
		this.dataManager.register(DATA_ARMOR_HELM, ItemStack.EMPTY);
		
		final EntityTameDragonRed dragon = this;
		aiPlayerTarget = new DragonAINearestAttackableTarget<PlayerEntity>(this, PlayerEntity.class, true, new Predicate<PlayerEntity>() {
			@Override
			public boolean apply(PlayerEntity input) {
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
		aiRevengeTarget = new HurtByTargetGoal(this);
	}
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (key == SYNCED_MAX_HEALTH) {
			this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(
					this.dataManager.get(SYNCED_MAX_HEALTH).floatValue()
					);
		}
	}
	
	protected void setupTamedAI() {
		//this.targetTasks.removeTask(aiPlayerTarget);
	}
	
	protected void setupBaseAI() {
		int priority = 0;
		this.goalSelector.addGoal(priority++, new SwimGoal(this));
		this.goalSelector.addGoal(priority++, new EntityAISitGeneric<EntityTameDragonRed>(this));
		this.goalSelector.addGoal(priority++, new EntityAIPanicGeneric<EntityTameDragonRed>(this, 1.0D, new Predicate<EntityTameDragonRed>() {
			@Override
			public boolean apply(EntityTameDragonRed input) {
				return !input.isTamed() && input.getHealth() <= DRAGON_MIN_HEALTH;
			}
		}));
		// Target gambits
		final EntityTameDragonRed selfDragon = this;
		this.goalSelector.addGoal(priority++, new DragonGambittedSpellAttackTask<EntityTameDragonRed>(this, 20, 4) {

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
				
				NonNullList<ItemStack> scrolls = selfDragon.spellInventory.getTargetSpells();
				Spell[] spells = new Spell[scrolls.size()];
				
				for (int i = 0; i < spells.length; i++) {
					// We odn't check for null here cause we sanitize input on placement
					spells[i] = SpellScroll.getSpell(scrolls.get(i));
				}
				
				return spells;
			}

			@Override
			public LivingEntity getTarget(EntityTameDragonRed dragon) {
				if (!selfDragon.getCanUseMagic()) {
					return null;
				}
				
				return selfDragon.getAttackTarget();
			}
			
		});
		// Self
		this.goalSelector.addGoal(priority++, new DragonGambittedSpellAttackTask<EntityTameDragonRed>(this, 20, 4) {

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
				
				NonNullList<ItemStack> scrolls = selfDragon.spellInventory.getSelfSpells();
				Spell[] spells = new Spell[scrolls.size()];
				
				for (int i = 0; i < spells.length; i++) {
					// We odn't check for null here cause we sanitize input on placement
					spells[i] = SpellScroll.getSpell(scrolls.get(i));
				}
				
				return spells;
			}

			@Override
			public LivingEntity getTarget(EntityTameDragonRed dragon) {
				if (!selfDragon.getCanUseMagic()) {
					return null;
				}
				
				return selfDragon;
			}
			
		});
		// Ally
		this.goalSelector.addGoal(priority++, new DragonGambittedSpellAttackTask<EntityTameDragonRed>(this, 20, 4) {

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
				
				NonNullList<ItemStack> scrolls = selfDragon.spellInventory.getAllySpells();
				Spell[] spells = new Spell[scrolls.size()];
				
				for (int i = 0; i < spells.length; i++) {
					// We odn't check for null here cause we sanitize input on placement
					spells[i] = SpellScroll.getSpell(scrolls.get(i));
				}
				
				return spells;
			}

			@Override
			public LivingEntity getTarget(EntityTameDragonRed dragon) {
				if (!selfDragon.getCanUseMagic()) {
					return null;
				}
				
				if (selfDragon.isTamed()) {
					LivingEntity owner = selfDragon.getOwner();
					if (owner != null) {
						List<LivingEntity> nearby = owner.world.getEntitiesWithinAABB(LivingEntity.class,
								new AxisAlignedBB(owner.getPosX() - 8, owner.getPosY() - 5, owner.getPosZ() - 8, owner.getPosX() + 8, owner.getPosY() + 5, owner.getPosZ() + 8),
								new Predicate<LivingEntity>() {

									@Override
									public boolean apply(LivingEntity input) {
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
		this.goalSelector.addGoal(priority++, new EntityAIFollowEntityGeneric<EntityTameDragonRed>(this, 1.0D, .5f, 1.5f, false) {
			@Override
			protected LivingEntity getTarget(EntityTameDragonRed entity) {
				if (selfDragon.isTamed()) {
					return selfDragon.getEgg(); // can be null
				}
				
				return null;
			}
		});
		this.goalSelector.addGoal(priority++, new DragonMeleeAttackTask(this, 1.0D, true, 15.0D));
		this.goalSelector.addGoal(priority++, new FollowOwnerGenericGoal<EntityTameDragonRed>(this, 1.0D, 16.0F, 4.0F, new Predicate<EntityTameDragonRed>() {
			@Override
			public boolean apply(EntityTameDragonRed input) {
				// Don't follow unless we've bonded enough
				return (input.getBond() >= BOND_LEVEL_FOLLOW);
			}
		}));
		this.goalSelector.addGoal(priority++, new WaterAvoidingRandomWalkingGoal(this, 1.0D, 30));
		
		this.targetSelector.addGoal(1, new OwnerHurtByTargetGoalGeneric<EntityTameDragonRed>(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGenericGoal<EntityTameDragonRed>(this));
        this.targetSelector.addGoal(3, aiRevengeTarget);
        this.targetSelector.addGoal(4, aiPlayerTarget);
		this.targetSelector.addGoal(5, new DragonAINearestAttackableTarget<ZombieEntity>(this, ZombieEntity.class, true));
		this.targetSelector.addGoal(6, new DragonAINearestAttackableTarget<SheepEntity>(this, SheepEntity.class, true));
		this.targetSelector.addGoal(7, new DragonAINearestAttackableTarget<CowEntity>(this, CowEntity.class, true));
		this.targetSelector.addGoal(8, new DragonAINearestAttackableTarget<PigEntity>(this, PigEntity.class, true));
	}
	
	@Override
	protected void registerGoals() {
		super.registerGoals();
		
		this.setupBaseAI();
		if (this.isTamed()) {
			this.setupTamedAI();
		}
	}
	
	@Override
	public void baseTick() {
		super.baseTick();
		
		if (world != null && !world.isRemote) {
			if (!this.isEntitySitting()) {
				if (this.getAttackTarget() == null) {
					setPetAction(PetAction.WAITING);
				} else {
					setPetAction(PetAction.ATTACKING);
				}
			}
		}
	}
	
	@Override
	protected float getSoundVolume() {
		return 1F;
	}
	
	public boolean isBreedingItem(@Nonnull ItemStack stack) {
		if (stack.isEmpty() || !this.isTamed())
			return false;
		
		return stack.getItem() instanceof NostrumRoseItem;
	}
	
	public boolean isHungerItem(@Nonnull ItemStack stack) {
		if (stack.isEmpty()) {
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
	public boolean processInteract(PlayerEntity player, Hand hand) {
		// Shift-right click toggles the dragon sitting.
		// When not sitting, right-click mounts the dragon.
		// When sitting, right-click opens the GUI
		final @Nonnull ItemStack stack = player.getHeldItem(hand);
		if (this.isTamed() && player == this.getOwner()) {
			if (hand == Hand.MAIN_HAND) {
				
				if (player.isSneaking()) {
					if (!this.world.isRemote) {
						this.setSitting(!this.isEntitySitting());
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
				} else if (this.isEntitySitting() && stack.isEmpty()) {
					if (!this.world.isRemote) {
						//player.openGui(NostrumMagica.instance, NostrumGui.dragonID, this.world, (int) this.getPosX(), (int) this.getPosY(), (int) this.getPosZ());
						NostrumMagica.instance.proxy.openPetGUI(player, this);
					}
					return true;
				} else if (isBreedingItem(stack) && this.getBond() > BOND_LEVEL_BREED && this.getEgg() == null) {
					if (!this.world.isRemote) {
						layEgg();
						if (!player.isCreative()) {
							stack.shrink(1);
						}
					}
					return true;
				} else if (stack.isEmpty()) {
					if (!this.world.isRemote) {
						if (this.getBond() >= BOND_LEVEL_ALLOW_RIDE) {
							if (this.getHealth() < DRAGON_MIN_HEALTH) {
								player.sendMessage(new TranslationTextComponent("info.tamed_dragon.low_health", this.getName()));
							} else {
								player.startRiding(this);
							}
						} else {
							player.sendMessage(new TranslationTextComponent("info.tamed_dragon.no_ride", this.getName()));
						}
					}
					return true;
				}
				else {
					; // fall through; we didn't handle it
				}
				
			}
		} else if (!this.isTamed()) {
			if (hand == Hand.MAIN_HAND) {
				if (!this.world.isRemote) {
					this.tame(player, player.isCreative());
				}
				return true;
			}
		} else if (this.isTamed() && player.isCreative() && hand == Hand.MAIN_HAND && player.isSneaking()) {
			if (!this.world.isRemote) {
				this.tame(player, true);
				this.setBond(1f);
			}
			return true;
		} else if (this.isTamed() && hand == Hand.MAIN_HAND) {
			// Someone other than the owner clicked
			if (!this.world.isRemote) {
				player.sendMessage(new TranslationTextComponent("info.tamed_dragon.not_yours", this.getName()));
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
		return entity instanceof LivingEntity;
	}
	
	@Override
	 public boolean canBeLeashedTo(PlayerEntity player) {
		return !isEntitySitting() && player == getOwner();
	}
	
	public boolean wasHatched() {
		return this.dataManager.get(HATCHED);
	}
	
	public void setWasHatched(boolean hatched) {
		this.dataManager.set(HATCHED, hatched);
		
		if (world != null && !world.isRemote) {
			ObfuscationReflectionHelper.setPrivateValue(MobEntity.class, this, hatched, "field_82179_bU");
		}
	}
	
	@Override
	public boolean isSoulBound() {
		return this.dataManager.get(SOULBOUND);
	}
	
	public void setSoulBound(boolean soulBound) {
		this.dataManager.set(SOULBOUND, soulBound);
	}

	@Nullable
	public UUID getOwnerId() {
		return ((Optional<UUID>)this.dataManager.get(OWNER_UNIQUE_ID)).orElse(null);
	}

	public void setOwnerId(@Nullable UUID p_184754_1_) {
		this.dataManager.set(OWNER_UNIQUE_ID, Optional.ofNullable(p_184754_1_));
	}
	
	protected UUID getEggID() {
		return this.dataManager.get(EGG_UNIQUE_ID).orElse(null);
	}
	
	public void setEggId(UUID id) {
		dataManager.set(EGG_UNIQUE_ID, Optional.ofNullable(id));
	}
	
	public EntityDragonEgg getEgg() {
		UUID id = getEggID();
		if (id != null) {
			Entity entRaw = Entities.FindEntity(world, id);
			if (entRaw != null && entRaw instanceof EntityDragonEgg) {
				return (EntityDragonEgg) entRaw;
			}
			
			// Means we didn't find the egg.
			// Either we've left it, so abandon :/  or it hatched or was broken.
			setEggId(null);
			return null;
		}
		
		return null;
	}

	@Nullable
	public LivingEntity getOwner() {
		try {
			UUID uuid = this.getOwnerId();
			return uuid == null ? null : this.world.getPlayerByUuid(uuid);
		} catch (IllegalArgumentException var2) {
			return null;
		}
	}

	public boolean isOwner(LivingEntity entityIn) {
		return entityIn == this.getOwner();
	}
	
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);

		if (this.getOwnerId() == null) {
			compound.putString(NBT_OWNER_ID, "");
		} else {
			compound.putString(NBT_OWNER_ID, this.getOwnerId().toString());
		}
		
		compound.putUniqueId(NBT_SOUL_ID, soulID);
		if (worldID != null) {
			compound.putUniqueId(NBT_SOUL_WORLDID, worldID);
		}
		
		compound.putBoolean(NBT_SITTING, this.isEntitySitting());
		compound.putFloat(NBT_AGE, this.getGrowingAge());
		
		UUID eggID = this.getEggID();
		if (eggID != null) {
			compound.putUniqueId(NBT_EGG_ID, eggID);
		}
		
		compound.putBoolean(NBT_SOUL_BOUND, this.isSoulBound());
		
		compound.putBoolean(NBT_CAP_FLY, this.getCanFly());
		compound.putByte(NBT_CAP_JUMP, (byte) this.getBonusJumps());
		compound.putFloat(NBT_CAP_JUMPBOOST, this.getJumpHeightBonus());
		compound.putFloat(NBT_CAP_SPEED, this.getSpeedBonus());
		compound.putInt(NBT_CAP_MANA, this.getCurrentMana());
		compound.putInt(NBT_CAP_MAXMANA, this.getDragonMana());
		compound.putFloat(NBT_CAP_MANAREGEN, this.getManaRegen());
		compound.putBoolean(NBT_CAP_MAGIC, this.getCanUseMagic());
		compound.putInt(NBT_CAP_MAGIC_SIZE, this.getMagicMemorySize());
		compound.putInt(NBT_ATTR_XP, this.getXP());
		compound.putInt(NBT_ATTR_LEVEL, this.getLevel());
		compound.putFloat(NBT_ATTR_BOND, this.getBond());
		
		// Write inventory
		{
			ListNBT invTag = new ListNBT();
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				CompoundNBT tag = new CompoundNBT();
				ItemStack stack = inventory.getStackInSlot(i);
				if (!stack.isEmpty()) {
					stack.write(tag);
				}
				
				invTag.add(tag);
			}
			
			compound.put(NBT_INVENTORY, invTag);
		}
		
		// Write equipment
		{
			CompoundNBT equipTag = this.equipment.serializeNBT();
			compound.put(NBT_EQUIPMENT, equipTag);
		}
		
		// Write spell inventory
		{
			compound.put(NBT_SPELL_INVENTORY, this.spellInventory.toNBT());
			
//			ListNBT invTag = new ListNBT();
//			for (int i = 0; i < spellInventory.getSizeInventory(); i++) {
//				CompoundNBT tag = new CompoundNBT();
//				ItemStack stack = spellInventory.getStackInSlot(i);
//				if (stack != null) {
//					stack.writeToNBT(tag);
//				}
//				
//				invTag.add(tag);
//			}
//			
//			compound.put(NBT_SPELL_INVENTORY, invTag);
		}
		
		compound.putBoolean(NBT_HATCHED, this.wasHatched());
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		String s;

		if (compound.contains("OwnerUUID", 8)) {
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
		
		// Summon command passes empty NBT to parse. Don't overwrite random UUID if not present.
		if (compound.hasUniqueId(NBT_SOUL_ID)) {
			this.soulID = compound.getUniqueId(NBT_SOUL_ID);
		}
		
		if (compound.hasUniqueId(NBT_SOUL_WORLDID)) {
			this.worldID = compound.getUniqueId(NBT_SOUL_WORLDID);
		} else {
			this.worldID = null;
		}
		
		this.setSoulBound(compound.getBoolean(NBT_SOUL_BOUND));
		
		this.setSitting(compound.getBoolean(NBT_SITTING));
		this.setGrowingAge(compound.getFloat(NBT_AGE));
		
		this.setEggId(compound.getUniqueId(NBT_EGG_ID));
		
		boolean canFly = compound.getBoolean(NBT_CAP_FLY);
		byte jumps = compound.getByte(NBT_CAP_JUMP);
		float jumpboost = compound.getFloat(NBT_CAP_JUMPBOOST);
		float speed = compound.getFloat(NBT_CAP_SPEED);
		int mana = compound.getInt(NBT_CAP_MANA);
		int maxmana = compound.getInt(NBT_CAP_MAXMANA);
		float regen = compound.getFloat(NBT_CAP_MANAREGEN);
		boolean canCast = compound.getBoolean(NBT_CAP_MAGIC);
		int magicMemory = compound.getInt(NBT_CAP_MAGIC_SIZE);
		int xp = compound.getInt(NBT_ATTR_XP);
		int level = compound.getInt(NBT_ATTR_LEVEL);
		float bond = compound.getFloat(NBT_ATTR_BOND);
		
		this.setStats(canFly, jumps, jumpboost, speed, this.getMaxHealth(), this.getHealth(), maxmana, mana, regen, canCast, magicMemory, xp, level, bond);
		
		// Read inventory
		{
			ListNBT list = compound.getList(NBT_INVENTORY, NBT.TAG_COMPOUND);
			this.inventory = new Inventory(DRAGON_INV_SIZE);
			
			for (int i = 0; i < DRAGON_INV_SIZE; i++) {
				CompoundNBT tag = list.getCompound(i);
				ItemStack stack = ItemStack.EMPTY;
				if (tag != null) {
					stack = ItemStack.read(tag);
				}
				this.inventory.setInventorySlotContents(i, stack);
			}
		}
		
		// Read equipment
		{
			this.equipment.clear();
			this.equipment.readFromNBT(compound.getCompound(NBT_EQUIPMENT));
		}
		
		// Read spell inventory
		if (canCast) {
			this.spellInventory = RedDragonSpellInventory.fromNBT(compound.getCompound(NBT_SPELL_INVENTORY));
			
//			ListNBT list = compound.getList(NBT_SPELL_INVENTORY, NBT.TAG_COMPOUND);
//			if (list != null) {
//				
//				for (int i = 0; i < spellInventory.getSizeInventory(); i++) {
//					CompoundNBT tag = list.getCompoundTagAt(i);
//					ItemStack stack = ItemStack.EMPTY;
//					if (tag != null) {
//						stack = new ItemStack(tag);
//					}
//					this.spellInventory.setInventorySlotContents(i, stack);
//				}
//			}
		} else {
			this.spellInventory = new RedDragonSpellInventory();
		}
		
		this.setWasHatched(compound.getBoolean(NBT_HATCHED));
	}
	
	public boolean isTamed() {
		return this.dataManager.get(TAMED);
	}

	public void setTamed(boolean tamed) {
		this.dataManager.set(TAMED, tamed);
		if (tamed) {
			this.setupTamedAI();
			if (world != null && !world.isRemote) {
				ObfuscationReflectionHelper.setPrivateValue(MobEntity.class, this, true, "field_82179_bU");
			}
		}
	}
	
	private void tame(PlayerEntity player, boolean force) {
		
		boolean success = false;
		
		if (force || this.getHealth() < DRAGON_MIN_HEALTH) {
			if (force || this.getRNG().nextInt(10) == 0) {
				player.sendMessage(new TranslationTextComponent("info.tamed_dragon.wild.tame_success", this.getName()));
				
				this.setTamed(true);
				this.navigator.clearPath();
				this.setAttackTarget(null);
				this.setHealth((float) this.getAttribute(Attributes.MAX_HEALTH).getBaseValue());
				this.setOwnerId(player.getUniqueID());
				this.setSitting(true);
				
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				if (attr != null) {
					attr.giveFullLore(TameRedDragonLore.instance());
				}
				success = true;
			} else {
				// Failed
				player.sendMessage(new TranslationTextComponent("info.tamed_dragon.wild.tame_fail", this.getName()));
				this.heal(5.0f);
			}
		} else {
			player.sendMessage(new TranslationTextComponent("info.tamed_dragon.wild.high_health", this.getName()));
		}

		if (!this.world.isRemote) {
			this.world.setEntityState(this, success ? (byte) 7 : (byte) 6);
		}
		
		if (!success) {
			this.setAttackTarget(player);
		}
		
		playTameEffect(success);
	}
	
	private void playTameEffect(boolean success) {
		
		IParticleData particle = success ? ParticleTypes.HEART : ParticleTypes.ANGRY_VILLAGER;

		for (int i = 0; i < 15; ++i) {
			double d0 = this.rand.nextGaussian() * 0.02D;
			double d1 = this.rand.nextGaussian() * 0.02D;
			double d2 = this.rand.nextGaussian() * 0.02D;
			this.world.addParticle(particle, this.getPosX() + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), this.getPosY() + 0.5D + (double)(this.rand.nextFloat() * this.getHeight()), this.getPosZ() + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), d0, d1, d2);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
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
		
		this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.31D * (1D + (double) bonusSpeed));
		this.dataManager.set(SYNCED_MAX_HEALTH, maxHealth);
		//this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth); Synced thr ough data manager
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
	
	@Override
	public void rerollStats() {
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
		float health = (float) (rand.nextInt(2) * 10);
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
		
		LivingEntity owner = this.getOwner();
		if (owner != null) {
			NostrumMagicaSounds.DRAGON_DEATH.play(owner);
			owner.sendMessage(new StringTextComponent(this.getName() + " leveled up!"));
		}
	}
	
	@Override
	public Team getTeam() {
		if (this.isTamed()) {
			LivingEntity entitylivingbase = this.getOwner();

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
			LivingEntity myOwner = this.getOwner();

			if (entityIn == myOwner) {
				return true;
			}

			if (myOwner != null) {
				LivingEntity theirOwner = NostrumMagica.getOwner(entityIn);
				if (theirOwner == myOwner) {
					return true;
				}
				
				return myOwner.isOnSameTeam(entityIn);
			}
		}

		return super.isOnSameTeam(entityIn);
	}
	
	protected void dropInventory() {
		if (!this.world.isRemote) {
			if (this.inventory != null) {
				for (int i = 0; i < inventory.getSizeInventory(); i++) {
					ItemStack stack = inventory.getStackInSlot(i);
					if (!stack.isEmpty()) {
						ItemEntity item = new ItemEntity(this.world, this.getPosX(), this.getPosY(), this.getPosZ(), stack);
						this.world.addEntity(item);
					}
				}
			}
			
			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
				ItemStack stack = equipment.getStackInSlot(slot);
				if (!stack.isEmpty()) {
					ItemEntity item = new ItemEntity(this.world, this.getPosX(), this.getPosY(), this.getPosZ(), stack);
					this.world.addEntity(item);
				}
			}
			inventory.clear();
			equipment.clear();
		}
	}

	/**
	 * Called when the mob's health reaches 0.
	 */
	@Override
	public void onDeath(DamageSource cause) {
		if (this.getOwner() != null && !this.world.isRemote && this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES) && this.getOwner() instanceof ServerPlayerEntity) {
			this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage());
		}
		
		dropInventory();

		super.onDeath(cause);
	}

	@Override
	public boolean isEntityTamed() {
		return this.isTamed();
	}

	@Override
	public boolean isEntitySitting() {
		return this.dataManager.get(SITTING);
	}
	
	public void setSitting(boolean sitting) {
		this.dataManager.set(SITTING, sitting);
		setPetAction(PetAction.SITTING);
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
	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		super.registerAttributes();
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.31D);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0D);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10.0D);
        this.getAttribute(Attributes.ARMOR).setBaseValue(10.0D);
        this.getAttributes().registerAttribute(Attributes.ATTACK_SPEED);
        this.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(0.5D);
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(64D);
    }
	
	@Override
	public boolean canDespawn(double nearestPlayer) {
		return !this.wasHatched() && !this.isTamed();
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.dead) {
			return;
		}
		
		// Check world ID (if soul is saved) and make sure our worldkey matches.
		if (!world.isRemote && this.worldID != null && this.ticksExisted % 20 == 1) {
			if (!NostrumMagica.instance.getPetSoulRegistry().checkCurrentWorldID(this)) {
				NostrumMagica.logger.info("Removing entity " + this + " as its world ID doesn't match: " + (this.worldID == null ? "NULL" : worldID));
				this.remove();
				//((ServerWorld) this.world).removeEntity(this);
				return;
			}
		}
		
		LivingEntity target = this.getAttackTarget();
		if (target != null) {
			if (isOnSameTeam(target)) {
				this.setAttackTarget(null);
				aiRevengeTarget.resetTask();
			}
		}
		
		if (world.isRemote) {
			// If strong flight gets added here, this should be adjusted so that strong flight can flap when standing still, etc.
			// Checking whether motion is high is great for gliding but probably won't work well if the dragon can wade basically
			if (this.isFlying() && !this.getWingFlapping()) {
				final Vector3d curMotion = this.getMotion();
				final double motion = Math.abs(curMotion.x) + Math.abs(curMotion.z);
				
				final double glideStallMotion = .4;
				boolean flap = false;
				boolean flapFast = false;
				
				if (this.getPosY() > this.prevPosY) {
					flap = true;
					flapFast = true;
				}
				// Gliding TODO hide behind 'is gliding flier' bool
				else if (motion > glideStallMotion && ticksExisted % 160 == 0) {
					flap = true;
					flapFast = false;
				}
				
				if (flap) {
					this.flapWing(flapFast ? 1f : .5f);
				}
			}
		}
	}
	
	@Override
	public void livingTick() {
		super.livingTick();
		
		if (this.world.isRemote) {
			if (this.considerFlying()) {
				if (this.getMotion().y < 0.0D) {
					double relief = Math.min(1.0D, Math.abs(this.getMotion().x) + Math.abs(this.getMotion().z));
					this.setMotion(this.getMotion().mul(1, 1D - (0.9D * relief), 1));
				}
			}
		} else {
			if (this.ticksExisted % 20 == 0) {
				if (this.getDragonMana() > 0 && this.getCurrentMana() < this.getDragonMana()) {
					float amt = this.getManaRegen();
					
					// Augment with bonuses
					amt += this.getAttribute(AttributeManaRegen.instance()).getValue() / 100.0;
					
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
				if (egg.getDistanceSq(this) < 4.0) {
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
	public void travel(Vector3d motion /*float strafe, float vertical, float forward*/) {
		
		if (this.onGround && this.getMotion().y <= 0) {
			this.jumpCount = 0;
		}
		
		if (this.isBeingRidden() && this.canBeSteered()) {
			LivingEntity entitylivingbase = (LivingEntity)this.getControllingPassenger();
			this.rotationYaw = entitylivingbase.rotationYaw;
			this.prevRotationYaw = this.rotationYaw;
			this.rotationPitch = entitylivingbase.rotationPitch * 0.5F;
			this.setRotation(this.rotationYaw, this.rotationPitch);
			this.renderYawOffset = this.rotationYaw;
			this.rotationYawHead = this.renderYawOffset;
			float strafe = entitylivingbase.moveStrafing * 0.45F;
			float forward = entitylivingbase.moveForward * .7f;

			if (forward < 0.0F)
			{
				forward *= 0.5F;
			}
			
			this.jumpMovementFactor = this.getAIMoveSpeed() * 0.33F;

			if (this.canPassengerSteer())
			{
//				if (this.setJump) {
//					this.setJump = false;
//					this.getMotion().y = (double)this.getJumpUpwardsMotion();
//					
//					if (this.isPotionActive(MobEffects.JUMP_BOOST)) {
//						this.getMotion().y += (double)((float)(this.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
//					}
//					
//					this.isAirBorne = true;
//					net.minecraftforge.common.ForgeHooks.onLivingJump(this);
//				}
				
				this.setAIMoveSpeed((float)this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
				super.travel(new Vector3d(strafe, motion.y, forward));
			}
			else if (entitylivingbase instanceof PlayerEntity)
			{
				this.setMotion(Vector3d.ZERO);
			}

			this.prevLimbSwingAmount = this.limbSwingAmount;
			double d1 = this.getPosX() - this.prevPosX;
			double d0 = this.getPosZ() - this.prevPosZ;
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
			super.travel(motion);
		}
	}
	
	@Override
	public double getMountedYOffset() {
		// Dragons go from 60% to 100% height.
		// This is synced with the rendering code.
		return (this.getHeight() * 0.6D) - ((0.4f * this.getHeight()) * (1f-getGrowingAge()));
	}
	
	@Override
	public void dragonJump() {
		if (this.jumpCount == 0 && !this.onGround) {
			// Lose first jump if you didn't jump from the ground
			jumpCount = 1;
		}
		
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
		
		if (hurt && source.getTrueSource() != null) {
			if (this.isRidingOrBeingRiddenBy(source.getTrueSource())) {
				hurt = false;
			}
		}
		
		if (hurt && this.isTamed()) {
			LivingEntity owner = this.getOwner();
			float health = this.getHealth();
			if (health > 0f && health < DRAGON_MIN_HEALTH) {
				if (owner != null && owner instanceof PlayerEntity) {
					((PlayerEntity) this.getOwner()).sendMessage(new TranslationTextComponent("info.tamed_dragon.hurt", this.getName()));
				}
				this.stopRiding();
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
	protected float getJumpUpwardsMotion() {
		return 0.75f * (1f + this.getJumpHeightBonus() + (this.considerFlying() ? 1 : 0));
	}
	
	@Override
	protected void jump() {
		super.jump();
		this.flapWing();
	}
	
	@Override
	public boolean isFlying() {
		return considerFlying();
	}
	
	private boolean considerFlying() {
		return this.getCanFly() && jumpCount >= 2;
	}

	@Override
	public IPetGUISheet<? extends IEntityPet>[] getContainerSheets(PlayerEntity player) {
		return ArrayUtil.MakeArray(
				new RedDragonInfoSheet(this),
				new RedDragonBondInfoSheet(this),
				new RedDragonInventorySheet(this),
				new RedDragonSpellSheet(this));
	}

	@Override
	public PetGUIStatAdapter<EntityTameDragonRed> getGUIAdapter() {
		return new PetGUIStatAdapter<EntityTameDragonRed>() {

			@Override
			public float getSecondaryAmt(EntityTameDragonRed pet) {
				return pet.getMana();
			}

			@Override
			public float getMaxSecondaryAmt(EntityTameDragonRed pet) {
				return pet.getMaxMana();
			}

			@Override
			public String getSecondaryLabel(EntityTameDragonRed pet) {
				return "Mana";
			}

			@Override
			public float getTertiaryAmt(EntityTameDragonRed pet) {
				return pet.getBond();
			}

			@Override
			public float getMaxTertiaryAmt(EntityTameDragonRed pet) {
				return 1f;
			}

			@Override
			public String getTertiaryLabel(EntityTameDragonRed pet) {
				return "Bond";
			}

			@Override
			public float getQuaternaryAmt(EntityTameDragonRed pet) {
				return pet.getXP();
			}

			@Override
			public float getMaxQuaternaryAmt(EntityTameDragonRed pet) {
				return pet.getMaxXP();
			}

			@Override
			public String getQuaternaryLabel(EntityTameDragonRed pet) {
				return "XP";
			}

		};
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
		
		if (newXP > this.getMaxXP()) {
			newXP -= this.getMaxXP();
			this.levelup();
		}
		
		this.dataManager.set(ATTRIBUTE_XP, newXP);
	}
	
	public void slash(LivingEntity target) {
		super.slash(target);
		
		if (this.isTamed()) {
			Random rand = getRNG();
			int xp = 0;
			
			if (rand.nextBoolean() && rand.nextBoolean()) {
				xp = rand.nextInt(2);
			}
			
			if (!target.isAlive() || target.getHealth() < 0f) {
				xp += 1;
			}
			
			this.addXP(xp);
			
			// Bond with nearby owner
			@Nullable LivingEntity owner = this.getOwner();
			if (owner != null) {
				if (owner.equals(this.getControllingPassenger())) {
					this.addBond(.5f);
				} else if (this.getDistanceSq(owner) < DRAGON_BOND_DISTANCE_SQ) {
					this.addBond(.2f);
				}
			}
		}
		
		if (getRNG().nextInt(10) == 0) {
			this.age();
		}
	}
	
	public void bite(LivingEntity target) {
		super.bite(target);
		
		if (this.isTamed()) {
			Random rand = getRNG();
			int xp = 0;
			
			if (rand.nextBoolean()) {
				xp = rand.nextInt(3);
			}
			
			if (!target.isAlive() || target.getHealth() < 0f) {
				xp += 2;
			}
			
			this.addXP(xp);
			
			// Bond with nearby owner
			@Nullable LivingEntity owner = this.getOwner();
			if (owner != null) {
				if (owner.equals(this.getControllingPassenger())) {
					this.addBond(.5f);
				} else if (this.getDistanceSq(owner) < DRAGON_BOND_DISTANCE_SQ) {
					this.addBond(.2f);
				}
			}
		}
		
		if (target instanceof ZombieEntity || target instanceof PlayerEntity) {
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
		PlayerEntity player = null;
		LivingEntity owner = this.getOwner();
		if (owner != null && owner instanceof PlayerEntity) {
			player = (PlayerEntity) owner;
		}
		EntityDragonEgg egg = new EntityDragonEgg(NostrumEntityTypes.dragonEgg, world, player, this.rollInheritedStats());
		egg.setPosition((int) posX + .5, (int) posY, (int) posZ + .5);
		if (world.addEntity(egg)) {
			this.setEggId(egg.getUniqueID());
			
			if (player != null) {
				player.sendMessage(new TranslationTextComponent("info.egg.lay", this.getDisplayName()));
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
	public int addMana(int mana) {
		int orig = this.getMana();
		int cur = Math.max(0, Math.min(orig + mana, this.getMaxMana()));
		
		this.dataManager.set(CAPABILITY_MANA, cur);
		return mana - (cur - orig);
	}
	
	@Override
	public boolean takeMana(int mana) {
		final int cur = getMana();
		if (cur >= mana) {
			addMana(-mana);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean sharesMana(PlayerEntity player) {
		return player != null && player.isEntityEqual(this.getOwner()) && this.getBond() >= BOND_LEVEL_MANA;
	}
	
	public void setPetAction(PetAction action) {
		dataManager.set(DATA_PET_ACTION, action);
	}

	@Override
	public PetAction getPetAction() {
		return dataManager.get(DATA_PET_ACTION);
	}
	
	public static class RedDragonSpellInventory extends Inventory {
		
		public static final int MaxSpellsPerCategory = 5;
		private static final int TargetSpellIndex = 0;
		private static final int SelfSpellIndex = MaxSpellsPerCategory;
		private static final int AllySpellIndex = MaxSpellsPerCategory + MaxSpellsPerCategory;
		
		private static final String NBT_ITEMS = "items";
		private static final String NBT_GAMBITS = "predicates";
		
		// Items kept in super inventory
		// Gambits we keep here
		private EntityDragonGambit gambits[];

		public RedDragonSpellInventory() {
			super(MaxSpellsPerCategory * 3);
			gambits = new EntityDragonGambit[MaxSpellsPerCategory * 3];
			Arrays.fill(gambits, EntityDragonGambit.ALWAYS);
		}
		
		public NonNullList<ItemStack> getTargetSpells() {
			NonNullList<ItemStack> list = NonNullList.withSize(MaxSpellsPerCategory, ItemStack.EMPTY);
			for (int i = 0; i < MaxSpellsPerCategory; i++) {
				list.set(i, this.getStackInSlot(i + TargetSpellIndex));
			}
			return list;
		}
		
		public NonNullList<ItemStack> getSelfSpells() {
			NonNullList<ItemStack> list = NonNullList.withSize(MaxSpellsPerCategory, ItemStack.EMPTY);
			for (int i = 0; i < MaxSpellsPerCategory; i++) {
				list.set(i, this.getStackInSlot(i + SelfSpellIndex));
			}
			return list;
		}
		
		public NonNullList<ItemStack> getAllySpells() {
			NonNullList<ItemStack> list = NonNullList.withSize(MaxSpellsPerCategory, ItemStack.EMPTY);
			for (int i = 0; i < MaxSpellsPerCategory; i++) {
				list.set(i, this.getStackInSlot(i + AllySpellIndex));
			}
			return list;
		}
		
		@Nonnull
		public ItemStack setStackInTargetSlot(ItemStack stack, int slotIndex) {
			if (slotIndex <  TargetSpellIndex || slotIndex >= TargetSpellIndex + MaxSpellsPerCategory) {
				return stack;
			}
			
			int fixedIndex = slotIndex + TargetSpellIndex;
			ItemStack ret = this.getStackInSlot(fixedIndex);
			this.setInventorySlotContents(fixedIndex, stack);
			return ret;
		}
		
		@Nonnull
		public ItemStack setStackInSelfSlot(ItemStack stack, int slotIndex) {
			if (slotIndex <  SelfSpellIndex || slotIndex >= SelfSpellIndex + MaxSpellsPerCategory) {
				return stack;
			}
			
			int fixedIndex = slotIndex + SelfSpellIndex;
			ItemStack ret = this.getStackInSlot(fixedIndex);
			this.setInventorySlotContents(fixedIndex, stack);
			return ret;
		}
		
		@Nonnull
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
				if (!this.getStackInSlot(i).isEmpty()) {
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
		
		public CompoundNBT toNBT() {
			CompoundNBT nbt = new CompoundNBT();
			
			// Write item inventory
			{
				ListNBT list = new ListNBT();
				
				for (int i = 0; i < this.getSizeInventory(); i++) {
					CompoundNBT tag = new CompoundNBT();
					
					ItemStack stack = this.getStackInSlot(i);
					if (!stack.isEmpty()) {
						stack.write(tag);
					}
					
					list.add(tag);
				}
				
				nbt.put(NBT_ITEMS, list);
			}
			
			// Write gambits
			{
				ListNBT list = new ListNBT();
				
				for (int i = 0; i < this.getSizeInventory(); i++) {
					EntityDragonGambit gambit = gambits[i];
					if (gambit == null) {
						gambit = EntityDragonGambit.ALWAYS;
					}
					
					list.add(StringNBT.valueOf(gambit.name()));
				}
				
				nbt.put(NBT_GAMBITS, list);
			}
			
			return nbt;
		}
		
		public static RedDragonSpellInventory fromNBT(CompoundNBT nbt) {
			RedDragonSpellInventory inv = new RedDragonSpellInventory();
			
			// Item inventory
			{
				ListNBT list = nbt.getList(NBT_ITEMS, NBT.TAG_COMPOUND);
				if (list != null) {
					for (int i = 0; i < inv.getSizeInventory(); i++) {
						CompoundNBT tag = list.getCompound(i);
						ItemStack stack = ItemStack.EMPTY;
						if (tag != null) {
							stack = ItemStack.read(tag);
						}
						inv.setInventorySlotContents(i, stack);
					}
				}
			}
			
			// Gambits
			{
				ListNBT list = nbt.getList(NBT_GAMBITS, NBT.TAG_STRING);
				if (list != null) {
					for (int i = 0; i < inv.getSizeInventory(); i++) {
						String name = list.getString(i);
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
				if (this.getStackInSlot(index).isEmpty()) {
					// This slot is empty. Are there any further on?
					boolean fixed = false;
					for (int j = i + 1; j < MaxSpellsPerCategory; j++) {
						int lookIndex = j + startIndex;
						ItemStack stack = this.getStackInSlot(lookIndex);
						if (!stack.isEmpty()) {
							// Fix gambits first, since we hook into setContents later
							gambits[index] = gambits[lookIndex];
							gambits[lookIndex] = EntityDragonGambit.ALWAYS;
							
							this.setInventorySlotContents(index, stack);
							this.setInventorySlotContents(lookIndex, ItemStack.EMPTY);
							
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
		public @Nonnull ItemStack removeStackFromSlot(int index) {
			ItemStack stack = super.removeStackFromSlot(index);
			
			if (!stack.isEmpty()) {
				// Item removed!
				gambits[index] = EntityDragonGambit.ALWAYS;
				this.clean();
			}
			
			return stack;
		}
		
		@Override
		public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
			super.setInventorySlotContents(index, stack);
			
			if (stack.isEmpty()) {
				gambits[index] = EntityDragonGambit.ALWAYS;
			}
		}
		
		@Override
		public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
			if (stack.isEmpty()) {
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
	
	public static final class SoulBoundDragonLore implements ILoreTagged {
		
		private static SoulBoundDragonLore instance = null;
		public static SoulBoundDragonLore instance() {
			if (instance == null) {
				instance = new SoulBoundDragonLore();
			}
			return instance;
		}

		@Override
		public String getLoreKey() {
			return "lore_tamedragon_soulbound";
		}

		@Override
		public String getLoreDisplayName() {
			return "Soulbound Dragons";
		}

		@Override
		public Lore getBasicLore() {
			return new Lore().add("You've soulbonded with your dragon! Its soul is not intertwined with yours!", "As a symbol of this bond, you've received a Dragon Soul Ember with your dragon's soul in it.", "This special item can be used to revive your dragon if it perishes. Be sure not to lose it!");
		}

		@Override
		public Lore getDeepLore() {
			return new Lore().add("You've soulbonded with your dragon! Its soul is not intertwined with yours!", "As a symbol of this bond, you've received a Dragon Soul Ember with your dragon's soul in it.", "This special item can be used to revive your dragon if it perishes. Be sure not to lose it!");
		}

		@Override
		public InfoScreenTabs getTab() {
			// Don't actually display! We're going to show our own page!
			return InfoScreenTabs.INFO_DRAGONS;
		}
		
	}
	
	public static class RedDragonSpawnData extends IDragonSpawnData<EntityTameDragonRed> {

		private static final String SPAWN_KEY = "RedDragon";
		
		private static RedDragonSpawnData fromNBT(CompoundNBT nbt) {
			return new RedDragonSpawnData(
					nbt.getBoolean("canFly"),
					nbt.getInt("bonusJumps"),
					nbt.getFloat("bonusJumpHeight"),
					nbt.getFloat("bonusSpeed"),
					nbt.getFloat("maxHealth"),
					nbt.getInt("maxMana"),
					nbt.getFloat("regen"),
					nbt.getBoolean("hasMagic"),
					nbt.getInt("magicMemory")
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
		public void writeToNBT(CompoundNBT nbt) {
			nbt.putBoolean("canFly", canFly);
			nbt.putInt("bonusJumps", bonusJumps);
			nbt.putFloat("bonusJumpHeight", bonusJumpHeight);
			nbt.putFloat("bonusSpeed", bonusSpeed);
			nbt.putFloat("maxHealth", maxHealth);
			nbt.putInt("maxMana", maxMana);
			nbt.putFloat("regen", regen);
			nbt.putBoolean("hasMagic", hasMagic);
			nbt.putInt("magicMemory", magicMemory);
		}

		@Override
		public EntityTameDragonRed spawnDragon(World world, double x, double y, double z) {
			EntityTameDragonRed dragon = new EntityTameDragonRed(NostrumEntityTypes.tameDragonRed, world);
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
	
	@Override
	public PetInfo getPetSummary() {
		return PetInfo.claim(getHealth(), getMaxHealth(), getXP(), getMaxXP(), SecondaryFlavor.PROGRESS, getPetAction());
	}

	@Override
	public void onChange(DragonEquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
		if (slot != null) {
			if (slot == DragonEquipmentSlot.BODY) {
				dataManager.set(DATA_ARMOR_BODY, newStack);
			} else if (slot == DragonEquipmentSlot.HELM) {
				dataManager.set(DATA_ARMOR_HELM, newStack);
			}
		}
		// else
		// All slots changed. Scan and update!
		for (DragonEquipmentSlot scanSlot : DragonEquipmentSlot.values()) {
			@Nonnull ItemStack inSlot = equipment.getStackInSlot(scanSlot);
			if (scanSlot == DragonEquipmentSlot.BODY) {
				dataManager.set(DATA_ARMOR_BODY, inSlot);
			} else if (scanSlot == DragonEquipmentSlot.HELM) {
				dataManager.set(DATA_ARMOR_HELM, inSlot);
			}
		}
	}
	
	public DragonEquipmentInventory getDragonEquipmentInventory() {
		return this.equipment;
	}
	
//	// I hate this
//	private static final Map<DragonArmorMaterial, Map<DragonEquipmentSlot, ItemStack>> ClientStacks = new EnumMap<>(DragonArmorMaterial.class);
	
	@Override
	public @Nonnull ItemStack getDragonEquipment(DragonEquipmentSlot slot) {
		// Defer to synced data param if on client. Otherwise, look in equipment inventory
		if (this.world.isRemote) {
			
//			// Init stacks if missing
//			if (ClientStacks.isEmpty() || ClientStacks.get(DragonArmorMaterial.IRON) == null) {
//				for (DragonArmorMaterial mat : DragonArmorMaterial.values()) {
//					Map<DragonEquipmentSlot, ItemStack> submap = new EnumMap<>(DragonEquipmentSlot.class);
//					for (DragonEquipmentSlot initSlot : DragonEquipmentSlot.values()) {
//						submap.put(initSlot, new ItemStack(DragonArmor.GetArmor(initSlot, mat)));
//					}
//					ClientStacks.put(mat, submap);
//				}
//			}
			
			switch (slot) {
			case BODY:
				return dataManager.get(DATA_ARMOR_BODY);
			case HELM:
				return dataManager.get(DATA_ARMOR_HELM);
			case WINGS: // UNIMPLEMENTED
			case CREST: // UNIMPLEMENTED
			default:
				break;
			}
			
			return ItemStack.EMPTY;
		} else {
			return this.equipment.getStackInSlot(slot);
		}
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
	public boolean onSoulStab(LivingEntity stabber, ItemStack stabbingItem) {
		
		if (this.isOwner(stabber) && !isSoulBound() && this.getBond() >= 1f) {
			// Die and scream and drop a soul ember
			this.setSoulBound(true);
			
			// Drop inventory before snapshotting
			dropInventory();
			
			final ItemStack stack = DragonSoulItem.MakeSoulItem(this, true);
			if (!stack.isEmpty()) {
				this.entityDropItem(stack, 1f);
				this.attackEntityFrom(DamageSource.GENERIC, 1000000f);
			}
			
			// Award lore about soul bonding
			INostrumMagic attr = NostrumMagica.getMagicWrapper(stabber);
			if (attr != null) {
				attr.giveFullLore(SoulBoundLore.instance());
				attr.giveBasicLore(SoulBoundDragonLore.instance);
			}
			
			return true;
		}
		return false;
	}

	@Override
	public CompoundNBT serializeNBT() {
		return super.serializeNBT();
	}

	@Override
	public UUID getPetID() {
		return this.getUniqueID();
	}
	
}
