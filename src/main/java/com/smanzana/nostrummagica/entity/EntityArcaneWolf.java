package com.smanzana.nostrummagica.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.NostrumAttributes;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.gui.petgui.arcanewolf.ArcaneWolfAbilitySheet;
import com.smanzana.nostrummagica.client.gui.petgui.arcanewolf.ArcaneWolfBondInfoSheet;
import com.smanzana.nostrummagica.client.gui.petgui.arcanewolf.ArcaneWolfInfoSheet;
import com.smanzana.nostrummagica.client.gui.petgui.arcanewolf.ArcaneWolfInventorySheet;
import com.smanzana.nostrummagica.client.gui.petgui.arcanewolf.ArcaneWolfTrainingSheet;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.entity.tasks.EntitySpellAttackTask;
import com.smanzana.nostrummagica.entity.tasks.FollowOwnerGenericGoal;
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
import com.smanzana.nostrummagica.serializers.ArcaneWolfElementalTypeSerializer;
import com.smanzana.nostrummagica.serializers.MagicElementDataSerializer;
import com.smanzana.nostrummagica.serializers.PetJobSerializer;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.SpellShapePartProperties;
import com.smanzana.nostrummagica.spells.components.SpellEffectPart;
import com.smanzana.nostrummagica.spells.components.SpellShapePart;
import com.smanzana.nostrummagica.spells.components.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spells.components.shapes.TouchShape;
import com.smanzana.nostrummagica.utils.ArrayUtil;
import com.smanzana.nostrummagica.utils.Inventories;
import com.smanzana.petcommand.api.PetCommandAPI;
import com.smanzana.petcommand.api.PetFuncs;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.client.petgui.PetGUIStatAdapter;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.entity.ITameableEntity;
import com.smanzana.petcommand.api.pet.PetInfo;
import com.smanzana.petcommand.api.pet.PetInfo.PetAction;
import com.smanzana.petcommand.api.pet.PetInfo.SecondaryFlavor;

