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
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.gui.petgui.reddragon.RedDragonBondInfoSheet;
import com.smanzana.nostrummagica.client.gui.petgui.reddragon.RedDragonInfoSheet;
import com.smanzana.nostrummagica.client.gui.petgui.reddragon.RedDragonInventorySheet;
import com.smanzana.nostrummagica.client.gui.petgui.reddragon.RedDragonSpellSheet;
import com.smanzana.nostrummagica.entity.IStabbableEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.dragon.DragonEntity.DragonEquipmentInventory.IChangeListener;
import com.smanzana.nostrummagica.entity.dragon.IDragonSpawnData.IDragonSpawnFactory;
import com.smanzana.nostrummagica.entity.tasks.FollowEntityGenericGoal;
import com.smanzana.nostrummagica.entity.tasks.FollowOwnerGenericGoal;
import com.smanzana.nostrummagica.entity.tasks.OwnerHurtByTargetGoalGeneric;
import com.smanzana.nostrummagica.entity.tasks.OwnerHurtTargetGenericGoal;
import com.smanzana.nostrummagica.entity.tasks.PanicGenericGoal;
import com.smanzana.nostrummagica.entity.tasks.SitGenericGoal;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonGambittedSpellAttackGoal;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonMeleeAttackGoal;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonNearestAttackableTargetGoal;
import com.smanzana.nostrummagica.item.DragonSoulItem;
import com.smanzana.nostrummagica.item.RoseItem;
import com.smanzana.nostrummagica.item.SpellScroll;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonEquipmentSlot;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.pet.IPetWithSoul;
import com.smanzana.nostrummagica.serializer.PetJobSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.util.ArrayUtil;
import com.smanzana.nostrummagica.util.Entities;
import com.smanzana.petcommand.api.PetCommandAPI;
import com.smanzana.petcommand.api.PetFuncs;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.entity.ITameableEntity;
import com.smanzana.petcommand.api.pet.EPetAction;
import com.smanzana.petcommand.api.pet.PetInfo;
import com.smanzana.petcommand.api.pet.PetInfo.PetValue;
import com.smanzana.petcommand.api.pet.PetInfo.ValueFlavor;

