package com.smanzana.nostrummagica.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.gui.petgui.arcanewolf.ArcaneWolfAbilitySheet;
import com.smanzana.nostrummagica.client.gui.petgui.arcanewolf.ArcaneWolfBondInfoSheet;
import com.smanzana.nostrummagica.client.gui.petgui.arcanewolf.ArcaneWolfInfoSheet;
import com.smanzana.nostrummagica.client.gui.petgui.arcanewolf.ArcaneWolfInventorySheet;
import com.smanzana.nostrummagica.client.gui.petgui.arcanewolf.ArcaneWolfTrainingSheet;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.tasks.FollowOwnerGenericGoal;
import com.smanzana.nostrummagica.entity.tasks.SpellAttackGoal;
import com.smanzana.nostrummagica.entity.tasks.arcanewolf.ArcaneWolfBarrierGoal;
import com.smanzana.nostrummagica.entity.tasks.arcanewolf.ArcaneWolfEldrichGoal;
import com.smanzana.nostrummagica.entity.tasks.arcanewolf.ArcaneWolfHellGoal;
import com.smanzana.nostrummagica.entity.tasks.arcanewolf.ArcaneWolfMysticGoal;
import com.smanzana.nostrummagica.entity.tasks.arcanewolf.ArcaneWolfNatureGoal;
import com.smanzana.nostrummagica.entity.tasks.arcanewolf.ArcaneWolfStormGoal;
import com.smanzana.nostrummagica.item.ArcaneWolfSoulItem;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.pet.IPetWithSoul;
import com.smanzana.nostrummagica.serializer.ArcaneWolfElementalTypeSerializer;
import com.smanzana.nostrummagica.serializer.MagicElementDataSerializer;
import com.smanzana.nostrummagica.serializer.PetJobSerializer;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.component.shapes.TouchShape;
import com.smanzana.nostrummagica.util.ArrayUtil;
import com.smanzana.nostrummagica.util.Inventories;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;

