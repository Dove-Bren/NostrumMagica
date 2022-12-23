package com.smanzana.nostrummagica.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.attributes.AttributeManaRegen;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetContainer;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetGUIStatAdapter;
import com.smanzana.nostrummagica.client.gui.petgui.arcanewolf.ArcaneWolfBondInfoSheet;
import com.smanzana.nostrummagica.client.gui.petgui.arcanewolf.ArcaneWolfInfoSheet;
import com.smanzana.nostrummagica.client.gui.petgui.arcanewolf.ArcaneWolfInventorySheet;
import com.smanzana.nostrummagica.client.gui.petgui.arcanewolf.ArcaneWolfTrainingSheet;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.tasks.EntityAIFollowOwnerAdvanced;
import com.smanzana.nostrummagica.entity.tasks.EntityAIFollowOwnerGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAIPetTargetTask;
import com.smanzana.nostrummagica.entity.tasks.EntitySpellAttackTask;
import com.smanzana.nostrummagica.entity.tasks.arcanewolf.ArcaneWolfAIBarrierTask;
import com.smanzana.nostrummagica.entity.tasks.arcanewolf.ArcaneWolfAIEldrichTask;
import com.smanzana.nostrummagica.entity.tasks.arcanewolf.ArcaneWolfAIHellTask;
import com.smanzana.nostrummagica.entity.tasks.arcanewolf.ArcaneWolfAIMysticTask;
import com.smanzana.nostrummagica.entity.tasks.arcanewolf.ArcaneWolfAINatureTask;
import com.smanzana.nostrummagica.entity.tasks.arcanewolf.ArcaneWolfAIStormTask;
import com.smanzana.nostrummagica.items.ArcaneWolfSoulItem;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.pet.IPetWithSoul;
import com.smanzana.nostrummagica.pet.PetInfo;
import com.smanzana.nostrummagica.pet.PetInfo.PetAction;
import com.smanzana.nostrummagica.pet.PetInfo.SecondaryFlavor;
import com.smanzana.nostrummagica.potions.MagicBoostPotion;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.serializers.ArcaneWolfElementalTypeSerializer;
import com.smanzana.nostrummagica.serializers.MagicElementDataSerializer;
import com.smanzana.nostrummagica.serializers.PetJobSerializer;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.ChainShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBeg;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityArcaneWolf extends EntityWolf implements IEntityTameable, IEntityPet, IPetWithSoul, IStabbableEntity {
	
	public static enum ArcaneWolfElementalType {
		NONELEMENTAL("nonelemental", 0x00000000, null),
		FIRE_ONLY("fire", EMagicElement.FIRE),
		ICE_ONLY("ice", EMagicElement.ICE),
		WIND_ONLY("wind", EMagicElement.WIND),
		EARTH_ONLY("earth", EMagicElement.EARTH),
		ENDER_ONLY("ender", EMagicElement.ENDER),
		LIGHTNING_ONLY("lightning", EMagicElement.LIGHTNING),
		
		// Composites
		BARRIER("barrier", 0xFFA0BBE5, EMagicElement.EARTH, EMagicElement.ICE), // Earth + Ice
		STORM("storm", 0xFFEFFF99, EMagicElement.WIND, EMagicElement.LIGHTNING), // Wind + Lightning
		ELDRICH("eldrich", 0xFFE030B0, EMagicElement.ENDER, EMagicElement.FIRE), // Ender + Fire
		MYSTIC("mystic", 0xFF1A21E0, EMagicElement.ICE, EMagicElement.ENDER), // Ice + Ender
		NATURE("nature", 0xFFD89254, EMagicElement.LIGHTNING, EMagicElement.EARTH), // Lightning + Earth
		HELL("hell", 0xFFD05000, EMagicElement.FIRE, EMagicElement.WIND); // Fire + Wind
		
		private final String key;
		private final @Nullable EMagicElement primary; // Only nonelemental has null though so we pretendit's not nullable
		private final @Nullable EMagicElement secondary;
		private final int color; // ARGB
		
		private ArcaneWolfElementalType(String key, int ARGB, EMagicElement primary) {
			this(key, ARGB, primary, null);
		}
		
		private ArcaneWolfElementalType(String key, EMagicElement primary) {
			this(key, primary.getColor(), primary);
		}
		
		private ArcaneWolfElementalType(String key, int ARGB, EMagicElement primary, @Nullable EMagicElement secondary) {
			this.primary = primary;
			this.secondary = secondary;
			this.key = key;
			this.color = ARGB;
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

		public int getColor() {
			return color;
		}
	}
	
	public static enum WolfBondCapability {
		FOLLOWS("follows", 0f),
		INVENTORY("inventory", .2f),
		TRAINABLE("trainable", .5f),
		RIDEABLE("rideable", .7f),
		;
		
		private final String key;
		private final float bond;
		
		private WolfBondCapability(String key, float bond) {
			this.key = key;
			this.bond = bond;
		}
		
		public String getKey() {
			return key;
		}
		
		public float getBondLevel() {
			return this.bond;
		}
		
		private static final List<WolfBondCapability> SortedCapabilities = new ArrayList<>();
		public static final List<WolfBondCapability> GetSortedLowHigh() {
			if (SortedCapabilities.isEmpty()) {
				for (WolfBondCapability c : WolfBondCapability.values()) {
					SortedCapabilities.add(c);
				}
				Collections.sort(SortedCapabilities, (a, b) -> {
					return (int) (100f * (a.bond - b.bond));
				});
			}
			return SortedCapabilities;
		}
	}
	
	public static enum WolfTypeCapability {
		BONUS_INVENTORY("bonus_inventory", EMagicElement.EARTH, 3),
		LAVA_WALK("lava_walk", EMagicElement.FIRE, 3),
		WIND_JUMP("wind_jump", EMagicElement.WIND, 2),
		WOLF_BLINK("wolf_blink", EMagicElement.ENDER, 2),
		MAGIC_RESIST("magic_resist", EMagicElement.LIGHTNING, 1),
		STORM_JUMP("storm_jump", ArcaneWolfElementalType.STORM),
		;
		
		private final String key;
		
		// Either type (exact arcane wolf type) or subtype (magic type) should be filled out.
		// If type is present, you can assume subtype is not.
		private final @Nullable ArcaneWolfElementalType type;
		
		private final @Nullable EMagicElement subtype;
		private final int subtypeLevel;
		
		private WolfTypeCapability(String key, ArcaneWolfElementalType type) {
			this.key = key;
			this.type = type;
			this.subtype = null;
			this.subtypeLevel = 0;
		}
		
		private WolfTypeCapability(String key, EMagicElement subtype, int level) {
			this.key = key;
			this.type = null;
			this.subtype = subtype;
			this.subtypeLevel = level;
		}
		
		public String getKey() {
			return key;
		}
		
		protected @Nullable ArcaneWolfElementalType getType() {
			return this.type;
		}
		
		protected @Nullable EMagicElement getSubType() {
			return this.subtype;
		}
		
		protected int getSubTypeLevel() {
			return this.subtypeLevel;
		}
	}
	
	protected static enum WolfSpellTargetGroup {
		SELF,
		ALLY,
		ENEMY,
	}
	
	protected static enum WolfSpell {
		GROUP_SPEED(WolfSpellTargetGroup.SELF, 50,
				(new Spell("WolfSpeed", true)).addPart(new SpellPart(SelfTrigger.instance())).addPart(new SpellPart(ChainShape.instance(), EMagicElement.WIND, 1, EAlteration.SUPPORT, new SpellPartParam(8, true))),
				(wolf, target) -> {
					return wolf.hasElementLevel(EMagicElement.WIND, 1)
							&& wolf.getAttackTarget() == null // Not in battle
							&& wolf.getMana() >= wolf.getMaxMana() * .30 // >= 30% mana
							;
				}),
		WIND_CUTTER(WolfSpellTargetGroup.ENEMY, 20,
				(new Spell("WolfWindCutter", true)).addPart(new SpellPart(MagicCutterTrigger.instance())).addPart(new SpellPart(SingleShape.instance(), EMagicElement.WIND, 2, EAlteration.RUIN)),
				(wolf, target) -> {
					return wolf.hasElementLevel(EMagicElement.WIND, 3);
				}),
		ROOTS(WolfSpellTargetGroup.ENEMY, 25,
				(new Spell("WolfRoots", true)).addPart(new SpellPart(SeekingBulletTrigger.instance())).addPart(new SpellPart(SingleShape.instance(), EMagicElement.EARTH, 2, EAlteration.INFLICT)),
				(wolf, target) -> {
					return wolf.hasElementLevel(EMagicElement.EARTH, 1)
							&& target.getActivePotionEffect(RootedPotion.instance()) == null;
				}),
		REGEN(WolfSpellTargetGroup.ALLY, 50,
				(new Spell("WolfRegen", true)).addPart(new SpellPart(AITargetTrigger.instance())).addPart(new SpellPart(SingleShape.instance(), EMagicElement.EARTH, 2, EAlteration.GROWTH)),
				(wolf, target) -> {
					return wolf.hasElementLevel(EMagicElement.EARTH, 3)
							&& target.getHealth() < target.getMaxHealth()
							&& target.getActivePotionEffect(Potion.getPotionFromResourceLocation("regeneration")) == null;
				}),
		MAGIC_SHIELD(WolfSpellTargetGroup.SELF, 30,
				(new Spell("WolfMagicShield", true)).addPart(new SpellPart(SelfTrigger.instance())).addPart(new SpellPart(ChainShape.instance(), EMagicElement.ICE, 1, EAlteration.SUPPORT, new SpellPartParam(8, true))),
				(wolf, target) -> {
					return wolf.hasElementLevel(EMagicElement.ICE, 1)
							&& wolf.getAttackTarget() != null; // Don't want to cast out of battle
				}),
		WOLF_HEAL(null, 20,
				(new Spell("WolfHeal", true)).addPart(new SpellPart(AITargetTrigger.instance())).addPart(new SpellPart(SingleShape.instance(), EMagicElement.ICE, 2, EAlteration.GROWTH)),
				(wolf, target) -> {
					if (!wolf.hasElementLevel(EMagicElement.ICE, 2)) {
						return false;
					}
					
					if (target.isEntityUndead()) {
						// An attack against undead!
						return !NostrumMagica.IsSameTeam(wolf, target);
					} else {
						return target.getHealth() < target.getMaxHealth() && NostrumMagica.IsSameTeam(wolf, target);
					}
				}),
		ICE_FANGS(WolfSpellTargetGroup.SELF, 100,
				(new Spell("WolfIceFangs", true)).addPart(new SpellPart(SelfTrigger.instance())).addPart(new SpellPart(ChainShape.instance(), EMagicElement.ICE, 2, EAlteration.ENCHANT, new SpellPartParam(8, true))),
				(wolf, target) -> {
					return wolf.hasElementLevel(EMagicElement.ICE, 3)
							&& wolf.getAttackTarget() != null; // Don't want to cast out of battle
				}),
		FIRE_TOUCH(WolfSpellTargetGroup.ENEMY, 10,
				(new Spell("WolfFireBite", true)).addPart(new SpellPart(TouchTrigger.instance())).addPart(new SpellPart(SingleShape.instance(), EMagicElement.FIRE, 2, EAlteration.RUIN)),
				(wolf, target) -> {
					return wolf.hasElementLevel(EMagicElement.FIRE, 1)
							&& wolf.getDistance(target) <= TouchTrigger.TOUCH_RANGE;
				}),
		MAGIC_BOOST(WolfSpellTargetGroup.ALLY, 20,
				(new Spell("WolfMagicBoost", true)).addPart(new SpellPart(AITargetTrigger.instance())).addPart(new SpellPart(SingleShape.instance(), EMagicElement.FIRE, 1, EAlteration.SUPPORT)),
				(wolf, target) -> {
					return wolf.hasElementLevel(EMagicElement.FIRE, 3)
							&& target.getActivePotionEffect(MagicBoostPotion.instance()) == null
							&& (wolf.getAttackTarget() != null || wolf.getMana() >= wolf.getMaxMana() * .75) // in battle or >= 75% mana
							;
				}),
		ENDER_SHROUD(WolfSpellTargetGroup.ENEMY, 20,
				(new Spell("WolfEnderShroud", true)).addPart(new SpellPart(SeekingBulletTrigger.instance())).addPart(new SpellPart(AoEShape.instance(), EMagicElement.ENDER, 2, null, new SpellPartParam(3, true))).addPart(new SpellPart(SingleShape.instance(), EMagicElement.ENDER, 1, EAlteration.INFLICT)),
				(wolf, target) -> {
					return wolf.hasElementLevel(EMagicElement.ENDER, 1);
				}),
		ENDER_FANGS(WolfSpellTargetGroup.SELF, 75,
				(new Spell("WolfEnderFangs", true)).addPart(new SpellPart(SelfTrigger.instance())).addPart(new SpellPart(ChainShape.instance(), EMagicElement.ENDER, 1, EAlteration.ENCHANT, new SpellPartParam(8, true))),
				(wolf, target) -> {
					return wolf.hasElementLevel(EMagicElement.ENDER, 3)
							&& wolf.getAttackTarget() != null; // Don't want to cast out of battle
				}),
		SLOW(WolfSpellTargetGroup.ENEMY, 10,
				(new Spell("WolfSlow", true)).addPart(new SpellPart(AITargetTrigger.instance())).addPart(new SpellPart(SingleShape.instance(), EMagicElement.LIGHTNING, 1, EAlteration.INFLICT)),
				(wolf, target) -> {
					return wolf.hasElementLevel(EMagicElement.LIGHTNING, 1)
							&& target.getActivePotionEffect(Potion.getPotionFromResourceLocation("slowness")) == null;
				}),
		CHAIN_LIGHTNING(WolfSpellTargetGroup.ENEMY, 40,
				(new Spell("WolfChainLightning", true)).addPart(new SpellPart(SeekingBulletTrigger.instance())).addPart(new SpellPart(ChainShape.instance(), EMagicElement.LIGHTNING, 2, EAlteration.RUIN, new SpellPartParam(6, true))),
				(wolf, target) -> {
					return wolf.hasElementLevel(EMagicElement.LIGHTNING, 3);
				}),
		;
		
		private static interface ISpellPredicate {
			public boolean apply(EntityArcaneWolf wolf, EntityLivingBase target);
		}
		
		private final @Nullable WolfSpellTargetGroup group;
		private final Spell spell;
		private final ISpellPredicate predicate;
		private final int cost;
		
		private WolfSpell(@Nullable WolfSpellTargetGroup group, int cost, Spell spell, ISpellPredicate predicate) {
			this.group = group;
			this.spell = spell;
			this.predicate = predicate;
			this.cost = cost;
		}
		
		public Spell getSpell() {
			return spell;
		}
		
		public boolean matches(WolfSpellTargetGroup group, EntityArcaneWolf wolf, EntityLivingBase target) {
			return (this.group == null || this.group == group) && predicate.apply(wolf, target);
		}
		
		public int getCost() {
			return this.cost;
		}
	}
	
	protected static final DataParameter<Boolean> SOULBOUND = EntityDataManager.<Boolean>createKey(EntityArcaneWolf.class, DataSerializers.BOOLEAN);
	
	protected static final DataParameter<Integer> ATTRIBUTE_XP  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    protected static final DataParameter<Integer> ATTRIBUTE_LEVEL  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    protected static final DataParameter<Float> ATTRIBUTE_BOND  = EntityDataManager.<Float>createKey(EntityArcaneWolf.class, DataSerializers.FLOAT);
    protected static final DataParameter<Float> ATTRIBUTE_MANA_REGEN  = EntityDataManager.<Float>createKey(EntityArcaneWolf.class, DataSerializers.FLOAT);
    
    protected static final DataParameter<Float> SYNCED_MAX_HEALTH  = EntityDataManager.<Float>createKey(EntityArcaneWolf.class, DataSerializers.FLOAT);
    protected static final DataParameter<Integer> MANA  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    protected static final DataParameter<Integer> MAX_MANA  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    protected static final DataParameter<PetAction> DATA_PET_ACTION = EntityDataManager.<PetAction>createKey(EntityArcaneWolf.class, PetJobSerializer.instance);
    protected static final DataParameter<Integer> RUNE_COLOR = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    
    protected static final DataParameter<ArcaneWolfElementalType> ELEMENTAL_TYPE = EntityDataManager.<ArcaneWolfElementalType>createKey(EntityArcaneWolf.class, ArcaneWolfElementalTypeSerializer.instance);
    protected static final DataParameter<EMagicElement> TRAINING_ELEMENT = EntityDataManager.<EMagicElement>createKey(EntityArcaneWolf.class, MagicElementDataSerializer.instance);
    protected static final DataParameter<Integer> TRAINING_XP  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    protected static final DataParameter<Integer> TRAINING_LEVEL  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    
    private static final String NBT_SOUL_BOUND = "SoulBound";
    private static final String NBT_ATTR_XP = "AttrXP";
    private static final String NBT_ATTR_LEVEL = "AttrLevel";
    private static final String NBT_ATTR_BOND = "AttrBond";
    private static final String NBT_MANA_REGEN = "AttrManaRegen";
    private static final String NBT_MANA = "Mana";
    private static final String NBT_MAX_MANA = "MaxMana";
    private static final String NBT_SOUL_ID = "SoulID";
    private static final String NBT_SOUL_WORLDID = "SoulWorldID";
    private static final String NBT_RUNE_COLOR = "RuneColor";
    private static final String NBT_TRAINING_ELEMENT = "TrainingElement";
    private static final String NBT_TRAINING_LEVEL = "TrainingLevel";
    private static final String NBT_TRAINING_XP = "TrainingXP";
    private static final String NBT_ELEMENTAL_TYPE = "ElementType";
    private static final String NBT_INVENTORY = "Inventory";
    
    private static final float ARCANE_WOLF_WARN_HEALTH = 10.0f;
    private static final int ARCANE_WOLF_BASE_INV_SIZE = 9;
    private static final int ARCANE_WOLF_EARTH_INV_SIZE_BONUS = 9;
    
    private static final String UUID_MAGIC_RESIST_MOD = "e68d5719-914d-4a14-88bc-a09e2bf8cbd5";
    private static final String UUID_EXTRA_ARMOR_MOD = "516f982f-1d97-468e-8b07-54efe1b819f6";
    
    private IInventory inventory;
    
    private UUID soulID;
    private UUID worldID;
    private int jumpCount;
    
	public EntityArcaneWolf(World worldIn) {
		super(worldIn);
		this.setSize(0.7F, 0.95F);
        
        soulID = UUID.randomUUID();
        worldID = null;
        jumpCount = 0;
        inventory = new InventoryBasic("Arcane Wolf Inventory", true, ARCANE_WOLF_BASE_INV_SIZE);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(SOULBOUND, false);
		dataManager.register(ATTRIBUTE_XP, 0);
		dataManager.register(ATTRIBUTE_LEVEL, 0);
		dataManager.register(ATTRIBUTE_BOND, 0f);
		dataManager.register(ATTRIBUTE_MANA_REGEN, 1f);
		dataManager.register(SYNCED_MAX_HEALTH, 50f);
		dataManager.register(DATA_PET_ACTION, PetAction.WAITING);
		dataManager.register(MANA, 0);
		dataManager.register(MAX_MANA, 100);
		dataManager.register(RUNE_COLOR, 0x00000000);
		dataManager.register(ELEMENTAL_TYPE, ArcaneWolfElementalType.NONELEMENTAL);
		dataManager.register(TRAINING_ELEMENT, EMagicElement.PHYSICAL);
		dataManager.register(TRAINING_XP, 0);
		dataManager.register(TRAINING_LEVEL, 0);
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
		//super.initEntityAI();
		
		this.aiSit = new EntityAISit(this);
		
		int priority = 1;
		this.tasks.addTask(priority++, new EntityAISwimming(this));
		this.tasks.addTask(priority++, this.aiSit);
		this.tasks.addTask(priority++, new ArcaneWolfAIBarrierTask(this, 5));
		this.tasks.addTask(priority++, new ArcaneWolfAIStormTask(this, 35));
		this.tasks.addTask(priority++, new ArcaneWolfAIEldrichTask(this, 40));
		this.tasks.addTask(priority++, new ArcaneWolfAIMysticTask(this, 10));
		this.tasks.addTask(priority++, new ArcaneWolfAINatureTask(this, 25));
		this.tasks.addTask(priority++, new ArcaneWolfAIHellTask(this, 40));
		this.tasks.addTask(priority++, new EntityAILeapAtTarget(this, 0.4F));
		this.tasks.addTask(priority++, new EntityAIAttackMelee(this, 1.0D, true) {
			@Override
			protected void checkAndPerformAttack(EntityLivingBase target, double dist) {

				if (this.attackTick <= 0 && dist > this.getAttackReachSqr(target)) {
					// Too far
					if (EntityArcaneWolf.this.hasWolfCapability(WolfTypeCapability.WOLF_BLINK)
							&& EntityArcaneWolf.this.rand.nextFloat() < .05) {
						Vec3d currentPos = EntityArcaneWolf.this.getPositionVector();
						if (EntityArcaneWolf.this.teleportToEnemy(target)) {
							EntityArcaneWolf.this.world.playSound(null, currentPos.x, currentPos.y, currentPos.z,
									SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.NEUTRAL, 1f, 1f);
							EntityArcaneWolf.this.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1f, 1f);
							
							// If currently training ender, get some xp!
							if (EntityArcaneWolf.this.getTrainingElement() == EMagicElement.ENDER) {
								EntityArcaneWolf.this.addTrainingXP(1);
							}
						}
					}
				}
				
				super.checkAndPerformAttack(target, dist);
			}
		});
		// Attack/Offensive spells
		this.tasks.addTask(priority++, new EntitySpellAttackTask<EntityArcaneWolf>(this, 20 * 3, 4, true, (w) -> {return !w.isSitting();}) {
			private List<Spell> spellList = new ArrayList<>();
			@Override
			protected Spell pickSpell(Spell[] spells, EntityArcaneWolf entity) {
				spellList = EntityArcaneWolf.this.getTargetSpells(getTarget(), spellList);
				if (spellList.isEmpty()) {
					return null;
				}
				return spellList.get(entity.rand.nextInt(spellList.size()));
			}
			
			@Override
			protected void deductMana(Spell spell, EntityArcaneWolf wolf) {
				final int cost = getWolfSpellCost(spell);
				wolf.addMana(-cost);
				wolf.onWolfCast(spell, cost);
				wolf.playSound(SoundEvents.ENTITY_WOLF_GROWL, 1f, .8f);
			}
		});
		// Ally spells
		this.tasks.addTask(priority++, new EntitySpellAttackTask<EntityArcaneWolf>(this, 20 * 3, 20, false, (w) -> {return !w.isSitting();}) {
			private List<Spell> spellList = new ArrayList<>();
			@Override
			protected Spell pickSpell(Spell[] spells, EntityArcaneWolf entity) {
				spellList = EntityArcaneWolf.this.getAllySpells(getTarget(), spellList);
				if (spellList.isEmpty()) {
					return null;
				}
				return spellList.get(entity.rand.nextInt(spellList.size()));
			}
			
			@Override
			protected @Nullable EntityLivingBase getTarget() {
				// Want to make sure to be stable so that multiple calls when finding spell etc.
				// return the same answer
				EntityLivingBase owner = entity.getOwner();
				if (owner != null) {
					List<EntityLivingBase> tames = NostrumMagica.getTamedEntities(owner);
					tames.add(owner);
					tames.removeIf((e) -> { return e.getDistance(entity) > 15;});
					Collections.shuffle(tames, new Random(entity.ticksExisted));
					return tames.get(0);
				} else {
					return null;
				}
			}
			
			@Override
			protected void deductMana(Spell spell, EntityArcaneWolf wolf) {
				final int cost = getWolfSpellCost(spell);
				wolf.addMana(-cost);
				wolf.onWolfCast(spell, cost);
				wolf.playSound(SoundEvents.ENTITY_WOLF_AMBIENT, 1f, .8f);
			}
		});
		// Self spells (longer recast)
		this.tasks.addTask(priority++, new EntitySpellAttackTask<EntityArcaneWolf>(this, 20 * 5, 100, false, (w) -> {return !w.isSitting();}) {
			private List<Spell> spellList = new ArrayList<>();
			@Override
			protected Spell pickSpell(Spell[] spells, EntityArcaneWolf entity) {
				spellList = EntityArcaneWolf.this.getSelfSpells(getTarget(), spellList);
				if (spellList.isEmpty()) {
					return null;
				}
				return spellList.get(entity.rand.nextInt(spellList.size()));
			}
			
			@Override
			protected @Nullable EntityLivingBase getTarget() {
				return entity;
			}
			
			@Override
			protected void deductMana(Spell spell, EntityArcaneWolf wolf) {
				final int cost = getWolfSpellCost(spell);
				wolf.addMana(-cost);
				wolf.onWolfCast(spell, cost);
				wolf.playSound(SoundEvents.ENTITY_WOLF_PANT, 1f, .8f);
			}
		});
		this.tasks.addTask(priority++, new EntityAIFollowOwnerAdvanced<EntityArcaneWolf>(this, 1.5f, 0f, .5f));
		this.tasks.addTask(priority++, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
		//this.tasks.addTask(7, new EntityAIMate(this, 1.0D));
		this.tasks.addTask(priority++, new EntityAIWanderAvoidWater(this, 1.0D));
		this.tasks.addTask(priority++, new EntityAIBeg(this, 8.0F));
		this.tasks.addTask(priority++, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(priority++, new EntityAILookIdle(this));
		
		priority = 1;
		this.targetTasks.addTask(priority++, new EntityAIPetTargetTask<EntityArcaneWolf>(this));
		this.targetTasks.addTask(priority++, new EntityAIOwnerHurtByTarget(this));
		this.targetTasks.addTask(priority++, new EntityAIOwnerHurtTarget(this));
		this.targetTasks.addTask(priority++, new EntityAIHurtByTarget(this, true, new Class[0]));
		this.targetTasks.addTask(priority++, new EntityAINearestAttackableTarget<AbstractSkeleton>(this, AbstractSkeleton.class, false));
	}
	
	@Override
	public EntityLivingBase getLivingOwner() {
		Entity owner = this.getOwner();
		if (owner instanceof EntityLivingBase) {
			return (EntityLivingBase) owner;
		}
		return null;
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
		this.stepHeight = 1.1f;
	}
	
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		
		if (world.isRemote) {
			return;
		}
		
		if (this.getAttackTarget() != null && this.getAttackTarget().isDead) {
			this.setAttackTarget(null);
		}
		
		if (this.ticksExisted % 20 == 0) {
			if (this.getMaxMana() > 0 && this.getMana() < this.getMaxMana()) {
				float amt = this.getManaRegen();
				
				// Augment with bonuses
				amt += this.getEntityAttribute(AttributeManaRegen.instance()).getAttributeValue() / 100.0;
				
				int mana = (int) (amt);
				amt = amt - mana; // Get fraction
				if (amt > 0f && NostrumMagica.rand.nextFloat() < amt)
					mana++;
				
				this.addMana(mana);
			}
		}
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
	public boolean isInLava() {
		if (this.hasWolfCapability(WolfTypeCapability.LAVA_WALK)) {
			return false;
		}
		return super.isInLava();
	}
	
	@Override
	public boolean isPushedByWater() {
		if (this.hasWolfCapability(WolfTypeCapability.LAVA_WALK)) {
			return false;
		}
		return super.isPushedByWater();
	}
	
	@Override
	public boolean isBreedingItem(@Nonnull ItemStack stack) {
		return false;
	}
	
	@Override
	public boolean canMateWith(EntityAnimal otherAnimal) {
		return false;
	}
	
	@Override
	public boolean isInLove() {
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	public float getTailRotation() {
		return (float) (Math.PI * (.35f + .2f * (this.getHealth() / this.getMaxHealth())));
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
					player.openGui(NostrumMagica.instance, NostrumGui.dragonID, this.world, (int) this.posX, (int) this.posY, (int) this.posZ);
					NostrumMagica.proxy.openPetGUI(player, this);
				}
				return true;
			} else if (stack.isEmpty()) {
				if (!this.world.isRemote) {
					if (this.hasWolfCapability(WolfBondCapability.RIDEABLE)) {
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
		return super.getJumpUpwardsMotion() + getBonusJumpHeight();
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
		if (dataManager.get(MAX_MANA) <= 0) {
			this.setMaxMana(100);
		}
		return dataManager.get(MAX_MANA);
	}
	
	protected void setMaxMana(int maxMana) {
		this.dataManager.set(MAX_MANA, Math.max(0, maxMana));
	}
	
	public float getManaRegen() {
		return dataManager.get(ATTRIBUTE_MANA_REGEN);
	}
	
	protected void setManaRegen(float regen) {
		dataManager.set(ATTRIBUTE_MANA_REGEN, Math.max(.01f, regen));
	}
	
	protected void addManaRegen(float diff) {
		setManaRegen(getManaRegen() + diff);
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
		
		while (newXP >= this.getMaxXP()) {
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
			levelUpTrainingElement(this.getTrainingElement());
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
		return (int) (maxXP * (1 + .5 * (this.getTrainingLevel()-1)));
	}
	
	public @Nullable EMagicElement getTrainingElement() {
		final EMagicElement elem = dataManager.get(TRAINING_ELEMENT);
		if (elem == EMagicElement.PHYSICAL) {
			return null;
		}
		return elem;
	}
	
	protected void setTrainingElement(@Nullable EMagicElement element) {
		if (element == null) {
			element = EMagicElement.PHYSICAL;
		}
		dataManager.set(TRAINING_ELEMENT, element);
	}
	
	public boolean startTraining(EMagicElement element) {
		if (element == null || element == EMagicElement.PHYSICAL || this.getTrainingElement() != null) {
			return false;
		}
		
		this.setTrainingLevel(1);
		this.setTrainingXP(0);
		this.setTrainingElement(element);
		return true;
	}
	
	public int getTrainingLevel() {
		return dataManager.get(TRAINING_LEVEL);
	}
	
	protected void setTrainingLevel(int level) {
		dataManager.set(TRAINING_LEVEL, level);
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
		int bonus = 0;
		
		if (this.hasWolfCapability(WolfTypeCapability.WIND_JUMP)) {
			bonus += 1;
		}
		
		if (this.hasWolfCapability(WolfTypeCapability.STORM_JUMP)) {
			bonus += 1;
		}
		
		return bonus;
	}
	
	public float getBonusJumpHeight() {
		// Note: wolf base is .42
		float bonus = 0f;
		
		if (this.hasElementLevel(EMagicElement.WIND, 1)) {
			bonus += .025;
		}
			
		if (this.hasElementLevel(EMagicElement.LIGHTNING, 1)) {
			bonus += .1;
		}
		
		return bonus;
	}
	
	public float getFallReduction() {
		float reduc = 0f;
		
		if (this.hasElementLevel(EMagicElement.WIND, 1)) {
			reduc += .25;
		}
		
		if (this.hasWolfCapability(WolfTypeCapability.WIND_JUMP)) {
			reduc += 1; // Mostly to offset cost of extra jump 
		}
			
		if (this.hasElementLevel(EMagicElement.LIGHTNING, 1)) {
			reduc += 1;
		}
		
		if (this.hasElementLevel(EMagicElement.ENDER, 1)) {
			reduc += .5;
		}
		
		if (this.hasWolfCapability(WolfTypeCapability.STORM_JUMP)) {
			reduc += 2;
		}
		
		return reduc;
	}
	
	protected int getInventorySize() {
		int size = ARCANE_WOLF_BASE_INV_SIZE;
		if (this.hasWolfCapability(WolfTypeCapability.BONUS_INVENTORY)) {
			size += ARCANE_WOLF_EARTH_INV_SIZE_BONUS;
		}
		return size;
	}
	
	protected IInventory ensureInventorySize() {
		final int size = getInventorySize();
		if (this.inventory == null || this.inventory.getSizeInventory() != size) {
			IInventory old = this.inventory;
			this.inventory = new InventoryBasic("Arcane Wolf Inventory", true, size);
			
			if (old != null) {
				// Copy over what we can. Drop the rest
				int i = 0;
				for (; i < Math.min(old.getSizeInventory(), inventory.getSizeInventory()); i++) {
					inventory.setInventorySlotContents(i, old.removeStackFromSlot(i));
				}
				
				for (; i < old.getSizeInventory(); i++) {
					this.entityDropItem(old.getStackInSlot(i), .5f);
				}
			}
		}
		return this.inventory;
	}
	
	public IInventory getInventory() {
		if (world == null || world.isRemote) {
			ensureInventorySize(); // Client doesn't read NBT normally and will have wrong size
		}
		return inventory;
	}
	
	public void wolfJump() {
		if (this.jumpCount == 0 && !this.onGround) {
			// Lose first jump if you didn't jump from the ground
			jumpCount = 1;
		}
		
		if (this.jumpCount < 1 + this.getBonusJumps()) {
			this.jumpCount++;
			this.jump();
		}
	}
	
	@Override
	public void fall(float distance, float damageMulti) {
		super.fall(Math.max(0, distance-this.getFallReduction()), damageMulti);
		this.jumpCount = 0;
	}
	
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		
		compound.setBoolean(NBT_SOUL_BOUND, this.isSoulBound());
		compound.setInteger(NBT_ATTR_XP, this.getXP());
		compound.setInteger(NBT_ATTR_LEVEL, this.getLevel());
		compound.setFloat(NBT_ATTR_BOND, this.getBond());
		compound.setFloat(NBT_MANA_REGEN, this.getManaRegen());
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
			compound.setInteger(NBT_TRAINING_LEVEL, this.getTrainingLevel());
		}
		compound.setInteger(NBT_TRAINING_XP, this.getTrainingXP());
		compound.setString(NBT_ELEMENTAL_TYPE, this.getElementalType().name());
		
		compound.setTag(NBT_INVENTORY, Inventories.serializeInventory(inventory));
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		
		this.setSoulBound(compound.getBoolean(NBT_SOUL_BOUND));
		this.setXP(compound.getInteger(NBT_ATTR_XP));
		this.setLevel(compound.getInteger(NBT_ATTR_LEVEL));
		this.setBond(compound.getFloat(NBT_ATTR_BOND));
		this.setManaRegen(compound.getFloat(NBT_MANA_REGEN));
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
			this.setTrainingLevel(compound.getInteger(NBT_TRAINING_LEVEL));
		}
		this.setTrainingXP(compound.getInteger(NBT_TRAINING_XP));
		try {
			this.setElementalType(ArcaneWolfElementalType.valueOf(compound.getString(NBT_ELEMENTAL_TYPE).toUpperCase()));
		} catch (Exception e) {
			; // Unfortunately, expected because of the summon command
			this.setElementalType(ArcaneWolfElementalType.NONELEMENTAL);
		}
		
		ensureInventorySize();
		Inventories.deserializeInventory(inventory, compound.getTag(NBT_INVENTORY));
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
		if (!this.world.isRemote) {
			if (this.inventory != null) {
				for (int i = 0; i < inventory.getSizeInventory(); i++) {
					ItemStack stack = inventory.getStackInSlot(i);
					if (!stack.isEmpty()) {
						EntityItem item = new EntityItem(this.world, this.posX, this.posY, this.posZ, stack);
						this.world.spawnEntity(item);
					}
				}
			}
			
//			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
//				ItemStack stack = equipment.getStackInSlot(slot);
//				if (!stack.isEmpty()) {
//					EntityItem item = new EntityItem(this.world, this.posX, this.posY, this.posZ, stack);
//					this.world.spawnEntity(item);
//				}
//			}
//			equipment.clear();
			inventory.clear();
		}
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
	public boolean attackEntityAsMob(Entity entityIn) {
		boolean hit = super.attackEntityAsMob(entityIn);
		
		if (hit && !world.isRemote) {
			EntityLivingBase owner = this.getOwner();
			if (owner != null) {
				final double dist = owner.getDistance(owner);
				if (dist <= 10) {
					this.addBond(dist < 4 ? .5f : .2f);
				}
			}
			if (rand.nextBoolean() && rand.nextBoolean()) {
				this.addXP(1);
			}
		}
		
		return hit;
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
			
			final ItemStack stack = ArcaneWolfSoulItem.MakeSoulItem(this, true);
			if (!stack.isEmpty()) {
				this.entityDropItem(stack, 1f);
				this.attackEntityFrom(DamageSource.GENERIC, 1000000f);
			}
			
			// Award lore about soul bonding
			INostrumMagic attr = NostrumMagica.getMagicWrapper(stabber);
			if (attr != null) {
				attr.giveFullLore(SoulBoundLore.instance());
			}
			
			return true;
		}
		return false;
	}
	
	public void levelup() {
		int level = this.getLevel();
		
		Random rand = getRNG();
		float roll;
		
//		// Jump height:
//		float jumpHeight = 0f;
//		if (rand.nextBoolean()) {
//			jumpHeight = rand.nextFloat() * 0.05f; // 0-5%
//		}
		
		// Health
		// 20% 0, 40% 2, 20% 4, 10% 5
		// (EV: 2.1)
		float health;
		roll = rand.nextFloat();
		if (roll < .2f) { // 20%
			health = 0;
		} else if (roll < .6f) { // 40%
			health = 2;
		} else if (roll < .8f) { // 20%
			health = 4;
		} else {
			health = 5;
		}
		if (this.hasFullElement(EMagicElement.EARTH)) {
			health += 2;
		}
		if (this.hasFullElement(EMagicElement.ICE)) {
			health += 1;
		}
		
		
		// Mana
		// 75% 20, 15% 40, 10% 50
		// (EV: 26)
		int mana = 0;
		roll = rand.nextFloat();
		if (roll < .75f) {
			mana = 20;
		} else if (roll < .9f) {
			mana = 40;
		} else {
			mana = 50;
		}
		if (this.hasFullElement(EMagicElement.ICE)) {
			mana += 20;
		}
		if (this.hasFullElement(EMagicElement.LIGHTNING)) {
			mana += 10;
		}
		if (this.hasFullElement(EMagicElement.ENDER)) {
			mana += 10;
		}
		
		// Mana regen
		// 50% .2, 20% .25, 20% .3, 10% .5
		// (EV: .26)
		float regen = 0f;
		roll = rand.nextFloat();
		if (roll < .5) {
			regen = .2f;
		} else if (roll < .7) {
			regen = .25f;
		} else if (roll < .9) {
			regen = .3f;
		} else {
			regen = .5f;
		}
		
		
		health += this.getMaxHealth();
		mana += this.getMaxMana();
		
		this.setMaxHealth(health);
		this.setHealth(this.getMaxHealth());
		this.setMaxMana(mana);
		this.setMana(mana);
		this.addManaRegen(regen);
		this.setLevel(level + 1);
		
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
	
	protected void playTrainingLevelUp(EMagicElement element) {
		if (!this.world.isRemote) {
			this.playSound(SoundEvents.ENTITY_WOLF_PANT, 1f, 1f);
			NostrumParticles.GLOW_ORB.spawn(this.world, new SpawnParams(
					50, posX, posY, posZ, 3.0, 30, 10, this.getEntityId()
					).color(element.getColor()).dieOnTarget(true));
		}
	}
	
	protected void levelUpTrainingElement(EMagicElement element) {
		if (element == null || element == EMagicElement.PHYSICAL) {
			return;
		}
		
		int level = this.getTrainingLevel() + 1;
		playTrainingLevelUp(element);
		if (level >= 3) {
			level = 0;
			finishTrainingElement(element);
		}
		this.setTrainingLevel(level);
	}
	
	protected void playTrainingFinishEffects(EMagicElement element) {
		if (!world.isRemote) {
			this.playSound(SoundEvents.ENTITY_WOLF_HOWL, 1f, 1f);
			NostrumParticles.FILLED_ORB.spawn(this.world, new SpawnParams(
					100, posX, posY, posZ, 3.0, 60, 20, this.getEntityId()
					).color(element.getColor()));
		}
	}
	
	/**
	 * Wolf has finished training in the provided element. Do any special effects for the element.
	 * @param element
	 */
	protected void doBonusTrainingEffects(EMagicElement element) {
		if (element == EMagicElement.EARTH) {
			// Earth upgrades inventory size!
			ensureInventorySize(); // Auto resizes
			// And adds armor
			this.getEntityAttribute(SharedMonsterAttributes.ARMOR).applyModifier(new AttributeModifier(
					UUID.fromString(UUID_EXTRA_ARMOR_MOD),
					"ArcaneWolfEarthArmor",
					5.0D,
					0
					));
		}
		if (element == EMagicElement.LIGHTNING) {
			// Lightning gives bonus magic resistance!
			this.getEntityAttribute(AttributeMagicResist.instance()).applyModifier(new AttributeModifier(
					UUID.fromString(UUID_MAGIC_RESIST_MOD),
					"ArcaneWolfLightningResist",
					30.0D,
					0
					));
		}
		
		// Set new rune color based on new type
		ArcaneWolfElementalType type = this.getElementalType();
		final int color = type.getColor();
		this.setRuneColor(color);
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
			this.setTrainingElement(null);
			this.setElementalType(result);
			playTrainingFinishEffects(element);
			this.doBonusTrainingEffects(element);
		}
	}
	
	/**
	 * Helper func to see if this wolf has trained the specified element up to the provided level.
	 * Levels are 1,2,3.
	 * Elements that aren't in training anymore but are part of our elemental type are
	 * considered level 3 -- fully trained.
	 * @param element
	 * @param level
	 * @return
	 */
	protected boolean hasElementLevel(EMagicElement element, int level) {
		if (this.getTrainingElement() == element) {
			return this.getTrainingLevel() >= level;
		}
		
		final ArcaneWolfElementalType type = this.getElementalType();
		return type.primary == element || type.secondary == element;
	}
	
	protected boolean hasFullElement(EMagicElement element) {
		return hasElementLevel(element, 3);
	}
	
	@Override
	public PetContainer<EntityArcaneWolf> getGUIContainer(EntityPlayer player) {
		return new PetContainer<>(this, player,
				new ArcaneWolfInfoSheet(this),
				new ArcaneWolfBondInfoSheet(this),
				new ArcaneWolfInventorySheet(this),
				new ArcaneWolfTrainingSheet(this)
				);
	}

	@Override
	public PetGUIStatAdapter<EntityArcaneWolf> getGUIAdapter() {
		return new PetGUIStatAdapter<EntityArcaneWolf>() {

			@Override
			public float getSecondaryAmt(EntityArcaneWolf pet) {
				return pet.getMana();
			}

			@Override
			public float getMaxSecondaryAmt(EntityArcaneWolf pet) {
				return pet.getMaxMana();
			}

			@Override
			public String getSecondaryLabel(EntityArcaneWolf pet) {
				return "Mana";
			}

			@Override
			public float getTertiaryAmt(EntityArcaneWolf pet) {
				return pet.getBond();
			}

			@Override
			public float getMaxTertiaryAmt(EntityArcaneWolf pet) {
				return 1f;
			}

			@Override
			public String getTertiaryLabel(EntityArcaneWolf pet) {
				return "Bond";
			}

			@Override
			public float getQuaternaryAmt(EntityArcaneWolf pet) {
				return pet.getXP();
			}

			@Override
			public float getMaxQuaternaryAmt(EntityArcaneWolf pet) {
				return pet.getMaxXP();
			}

			@Override
			public String getQuaternaryLabel(EntityArcaneWolf pet) {
				return "XP";
			}
		};
	}
	
	public boolean hasWolfCapability(WolfBondCapability capability) {
		return this.getBond() >= capability.bond;
	}
	
	public boolean hasWolfCapability(WolfTypeCapability capability) {
		if (capability.getType() != null) {
			return this.getElementalType() == capability.getType();
		}
		
		return this.hasElementLevel(capability.getSubType(), capability.getSubTypeLevel());
	}
	
	protected boolean teleportToEnemy(EntityLivingBase target) {
		if (target == null || target.isDead) {
			return false;
		}
		
		return EntityAIFollowOwnerGeneric.TeleportAroundEntity(this, target);
	}
	
	protected List<Spell> getTargetSpells(EntityLivingBase target, List<Spell> listToAddTo) {
		listToAddTo.clear();
		for (WolfSpell spell : WolfSpell.values()) {
			if (this.getMana() >= spell.getCost()
					&& spell.matches(WolfSpellTargetGroup.ENEMY, this, target)) {
				listToAddTo.add(spell.getSpell());
			}
		}
		return listToAddTo;
	}
	
	protected List<Spell> getSelfSpells(EntityLivingBase target, List<Spell> listToAddTo) {
		listToAddTo.clear();
		for (WolfSpell spell : WolfSpell.values()) {
			if (this.getMana() >= spell.getCost()
					&& spell.matches(WolfSpellTargetGroup.SELF, this, target)) {
				listToAddTo.add(spell.getSpell());
			}
		}
		return listToAddTo;
	}
	
	protected List<Spell> getAllySpells(EntityLivingBase target, List<Spell> listToAddTo) {
		listToAddTo.clear();
		for (WolfSpell spell : WolfSpell.values()) {
			if (this.getMana() >= spell.getCost()
					&& spell.matches(WolfSpellTargetGroup.ALLY, this, target)) {
				listToAddTo.add(spell.getSpell());
			}
		}
		return listToAddTo;
	}
	
	protected int getWolfSpellCost(Spell realSpell) {
		for (WolfSpell spell : WolfSpell.values()) {
			if (spell.getSpell() == realSpell) {
				return spell.getCost();
			}
		}
		return 0;
	}
	
	protected void onWolfCast(Spell spell, int cost) {
		if (getTrainingElement() == spell.getPrimaryElement()) {
			addTrainingXP(Math.max(1, (int)Math.ceil((float)cost/25f)));
		}
	}
	
	public static EntityArcaneWolf TransformWolf(EntityWolf wolf, EntityPlayer player) {
		EntityArcaneWolf newWolf = new EntityArcaneWolf(wolf.world);
		newWolf.setPosition(wolf.posX, wolf.posY, wolf.posZ);
		newWolf.setTamedBy(player);
		newWolf.setHealth(5f);
		wolf.setDead();
		wolf.world.spawnEntity(newWolf);
		return newWolf;
	}
	
	public static final class WolfTameLore implements ILoreTagged {
		
		private static WolfTameLore instance = null;
		public static WolfTameLore instance() {
			if (instance == null) {
				instance = new WolfTameLore();
			}
			return instance;
		}

		@Override
		public String getLoreKey() {
			return "lore_wolf_tame";
		}

		@Override
		public String getLoreDisplayName() {
			return "Tamed Wolves";
		}

		@Override
		public Lore getBasicLore() {
			return new Lore().add("Feeding a bone to this wild wolf tamed it!");
		}

		@Override
		public Lore getDeepLore() {
			return new Lore().add("Feeding a bone to this wild wolf tamed it!");
		}

		@Override
		public InfoScreenTabs getTab() {
			// Don't actually display! We're going to show our own page!
			return InfoScreenTabs.INFO_ENTITY;
		}
		
	}

	@Override
	public boolean isEntityTamed() {
		return this.isTamed();
	}

	@Override
	public boolean isEntitySitting() {
		return this.isSitting();
	}
}