import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class TameRedDragonEntity extends RedDragonBaseEntity implements ITameableEntity, ITameDragon, IChangeListener, IPetWithSoul, IStabbableEntity {

	public static final String ID = "entity_tame_dragon_red";
	
	protected static final EntityDataAccessor<Boolean> HATCHED = SynchedEntityData.<Boolean>defineId(TameRedDragonEntity.class, EntityDataSerializers.BOOLEAN);
	protected static final EntityDataAccessor<Boolean> TAMED = SynchedEntityData.<Boolean>defineId(TameRedDragonEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Optional<UUID>> OWNER_UNIQUE_ID = SynchedEntityData.<Optional<UUID>>defineId(TameRedDragonEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.<Boolean>defineId(TameRedDragonEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Float> AGE = SynchedEntityData.<Float>defineId(TameRedDragonEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Optional<UUID>> EGG_UNIQUE_ID = SynchedEntityData.<Optional<UUID>>defineId(TameRedDragonEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Boolean> SOULBOUND = SynchedEntityData.<Boolean>defineId(TameRedDragonEntity.class, EntityDataSerializers.BOOLEAN);
    
    protected static final EntityDataAccessor<Boolean> CAPABILITY_FLY = SynchedEntityData.<Boolean>defineId(TameRedDragonEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Byte> CAPABILITY_JUMP = SynchedEntityData.<Byte>defineId(TameRedDragonEntity.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Float> CAPABILITY_JUMP_HEIGHT = SynchedEntityData.<Float>defineId(TameRedDragonEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> CAPABILITY_SPEED = SynchedEntityData.<Float>defineId(TameRedDragonEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> CAPABILITY_MAXMANA = SynchedEntityData.<Integer>defineId(TameRedDragonEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> CAPABILITY_MANA = SynchedEntityData.<Integer>defineId(TameRedDragonEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> CAPABILITY_MANA_REGEN  = SynchedEntityData.<Float>defineId(TameRedDragonEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Boolean> CAPABILITY_MAGIC = SynchedEntityData.<Boolean>defineId(TameRedDragonEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Integer> CAPABILITY_MAGIC_SIZE  = SynchedEntityData.<Integer>defineId(TameRedDragonEntity.class, EntityDataSerializers.INT);
    
    
    protected static final EntityDataAccessor<Integer> ATTRIBUTE_XP  = SynchedEntityData.<Integer>defineId(TameRedDragonEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> ATTRIBUTE_LEVEL  = SynchedEntityData.<Integer>defineId(TameRedDragonEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> ATTRIBUTE_BOND  = SynchedEntityData.<Float>defineId(TameRedDragonEntity.class, EntityDataSerializers.FLOAT);
    
    protected static final EntityDataAccessor<Float> SYNCED_MAX_HEALTH  = SynchedEntityData.<Float>defineId(TameRedDragonEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<EPetAction> DATA_PET_ACTION = SynchedEntityData.<EPetAction>defineId(TameRedDragonEntity.class, PetJobSerializer.GetInstance());
    
    protected static final EntityDataAccessor<ItemStack> DATA_ARMOR_BODY = SynchedEntityData.<ItemStack>defineId(TameRedDragonEntity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> DATA_ARMOR_HELM = SynchedEntityData.<ItemStack>defineId(TameRedDragonEntity.class, EntityDataSerializers.ITEM_STACK);
    
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
    	IDragonSpawnData.register(TameRedDragonEntity.RedDragonSpawnData.SPAWN_KEY, new IDragonSpawnFactory() {
			@Override
			public IDragonSpawnData<?> create(CompoundTag nbt) {
				return TameRedDragonEntity.RedDragonSpawnData.fromNBT(nbt);
			}
		});
    }
    
    // AI tasks to swap when tamed
    private DragonNearestAttackableTargetGoal<Player> aiPlayerTarget;
    private HurtByTargetGoal aiRevengeTarget;
    
    private Container inventory; // Full player-accessable inventory
    private final DragonEquipmentInventory equipment;
    private RedDragonSpellInventory spellInventory;
    private UUID soulID;
    private UUID worldID;
    
    // Internal timers for controlling while riding
    private int jumpCount; // How many times we've jumped
    
	public TameRedDragonEntity(EntityType<? extends TameRedDragonEntity> type, Level worldIn) {
		super(type, worldIn);
		
        this.inventory = new SimpleContainer(DRAGON_INV_SIZE);
        this.equipment = new DragonEquipmentInventory(this);
        this.spellInventory = new RedDragonSpellInventory();
        
        soulID = UUID.randomUUID();
        worldID = null;
	}
	
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(TAMED, false);
		this.entityData.define(OWNER_UNIQUE_ID, Optional.<UUID>empty());
		this.entityData.define(SITTING, false);
		this.entityData.define(AGE, 0f);
		this.entityData.define(EGG_UNIQUE_ID, Optional.<UUID>empty());
		this.entityData.define(CAPABILITY_FLY, Boolean.FALSE);
		this.entityData.define(CAPABILITY_JUMP, (byte) 0);
		this.entityData.define(CAPABILITY_JUMP_HEIGHT, 0f);
		this.entityData.define(CAPABILITY_SPEED, 0f);
		this.entityData.define(CAPABILITY_MANA, 0);
		this.entityData.define(CAPABILITY_MANA_REGEN, 1.0f);
		this.entityData.define(CAPABILITY_MAXMANA, 0);
		this.entityData.define(CAPABILITY_MAGIC, Boolean.FALSE);
		this.entityData.define(CAPABILITY_MAGIC_SIZE, 0);
		this.entityData.define(ATTRIBUTE_XP, 0);
		this.entityData.define(ATTRIBUTE_LEVEL, 0);
		this.entityData.define(ATTRIBUTE_BOND, 0f);
		this.entityData.define(SYNCED_MAX_HEALTH, 100.0f);
		this.entityData.define(DATA_PET_ACTION, EPetAction.IDLE);
		this.entityData.define(HATCHED, false);
		this.entityData.define(SOULBOUND, false);
		this.entityData.define(DATA_ARMOR_BODY, ItemStack.EMPTY);
		this.entityData.define(DATA_ARMOR_HELM, ItemStack.EMPTY);
	}
	
	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (key == SYNCED_MAX_HEALTH) {
			this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(
					this.entityData.get(SYNCED_MAX_HEALTH).floatValue()
					);
		}
	}
	
	protected void setupTamedAI() {
		//this.targetTasks.removeTask(aiPlayerTarget);
	}
	
	protected void setupBaseAI() {
		final TameRedDragonEntity dragon = this;
		aiPlayerTarget = new DragonNearestAttackableTargetGoal<Player>(this, Player.class, true, new Predicate<Player>() {
			@Override
			public boolean apply(Player input) {
				float bond = dragon.isTamed() ? dragon.getBond() : 0f;
				
				if (bond > BOND_LEVEL_PLAYERS) {
					// Have a _chance_ of attacking for a while... about 5%
					bond -= BOND_LEVEL_PLAYERS;
					if (bond <= .05f) {
						return (dragon.getRandom().nextFloat() <= bond);
					} else {
						return false;
					}
				}
				
				return true;
			}
		});
		aiRevengeTarget = new HurtByTargetGoal(this);
		
		int priority = 0;
		this.goalSelector.addGoal(priority++, new FloatGoal(this));
		this.goalSelector.addGoal(priority++, new SitGenericGoal<TameRedDragonEntity>(this));
		this.goalSelector.addGoal(priority++, new PanicGenericGoal<TameRedDragonEntity>(this, 1.0D, new Predicate<TameRedDragonEntity>() {
			@Override
			public boolean apply(TameRedDragonEntity input) {
				return !input.isTamed() && input.getHealth() <= DRAGON_MIN_HEALTH;
			}
		}));
		// Target gambits
		final TameRedDragonEntity selfDragon = this;
		this.goalSelector.addGoal(priority++, new DragonGambittedSpellAttackGoal<TameRedDragonEntity>(this, 20, 4) {

			@Override
			public DragonGambit[] getGambits() {
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
					spells[i] = SpellScroll.GetSpell(scrolls.get(i));
				}
				
				return spells;
			}

			@Override
			public LivingEntity getTarget(TameRedDragonEntity dragon) {
				if (!selfDragon.getCanUseMagic()) {
					return null;
				}
				
				return selfDragon.getTarget();
			}
			
		});
		// Self
		this.goalSelector.addGoal(priority++, new DragonGambittedSpellAttackGoal<TameRedDragonEntity>(this, 20, 4) {

			@Override
			public DragonGambit[] getGambits() {
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
					spells[i] = SpellScroll.GetSpell(scrolls.get(i));
				}
				
				return spells;
			}

			@Override
			public LivingEntity getTarget(TameRedDragonEntity dragon) {
				if (!selfDragon.getCanUseMagic()) {
					return null;
				}
				
				return selfDragon;
			}
			
		});
		// Ally
		this.goalSelector.addGoal(priority++, new DragonGambittedSpellAttackGoal<TameRedDragonEntity>(this, 20, 4) {

			@Override
			public DragonGambit[] getGambits() {
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
					spells[i] = SpellScroll.GetSpell(scrolls.get(i));
				}
				
				return spells;
			}

			@Override
			public LivingEntity getTarget(TameRedDragonEntity dragon) {
				if (!selfDragon.getCanUseMagic()) {
					return null;
				}
				
				if (selfDragon.isTamed()) {
					LivingEntity owner = selfDragon.getOwner();
					if (owner != null) {
						List<LivingEntity> nearby = owner.level.getEntitiesOfClass(LivingEntity.class,
								new AABB(owner.getX() - 8, owner.getY() - 5, owner.getZ() - 8, owner.getX() + 8, owner.getY() + 5, owner.getZ() + 8),
								new Predicate<LivingEntity>() {

									@Override
									public boolean apply(LivingEntity input) {
										return input != null && (input == owner || input.isAlliedTo(owner));
									}
							
						});
						
						if (nearby == null || nearby.isEmpty()) {
							return owner;
						}
						
						return nearby.get(getRandom().nextInt(nearby.size()));
					}
				}
				
				return null;
			}
			
		});
		this.goalSelector.addGoal(priority++, new FollowEntityGenericGoal<TameRedDragonEntity>(this, 1.0D, .5f, 1.5f, false) {
			@Override
			protected LivingEntity getTarget(TameRedDragonEntity entity) {
				if (selfDragon.isTamed()) {
					return selfDragon.getEgg(); // can be null
				}
				
				return null;
			}
		});
		this.goalSelector.addGoal(priority++, new DragonMeleeAttackGoal(this, 1.0D, true, 15.0D));
		this.goalSelector.addGoal(priority++, new FollowOwnerGenericGoal<TameRedDragonEntity>(this, 1.0D, 16.0F, 4.0F, new Predicate<TameRedDragonEntity>() {
			@Override
			public boolean apply(TameRedDragonEntity input) {
				// Don't follow unless we've bonded enough
				return (input.getBond() >= BOND_LEVEL_FOLLOW);
			}
		}));
		this.goalSelector.addGoal(priority++, new WaterAvoidingRandomStrollGoal(this, 1.0D, 30));
		
		this.targetSelector.addGoal(1, new OwnerHurtByTargetGoalGeneric<TameRedDragonEntity>(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGenericGoal<TameRedDragonEntity>(this));
        this.targetSelector.addGoal(3, aiRevengeTarget);
        this.targetSelector.addGoal(4, aiPlayerTarget);
		this.targetSelector.addGoal(5, new DragonNearestAttackableTargetGoal<Zombie>(this, Zombie.class, true));
		this.targetSelector.addGoal(6, new DragonNearestAttackableTargetGoal<Sheep>(this, Sheep.class, true));
		this.targetSelector.addGoal(7, new DragonNearestAttackableTargetGoal<Cow>(this, Cow.class, true));
		this.targetSelector.addGoal(8, new DragonNearestAttackableTargetGoal<Pig>(this, Pig.class, true));
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
		
		if (level != null && !level.isClientSide) {
			final @Nullable EPetAction order = PetInfo.GetOrderAction(this);
			if (order != null) {
				setPetAction(order);
				return;
			}
			
			if (!this.isEntitySitting()) {
				if (this.getTarget() == null) {
					setPetAction(EPetAction.IDLE);
				} else {
					setPetAction(EPetAction.ATTACK);
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
		
		return stack.getItem() instanceof RoseItem;
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
	public InteractionResult /*processInteract*/ mobInteract(Player player, InteractionHand hand) {
		// Shift-right click toggles the dragon sitting.
		// When not sitting, right-click mounts the dragon.
		// When sitting, right-click opens the GUI
		final @Nonnull ItemStack stack = player.getItemInHand(hand);
		if (this.isTamed() && player == this.getOwner()) {
			if (hand == InteractionHand.MAIN_HAND) {
				
				if (player.isShiftKeyDown()) {
					if (!this.level.isClientSide) {
						this.setEntitySitting(!this.isEntitySitting());
						if (player.isCreative()) {
							this.setBond(1f);
						}
					}
					return InteractionResult.SUCCESS;
				} else if (this.getHealth() < this.getMaxHealth() && isHungerItem(stack)) {
					if (!this.level.isClientSide) {
						this.heal(5f);
						this.addBond(.2f);
						
						if (!player.isCreative()) {
							player.getItemInHand(hand).shrink(1);
						}
					}
					return InteractionResult.SUCCESS;
				} else if (this.isEntitySitting() && stack.isEmpty()) {
					if (!this.level.isClientSide) {
						//player.openGui(NostrumMagica.instance, NostrumGui.dragonID, this.world, (int) this.getPosX(), (int) this.getPosY(), (int) this.getPosZ());
						PetCommandAPI.OpenPetGUI(player, this);
					}
					return InteractionResult.SUCCESS;
				} else if (isBreedingItem(stack) && this.getBond() > BOND_LEVEL_BREED && this.getEgg() == null) {
					if (!this.level.isClientSide) {
						layEgg();
						if (!player.isCreative()) {
							stack.shrink(1);
						}
					}
					return InteractionResult.SUCCESS;
				} else if (stack.isEmpty()) {
					if (!this.level.isClientSide) {
						if (this.getBond() >= BOND_LEVEL_ALLOW_RIDE) {
							if (this.getHealth() < DRAGON_MIN_HEALTH) {
								player.sendMessage(new TranslatableComponent("info.tamed_dragon.low_health", this.getName()), Util.NIL_UUID);
							} else {
								player.startRiding(this);
							}
						} else {
							player.sendMessage(new TranslatableComponent("info.tamed_dragon.no_ride", this.getName()), Util.NIL_UUID);
						}
					}
					return InteractionResult.SUCCESS;
				}
				else {
					; // fall through; we didn't handle it
				}
				
			}
		} else if (!this.isTamed()) {
			if (hand == InteractionHand.MAIN_HAND) {
				if (!this.level.isClientSide) {
					this.tame(player, player.isCreative());
				}
				return InteractionResult.SUCCESS;
			}
		} else if (this.isTamed() && player.isCreative() && hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {
			if (!this.level.isClientSide) {
				this.tame(player, true);
				this.setBond(1f);
			}
			return InteractionResult.SUCCESS;
		} else if (this.isTamed() && hand == InteractionHand.MAIN_HAND) {
			// Someone other than the owner clicked
			if (!this.level.isClientSide) {
				player.sendMessage(new TranslatableComponent("info.tamed_dragon.not_yours", this.getName()), Util.NIL_UUID);
			}
			return InteractionResult.SUCCESS;
		}
		
		return InteractionResult.PASS;			
	}
	
	@Nullable
	public Entity getControllingPassenger() {
		return this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
	}
	
	@Override
	public boolean canBeControlledByRider() {
		Entity entity = this.getControllingPassenger();
		return entity instanceof LivingEntity;
	}
	
	@Override
	 public boolean canBeLeashed(Player player) {
		return !isEntitySitting() && player == getOwner();
	}
	
	public boolean wasHatched() {
		return this.entityData.get(HATCHED);
	}
	
	public void setWasHatched(boolean hatched) {
		this.entityData.set(HATCHED, hatched);
		
		if (level != null && !level.isClientSide) {
			ObfuscationReflectionHelper.setPrivateValue(Mob.class, this, hatched, "persistenceRequired");
		}
	}
	
	@Override
	public boolean isSoulBound() {
		return this.entityData.get(SOULBOUND);
	}
	
	public void setSoulBound(boolean soulBound) {
		this.entityData.set(SOULBOUND, soulBound);
	}

	@Nullable
	public UUID getOwnerId() {
		return ((Optional<UUID>)this.entityData.get(OWNER_UNIQUE_ID)).orElse(null);
	}

	public void setOwnerId(@Nullable UUID p_184754_1_) {
		this.entityData.set(OWNER_UNIQUE_ID, Optional.ofNullable(p_184754_1_));
	}
	
	protected UUID getEggID() {
		return this.entityData.get(EGG_UNIQUE_ID).orElse(null);
	}
	
	public void setEggId(UUID id) {
		entityData.set(EGG_UNIQUE_ID, Optional.ofNullable(id));
	}
	
	public DragonEggEntity getEgg() {
		UUID id = getEggID();
		if (id != null) {
			Entity entRaw = Entities.FindEntity(level, id);
			if (entRaw != null && entRaw instanceof DragonEggEntity) {
				return (DragonEggEntity) entRaw;
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
			return uuid == null ? null : this.level.getPlayerByUUID(uuid);
		} catch (IllegalArgumentException var2) {
			return null;
		}
	}

	public boolean isOwner(LivingEntity entityIn) {
		return entityIn == this.getOwner();
	}
	
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);

		if (this.getOwnerId() != null) {
			compound.putString(NBT_OWNER_ID, this.getOwnerId().toString());
		}
		
		compound.putUUID(NBT_SOUL_ID, soulID);
		if (worldID != null) {
			compound.putUUID(NBT_SOUL_WORLDID, worldID);
		}
		
		compound.putBoolean(NBT_SITTING, this.isEntitySitting());
		compound.putFloat(NBT_AGE, this.getGrowingAge());
		
		UUID eggID = this.getEggID();
		if (eggID != null) {
			compound.putUUID(NBT_EGG_ID, eggID);
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
		compound.putInt(NBT_ATTR_LEVEL, this.getDragonLevel());
		compound.putFloat(NBT_ATTR_BOND, this.getBond());
		
		// Write inventory
		{
			ListTag invTag = new ListTag();
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				CompoundTag tag = new CompoundTag();
				ItemStack stack = inventory.getItem(i);
				if (!stack.isEmpty()) {
					stack.save(tag);
				}
				
				invTag.add(tag);
			}
			
			compound.put(NBT_INVENTORY, invTag);
		}
		
		// Write equipment
		{
			CompoundTag equipTag = this.equipment.serializeNBT();
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
	 * (abstract) Protected helper method to read subclass entity data from Tag.
	 */
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		UUID id;

		if (compound.contains(NBT_OWNER_ID, Tag.TAG_STRING)) {
			id = UUID.fromString(compound.getString(NBT_OWNER_ID));
		}
		else {
			String s1 = compound.getString("Owner");
			id = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s1);
		}

		if (id != null) {
			try {
				this.setOwnerId(id);
				this.setTamed(true);
			}
			catch (Throwable var4) {
				this.setTamed(false);
			}
		}
		
		// Summon command passes empty NBT to parse. Don't overwrite random UUID if not present.
		if (compound.hasUUID(NBT_SOUL_ID)) {
			this.soulID = compound.getUUID(NBT_SOUL_ID);
		}
		
		if (compound.hasUUID(NBT_SOUL_WORLDID)) {
			this.worldID = compound.getUUID(NBT_SOUL_WORLDID);
		} else {
			this.worldID = null;
		}
		
		this.setSoulBound(compound.getBoolean(NBT_SOUL_BOUND));
		
		this.setEntitySitting(compound.getBoolean(NBT_SITTING));
		this.setGrowingAge(compound.getFloat(NBT_AGE));
		
		if (compound.contains(NBT_EGG_ID)) { // 1.16.5 crashes without this if no UUID present, which is awful
											 // since before there wasn't a tag with this name and instead if was two different longs!
			this.setEggId(compound.getUUID(NBT_EGG_ID));
		}
		
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
			ListTag list = compound.getList(NBT_INVENTORY, Tag.TAG_COMPOUND);
			this.inventory = new SimpleContainer(DRAGON_INV_SIZE);
			
			for (int i = 0; i < DRAGON_INV_SIZE; i++) {
				CompoundTag tag = list.getCompound(i);
				ItemStack stack = ItemStack.EMPTY;
				if (tag != null) {
					stack = ItemStack.of(tag);
				}
				this.inventory.setItem(i, stack);
			}
		}
		
		// Read equipment
		{
			this.equipment.clearContent();
			this.equipment.readFromNBT(compound.getCompound(NBT_EQUIPMENT));
		}
		
		// Read spell inventory
		if (canCast) {
			this.spellInventory = RedDragonSpellInventory.fromNBT(compound.getCompound(NBT_SPELL_INVENTORY));
			
//			ListNBT list = compound.getList(NBT_SPELL_INVENTORY, Tag.TAG_COMPOUND);
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
		return this.entityData.get(TAMED);
	}

	public void setTamed(boolean tamed) {
		this.entityData.set(TAMED, tamed);
		if (tamed) {
			this.setupTamedAI();
			if (level != null && !level.isClientSide) {
				ObfuscationReflectionHelper.setPrivateValue(Mob.class, this, true, "persistenceRequired");
			}
		}
	}
	
	private void tame(Player player, boolean force) {
		
		boolean success = false;
		
		if (force || this.getHealth() < DRAGON_MIN_HEALTH) {
			if (force || this.getRandom().nextInt(10) == 0) {
				player.sendMessage(new TranslatableComponent("info.tamed_dragon.wild.tame_success", this.getName()), Util.NIL_UUID);
				
				this.setTamed(true);
				this.navigation.stop();
				this.setTarget(null);
				this.setHealth((float) this.getAttribute(Attributes.MAX_HEALTH).getBaseValue());
				this.setOwnerId(player.getUUID());
				this.setEntitySitting(true);
				
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				if (attr != null) {
					attr.giveFullLore(TameRedDragonLore.instance());
				}
				success = true;
			} else {
				// Failed
				player.sendMessage(new TranslatableComponent("info.tamed_dragon.wild.tame_fail", this.getName()), Util.NIL_UUID);
				this.heal(5.0f);
			}
		} else {
			player.sendMessage(new TranslatableComponent("info.tamed_dragon.wild.high_health", this.getName()), Util.NIL_UUID);
		}

		if (!this.level.isClientSide) {
			this.level.broadcastEntityEvent(this, success ? (byte) 7 : (byte) 6);
		}
		
		if (!success) {
			this.setTarget(player);
		}
		
		playTameEffect(success);
	}
	
	private void playTameEffect(boolean success) {
		
		ParticleOptions particle = success ? ParticleTypes.HEART : ParticleTypes.ANGRY_VILLAGER;

		for (int i = 0; i < 15; ++i) {
			double d0 = this.random.nextGaussian() * 0.02D;
			double d1 = this.random.nextGaussian() * 0.02D;
			double d2 = this.random.nextGaussian() * 0.02D;
			this.level.addParticle(particle, this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), this.getY() + 0.5D + (double)(this.random.nextFloat() * this.getBbHeight()), this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), d0, d1, d2);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void handleEntityEvent(byte id) {
		if (id == 7) {
			this.playTameEffect(true);
		} else if (id == 6) {
			this.playTameEffect(false);
		} else {
			super.handleEntityEvent(id);
		}
	}
	
	public boolean getCanFly() {
		return this.entityData.get(CAPABILITY_FLY);
	}
	
	public int getBonusJumps() {
		return (int) this.entityData.get(CAPABILITY_JUMP);
	}
	
	public float getJumpHeightBonus() {
		return this.entityData.get(CAPABILITY_JUMP_HEIGHT);
	}
	
	public float getSpeedBonus() {
		return this.entityData.get(CAPABILITY_SPEED);
	}
	
	public int getDragonMana() {
		return this.entityData.get(CAPABILITY_MAXMANA);
	}
	
	public int getCurrentMana() {
		return this.entityData.get(CAPABILITY_MANA);
	}
	
	public float getManaRegen() {
		return this.entityData.get(CAPABILITY_MANA_REGEN);
	}
	
	public void addManaRegen(float regen) {
		float old = this.getManaRegen();
		this.entityData.set(CAPABILITY_MANA_REGEN, old + regen);
	}
	
	public boolean getCanUseMagic() {
		return this.entityData.get(CAPABILITY_MAGIC);
	}
	
	public int getMagicMemorySize() {
		return this.entityData.get(CAPABILITY_MAGIC_SIZE);
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
		
		this.entityData.set(CAPABILITY_FLY, canFly);
		this.entityData.set(CAPABILITY_JUMP, (byte) bonusJumps);
		this.entityData.set(CAPABILITY_JUMP_HEIGHT, bonusJumpHeight);
		this.entityData.set(CAPABILITY_SPEED, bonusSpeed);
		this.entityData.set(CAPABILITY_MAXMANA, maxMana);
		this.entityData.set(CAPABILITY_MANA, mana);
		this.entityData.set(CAPABILITY_MANA_REGEN, regen);
		this.entityData.set(CAPABILITY_MAGIC, hasMagic);
		this.entityData.set(CAPABILITY_MAGIC_SIZE, magicMemory);
		this.entityData.set(ATTRIBUTE_XP, xp);
		this.entityData.set(ATTRIBUTE_LEVEL, level);
		this.entityData.set(ATTRIBUTE_BOND, bond);
		
		this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.31D * (1D + (double) bonusSpeed));
		this.entityData.set(SYNCED_MAX_HEALTH, maxHealth);
		//.createMutableAttribute(Attributes.MAX_HEALTH, maxHealth) Synced thr ough data manager
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
		
		//this.setStats(canFly, bonusJumps, bonusJumpHeight, bonusSpeed, health, health, mana, mana, regen, hasMagic, magicMemory, this.getXP(), this.getDragonLevel(), this.getBond());
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
		Random random = this.getRandom();
		
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
			mana = 2000 + (100 * random.nextInt(10)); // Same as generating random
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
					hasMagic = random.nextInt(5) < 2;
				} else {
					hasMagic = random.nextInt(4)  == 0;
				}
			}
		}
		
		int magicMemory = 0;
		if (hasMagic) {
			// Always re-roll, but with better odds if parents were magic.
			if (this.getCanUseMagic()) {
				magicMemory = random.nextInt(3) + 2;
			} else {
				magicMemory = random.nextInt(2) + 1;
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
		int level = this.getDragonLevel();
		
		// Red dragons possible gain jump height, max Health, max Mana, and Speed.
		Random rand = getRandom();
		
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
			owner.sendMessage(new TextComponent(this.getName().getString() + " leveled up!"), Util.NIL_UUID);
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
	public boolean isAlliedTo(Entity entityIn) {
		if (this.isTamed()) {
			LivingEntity myOwner = this.getOwner();

			if (entityIn == myOwner) {
				return true;
			}

			if (myOwner != null) {
				LivingEntity theirOwner = PetFuncs.GetOwner(entityIn);
				if (theirOwner == myOwner) {
					return true;
				}
				
				return myOwner.isAlliedTo(entityIn);
			}
		}

		return super.isAlliedTo(entityIn);
	}
	
	protected void dropEquipment() {
		if (!this.level.isClientSide) {
			if (this.inventory != null) {
				for (int i = 0; i < inventory.getContainerSize(); i++) {
					ItemStack stack = inventory.getItem(i);
					if (!stack.isEmpty()) {
						ItemEntity item = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), stack);
						this.level.addFreshEntity(item);
					}
				}
			}
			
			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
				ItemStack stack = equipment.getStackInSlot(slot);
				if (!stack.isEmpty()) {
					ItemEntity item = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), stack);
					this.level.addFreshEntity(item);
				}
			}
			inventory.clearContent();
			equipment.clearContent();
		}
	}

	/**
	 * Called when the mob's health reaches 0.
	 */
	@Override
	public void die(DamageSource cause) {
		if (this.getOwner() != null && !this.level.isClientSide && this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getOwner() instanceof ServerPlayer) {
			this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage(), Util.NIL_UUID);
		}
		
		dropEquipment();

		super.die(cause);
	}

	@Override
	public boolean isEntityTamed() {
		return this.isTamed();
	}

	@Override
	public boolean isEntitySitting() {
		return this.entityData.get(SITTING);
	}
	
	@Override
	public boolean setEntitySitting(boolean sitting) {
		this.entityData.set(SITTING, sitting);
		setPetAction(EPetAction.STAY);
		return true;
	}
	
	public float getGrowingAge() {
		return this.entityData.get(AGE);
	}
	
	protected void setGrowingAge(float age) {
		this.entityData.set(AGE, Math.max(0, Math.min(1f, age)));
	}

	@Override
	protected void setFlyingAI() {
		//
	}

	@Override
	protected void setGroundedAI() {
		//
	}

	public static final AttributeSupplier.Builder BuildAttributes() {
		return RedDragonBaseEntity.BuildBaseRedDragonAttributes()
	        .add(Attributes.MOVEMENT_SPEED, 0.31D)
	        .add(Attributes.MAX_HEALTH, 100.0D)
	        .add(Attributes.ATTACK_DAMAGE, 10.0D)
	        .add(Attributes.ARMOR, 10.0D)
	        .add(Attributes.ATTACK_SPEED, 0.5D)
	        .add(Attributes.FOLLOW_RANGE, 64D)
	        .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 2)
	        ;
    }
	
	@Override
	public boolean removeWhenFarAway(double nearestPlayer) {
		return !this.wasHatched() && !this.isTamed();
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.dead) {
			return;
		}
		
		// Check world ID (if soul is saved) and make sure our worldkey matches.
		if (!level.isClientSide && this.worldID != null && this.tickCount % 20 == 1) {
			if (!NostrumMagica.instance.getPetSoulRegistry().checkCurrentWorldID(this)) {
				NostrumMagica.logger.info("Removing entity " + this + " as its world ID doesn't match: " + (this.worldID == null ? "NULL" : worldID));
				this.discard();
				//((ServerWorld) this.world).removeEntity(this);
				return;
			}
		}
		
		LivingEntity target = this.getTarget();
		if (target != null) {
			if (isAlliedTo(target)) {
				this.setTarget(null);
				aiRevengeTarget.stop();
			}
		}
		
		if (level.isClientSide) {
			// If strong flight gets added here, this should be adjusted so that strong flight can flap when standing still, etc.
			// Checking whether motion is high is great for gliding but probably won't work well if the dragon can wade basically
			if (this.isFlying() && !this.getWingFlapping()) {
				final Vec3 curMotion = this.getDeltaMovement();
				final double motion = Math.abs(curMotion.x) + Math.abs(curMotion.z);
				
				final double glideStallMotion = .4;
				boolean flap = false;
				boolean flapFast = false;
				
				if (this.getY() > this.yo) {
					flap = true;
					flapFast = true;
				}
				// Gliding TODO hide behind 'is gliding flier' bool
				else if (motion > glideStallMotion && tickCount % 160 == 0) {
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
	public void aiStep() {
		super.aiStep();
		
		if (this.level.isClientSide) {
			if (this.considerFlying()) {
				if (this.getDeltaMovement().y < 0.0D) {
					double relief = Math.min(1.0D, Math.abs(this.getDeltaMovement().x) + Math.abs(this.getDeltaMovement().z));
					this.setDeltaMovement(this.getDeltaMovement().multiply(1, 1D - (0.9D * relief), 1));
				}
			}
		} else {
			if (this.tickCount % 20 == 0) {
				if (this.getDragonMana() > 0 && this.getCurrentMana() < this.getDragonMana()) {
					float amt = this.getManaRegen();
					
					// Augment with bonuses
					amt += this.getAttribute(NostrumAttributes.manaRegen).getValue() / 100.0;
					
					int mana = (int) (amt);
					amt = amt - (int) amt;
					if (amt > 0f && NostrumMagica.rand.nextFloat() < amt)
						mana++;
					
					this.addMana(mana);
				}
			}
			if (this.tickCount % (20 * 30) == 0) {
				this.age();
			}
			
			DragonEggEntity egg = this.getEgg();
			if (egg != null) {
				if (egg.distanceToSqr(this) < 4.0) {
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
	public void travel(Vec3 motion /*float strafe, float vertical, float forward*/) {
		
		if (this.onGround && this.getDeltaMovement().y <= 0) {
			this.jumpCount = 0;
		}
		
		if (this.isVehicle() && this.canBeControlledByRider()) {
			LivingEntity entitylivingbase = (LivingEntity)this.getControllingPassenger();
			this.setYRot(entitylivingbase.getYRot());
			this.yRotO = this.getYRot();
			this.setXRot(entitylivingbase.getXRot() * 0.5F);
			this.setRot(this.getYRot(), this.getXRot());
			this.yBodyRot = this.getYRot();
			this.yHeadRot = this.yBodyRot;
			float strafe = entitylivingbase.xxa * 0.45F;
			float forward = entitylivingbase.zza * .7f;

			if (forward < 0.0F)
			{
				forward *= 0.5F;
			}
			
			this.flyingSpeed = this.getSpeed() * 0.33F;

			if (this.isControlledByLocalInstance())
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
				
				this.setSpeed((float)this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
				super.travel(new Vec3(strafe, motion.y, forward));
			}
			else if (entitylivingbase instanceof Player)
			{
				this.setDeltaMovement(Vec3.ZERO);
			}

			this.animationSpeedOld = this.animationSpeed;
			double d1 = this.getX() - this.xo;
			double d0 = this.getZ() - this.zo;
			float f2 = (float) Math.sqrt(d1 * d1 + d0 * d0) * 4.0F;

			if (f2 > 1.0F)
			{
				f2 = 1.0F;
			}

			this.animationSpeed += (f2 - this.animationSpeed) * 0.4F;
			this.animationPosition += this.animationSpeed;
		}
		else
		{
			this.flyingSpeed = 0.02F;
			super.travel(motion);
		}
	}
	
	@Override
	public double getPassengersRidingOffset() {
		// Dragons go from 60% to 100% height.
		// This is synced with the rendering code.
		return (this.getBbHeight() * 0.6D) - ((0.4f * this.getBbHeight()) * (1f-getGrowingAge()));
	}
	
	@Override
	public void dragonJump() {
		if (this.jumpCount == 0 && !this.onGround) {
			// Lose first jump if you didn't jump from the ground
			jumpCount = 1;
		}
		
		if (this.jumpCount < 1 + this.getBonusJumps()) {
			this.jumpCount++;
			this.jumpFromGround();
			
			if (this.considerFlying()) {
				this.setFlyState(FlyState.FLYING);
			}
		}
	}
	
	@Override
	public boolean causeFallDamage(float distance, float damageMulti, DamageSource source) {
		this.jumpCount = 0;
		this.setFlyState(FlyState.LANDED);
		return super.causeFallDamage(distance, damageMulti, source);
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
	public boolean skipAttackInteraction(Entity entityIn) {
		if (this.isPassengerOfSameVehicle(entityIn)) {
			return true;
		}
		
		return super.skipAttackInteraction(entityIn);
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		boolean hurt = super.hurt(source, amount);
		
		if (hurt && source.getEntity() != null) {
			if (this.isPassengerOfSameVehicle(source.getEntity())) {
				hurt = false;
			}
		}
		
		if (hurt && this.isTamed()) {
			LivingEntity owner = this.getOwner();
			float health = this.getHealth();
			if (health > 0f && health < DRAGON_MIN_HEALTH) {
				if (owner != null && owner instanceof Player) {
					((Player) this.getOwner()).sendMessage(new TranslatableComponent("info.tamed_dragon.hurt", this.getName()), Util.NIL_UUID);
				}
				this.stopRiding();
			} else if (health > 0f) {
				if (source.getEntity() == owner) {
					// Hurt by the owner!
					if (this.getRandom().nextBoolean()) {
						// Remove bond!
						this.removeBond(0.75f);
					}
				}
			}
		}
		
		return hurt;
	}
	
	@Override
	protected float getJumpPower() {
		return 0.75f * (1f + this.getJumpHeightBonus() + (this.considerFlying() ? 1 : 0));
	}
	
	@Override
	protected void jumpFromGround() {
		super.jumpFromGround();
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
	public IPetGUISheet<? extends IEntityPet>[] getContainerSheets(Player player) {
		return ArrayUtil.MakeArray(
				new RedDragonInfoSheet(this),
				new RedDragonBondInfoSheet(this),
				new RedDragonInventorySheet(this),
				new RedDragonSpellSheet(this));
	}

	public int getDragonLevel() {
		return this.entityData.get(ATTRIBUTE_LEVEL);
	}
	
	protected int getMaxXP(int level) {
		return (int) (100D * Math.pow(1.5, level));
	}

	@Override
	public int getXP() {
		return this.entityData.get(ATTRIBUTE_XP);
	}

	@Override
	public int getMaxXP() {
		return this.getMaxXP(getDragonLevel());
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
		return this.entityData.get(ATTRIBUTE_BOND);
	}
	
	protected void setBond(float bond) {
		this.entityData.set(ATTRIBUTE_BOND, bond);
	}
	
	/**
	 * Has a chance of adding some amount of bonding to the dragon.
	 * Change is random and not in your control.
	 * Rate influences how much is added. 1f is a normal kind-deed's amount. 2f is double that.
	 * @param rate
	 */
	public void addBond(float rate) {
		if (getRandom().nextBoolean() && getRandom().nextBoolean()) {
			float amt = 0.025f;
			float current = getBond();
			float mod = rate * (1f + (getRandom().nextFloat() - 0.5f) * .5f); // 100% +- 25% of rate
			
			amt *= mod;
			
			amt = (current + amt);
			amt = Math.max(0f, Math.min(1f, amt));
			
			setBond(amt);
		}
	}
	
	public void removeBond(float rate) {
		float amt = -0.01f;
		float current = getBond();
		float mod = rate * (1f + (getRandom().nextFloat() - 0.5f) * .5f); // 100% +- 25% of rate
		
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
		
		this.entityData.set(ATTRIBUTE_XP, newXP);
	}
	
	public void slash(LivingEntity target) {
		super.slash(target);
		
		if (this.isTamed()) {
			Random rand = getRandom();
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
				} else if (this.distanceToSqr(owner) < DRAGON_BOND_DISTANCE_SQ) {
					this.addBond(.2f);
				}
			}
		}
		
		if (getRandom().nextInt(10) == 0) {
			this.age();
		}
	}
	
	public void bite(LivingEntity target) {
		super.bite(target);
		
		if (this.isTamed()) {
			Random rand = getRandom();
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
				} else if (this.distanceToSqr(owner) < DRAGON_BOND_DISTANCE_SQ) {
					this.addBond(.2f);
				}
			}
		}
		
		if (target instanceof Zombie || target instanceof Player) {
			this.heal(2);
		}
		
		if (getRandom().nextInt(20) == 0) {
			this.age();
		}
	}
	
	// Advances the dragon's age a small, random amount.
	private void age() {
		float currentAge = this.getGrowingAge();
		if (currentAge < 1f) {
			// setter already does bound checkin. So be lazy and just add here!
			currentAge += 0.001f + (getRandom().nextFloat() * 0.001f);
			this.setGrowingAge(currentAge);
		}
	}
	
	private void layEgg() {
		Player player = null;
		LivingEntity owner = this.getOwner();
		if (owner != null && owner instanceof Player) {
			player = (Player) owner;
		}
		DragonEggEntity egg = new DragonEggEntity(NostrumEntityTypes.dragonEgg, level, player, this.rollInheritedStats());
		egg.setPos((int) getX() + .5, (int) getY(), (int) getZ() + .5);
		if (level.addFreshEntity(egg)) {
			this.setEggId(egg.getUUID());
			
			if (player != null) {
				player.sendMessage(new TranslatableComponent("info.egg.lay", this.getDisplayName()), Util.NIL_UUID);
			}
			
			this.setBond(this.getBond() - .5f);
		}
	}
	
	public boolean canUseInventory() {
		return this.isTamed() && this.getBond() >= BOND_LEVEL_CHEST;
	}
	
	public Container getInventory() {
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
		
		this.entityData.set(CAPABILITY_MANA, cur);
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
	public boolean sharesMana(Player player) {
		return player != null && player.is(this.getOwner()) && this.getBond() >= BOND_LEVEL_MANA;
	}
	
	public void setPetAction(EPetAction action) {
		entityData.set(DATA_PET_ACTION, action);
	}

	@Override
	public EPetAction getPetAction() {
		return entityData.get(DATA_PET_ACTION);
	}
	
	public static class RedDragonSpellInventory extends SimpleContainer {
		
		public static final int MaxSpellsPerCategory = 5;
		private static final int TargetSpellIndex = 0;
		private static final int SelfSpellIndex = MaxSpellsPerCategory;
		private static final int AllySpellIndex = MaxSpellsPerCategory + MaxSpellsPerCategory;
		
		private static final String NBT_ITEMS = "items";
		private static final String NBT_GAMBITS = "predicates";
		
		// Items kept in super inventory
		// Gambits we keep here
		private DragonGambit gambits[];

		public RedDragonSpellInventory() {
			super(MaxSpellsPerCategory * 3);
			gambits = new DragonGambit[MaxSpellsPerCategory * 3];
			Arrays.fill(gambits, DragonGambit.ALWAYS);
		}
		
		public NonNullList<ItemStack> getTargetSpells() {
			NonNullList<ItemStack> list = NonNullList.withSize(MaxSpellsPerCategory, ItemStack.EMPTY);
			for (int i = 0; i < MaxSpellsPerCategory; i++) {
				list.set(i, this.getItem(i + TargetSpellIndex));
			}
			return list;
		}
		
		public NonNullList<ItemStack> getSelfSpells() {
			NonNullList<ItemStack> list = NonNullList.withSize(MaxSpellsPerCategory, ItemStack.EMPTY);
			for (int i = 0; i < MaxSpellsPerCategory; i++) {
				list.set(i, this.getItem(i + SelfSpellIndex));
			}
			return list;
		}
		
		public NonNullList<ItemStack> getAllySpells() {
			NonNullList<ItemStack> list = NonNullList.withSize(MaxSpellsPerCategory, ItemStack.EMPTY);
			for (int i = 0; i < MaxSpellsPerCategory; i++) {
				list.set(i, this.getItem(i + AllySpellIndex));
			}
			return list;
		}
		
		@Nonnull
		public ItemStack setStackInTargetSlot(ItemStack stack, int slotIndex) {
			if (slotIndex <  TargetSpellIndex || slotIndex >= TargetSpellIndex + MaxSpellsPerCategory) {
				return stack;
			}
			
			int fixedIndex = slotIndex + TargetSpellIndex;
			ItemStack ret = this.getItem(fixedIndex);
			this.setItem(fixedIndex, stack);
			return ret;
		}
		
		@Nonnull
		public ItemStack setStackInSelfSlot(ItemStack stack, int slotIndex) {
			if (slotIndex <  SelfSpellIndex || slotIndex >= SelfSpellIndex + MaxSpellsPerCategory) {
				return stack;
			}
			
			int fixedIndex = slotIndex + SelfSpellIndex;
			ItemStack ret = this.getItem(fixedIndex);
			this.setItem(fixedIndex, stack);
			return ret;
		}
		
		@Nonnull
		public ItemStack setStackInAllySlot(ItemStack stack, int slotIndex) {
			if (slotIndex <  AllySpellIndex || slotIndex >= AllySpellIndex + MaxSpellsPerCategory) {
				return stack;
			}
			
			int fixedIndex = slotIndex + AllySpellIndex;
			ItemStack ret = this.getItem(fixedIndex);
			this.setItem(fixedIndex, stack);
			return ret;
		}
		
		public int getUsedSlots() {
			int count = 0;
			for (int i = 0; i < this.getContainerSize(); i++) {
				if (!this.getItem(i).isEmpty()) {
					count++;
				}
			}
			return count;
		}
		
		public DragonGambit getTargetGambit(int index) {
			DragonGambit gambit = DragonGambit.ALWAYS;
			
			if (index >=  TargetSpellIndex && index < TargetSpellIndex + MaxSpellsPerCategory) {
				gambit = this.gambits[index + TargetSpellIndex];
			}
			
			return gambit;
		}
		
		public DragonGambit getSelfGambit(int index) {
			DragonGambit gambit = DragonGambit.ALWAYS;
			
			if (index >=  SelfSpellIndex && index < SelfSpellIndex + MaxSpellsPerCategory) {
				gambit = this.gambits[index + SelfSpellIndex];
			}
			
			return gambit;		
		}
		
		public DragonGambit getAllyGambit(int index) {
			DragonGambit gambit = DragonGambit.ALWAYS;
			
			if (index >=  AllySpellIndex && index < AllySpellIndex + MaxSpellsPerCategory) {
				gambit = this.gambits[index + AllySpellIndex];
			}
			
			return gambit;
		}
		
		public DragonGambit[] getTargetGambits() {
			return Arrays.copyOfRange(this.gambits, TargetSpellIndex, TargetSpellIndex + MaxSpellsPerCategory);
		}
		
		public DragonGambit[] getSelfGambits() {
			return Arrays.copyOfRange(this.gambits, SelfSpellIndex, SelfSpellIndex + MaxSpellsPerCategory);
		}

		public DragonGambit[] getAllyGambits() {
			return Arrays.copyOfRange(this.gambits, AllySpellIndex, AllySpellIndex + MaxSpellsPerCategory);
		}
		
		public CompoundTag toNBT() {
			CompoundTag nbt = new CompoundTag();
			
			// Write item inventory
			{
				ListTag list = new ListTag();
				
				for (int i = 0; i < this.getContainerSize(); i++) {
					CompoundTag tag = new CompoundTag();
					
					ItemStack stack = this.getItem(i);
					if (!stack.isEmpty()) {
						stack.save(tag);
					}
					
					list.add(tag);
				}
				
				nbt.put(NBT_ITEMS, list);
			}
			
			// Write gambits
			{
				ListTag list = new ListTag();
				
				for (int i = 0; i < this.getContainerSize(); i++) {
					DragonGambit gambit = gambits[i];
					if (gambit == null) {
						gambit = DragonGambit.ALWAYS;
					}
					
					list.add(StringTag.valueOf(gambit.name()));
				}
				
				nbt.put(NBT_GAMBITS, list);
			}
			
			return nbt;
		}
		
		public static RedDragonSpellInventory fromNBT(CompoundTag nbt) {
			RedDragonSpellInventory inv = new RedDragonSpellInventory();
			
			// Item inventory
			{
				ListTag list = nbt.getList(NBT_ITEMS, Tag.TAG_COMPOUND);
				if (list != null) {
					for (int i = 0; i < inv.getContainerSize(); i++) {
						CompoundTag tag = list.getCompound(i);
						ItemStack stack = ItemStack.EMPTY;
						if (tag != null) {
							stack = ItemStack.of(tag);
						}
						inv.setItem(i, stack);
					}
				}
			}
			
			// Gambits
			{
				ListTag list = nbt.getList(NBT_GAMBITS, Tag.TAG_STRING);
				if (list != null) {
					for (int i = 0; i < inv.getContainerSize(); i++) {
						String name = list.getString(i);
						DragonGambit gambit;
						try {
							gambit = DragonGambit.valueOf(name.toUpperCase());
						} catch (Exception e) {
							gambit = DragonGambit.ALWAYS;
						}
						
						inv.gambits[i] = gambit;
					}
				}
			}
			
			return inv;
		}

		public DragonGambit[] getAllGambits() {
			return gambits;
		}

		public void setGambit(int index, DragonGambit gambit) {
			if (index < 0 || index >= gambits.length) {
				return;
			}
			
			gambits[index] = gambit;
		}
		
		private void cleanRow(int startIndex) {
			for (int i = 0; i < MaxSpellsPerCategory; i++) {
				int index = i + startIndex;
				// See if it's empty
				if (this.getItem(index).isEmpty()) {
					// This slot is empty. Are there any further on?
					boolean fixed = false;
					for (int j = i + 1; j < MaxSpellsPerCategory; j++) {
						int lookIndex = j + startIndex;
						ItemStack stack = this.getItem(lookIndex);
						if (!stack.isEmpty()) {
							// Fix gambits first, since we hook into setContents later
							gambits[index] = gambits[lookIndex];
							gambits[lookIndex] = DragonGambit.ALWAYS;
							
							this.setItem(index, stack);
							this.setItem(lookIndex, ItemStack.EMPTY);
							
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
		public @Nonnull ItemStack removeItemNoUpdate(int index) {
			ItemStack stack = super.removeItemNoUpdate(index);
			
			if (!stack.isEmpty()) {
				// Item removed!
				gambits[index] = DragonGambit.ALWAYS;
				this.clean();
			}
			
			return stack;
		}
		
		@Override
		public void setItem(int index, @Nonnull ItemStack stack) {
			super.setItem(index, stack);
			
			if (stack.isEmpty()) {
				gambits[index] = DragonGambit.ALWAYS;
			}
		}
		
		@Override
		public boolean canPlaceItem(int index, @Nonnull ItemStack stack) {
			if (stack.isEmpty()) {
				return true;
			}
			
			if (!(stack.getItem() instanceof SpellScroll)) {
				return false;
			}
			
			if (SpellScroll.GetSpell(stack) == null) {
				return false;
			}
			
			return true;
		}
	}

	public static final class TameRedDragonLore implements IEntityLoreTagged<TameRedDragonEntity> {
		
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

		@Override
		public EntityType<TameRedDragonEntity> getEntityType() {
			return NostrumEntityTypes.tameDragonRed;
		}
		
	}
	
	public static final class SoulBoundDragonLore implements IEntityLoreTagged<TameRedDragonEntity> {
		
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

		@Override
		public EntityType<TameRedDragonEntity> getEntityType() {
			return NostrumEntityTypes.tameDragonRed;
		}
		
	}
	
	public static class RedDragonSpawnData extends IDragonSpawnData<TameRedDragonEntity> {

		private static final String SPAWN_KEY = "RedDragon";
		
		private static RedDragonSpawnData fromNBT(CompoundTag nbt) {
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
		public void writeToNBT(CompoundTag nbt) {
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
		public TameRedDragonEntity spawnDragon(Level world, double x, double y, double z) {
			TameRedDragonEntity dragon = new TameRedDragonEntity(NostrumEntityTypes.tameDragonRed, world);
			dragon.setPos(x, y, z);
			apply(dragon);
			return dragon;
		}

		@Override
		public String getKey() {
			return SPAWN_KEY;
		}
		
		public void apply(TameRedDragonEntity dragon) {
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
					dragon.getDragonLevel(),
					dragon.getBond()
					);
		}
		
	}
	
	protected static final Component LABEL_XP = new TextComponent("XP");
	protected static final Component LABEL_MANA = new TextComponent("Mana");
	protected static final Component LABEL_BOND = new TextComponent("Bond");
	
	@Override
	public PetInfo getPetSummary() {
		return PetInfo.claim(getPetAction(), getHealth(), getMaxHealth(),
				new PetValue(getXP(), getMaxXP(), ValueFlavor.PROGRESS, LABEL_XP),
				new PetValue(getMana(), getMaxMana(), ValueFlavor.GOOD, LABEL_MANA),
				new PetValue(getBond(), 1f, ValueFlavor.GRADUAL_GOOD, LABEL_BOND)
			);
	}

	@Override
	public void onChange(DragonEquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
		if (slot != null) {
			if (slot == DragonEquipmentSlot.BODY) {
				entityData.set(DATA_ARMOR_BODY, newStack);
			} else if (slot == DragonEquipmentSlot.HELM) {
				entityData.set(DATA_ARMOR_HELM, newStack);
			}
		}
		// else
		// All slots changed. Scan and update!
		for (DragonEquipmentSlot scanSlot : DragonEquipmentSlot.values()) {
			@Nonnull ItemStack inSlot = equipment.getStackInSlot(scanSlot);
			if (scanSlot == DragonEquipmentSlot.BODY) {
				entityData.set(DATA_ARMOR_BODY, inSlot);
			} else if (scanSlot == DragonEquipmentSlot.HELM) {
				entityData.set(DATA_ARMOR_HELM, inSlot);
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
		if (this.level.isClientSide) {
			
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
				return entityData.get(DATA_ARMOR_BODY);
			case HELM:
				return entityData.get(DATA_ARMOR_HELM);
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
			dropEquipment();
			
			final ItemStack stack = DragonSoulItem.MakeSoulItem(this, true);
			if (!stack.isEmpty()) {
				this.spawnAtLocation(stack, 1f);
				this.hurt(DamageSource.GENERIC, 1000000f);
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
	public CompoundTag serializeNBT() {
		return super.serializeNBT();
	}

	@Override
	public UUID getPetID() {
		return this.getUUID();
	}

	@Override
	public boolean isBigPet() {
		return true;
	}
	
}