import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.BegGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtByTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtTargetGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.Team;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityArcaneWolf extends WolfEntity implements ITameableEntity, IEntityPet, IPetWithSoul, IStabbableEntity, IMagicEntity {
	
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
	
	public static interface IWolfAbility {
		public ITextComponent getName();
		public ITextComponent getDescription();
		public @Nullable WolfSpellTargetGroup getTargetGroup();
		public int getCost();
	}
	
	public static enum WolfSpellTargetGroup {
		SELF,
		ALLY,
		ENEMY,
	}
	
	protected static enum WolfSpell implements IWolfAbility {
		GROUP_SPEED("packspeed", WolfSpellTargetGroup.SELF, 50,
				(Spell.CreateAISpell("WolfSpeed")).addPart(new SpellShapePart(NostrumSpellShapes.Chain, new SpellShapePartProperties(8, true))).addPart(new SpellEffectPart(EMagicElement.WIND, 1, EAlteration.SUPPORT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.WIND, 1),
				(wolf, target) -> {
					return wolf.getAttackTarget() == null // Not in battle
							&& wolf.getMana() >= wolf.getMaxMana() * .30 // >= 30% mana
							;
				}),
		WIND_CUTTER("windcutter", WolfSpellTargetGroup.ENEMY, 20,
				(Spell.CreateAISpell("WolfWindCutter")).addPart(new SpellShapePart(NostrumSpellShapes.Cutter)).addPart(new SpellEffectPart(EMagicElement.WIND, 2, EAlteration.RUIN)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.WIND, 3),
				(wolf, target) -> true),
		ROOTS("roots", WolfSpellTargetGroup.ENEMY, 25,
				(Spell.CreateAISpell("WolfRoots")).addPart(new SpellShapePart(NostrumSpellShapes.SeekingBullet)).addPart(new SpellEffectPart(EMagicElement.EARTH, 2, EAlteration.INFLICT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.EARTH, 1),
				(wolf, target) -> {
					return target.getActivePotionEffect(NostrumEffects.rooted) == null;
				}),
		REGEN("wolfregen", WolfSpellTargetGroup.ALLY, 50,
				(Spell.CreateAISpell("WolfRegen")).addPart(new SpellShapePart(NostrumSpellShapes.AI)).addPart(new SpellEffectPart(EMagicElement.EARTH, 2, EAlteration.GROWTH)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.EARTH, 3),
				(wolf, target) -> {
					return target.getHealth() < target.getMaxHealth()
							&& target.getActivePotionEffect(Effects.REGENERATION) == null;
				}),
		MAGIC_SHIELD("magicshield", WolfSpellTargetGroup.SELF, 30,
				(Spell.CreateAISpell("WolfMagicShield")).addPart(new SpellShapePart(NostrumSpellShapes.Chain, new SpellShapePartProperties(8, true))).addPart(new SpellEffectPart(EMagicElement.ICE, 1, EAlteration.SUPPORT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.ICE, 1),
				(wolf, target) -> {
					return wolf.getAttackTarget() != null; // Don't want to cast out of battle
				}),
		WOLF_HEAL("heal", null, 20,
				(Spell.CreateAISpell("WolfHeal")).addPart(new SpellShapePart(NostrumSpellShapes.AI)).addPart(new SpellEffectPart(EMagicElement.ICE, 2, EAlteration.GROWTH)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.ICE, 2),
				(wolf, target) -> {
					if (target.isEntityUndead()) {
						// An attack against undead!
						return !NostrumMagica.IsSameTeam(wolf, target);
					} else {
						return target.getHealth() < target.getMaxHealth() && NostrumMagica.IsSameTeam(wolf, target);
					}
				}),
		ICE_FANGS("icefang", WolfSpellTargetGroup.SELF, 100,
				(Spell.CreateAISpell("WolfIceFangs")).addPart(new SpellShapePart(NostrumSpellShapes.Chain, new SpellShapePartProperties(8, true))).addPart(new SpellEffectPart(EMagicElement.ICE, 2, EAlteration.ENCHANT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.ICE, 3),
				(wolf, target) -> {
					return wolf.getAttackTarget() != null; // Don't want to cast out of battle
				}),
		FIRE_TOUCH("firefang", WolfSpellTargetGroup.ENEMY, 10,
				(Spell.CreateAISpell("WolfFireBite")).addPart(new SpellShapePart(NostrumSpellShapes.Touch)).addPart(new SpellEffectPart(EMagicElement.FIRE, 2, EAlteration.RUIN)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.FIRE, 1),
				(wolf, target) -> {
					return wolf.getDistance(target) <= TouchShape.TOUCH_RANGE;
				}),
		MAGIC_BOOST("magicboost", WolfSpellTargetGroup.ALLY, 20,
				(Spell.CreateAISpell("WolfMagicBoost")).addPart(new SpellShapePart(NostrumSpellShapes.AI)).addPart(new SpellEffectPart(EMagicElement.FIRE, 1, EAlteration.SUPPORT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.FIRE, 3),
				(wolf, target) -> {
					return target.getActivePotionEffect(NostrumEffects.magicBoost) == null
							&& (wolf.getAttackTarget() != null || wolf.getMana() >= wolf.getMaxMana() * .75) // in battle or >= 75% mana
							;
				}),
		ENDER_SHROUD("endershroud", WolfSpellTargetGroup.ENEMY, 20,
				(Spell.CreateAISpell("WolfEnderShroud")).addPart(new SpellShapePart(NostrumSpellShapes.SeekingBullet)).addPart(new SpellShapePart(NostrumSpellShapes.Burst, new SpellShapePartProperties(3, true))).addPart(new SpellEffectPart(EMagicElement.ENDER, 2, null)).addPart(new SpellEffectPart(EMagicElement.ENDER, 1, EAlteration.INFLICT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.ENDER, 1),
				(wolf, target) -> true),
		ENDER_FANGS("enderfang", WolfSpellTargetGroup.SELF, 75,
				(Spell.CreateAISpell("WolfEnderFangs")).addPart(new SpellShapePart(NostrumSpellShapes.Chain, new SpellShapePartProperties(8, true))).addPart(new SpellEffectPart(EMagicElement.ENDER, 1, EAlteration.ENCHANT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.ENDER, 3),
				(wolf, target) -> {
					return wolf.getAttackTarget() != null; // Don't want to cast out of battle
				}),
		SLOW("slow", WolfSpellTargetGroup.ENEMY, 10,
				(Spell.CreateAISpell("WolfSlow")).addPart(new SpellShapePart(NostrumSpellShapes.AI)).addPart(new SpellEffectPart(EMagicElement.LIGHTNING, 1, EAlteration.INFLICT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.LIGHTNING, 1),
				(wolf, target) -> {
					return target.getActivePotionEffect(Effects.SLOWNESS) == null;
				}),
		CHAIN_LIGHTNING("chainlighting", WolfSpellTargetGroup.ENEMY, 40,
				(Spell.CreateAISpell("WolfChainLightning")).addPart(new SpellShapePart(NostrumSpellShapes.SeekingBullet)).addPart(new SpellShapePart(NostrumSpellShapes.Chain, new SpellShapePartProperties(6, true))).addPart(new SpellEffectPart(EMagicElement.LIGHTNING, 2, EAlteration.RUIN)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.LIGHTNING, 3),
				(wolf, target) -> true),
		;
		
		private static interface ISpellPredicate {
			public boolean apply(EntityArcaneWolf wolf, LivingEntity target);
		}
		
		private static interface IWolfPredicate {
			public boolean apply(EntityArcaneWolf wolf);
		}
		
		protected final String key;
		private final @Nullable WolfSpellTargetGroup group;
		private final Spell spell;
		private final ISpellPredicate predicate;
		private final IWolfPredicate wolfChecker;
		private final int cost;
		
		private final ITextComponent name;
		private final ITextComponent description;
		
		private WolfSpell(String key, @Nullable WolfSpellTargetGroup group, int cost, Spell spell, IWolfPredicate wolfChecker, ISpellPredicate predicate) {
			this.key = key;
			this.group = group;
			this.spell = spell;
			this.predicate = predicate;
			this.wolfChecker = wolfChecker;
			this.cost = cost;
			
			this.name = new TranslationTextComponent("info.wolf_ability." + key + ".name");
			this.description = new TranslationTextComponent("info.wolf_ability." + key + ".desc");
		}
		
		public Spell getSpell() {
			return spell;
		}
		
		public boolean matches(WolfSpellTargetGroup group, EntityArcaneWolf wolf, LivingEntity target) {
			return (this.group == null || this.group == group) && canCast(wolf) && predicate.apply(wolf, target);
		}
		
		public boolean canCast(EntityArcaneWolf wolf) {
			return wolfChecker.apply(wolf);
		}
		
		@Override
		public int getCost() {
			return this.cost;
		}
		
		@Override
		public WolfSpellTargetGroup getTargetGroup() {
			return this.group;
		}
		
		@Override
		public ITextComponent getName() {
			return name;
		}
		
		@Override
		public ITextComponent getDescription() {
			return description;
		}
	}
	
	private static final class StubbedWolfAbility implements IWolfAbility {
		private final ITextComponent name;
		private final ITextComponent desc;
		private final int manaCost;
		private final WolfSpellTargetGroup group;
		
		public StubbedWolfAbility(String key, int manaCost, WolfSpellTargetGroup group) {
			super();
			this.manaCost = manaCost;
			this.group = group;
			this.name = new TranslationTextComponent("info.wolf_ability." + key + ".name");
			this.desc = new TranslationTextComponent("info.wolf_ability." + key + ".desc");
		}
		
		@Override
		public ITextComponent getName() {
			return name;
		}
		
		@Override
		public ITextComponent getDescription() {
			return desc;
		}
		
		@Override
		public WolfSpellTargetGroup getTargetGroup() {
			return group;
		}
		
		@Override
		public int getCost() {
			return manaCost;
		}
		
	}
	
	private static final IWolfAbility ABILITY_INFO_BARRIER = new StubbedWolfAbility("class_barrier", 5, WolfSpellTargetGroup.ALLY);
	private static final IWolfAbility ABILITY_INFO_STORM = new StubbedWolfAbility("class_storm", 30, WolfSpellTargetGroup.ENEMY);
	private static final IWolfAbility ABILITY_INFO_ELDRICH = new StubbedWolfAbility("class_elrich", 40, WolfSpellTargetGroup.ENEMY);
	private static final IWolfAbility ABILITY_INFO_MYSTIC = new StubbedWolfAbility("class_mystic", 10, WolfSpellTargetGroup.ALLY);
	private static final IWolfAbility ABILITY_INFO_NATURE = new StubbedWolfAbility("class_nature", 25, WolfSpellTargetGroup.ALLY);
	private static final IWolfAbility ABILITY_INFO_HELL = new StubbedWolfAbility("class_hell", 40, WolfSpellTargetGroup.ENEMY);
	
	public static final String ID = "entity_arcane_wolf";
	
	protected static final DataParameter<Boolean> SOULBOUND = EntityDataManager.<Boolean>createKey(EntityArcaneWolf.class, DataSerializers.BOOLEAN);
	
	protected static final DataParameter<Integer> ATTRIBUTE_XP  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    protected static final DataParameter<Integer> ATTRIBUTE_LEVEL  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    protected static final DataParameter<Float> ATTRIBUTE_BOND  = EntityDataManager.<Float>createKey(EntityArcaneWolf.class, DataSerializers.FLOAT);
    protected static final DataParameter<Float> ATTRIBUTE_MANA_REGEN  = EntityDataManager.<Float>createKey(EntityArcaneWolf.class, DataSerializers.FLOAT);
    
    protected static final DataParameter<Float> SYNCED_MAX_HEALTH  = EntityDataManager.<Float>createKey(EntityArcaneWolf.class, DataSerializers.FLOAT);
    protected static final DataParameter<Integer> MANA  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    protected static final DataParameter<Integer> MAX_MANA  = EntityDataManager.<Integer>createKey(EntityArcaneWolf.class, DataSerializers.VARINT);
    protected static final DataParameter<PetAction> DATA_PET_ACTION = EntityDataManager.<PetAction>createKey(EntityArcaneWolf.class, PetJobSerializer.GetInstance());
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
    
	public EntityArcaneWolf(EntityType<? extends EntityArcaneWolf> type, World worldIn) {
		super(type, worldIn);
        
        soulID = UUID.randomUUID();
        worldID = null;
        jumpCount = 0;
        inventory = new Inventory(ARCANE_WOLF_BASE_INV_SIZE);
	}
	
	@Override
	protected void registerData() {
		super.registerData();
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
		
	
	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return WolfEntity.func_234233_eS_()
			.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.35D)
			.createMutableAttribute(Attributes.MAX_HEALTH, 50.0D)
			.createMutableAttribute(Attributes.ARMOR, 10.0D)
			.createMutableAttribute(Attributes.FOLLOW_RANGE, 60.0)
			.createMutableAttribute(NostrumAttributes.magicResist, 20.0D)
			.createMutableAttribute(Attributes.ATTACK_DAMAGE, 6.0D);
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
	protected void registerGoals() {
		//super.initEntityAI();
		
		int priority = 1;
		this.goalSelector.addGoal(priority++, new SwimGoal(this));
		this.goalSelector.addGoal(priority++, new SitGoal(this));
		this.goalSelector.addGoal(priority++, new ArcaneWolfAIBarrierTask(this, 5));
		this.goalSelector.addGoal(priority++, new ArcaneWolfAIStormTask(this, 35));
		this.goalSelector.addGoal(priority++, new ArcaneWolfAIEldrichTask(this, 40));
		this.goalSelector.addGoal(priority++, new ArcaneWolfAIMysticTask(this, 10));
		this.goalSelector.addGoal(priority++, new ArcaneWolfAINatureTask(this, 25));
		this.goalSelector.addGoal(priority++, new ArcaneWolfAIHellTask(this, 40));
		this.goalSelector.addGoal(priority++, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(priority++, new MeleeAttackGoal(this, 1.0D, true) {
			@Override
			protected void checkAndPerformAttack(LivingEntity target, double dist) {

				if (this.func_234040_h_() /*attackTick <= 0*/ && dist > this.getAttackReachSqr(target)) {
					// Too far
					if (EntityArcaneWolf.this.hasWolfCapability(WolfTypeCapability.WOLF_BLINK)
							&& EntityArcaneWolf.this.rand.nextFloat() < .05) {
						Vector3d currentPos = EntityArcaneWolf.this.getPositionVec();
						if (EntityArcaneWolf.this.teleportToEnemy(target)) {
							EntityArcaneWolf.this.world.playSound(null, currentPos.x, currentPos.y, currentPos.z,
									SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.NEUTRAL, 1f, 1f);
							EntityArcaneWolf.this.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
							
							// If currently training ender, get some xp!
							if (EntityArcaneWolf.this.getTrainingElement() == EMagicElement.ENDER) {
								EntityArcaneWolf.this.addTrainingXP(1);
							}
							
							if (EntityArcaneWolf.this.rand.nextBoolean()) {
								EntityArcaneWolf.this.addXP(1);
							}
						}
					}
				}
				
				super.checkAndPerformAttack(target, dist);
			}
		});
		// Attack/Offensive spells
		this.goalSelector.addGoal(priority++, new EntitySpellAttackTask<EntityArcaneWolf>(this, 20 * 3, 4, true, (w) -> {return !w.isSitting();}) {
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
				
				if (EntityArcaneWolf.this.rand.nextBoolean()) {
					EntityArcaneWolf.this.addXP(1);
				}
			}
		});
		// Ally spells
		this.goalSelector.addGoal(priority++, new EntitySpellAttackTask<EntityArcaneWolf>(this, 20 * 3, 20, false, (w) -> {return !w.isSitting();}) {
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
			protected @Nullable LivingEntity getTarget() {
				// Want to make sure to be stable so that multiple calls when finding spell etc.
				// return the same answer
				LivingEntity owner = entity.getOwner();
				if (owner != null) {
					List<LivingEntity> tames = PetFuncs.GetTamedEntities(owner);
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
				
				if (EntityArcaneWolf.this.rand.nextBoolean()) {
					EntityArcaneWolf.this.addXP(1);
				}
			}
		});
		// Self spells (longer recast)
		this.goalSelector.addGoal(priority++, new EntitySpellAttackTask<EntityArcaneWolf>(this, 20 * 5, 100, false, (w) -> {return !w.isSitting();}) {
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
			protected @Nullable LivingEntity getTarget() {
				return entity;
			}
			
			@Override
			protected void deductMana(Spell spell, EntityArcaneWolf wolf) {
				final int cost = getWolfSpellCost(spell);
				wolf.addMana(-cost);
				wolf.onWolfCast(spell, cost);
				wolf.playSound(SoundEvents.ENTITY_WOLF_PANT, 1f, .8f);
				
				if (EntityArcaneWolf.this.rand.nextBoolean()) {
					EntityArcaneWolf.this.addXP(1);
				}
			}
		});
		//this.goalSelector.addGoal(priority++, new FollowOwnerAdvancedGoal<EntityArcaneWolf>(this, 1.5f, 0f, .5f));
		this.goalSelector.addGoal(priority++, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, true));
		//this.goalSelector.addGoal(7, new EntityAIMate(this, 1.0D));
		this.goalSelector.addGoal(priority++, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(priority++, new BegGoal(this, 8.0F));
		this.goalSelector.addGoal(priority++, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(priority++, new LookRandomlyGoal(this));
		
		priority = 1;
		//this.targetSelector.addGoal(priority++, new PetTargetGoal<EntityArcaneWolf>(this));
		this.targetSelector.addGoal(priority++, new OwnerHurtByTargetGoal(this));
		this.targetSelector.addGoal(priority++, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setCallsForHelp(EntityArcaneWolf.class, WolfEntity.class));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<AbstractSkeletonEntity>(this, AbstractSkeletonEntity.class, false));
	}
	
	@Override
	public LivingEntity getLivingOwner() {
		Entity owner = this.getOwner();
		if (owner instanceof LivingEntity) {
			return (LivingEntity) owner;
		}
		return null;
	}

	@Override
	public void baseTick() {
		super.baseTick();
		
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
	public void tick() {
		super.tick();
		this.stepHeight = 1.1f;
		checkAndHandleLava();
	}
	
	@Override
	public void livingTick() {
		super.livingTick();
		
		if (world.isRemote) {
			return;
		}
		
		if (this.getAttackTarget() != null && !this.getAttackTarget().isAlive()) {
			this.setAttackTarget(null);
		}
		
		if (this.ticksExisted % 20 == 0) {
			if (this.getMaxMana() > 0 && this.getMana() < this.getMaxMana()) {
				float amt = this.getManaRegen();
				
				// Augment with bonuses
				amt += this.getAttribute(NostrumAttributes.manaRegen).getValue() / 100.0;
				
				int mana = (int) (amt);
				amt = amt - mana; // Get fraction
				if (amt > 0f && NostrumMagica.rand.nextFloat() < amt)
					mana++;
				
				this.addMana(mana);
			}
		}
	}
	
	@Override
	public void travel(Vector3d move) {
		//super.travel(strafe, vertical, forward);
		
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
			double strafe = entitylivingbase.moveStrafing * 0.45F;
			double forward = entitylivingbase.moveForward * .7f;

			if (forward < 0.0F) {
				forward *= 0.5F;
			}
			
			this.jumpMovementFactor = this.getAIMoveSpeed() * 0.33F;

			if (this.canPassengerSteer()) {
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
				super.travel(new Vector3d(strafe, move.y, forward));
			}
			else if (entitylivingbase instanceof PlayerEntity) {
				this.setMotion(Vector3d.ZERO);
			}

			this.prevLimbSwingAmount = this.limbSwingAmount;
			double d1 = this.getPosX() - this.prevPosX;
			double d0 = this.getPosZ() - this.prevPosZ;
			float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;

			if (f2 > 1.0F) {
				f2 = 1.0F;
			}

			this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
			this.limbSwing += this.limbSwingAmount;
		}
		else
		{
			this.jumpMovementFactor = 0.02F;
			super.travel(move);
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
	
	protected void checkAndHandleLava() {
		if (this.hasWolfCapability(WolfTypeCapability.LAVA_WALK) && super.isInLava()) {
			// Copied from Strider
			ISelectionContext iselectioncontext = ISelectionContext.forEntity(this);
			if (iselectioncontext.func_216378_a(FlowingFluidBlock.LAVA_COLLISION_SHAPE, this.getPosition(), true) && !this.world.getFluidState(this.getPosition().up()).isTagged(FluidTags.LAVA)) {
				this.onGround = true;
			} else {
				this.setMotion(this.getMotion().scale(0.5D).add(0.0D, 0.05D, 0.0D));
			}
		}
	}
	
	@Override
	public boolean func_230285_a_(Fluid fluid) {
		// Copied from Strider
		if (this.hasWolfCapability(WolfTypeCapability.LAVA_WALK)) {
			return fluid.isIn(FluidTags.LAVA);
		}
		
		return super.func_230285_a_(fluid);
	}
	
	@Override
	public boolean isBreedingItem(@Nonnull ItemStack stack) {
		return false;
	}
	
	@Override
	public boolean canMateWith(AnimalEntity otherAnimal) {
		return false;
	}
	
	@Override
	public boolean isInLove() {
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
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
	public ActionResultType /*processInteract*/ func_230254_b_(PlayerEntity player, Hand hand) {
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
						this.addTrainingXP(500);
					}
				}
				return ActionResultType.SUCCESS;
			} else if (this.getHealth() < this.getMaxHealth() && isHungerItem(stack)) {
				if (!this.world.isRemote) {
					this.heal(5f);
					this.addBond(.2f);
					
					if (!player.isCreative()) {
						player.getHeldItem(hand).shrink(1);
					}
				}
				return ActionResultType.SUCCESS;
			} else if (this.isSitting() && stack.isEmpty()) {
				if (!this.world.isRemote) {
					//player.openGui(NostrumMagica.instance, NostrumGui.dragonID, this.world, (int) this.getPosX(), (int) this.getPosY(), (int) this.getPosZ());
					PetCommandAPI.OpenPetGUI(player, this);
				}
				return ActionResultType.SUCCESS;
			} else if (stack.isEmpty()) {
				if (!this.world.isRemote) {
					if (this.hasWolfCapability(WolfBondCapability.RIDEABLE)) {
						if (this.getHealth() < ARCANE_WOLF_WARN_HEALTH) {
							player.sendMessage(new TranslationTextComponent("info.tamed_arcane_wolf.low_health", this.getName()), Util.DUMMY_UUID);
						} else {
							player.startRiding(this);
						}
					} else {
						player.sendMessage(new TranslationTextComponent("info.tamed_arcane_wolf.no_ride", this.getName()), Util.DUMMY_UUID);
					}
				}
				return ActionResultType.SUCCESS;
			}
			else {
				; // fall through; we didn't handle it
			}
		} else if (!this.isTamed() && player.isCreative() && hand == Hand.MAIN_HAND && player.isSneaking()) {
			if (!world.isRemote) {
				this.setTamedBy(player);
			}
			return ActionResultType.SUCCESS;
		} else {
			// Someone other than the owner clicked
			if (!this.world.isRemote) {
				player.sendMessage(new TranslationTextComponent("info.tamed_arcane_wolf.not_yours", this.getName()), Util.DUMMY_UUID);
			}
			return ActionResultType.SUCCESS;
		}
		
		return ActionResultType.PASS;
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
	public double getMountedYOffset() {
		// Dragons go from 60% to 100% height.
		// This is synced with the rendering code.
		return (this.getHeight() * 0.6D) - ((0.4f * this.getHeight()) * (1f-getGrowingAge()));
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
	 public boolean canBeLeashedTo(PlayerEntity player) {
		return !isSitting() && player == getOwner();
	}
	
	public boolean isSoulBound() {
		return this.dataManager.get(SOULBOUND);
	}
	
	public void setSoulBound(boolean soulBound) {
		this.dataManager.set(SOULBOUND, soulBound);
	}
	
	@Override
	public void func_233687_w_(boolean sitting) { // SetSitting
		super.func_233687_w_(sitting); // SetSitting
	}
	
	public void setSitting(boolean sitting) {
		func_233687_w_(sitting);
		this.setSleeping(sitting);
		if (sitting) {
			setPetAction(PetAction.SITTING);
		}
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
	
	@Override
	public int addMana(int mana) {
		int orig = this.getMana();
		int cur = Math.max(0, Math.min(orig + mana, this.getMaxMana()));
		
		this.setMana(cur);
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
	
	protected void setMaxHealth(float maxHealth) {
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
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
		//return PetInfo.claim(getHealth(), getMaxHealth(), getMana(), getMaxMana(), SecondaryFlavor.GOOD, getPetAction());
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
			this.inventory = new Inventory(size);
			
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
	public boolean onLivingFall(float distance, float damageMulti) {
		this.jumpCount = 0;
		return super.onLivingFall(Math.max(0, distance-this.getFallReduction()), damageMulti);
	}
	
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		
		compound.putBoolean(NBT_SOUL_BOUND, this.isSoulBound());
		compound.putInt(NBT_ATTR_XP, this.getXP());
		compound.putInt(NBT_ATTR_LEVEL, this.getLevel());
		compound.putFloat(NBT_ATTR_BOND, this.getBond());
		compound.putFloat(NBT_MANA_REGEN, this.getManaRegen());
		// Ignore max health; already saved
		compound.putInt(NBT_MANA, this.getMana());
		compound.putInt(NBT_MAX_MANA, this.getMaxMana());
		compound.putUniqueId(NBT_SOUL_ID, soulID);
		if (worldID != null) {
			compound.putUniqueId(NBT_SOUL_WORLDID, worldID);
		}
		compound.putInt(NBT_RUNE_COLOR, this.getRuneColor());
		if (this.getTrainingElement() != null) {
			compound.putString(NBT_TRAINING_ELEMENT, this.getTrainingElement().name());
			compound.putInt(NBT_TRAINING_LEVEL, this.getTrainingLevel());
		}
		compound.putInt(NBT_TRAINING_XP, this.getTrainingXP());
		compound.putString(NBT_ELEMENTAL_TYPE, this.getElementalType().name());
		
		compound.put(NBT_INVENTORY, Inventories.serializeInventory(inventory));
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		
		this.setSoulBound(compound.getBoolean(NBT_SOUL_BOUND));
		this.setXP(compound.getInt(NBT_ATTR_XP));
		this.setLevel(compound.getInt(NBT_ATTR_LEVEL));
		this.setBond(compound.getFloat(NBT_ATTR_BOND));
		this.setManaRegen(compound.getFloat(NBT_MANA_REGEN));
		// Continue ignoring max health
		this.setMaxMana(compound.getInt(NBT_MAX_MANA)); // before setting mana
		this.setMana(compound.getInt(NBT_MANA));
		
		// Summon command passes empty NBT to parse. Don't overwrite random UUID if not present.
		if (compound.hasUniqueId(NBT_SOUL_ID)) {
			this.soulID = compound.getUniqueId(NBT_SOUL_ID);
		}
		
		if (compound.hasUniqueId(NBT_SOUL_WORLDID)) {
			this.worldID = compound.getUniqueId(NBT_SOUL_WORLDID);
		} else {
			this.worldID = null;
		}
		
		this.setRuneColor(compound.getInt(NBT_RUNE_COLOR));
		this.setTrainingElement(null);
		if (compound.contains(NBT_TRAINING_ELEMENT)) {
			try {
				this.setTrainingElement(EMagicElement.valueOf(compound.getString(NBT_TRAINING_ELEMENT).toUpperCase()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.setTrainingLevel(compound.getInt(NBT_TRAINING_LEVEL));
		}
		this.setTrainingXP(compound.getInt(NBT_TRAINING_XP));
		try {
			this.setElementalType(ArcaneWolfElementalType.valueOf(compound.getString(NBT_ELEMENTAL_TYPE).toUpperCase()));
		} catch (Exception e) {
			; // Unfortunately, expected because of the summon command
			this.setElementalType(ArcaneWolfElementalType.NONELEMENTAL);
		}
		
		ensureInventorySize();
		Inventories.deserializeInventory(inventory, compound.get(NBT_INVENTORY));
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
				LivingEntity otherOwner = PetFuncs.GetOwner(entityIn);
				if (otherOwner != null && otherOwner.equals(myOwner)) {
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
			
//			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
//				ItemStack stack = equipment.getStackInSlot(slot);
//				if (!stack.isEmpty()) {
//					ItemEntity item = new ItemEntity(this.world, this.getPosX(), this.getPosY(), this.getPosZ(), stack);
//					this.world.addEntity(item);
//				}
//			}
//			equipment.clear();
			inventory.clear();
		}
	}
	
	@Override
	public void onDeath(DamageSource cause) {
		// dropInventory(); called by vanilla now

		super.onDeath(cause);
	}
	
	@Override
	public boolean canDespawn(double distanceToClosestPlayer) {
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
			LivingEntity owner = this.getOwner();
			float health = this.getHealth();
			if (health > 0f && health < ARCANE_WOLF_WARN_HEALTH) {
				if (owner != null && owner instanceof PlayerEntity) {
					((PlayerEntity) this.getOwner()).sendMessage(new TranslationTextComponent("info.tamed_arcane_wolf.hurt", this.getName()), Util.DUMMY_UUID);
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
	public boolean attackEntityAsMob(Entity entityIn) {
		boolean hit = super.attackEntityAsMob(entityIn);
		
		if (hit && !world.isRemote) {
			LivingEntity owner = this.getOwner();
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
	public boolean onSoulStab(LivingEntity stabber, ItemStack stabbingItem) {
		
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
		
		LivingEntity owner = this.getOwner();
		if (owner != null) {
			this.playSound(SoundEvents.ENTITY_WOLF_AMBIENT, 1f, 1f);
			owner.sendMessage(new StringTextComponent(this.getName().getString() + " leveled up!"), Util.DUMMY_UUID);
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
					50, this.getPosX(), this.getPosY(), this.getPosZ(), 3.0, 30, 10, this.getEntityId()
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
					100, this.getPosX(), this.getPosY(), this.getPosZ(), 3.0, 60, 20, this.getEntityId()
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
			this.getAttribute(Attributes.ARMOR).applyPersistentModifier(new AttributeModifier(
					UUID.fromString(UUID_EXTRA_ARMOR_MOD),
					"ArcaneWolfEarthArmor",
					5.0D,
					AttributeModifier.Operation.ADDITION
					));
		}
		if (element == EMagicElement.LIGHTNING) {
			// Lightning gives bonus magic resistance!
			this.getAttribute(NostrumAttributes.magicResist).applyPersistentModifier(new AttributeModifier(
					UUID.fromString(UUID_MAGIC_RESIST_MOD),
					"ArcaneWolfLightningResist",
					30.0D,
					AttributeModifier.Operation.ADDITION
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
	
	public List<IWolfAbility> getAbilityList() {
		List<IWolfAbility> abilities = new ArrayList<>(8);
		for (WolfSpell spell : WolfSpell.values()) {
			if (spell.canCast(this)) {
				abilities.add(spell);
			}
		}
		
		// Add class abilities which are not WolfSpells
		if (this.getElementalType() == ArcaneWolfElementalType.BARRIER) {
			abilities.add(ABILITY_INFO_BARRIER);
		}
		if (this.getElementalType() == ArcaneWolfElementalType.ELDRICH) {
			abilities.add(ABILITY_INFO_ELDRICH);
		}
		if (this.getElementalType() == ArcaneWolfElementalType.HELL) {
			abilities.add(ABILITY_INFO_HELL);
		}
		if (this.getElementalType() == ArcaneWolfElementalType.MYSTIC) {
			abilities.add(ABILITY_INFO_MYSTIC);
		}
		if (this.getElementalType() == ArcaneWolfElementalType.NATURE) {
			abilities.add(ABILITY_INFO_NATURE);
		}
		if (this.getElementalType() == ArcaneWolfElementalType.STORM) {
			abilities.add(ABILITY_INFO_STORM);
		}
		
		return abilities;
	}
	
	public boolean canWolfTrain() {
		return hasWolfCapability(WolfBondCapability.TRAINABLE)
				&& getElementalType().getSecondary() == null; // A bit hacky
	}
	
	@Override
	public IPetGUISheet<? extends IEntityPet>[] getContainerSheets(PlayerEntity player) {
		return ArrayUtil.MakeArray(
				new ArcaneWolfInfoSheet(this),
				new ArcaneWolfBondInfoSheet(this),
				new ArcaneWolfInventorySheet(this),
				new ArcaneWolfTrainingSheet(this),
				new ArcaneWolfAbilitySheet(this)
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
	
	protected boolean teleportToEnemy(LivingEntity target) {
		if (target == null || !target.isAlive()) {
			return false;
		}
		
		return FollowOwnerGenericGoal.TeleportAroundEntity(this, target);
	}
	
	protected List<Spell> getTargetSpells(LivingEntity target, List<Spell> listToAddTo) {
		listToAddTo.clear();
		if (target != null) {
			for (WolfSpell spell : WolfSpell.values()) {
				if (this.getMana() >= spell.getCost()
						&& spell.matches(WolfSpellTargetGroup.ENEMY, this, target)) {
					listToAddTo.add(spell.getSpell());
				}
			}
		}
		return listToAddTo;
	}
	
	protected List<Spell> getSelfSpells(LivingEntity target, List<Spell> listToAddTo) {
		listToAddTo.clear();
		for (WolfSpell spell : WolfSpell.values()) {
			if (this.getMana() >= spell.getCost()
					&& spell.matches(WolfSpellTargetGroup.SELF, this, target)) {
				listToAddTo.add(spell.getSpell());
			}
		}
		return listToAddTo;
	}
	
	protected List<Spell> getAllySpells(LivingEntity target, List<Spell> listToAddTo) {
		listToAddTo.clear();
		if (target != null) {
			for (WolfSpell spell : WolfSpell.values()) {
				if (this.getMana() >= spell.getCost()
						&& spell.matches(WolfSpellTargetGroup.ALLY, this, target)) {
					listToAddTo.add(spell.getSpell());
				}
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
			addTrainingXP(2 * Math.max(1, (int)Math.ceil((float)cost/25f)));
		}
	}
	
	public static EntityArcaneWolf TransformWolf(WolfEntity wolf, PlayerEntity player) {
		EntityArcaneWolf newWolf = new EntityArcaneWolf(NostrumEntityTypes.arcaneWolf, wolf.world);
		newWolf.setPosition(wolf.getPosX(), wolf.getPosY(), wolf.getPosZ());
		newWolf.setTamedBy(player);
		newWolf.setHealth(5f);
		wolf.remove();
		wolf.world.addEntity(newWolf);
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

	@Override
	public CompoundNBT serializeNBT() {
		return super.serializeNBT();
	}

	@Override
	public UUID getPetID() {
		return this.getUniqueID();
	}

	@Override
	public boolean isBigPet() {
		return false;
	}
}