public class ArcaneWolfEntity extends Wolf implements ITameableEntity, IEntityPet, IPetWithSoul, IStabbableEntity, IMagicEntity {
	
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
		public Component getName();
		public Component getDescription();
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
				(Spell.CreateAISpell("WolfSpeed")).addPart(new SpellShapePart(NostrumSpellShapes.Chain, NostrumSpellShapes.Chain.makeProps(8, true))).addPart(new SpellEffectPart(EMagicElement.WIND, 1, EAlteration.SUPPORT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.WIND, 1),
				(wolf, target) -> {
					return wolf.getTarget() == null // Not in battle
							&& wolf.getMana() >= wolf.getMaxMana() * .30 // >= 30% mana
							;
				}),
		WIND_CUTTER("windcutter", WolfSpellTargetGroup.ENEMY, 20,
				(Spell.CreateAISpell("WolfWindCutter")).addPart(new SpellShapePart(NostrumSpellShapes.Projectile)).addPart(new SpellEffectPart(EMagicElement.WIND, 2, EAlteration.RUIN)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.WIND, 3),
				(wolf, target) -> true),
		ROOTS("roots", WolfSpellTargetGroup.ENEMY, 25,
				(Spell.CreateAISpell("WolfRoots")).addPart(new SpellShapePart(NostrumSpellShapes.SeekingBullet)).addPart(new SpellEffectPart(EMagicElement.EARTH, 2, EAlteration.INFLICT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.EARTH, 1),
				(wolf, target) -> {
					return target.getEffect(NostrumEffects.rooted) == null;
				}),
		REGEN("wolfregen", WolfSpellTargetGroup.ALLY, 50,
				(Spell.CreateAISpell("WolfRegen")).addPart(new SpellShapePart(NostrumSpellShapes.AI)).addPart(new SpellEffectPart(EMagicElement.EARTH, 2, EAlteration.GROWTH)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.EARTH, 3),
				(wolf, target) -> {
					return target.getHealth() < target.getMaxHealth()
							&& target.getEffect(MobEffects.REGENERATION) == null;
				}),
		MAGIC_SHIELD("magicshield", WolfSpellTargetGroup.SELF, 30,
				(Spell.CreateAISpell("WolfMagicShield")).addPart(new SpellShapePart(NostrumSpellShapes.Chain, NostrumSpellShapes.Chain.makeProps(8, true))).addPart(new SpellEffectPart(EMagicElement.ICE, 1, EAlteration.SUPPORT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.ICE, 1),
				(wolf, target) -> {
					return wolf.getTarget() != null; // Don't want to cast out of battle
				}),
		WOLF_HEAL("heal", null, 20,
				(Spell.CreateAISpell("WolfHeal")).addPart(new SpellShapePart(NostrumSpellShapes.AI)).addPart(new SpellEffectPart(EMagicElement.ICE, 2, EAlteration.GROWTH)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.ICE, 2),
				(wolf, target) -> {
					if (target.isInvertedHealAndHarm()) {
						// An attack against undead!
						return !NostrumMagica.IsSameTeam(wolf, target);
					} else {
						return target.getHealth() < target.getMaxHealth() && NostrumMagica.IsSameTeam(wolf, target);
					}
				}),
		ICE_FANGS("icefang", WolfSpellTargetGroup.SELF, 100,
				(Spell.CreateAISpell("WolfIceFangs")).addPart(new SpellShapePart(NostrumSpellShapes.Chain, NostrumSpellShapes.Chain.makeProps(8, true))).addPart(new SpellEffectPart(EMagicElement.ICE, 2, EAlteration.ENCHANT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.ICE, 3),
				(wolf, target) -> {
					return wolf.getTarget() != null; // Don't want to cast out of battle
				}),
		FIRE_TOUCH("firefang", WolfSpellTargetGroup.ENEMY, 10,
				(Spell.CreateAISpell("WolfFireBite")).addPart(new SpellShapePart(NostrumSpellShapes.Touch)).addPart(new SpellEffectPart(EMagicElement.FIRE, 2, EAlteration.RUIN)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.FIRE, 1),
				(wolf, target) -> {
					return wolf.distanceTo(target) <= TouchShape.AI_TOUCH_RANGE;
				}),
		MAGIC_BOOST("magicboost", WolfSpellTargetGroup.ALLY, 20,
				(Spell.CreateAISpell("WolfMagicBoost")).addPart(new SpellShapePart(NostrumSpellShapes.AI)).addPart(new SpellEffectPart(EMagicElement.FIRE, 1, EAlteration.SUPPORT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.FIRE, 3),
				(wolf, target) -> {
					return target.getEffect(NostrumEffects.magicBoost) == null
							&& (wolf.getTarget() != null || wolf.getMana() >= wolf.getMaxMana() * .75) // in battle or >= 75% mana
							;
				}),
		ENDER_SHROUD("endershroud", WolfSpellTargetGroup.ENEMY, 20,
				(Spell.CreateAISpell("WolfEnderShroud")).addPart(new SpellShapePart(NostrumSpellShapes.SeekingBullet)).addPart(new SpellShapePart(NostrumSpellShapes.Burst, NostrumSpellShapes.Burst.makeProps(3))).addPart(new SpellEffectPart(EMagicElement.ENDER, 2, null)).addPart(new SpellEffectPart(EMagicElement.ENDER, 1, EAlteration.INFLICT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.ENDER, 1),
				(wolf, target) -> true),
		ENDER_FANGS("enderfang", WolfSpellTargetGroup.SELF, 75,
				(Spell.CreateAISpell("WolfEnderFangs")).addPart(new SpellShapePart(NostrumSpellShapes.Chain, NostrumSpellShapes.Chain.makeProps(8, true))).addPart(new SpellEffectPart(EMagicElement.ENDER, 1, EAlteration.ENCHANT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.ENDER, 3),
				(wolf, target) -> {
					return wolf.getTarget() != null; // Don't want to cast out of battle
				}),
		SLOW("slow", WolfSpellTargetGroup.ENEMY, 10,
				(Spell.CreateAISpell("WolfSlow")).addPart(new SpellShapePart(NostrumSpellShapes.AI)).addPart(new SpellEffectPart(EMagicElement.LIGHTNING, 1, EAlteration.INFLICT)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.LIGHTNING, 1),
				(wolf, target) -> {
					return target.getEffect(MobEffects.MOVEMENT_SLOWDOWN) == null;
				}),
		CHAIN_LIGHTNING("chainlighting", WolfSpellTargetGroup.ENEMY, 40,
				(Spell.CreateAISpell("WolfChainLightning")).addPart(new SpellShapePart(NostrumSpellShapes.SeekingBullet)).addPart(new SpellShapePart(NostrumSpellShapes.Chain, NostrumSpellShapes.Chain.makeProps(6, true))).addPart(new SpellEffectPart(EMagicElement.LIGHTNING, 2, EAlteration.RUIN)),
				(wolf) -> wolf.hasElementLevel(EMagicElement.LIGHTNING, 3),
				(wolf, target) -> true),
		;
		
		private static interface ISpellPredicate {
			public boolean apply(ArcaneWolfEntity wolf, LivingEntity target);
		}
		
		private static interface IWolfPredicate {
			public boolean apply(ArcaneWolfEntity wolf);
		}
		
		protected final String key;
		private final @Nullable WolfSpellTargetGroup group;
		private final Spell spell;
		private final ISpellPredicate predicate;
		private final IWolfPredicate wolfChecker;
		private final int cost;
		
		private final Component name;
		private final Component description;
		
		private WolfSpell(String key, @Nullable WolfSpellTargetGroup group, int cost, Spell spell, IWolfPredicate wolfChecker, ISpellPredicate predicate) {
			this.key = key;
			this.group = group;
			this.spell = spell;
			this.predicate = predicate;
			this.wolfChecker = wolfChecker;
			this.cost = cost;
			
			this.name = new TranslatableComponent("info.wolf_ability." + key + ".name");
			this.description = new TranslatableComponent("info.wolf_ability." + key + ".desc");
		}
		
		public Spell getSpell() {
			return spell;
		}
		
		public boolean matches(WolfSpellTargetGroup group, ArcaneWolfEntity wolf, LivingEntity target) {
			return (this.group == null || this.group == group) && canCast(wolf) && predicate.apply(wolf, target);
		}
		
		public boolean canCast(ArcaneWolfEntity wolf) {
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
		public Component getName() {
			return name;
		}
		
		@Override
		public Component getDescription() {
			return description;
		}
	}
	
	private static final class StubbedWolfAbility implements IWolfAbility {
		private final Component name;
		private final Component desc;
		private final int manaCost;
		private final WolfSpellTargetGroup group;
		
		public StubbedWolfAbility(String key, int manaCost, WolfSpellTargetGroup group) {
			super();
			this.manaCost = manaCost;
			this.group = group;
			this.name = new TranslatableComponent("info.wolf_ability." + key + ".name");
			this.desc = new TranslatableComponent("info.wolf_ability." + key + ".desc");
		}
		
		@Override
		public Component getName() {
			return name;
		}
		
		@Override
		public Component getDescription() {
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
	
	protected static final EntityDataAccessor<Boolean> SOULBOUND = SynchedEntityData.<Boolean>defineId(ArcaneWolfEntity.class, EntityDataSerializers.BOOLEAN);
	
	protected static final EntityDataAccessor<Integer> ATTRIBUTE_XP  = SynchedEntityData.<Integer>defineId(ArcaneWolfEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> ATTRIBUTE_LEVEL  = SynchedEntityData.<Integer>defineId(ArcaneWolfEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> ATTRIBUTE_BOND  = SynchedEntityData.<Float>defineId(ArcaneWolfEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> ATTRIBUTE_MANA_REGEN  = SynchedEntityData.<Float>defineId(ArcaneWolfEntity.class, EntityDataSerializers.FLOAT);
    
    protected static final EntityDataAccessor<Integer> MANA  = SynchedEntityData.<Integer>defineId(ArcaneWolfEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> MAX_MANA  = SynchedEntityData.<Integer>defineId(ArcaneWolfEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<EPetAction> DATA_PET_ACTION = SynchedEntityData.<EPetAction>defineId(ArcaneWolfEntity.class, PetJobSerializer.GetInstance());
    protected static final EntityDataAccessor<Integer> RUNE_COLOR = SynchedEntityData.<Integer>defineId(ArcaneWolfEntity.class, EntityDataSerializers.INT);
    
    protected static final EntityDataAccessor<ArcaneWolfElementalType> ELEMENTAL_TYPE = SynchedEntityData.<ArcaneWolfElementalType>defineId(ArcaneWolfEntity.class, ArcaneWolfElementalTypeSerializer.instance);
    protected static final EntityDataAccessor<EMagicElement> TRAINING_ELEMENT = SynchedEntityData.<EMagicElement>defineId(ArcaneWolfEntity.class, MagicElementDataSerializer.instance);
    protected static final EntityDataAccessor<Integer> TRAINING_XP  = SynchedEntityData.<Integer>defineId(ArcaneWolfEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> TRAINING_LEVEL  = SynchedEntityData.<Integer>defineId(ArcaneWolfEntity.class, EntityDataSerializers.INT);
    
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
    
    private Container inventory;
    
    private UUID soulID;
    private UUID worldID;
    private int jumpCount;
    
	public ArcaneWolfEntity(EntityType<? extends ArcaneWolfEntity> type, Level worldIn) {
		super(type, worldIn);
        
        soulID = UUID.randomUUID();
        worldID = null;
        jumpCount = 0;
        inventory = new SimpleContainer(ARCANE_WOLF_BASE_INV_SIZE);
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(SOULBOUND, false);
		entityData.define(ATTRIBUTE_XP, 0);
		entityData.define(ATTRIBUTE_LEVEL, 0);
		entityData.define(ATTRIBUTE_BOND, 0f);
		entityData.define(ATTRIBUTE_MANA_REGEN, 1f);
		entityData.define(DATA_PET_ACTION, EPetAction.IDLE);
		entityData.define(MANA, 0);
		entityData.define(MAX_MANA, 100);
		entityData.define(RUNE_COLOR, 0x00000000);
		entityData.define(ELEMENTAL_TYPE, ArcaneWolfElementalType.NONELEMENTAL);
		entityData.define(TRAINING_ELEMENT, EMagicElement.PHYSICAL);
		entityData.define(TRAINING_XP, 0);
		entityData.define(TRAINING_LEVEL, 0);
	}
		
	
	public static final AttributeSupplier.Builder BuildAttributes() {
		return Wolf.createAttributes()
			.add(Attributes.MOVEMENT_SPEED, 0.35D)
			.add(Attributes.MAX_HEALTH, 50.0D)
			.add(Attributes.ARMOR, 10.0D)
			.add(Attributes.FOLLOW_RANGE, 60.0)
			.add(NostrumAttributes.magicResist, 20.0D)
			.add(Attributes.ATTACK_DAMAGE, 6.0D)
			.add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 1.1)
			;
	}
	
	@Override
	public void setTame(boolean tamed) {
		// Parent resets max health. Reset health and max health after that
		final float maxHealth = this.getMaxHealth();
		final float health = this.getHealth();
		super.setTame(tamed);
		setMaxHealth(maxHealth);
		setHealth(health);
	}
	
	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
	}
	
	@Override
	protected void registerGoals() {
		//super.initEntityAI();
		
		int priority = 1;
		this.goalSelector.addGoal(priority++, new FloatGoal(this));
		this.goalSelector.addGoal(priority++, new SitWhenOrderedToGoal(this));
		this.goalSelector.addGoal(priority++, new ArcaneWolfBarrierGoal(this, 5));
		this.goalSelector.addGoal(priority++, new ArcaneWolfStormGoal(this, 35));
		this.goalSelector.addGoal(priority++, new ArcaneWolfEldrichGoal(this, 40));
		this.goalSelector.addGoal(priority++, new ArcaneWolfMysticGoal(this, 10));
		this.goalSelector.addGoal(priority++, new ArcaneWolfNatureGoal(this, 25));
		this.goalSelector.addGoal(priority++, new ArcaneWolfHellGoal(this, 40));
		this.goalSelector.addGoal(priority++, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(priority++, new MeleeAttackGoal(this, 1.0D, true) {
			@Override
			protected void checkAndPerformAttack(LivingEntity target, double dist) {

				if (this.isTimeToAttack() /*attackTick <= 0*/ && dist > this.getAttackReachSqr(target)) {
					// Too far
					if (ArcaneWolfEntity.this.hasWolfCapability(WolfTypeCapability.WOLF_BLINK)
							&& ArcaneWolfEntity.this.random.nextFloat() < .05) {
						Vec3 currentPos = ArcaneWolfEntity.this.position();
						if (ArcaneWolfEntity.this.teleportToEnemy(target)) {
							ArcaneWolfEntity.this.level.playSound(null, currentPos.x, currentPos.y, currentPos.z,
									SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1f, 1f);
							ArcaneWolfEntity.this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1f, 1f);
							
							// If currently training ender, get some xp!
							if (ArcaneWolfEntity.this.getTrainingElement() == EMagicElement.ENDER) {
								ArcaneWolfEntity.this.addTrainingXP(1);
							}
							
							if (ArcaneWolfEntity.this.random.nextBoolean()) {
								ArcaneWolfEntity.this.addXP(1);
							}
						}
					}
				}
				
				super.checkAndPerformAttack(target, dist);
			}
		});
		// Attack/Offensive spells
		this.goalSelector.addGoal(priority++, new SpellAttackGoal<ArcaneWolfEntity>(this, 20 * 3, 4, true, (w) -> {return !w.isOrderedToSit();}) {
			private List<Spell> spellList = new ArrayList<>();
			@Override
			protected Spell pickSpell(Spell[] spells, ArcaneWolfEntity entity) {
				spellList = ArcaneWolfEntity.this.getTargetSpells(getTarget(), spellList);
				if (spellList.isEmpty()) {
					return null;
				}
				return spellList.get(entity.random.nextInt(spellList.size()));
			}
			
			@Override
			protected void deductMana(Spell spell, ArcaneWolfEntity wolf) {
				final int cost = getWolfSpellCost(spell);
				wolf.addMana(-cost);
				wolf.onWolfCast(spell, cost);
				wolf.playSound(SoundEvents.WOLF_GROWL, 1f, .8f);
				
				if (ArcaneWolfEntity.this.random.nextBoolean()) {
					ArcaneWolfEntity.this.addXP(1);
				}
			}
		});
		// Ally spells
		this.goalSelector.addGoal(priority++, new SpellAttackGoal<ArcaneWolfEntity>(this, 20 * 3, 20, false, (w) -> {return !w.isOrderedToSit();}) {
			private List<Spell> spellList = new ArrayList<>();
			@Override
			protected Spell pickSpell(Spell[] spells, ArcaneWolfEntity entity) {
				spellList = ArcaneWolfEntity.this.getAllySpells(getTarget(), spellList);
				if (spellList.isEmpty()) {
					return null;
				}
				return spellList.get(entity.random.nextInt(spellList.size()));
			}
			
			@Override
			protected @Nullable LivingEntity getTarget() {
				// Want to make sure to be stable so that multiple calls when finding spell etc.
				// return the same answer
				LivingEntity owner = entity.getOwner();
				if (owner != null) {
					List<LivingEntity> tames = PetFuncs.GetTamedEntities(owner);
					tames.add(owner);
					tames.removeIf((e) -> { return e.distanceTo(entity) > 15;});
					Collections.shuffle(tames, new Random(entity.tickCount));
					return tames.get(0);
				} else {
					return null;
				}
			}
			
			@Override
			protected void deductMana(Spell spell, ArcaneWolfEntity wolf) {
				final int cost = getWolfSpellCost(spell);
				wolf.addMana(-cost);
				wolf.onWolfCast(spell, cost);
				wolf.playSound(SoundEvents.WOLF_AMBIENT, 1f, .8f);
				
				if (ArcaneWolfEntity.this.random.nextBoolean()) {
					ArcaneWolfEntity.this.addXP(1);
				}
			}
		});
		// Self spells (longer recast)
		this.goalSelector.addGoal(priority++, new SpellAttackGoal<ArcaneWolfEntity>(this, 20 * 5, 100, false, (w) -> {return !w.isOrderedToSit();}) {
			private List<Spell> spellList = new ArrayList<>();
			@Override
			protected Spell pickSpell(Spell[] spells, ArcaneWolfEntity entity) {
				spellList = ArcaneWolfEntity.this.getSelfSpells(getTarget(), spellList);
				if (spellList.isEmpty()) {
					return null;
				}
				return spellList.get(entity.random.nextInt(spellList.size()));
			}
			
			@Override
			protected @Nullable LivingEntity getTarget() {
				return entity;
			}
			
			@Override
			protected void deductMana(Spell spell, ArcaneWolfEntity wolf) {
				final int cost = getWolfSpellCost(spell);
				wolf.addMana(-cost);
				wolf.onWolfCast(spell, cost);
				wolf.playSound(SoundEvents.WOLF_PANT, 1f, .8f);
				
				if (ArcaneWolfEntity.this.random.nextBoolean()) {
					ArcaneWolfEntity.this.addXP(1);
				}
			}
		});
		//this.goalSelector.addGoal(priority++, new FollowOwnerAdvancedGoal<EntityArcaneWolf>(this, 1.5f, 0f, .5f));
		this.goalSelector.addGoal(priority++, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, true));
		//this.goalSelector.addGoal(7, new EntityAIMate(this, 1.0D));
		this.goalSelector.addGoal(priority++, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(priority++, new BegGoal(this, 8.0F));
		this.goalSelector.addGoal(priority++, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(priority++, new RandomLookAroundGoal(this));
		
		priority = 1;
		//this.targetSelector.addGoal(priority++, new PetTargetGoal<EntityArcaneWolf>(this));
		this.targetSelector.addGoal(priority++, new OwnerHurtByTargetGoal(this));
		this.targetSelector.addGoal(priority++, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setAlertOthers(ArcaneWolfEntity.class, Wolf.class));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<AbstractSkeleton>(this, AbstractSkeleton.class, false));
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
		
		if (level != null && !level.isClientSide) {
			final @Nullable EPetAction order = PetInfo.GetOrderAction(this);
			if (order != null) {
				setPetAction(order);
				return;
			}
			
			if (!this.isOrderedToSit()) {
				if (this.getTarget() == null) {
					setPetAction(EPetAction.IDLE);
				} else {
					setPetAction(EPetAction.ATTACK);
				}
			}
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		checkAndHandleLava();
	}
	
	@Override
	public void aiStep() {
		super.aiStep();
		
		if (level.isClientSide) {
			return;
		}
		
		if (this.getTarget() != null && !this.getTarget().isAlive()) {
			this.setTarget(null);
		}
		
		if (this.tickCount % 20 == 0) {
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
	public void travel(Vec3 move) {
		//super.travel(strafe, vertical, forward);
		
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
			double strafe = entitylivingbase.xxa * 0.45F;
			double forward = entitylivingbase.zza * .7f;

			if (forward < 0.0F) {
				forward *= 0.5F;
			}
			
			this.flyingSpeed = this.getSpeed() * 0.33F;

			if (this.isControlledByLocalInstance()) {
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
				super.travel(new Vec3(strafe, move.y, forward));
			}
			else if (entitylivingbase instanceof Player) {
				this.setDeltaMovement(Vec3.ZERO);
			}

			this.animationSpeedOld = this.animationSpeed;
			double d1 = this.getX() - this.xo;
			double d0 = this.getZ() - this.zo;
			float f2 = (float) (Math.sqrt(d1 * d1 + d0 * d0) * 4.0F);

			if (f2 > 1.0F) {
				f2 = 1.0F;
			}

			this.animationSpeed += (f2 - this.animationSpeed) * 0.4F;
			this.animationPosition += this.animationSpeed;
		}
		else
		{
			this.flyingSpeed = 0.02F;
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
	public boolean isPushedByFluid() {
		if (this.hasWolfCapability(WolfTypeCapability.LAVA_WALK)) {
			return false;
		}
		return super.isPushedByFluid();
	}
	
	protected void checkAndHandleLava() {
		if (this.hasWolfCapability(WolfTypeCapability.LAVA_WALK) && super.isInLava()) {
			// Copied from Strider
			CollisionContext iselectioncontext = CollisionContext.of(this);
			if (iselectioncontext.isAbove(LiquidBlock.STABLE_SHAPE, this.blockPosition(), true) && !this.level.getFluidState(this.blockPosition().above()).is(FluidTags.LAVA)) {
				this.onGround = true;
			} else {
				this.setDeltaMovement(this.getDeltaMovement().scale(0.5D).add(0.0D, 0.05D, 0.0D));
			}
		}
	}
	
	@Override
	public boolean canStandOnFluid(FluidState fluid) {
		// Copied from Strider
		if (this.hasWolfCapability(WolfTypeCapability.LAVA_WALK)) {
			return fluid.is(FluidTags.LAVA);
		}
		
		return super.canStandOnFluid(fluid);
	}
	
	@Override
	public boolean isFood(@Nonnull ItemStack stack) {
		return false;
	}
	
	@Override
	public boolean canMate(Animal otherAnimal) {
		return false;
	}
	
	@Override
	public boolean isInLove() {
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	public float getTailAngle() {
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
	public InteractionResult /*processInteract*/ mobInteract(Player player, InteractionHand hand) {
		// Shift-right click toggles sitting.
		// When not sitting, right-click mounts the wolf
		// When sitting, will eventually open a GUI
		final @Nonnull ItemStack stack = player.getItemInHand(hand);
		if (this.isTame() && player == this.getOwner()) {
			if (player.isShiftKeyDown()) {
				if (!this.level.isClientSide) {
					this.setEntitySitting(!this.isOrderedToSit());
					if (player.isCreative()) {
						this.setBond(1f);
						this.addTrainingXP(500);
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
			} else if (this.isOrderedToSit() && stack.isEmpty()) {
				if (!this.level.isClientSide) {
					//player.openGui(NostrumMagica.instance, NostrumGui.dragonID, this.world, (int) this.getPosX(), (int) this.getPosY(), (int) this.getPosZ());
					PetCommandAPI.OpenPetGUI(player, this);
				}
				return InteractionResult.SUCCESS;
			} else if (stack.isEmpty()) {
				if (!this.level.isClientSide) {
					if (this.hasWolfCapability(WolfBondCapability.RIDEABLE)) {
						if (this.getHealth() < ARCANE_WOLF_WARN_HEALTH) {
							player.sendMessage(new TranslatableComponent("info.tamed_arcane_wolf.low_health", this.getName()), Util.NIL_UUID);
						} else {
							player.startRiding(this);
						}
					} else {
						player.sendMessage(new TranslatableComponent("info.tamed_arcane_wolf.no_ride", this.getName()), Util.NIL_UUID);
					}
				}
				return InteractionResult.SUCCESS;
			}
			else {
				; // fall through; we didn't handle it
			}
		} else if (!this.isTame() && player.isCreative() && hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {
			if (!level.isClientSide) {
				this.tame(player);
			}
			return InteractionResult.SUCCESS;
		} else {
			// Someone other than the owner clicked
			if (!this.level.isClientSide) {
				player.sendMessage(new TranslatableComponent("info.tamed_arcane_wolf.not_yours", this.getName()), Util.NIL_UUID);
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
	public double getPassengersRidingOffset() {
		// Dragons go from 60% to 100% height.
		// This is synced with the rendering code.
		return (this.getBbHeight() * 0.6D) - ((0.4f * this.getBbHeight()) * (1f-getAge()));
	}
	
	@Override
	protected float getJumpPower() {
		return super.getJumpPower() + getBonusJumpHeight();
	}
	
	@Override
	protected void jumpFromGround() {
		super.jumpFromGround();
	}
	
	@Override
	 public boolean canBeLeashed(Player player) {
		return !isOrderedToSit() && player == getOwner();
	}
	
	public boolean isSoulBound() {
		return this.entityData.get(SOULBOUND);
	}
	
	public void setSoulBound(boolean soulBound) {
		this.entityData.set(SOULBOUND, soulBound);
	}
	
	@Override
	public void setOrderedToSit(boolean sitting) { // SetSitting
		super.setOrderedToSit(sitting); // SetSitting
	}
	
	@Override
	public boolean setEntitySitting(boolean sitting) {
		setOrderedToSit(sitting);
		this.setInSittingPose(sitting);
		if (sitting) {
			setPetAction(EPetAction.STAY);
		}
		return true;
	}
	
	public int getWolfLevel() {
		return this.entityData.get(ATTRIBUTE_LEVEL);
	}
	
	protected void setLevel(int level) {
		this.entityData.set(ATTRIBUTE_LEVEL, Math.max(1, level));
	}
	
	protected int getMaxXP(int level) {
		return (int) (100D * Math.pow(1.5, level));
	}

	public int getXP() {
		return this.entityData.get(ATTRIBUTE_XP);
	}

	public int getMaxXP() {
		return this.getMaxXP(getWolfLevel());
	}

	public int getMana() {
		return this.entityData.get(MANA);
	}
	
	protected void setMana(int mana) {
		this.entityData.set(MANA, Math.max(0, Math.min(mana, this.getMaxMana())));
	}

	public int getMaxMana() {
		if (entityData.get(MAX_MANA) <= 0) {
			this.setMaxMana(100);
		}
		return entityData.get(MAX_MANA);
	}
	
	protected void setMaxMana(int maxMana) {
		this.entityData.set(MAX_MANA, Math.max(0, maxMana));
	}
	
	public float getManaRegen() {
		return entityData.get(ATTRIBUTE_MANA_REGEN);
	}
	
	protected void setManaRegen(float regen) {
		entityData.set(ATTRIBUTE_MANA_REGEN, Math.max(1f, regen));
	}
	
	protected void addManaRegen(float diff) {
		setManaRegen(getManaRegen() + diff);
	}

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
		this.entityData.set(ATTRIBUTE_XP, xp);
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
		return entityData.get(RUNE_COLOR);
	}
	
	public void setRuneColor(int ARGB) {
		entityData.set(RUNE_COLOR, ARGB);
	}
	
	public ArcaneWolfElementalType getElementalType() {
		return entityData.get(ELEMENTAL_TYPE);
	}
	
	protected void setElementalType(ArcaneWolfElementalType type) {
		this.entityData.set(ELEMENTAL_TYPE, type);
	}
	
	public int getTrainingXP() {
		return entityData.get(TRAINING_XP);
	}
	
	protected void setTrainingXP(int xp) {
		entityData.set(TRAINING_XP, xp);
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
		final EMagicElement elem = entityData.get(TRAINING_ELEMENT);
		if (elem == EMagicElement.PHYSICAL) {
			return null;
		}
		return elem;
	}
	
	protected void setTrainingElement(@Nullable EMagicElement element) {
		if (element == null) {
			element = EMagicElement.PHYSICAL;
		}
		entityData.set(TRAINING_ELEMENT, element);
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
		return entityData.get(TRAINING_LEVEL);
	}
	
	protected void setTrainingLevel(int level) {
		entityData.set(TRAINING_LEVEL, level);
	}
	
	public void setPetAction(EPetAction action) {
		entityData.set(DATA_PET_ACTION, action);
	}

	public EPetAction getPetAction() {
		return entityData.get(DATA_PET_ACTION);
	}
	
	protected static final Component LABEL_XP = new TextComponent("XP");
	protected static final Component LABEL_MANA = new TextComponent("Mana");
	protected static final Component LABEL_BOND = new TextComponent("Bond");
	protected static final Component LABEL_TRAINING_XP = new TextComponent("Elemental XP");
	
	@Override
	public PetInfo getPetSummary() {
		final PetValue[] values;
		if (this.getTrainingElement() == null) {
			values = new PetValue[] {
				new PetValue(getXP(), getMaxXP(), ValueFlavor.PROGRESS, LABEL_XP),
				new PetValue(getMana(), getMaxMana(), ValueFlavor.GOOD, LABEL_MANA),
				new PetValue(getBond(), 1f, ValueFlavor.GRADUAL_GOOD, LABEL_BOND)
			};
		} else {
			values = new PetValue[] {
					new PetValue(getXP(), getMaxXP(), ValueFlavor.PROGRESS, LABEL_XP),
					new PetValue(getMana(), getMaxMana(), ValueFlavor.GOOD, LABEL_MANA),
					new PetValue(getBond(), 1f, ValueFlavor.GRADUAL_GOOD, LABEL_BOND),
					new PetValue(this.getTrainingXP(), this.getMaxTrainingXP(), ValueFlavor.PROGRESS, LABEL_TRAINING_XP)
			};
		}
		return PetInfo.claim(getPetAction(), getHealth(), getMaxHealth(), values);
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
	
	protected Container ensureInventorySize() {
		final int size = getInventorySize();
		if (this.inventory == null || this.inventory.getContainerSize() != size) {
			Container old = this.inventory;
			this.inventory = new SimpleContainer(size);
			
			if (old != null) {
				// Copy over what we can. Drop the rest
				int i = 0;
				for (; i < Math.min(old.getContainerSize(), inventory.getContainerSize()); i++) {
					inventory.setItem(i, old.removeItemNoUpdate(i));
				}
				
				for (; i < old.getContainerSize(); i++) {
					this.spawnAtLocation(old.getItem(i), .5f);
				}
			}
		}
		return this.inventory;
	}
	
	public Container getInventory() {
		if (level == null || level.isClientSide) {
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
			this.jumpFromGround();
		}
	}
	
	@Override
	public boolean causeFallDamage(float distance, float damageMulti, DamageSource source) {
		this.jumpCount = 0;
		return super.causeFallDamage(Math.max(0, distance-this.getFallReduction()), damageMulti, source);
	}
	
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		
		compound.putBoolean(NBT_SOUL_BOUND, this.isSoulBound());
		compound.putInt(NBT_ATTR_XP, this.getXP());
		compound.putInt(NBT_ATTR_LEVEL, this.getWolfLevel());
		compound.putFloat(NBT_ATTR_BOND, this.getBond());
		compound.putFloat(NBT_MANA_REGEN, this.getManaRegen());
		// Ignore max health; already saved
		compound.putInt(NBT_MANA, this.getMana());
		compound.putInt(NBT_MAX_MANA, this.getMaxMana());
		compound.putUUID(NBT_SOUL_ID, soulID);
		if (worldID != null) {
			compound.putUUID(NBT_SOUL_WORLDID, worldID);
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
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		
		this.setSoulBound(compound.getBoolean(NBT_SOUL_BOUND));
		this.setXP(compound.getInt(NBT_ATTR_XP));
		this.setLevel(compound.getInt(NBT_ATTR_LEVEL));
		this.setBond(compound.getFloat(NBT_ATTR_BOND));
		this.setManaRegen(compound.getFloat(NBT_MANA_REGEN));
		// Continue ignoring max health
		this.setMaxMana(compound.getInt(NBT_MAX_MANA)); // before setting mana
		this.setMana(compound.getInt(NBT_MANA));
		
		// Summon command passes empty NBT to parse. Don't overwrite random UUID if not present.
		if (compound.hasUUID(NBT_SOUL_ID)) {
			this.soulID = compound.getUUID(NBT_SOUL_ID);
		}
		
		if (compound.hasUUID(NBT_SOUL_WORLDID)) {
			this.worldID = compound.getUUID(NBT_SOUL_WORLDID);
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
		if (this.isTame()) {
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
		if (this.isTame()) {
			LivingEntity myOwner = this.getOwner();

			if (entityIn == myOwner) {
				return true;
			}
			
			if (myOwner != null) {
				LivingEntity otherOwner = PetFuncs.GetOwner(entityIn);
				if (otherOwner != null && otherOwner.equals(myOwner)) {
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
			
//			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
//				ItemStack stack = equipment.getStackInSlot(slot);
//				if (!stack.isEmpty()) {
//					ItemEntity item = new ItemEntity(this.world, this.getPosX(), this.getPosY(), this.getPosZ(), stack);
//					this.world.addEntity(item);
//				}
//			}
//			equipment.clear();
			inventory.clearContent();
		}
	}
	
	@Override
	public void die(DamageSource cause) {
		// dropInventory(); called by vanilla now

		super.die(cause);
	}
	
	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
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
		
		if (hurt && this.isTame()) {
			LivingEntity owner = this.getOwner();
			float health = this.getHealth();
			if (health > 0f && health < ARCANE_WOLF_WARN_HEALTH) {
				if (owner != null && owner instanceof Player) {
					((Player) this.getOwner()).sendMessage(new TranslatableComponent("info.tamed_arcane_wolf.hurt", this.getName()), Util.NIL_UUID);
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
	public boolean doHurtTarget(Entity entityIn) {
		boolean hit = super.doHurtTarget(entityIn);
		
		if (hit && !level.isClientSide) {
			LivingEntity owner = this.getOwner();
			if (owner != null) {
				final double dist = owner.distanceTo(owner);
				if (dist <= 10) {
					this.addBond(dist < 4 ? .5f : .2f);
				}
			}
			if (random.nextBoolean() && random.nextBoolean()) {
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
		
		if (this.isOwnedBy(stabber) && !isSoulBound() && this.getBond() >= 1f) {
			// Die and scream and drop a soul ember
			this.setSoulBound(true);
			
			// Drop inventory before snapshotting
			dropEquipment();
			
			final ItemStack stack = ArcaneWolfSoulItem.MakeSoulItem(this, true);
			if (!stack.isEmpty()) {
				this.spawnAtLocation(stack, 1f);
				this.hurt(DamageSource.GENERIC, 1000000f);
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
		int level = this.getWolfLevel();
		
		Random rand = getRandom();
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
			this.playSound(SoundEvents.WOLF_AMBIENT, 1f, 1f);
			owner.sendMessage(new TextComponent(this.getName().getString() + " leveled up!"), Util.NIL_UUID);
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
		if (!this.level.isClientSide) {
			this.playSound(SoundEvents.WOLF_PANT, 1f, 1f);
			NostrumParticles.GLOW_ORB.spawn(this.level, new SpawnParams(
					50, this.getX(), this.getY(), this.getZ(), 3.0, 30, 10, this.getId()
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
		if (!level.isClientSide) {
			this.playSound(SoundEvents.WOLF_HOWL, 1f, 1f);
			NostrumParticles.FILLED_ORB.spawn(this.level, new SpawnParams(
					100, this.getX(), this.getY(), this.getZ(), 3.0, 60, 20, this.getId()
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
			this.getAttribute(Attributes.ARMOR).addPermanentModifier(new AttributeModifier(
					UUID.fromString(UUID_EXTRA_ARMOR_MOD),
					"ArcaneWolfEarthArmor",
					5.0D,
					AttributeModifier.Operation.ADDITION
					));
		}
		if (element == EMagicElement.LIGHTNING) {
			// Lightning gives bonus magic resistance!
			this.getAttribute(NostrumAttributes.magicResist).addPermanentModifier(new AttributeModifier(
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
	public IPetGUISheet<? extends IEntityPet>[] getContainerSheets(Player player) {
		return ArrayUtil.MakeArray(
				new ArcaneWolfInfoSheet(this),
				new ArcaneWolfBondInfoSheet(this),
				new ArcaneWolfInventorySheet(this),
				new ArcaneWolfTrainingSheet(this),
				new ArcaneWolfAbilitySheet(this)
		);
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
	
	public static ArcaneWolfEntity TransformWolf(Wolf wolf, Player player) {
		ArcaneWolfEntity newWolf = new ArcaneWolfEntity(NostrumEntityTypes.arcaneWolf, wolf.level);
		newWolf.setPos(wolf.getX(), wolf.getY(), wolf.getZ());
		newWolf.tame(player);
		newWolf.setHealth(5f);
		wolf.discard();
		wolf.level.addFreshEntity(newWolf);
		return newWolf;
	}
	
	public static final class WolfTameLore implements IEntityLoreTagged<ArcaneWolfEntity> {
		
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

		@Override
		public EntityType<ArcaneWolfEntity> getEntityType() {
			return NostrumEntityTypes.arcaneWolf;
		}
		
	}

	@Override
	public boolean isEntityTamed() {
		return this.isTame();
	}

	@Override
	public boolean isEntitySitting() {
		return this.isOrderedToSit();
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
		return false;
	}
}
