package com.smanzana.nostrummagica.item.armor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.NostrumMagica.NostrumTeleportEvent;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.model.EnchantedArmorBaseModel;
import com.smanzana.nostrummagica.client.model.NostrumModelLayers;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.render.layer.AetherCloakLayer;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.AreaEffectEntity;
import com.smanzana.nostrummagica.entity.AreaEffectEntity.IAreaEntityEffect;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.item.IDragonWingRenderItem;
import com.smanzana.nostrummagica.item.IElytraRenderer;
import com.smanzana.nostrummagica.item.IReactiveEquipment;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.equipment.AspectedEnderWeapon;
import com.smanzana.nostrummagica.item.equipment.AspectedWeapon;
import com.smanzana.nostrummagica.listener.PlayerJumpEvent;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.EnchantedArmorStateUpdate;
import com.smanzana.nostrummagica.network.message.EnchantedArmorStateUpdate.ArmorState;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellDamage;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.Projectiles;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class ElementalArmor extends ArmorItem
		implements IReactiveEquipment, IDragonWingRenderItem, DyeableLeatherItem, IElytraRenderer {

	public static enum Type {
		NOVICE(.8f), ADEPT(.9f), MASTER(1f);

		public final float scale;

		private Type(float scale) {
			this.scale = scale;
		}

		public final @Nullable Type getNext() {
			switch (this) {
			case NOVICE:
				return ADEPT;
			case ADEPT:
				return MASTER;
			case MASTER:
				return null;
			}

			return null;
		}

		public final @Nullable Type getPrev() {
			switch (this) {
			case NOVICE:
				return null;
			case ADEPT:
				return NOVICE;
			case MASTER:
				return ADEPT;
			}

			return null;
		}
		
		public final EElementalMastery getMatchingMastery() {
			switch (this) {
			case NOVICE:
				return EElementalMastery.NOVICE;
			case ADEPT:
				return EElementalMastery.ADEPT;
			case MASTER:
				return EElementalMastery.MASTER;
			}
			
			return null;
		}
	}

	public static boolean isArmorElement(EMagicElement element) {

		switch (element) {
		case EARTH:
		case ENDER:
		case FIRE:
		case PHYSICAL:
			return true;
		case WIND:
		case ICE:
		case LIGHTNING:
		default:
			return false;
		}
	}

	// UUIDs for modifiers for base attributes from armor
	private static final UUID[] ARMOR_MODIFIERS = new UUID[] { UUID.fromString("922AB274-1111-56FE-19AE-365AA9758B8B"),
			UUID.fromString("D1459204-0E61-4716-A129-61666D432E0D"),
			UUID.fromString("1F7D236D-1118-6524-3375-34814505B28E"),
			UUID.fromString("2A632266-F4E1-2E67-7836-64FD783B7A50") };

	// UUID for speed and jump-boost modifiers
	private static final UUID[] ARMOR_SPEED_MODS = new UUID[] { UUID.fromString("822AB274-1111-56FE-19AE-365AA9758B8B"),
			UUID.fromString("C1459204-0E61-4716-A129-61666D432E0D"),
			UUID.fromString("2F7D236D-1118-6524-3375-34814505B28E"),
			UUID.fromString("3A632266-F4E1-2E67-7836-64FD783B7A50") };
	// private static final UUID[] ARMOR_JUMP_MODS = new UUID[]
	// {UUID.fromString("722AB274-1111-56FE-19AE-365AA9758B8B"),
	// UUID.fromString("B1459204-0E61-4716-A129-61666D432E0D"),
	// UUID.fromString("3F7D236D-1118-6524-3375-34814505B28E"),
	// UUID.fromString("4A632266-F4E1-2E67-7836-64FD783B7A50")};

	private static int calcArmor(EquipmentSlot slot, EMagicElement element, Type type) {

		// Ratio of full armor for a MASTER set
		final float fullProt;

		switch (element) {
		case EARTH:
			fullProt = (22f / 25f);
			break;
		case ENDER:
			fullProt = (20f / 25f);
			break;
		case FIRE:
			fullProt = (20f / 25f);
			break;
		case PHYSICAL:
			fullProt = 1f;
			break;
		case ICE:
			fullProt = (20f / 25f);
			break;
		case LIGHTNING:
			fullProt = (18f / 25f);
			break;
		case WIND:
			fullProt = (18f / 25f);
			break;
		default:
			fullProt = 0.5f;
		}
		
		// How much of the 'full' armor we get for this type
		final float typeScale = type.scale;

		// How much to put into each slot.
		// Iron is head:2, chest:6, legs:5, feet:2   / 15
		// Diamond is  :3,      :8,     :6,     :3   / 20
		final float slotScale;

		switch (slot) {
		case CHEST:
		default:
			slotScale = (8f/20f);
			break;
		case FEET:
			slotScale = (3f/20f);
			break;
		case HEAD:
			slotScale = (3f/20f);
			break;
		case LEGS:
			slotScale = (6f/20f);
			break;
		}

		final float real = fullProt * typeScale * slotScale * 25f;
		
		// Round, but with .5 rounding down
		final int amt = (int) ((real - (int)real) - .5f < .01 ? Math.floor(real) : Math.round(real));
		final int extra;
		
		if (ModConfig.config.usingAdvancedArmors()) {
			extra = 1;
		} else {
			extra = 0;
		}

		return amt + extra;
	}

	// Calcs magic resist on a scale from 0 to 1 (with 1 being 100%)
	private static float calcMagicResistBase(EquipmentSlot slot, EMagicElement element, Type type) {

		// Ratio of full armor for a MASTER set
		final float fullProt;
		
		switch (element) {
		case EARTH:
			fullProt = .35f;
			break;
		case ENDER:
			fullProt = .4f;
			break;
		case FIRE:
			fullProt = .45f;
			break;
		case PHYSICAL:
			fullProt = .5f;
			break;
		case ICE:
			fullProt = .6f;
			break;
		case LIGHTNING:
			fullProt = .45f;
			break;
		case WIND:
			fullProt = .4f;
			break;
		default:
			fullProt = 0.25f;
		}
		
		// How much of the 'full' armor we get for this type
		final float typeScale = type.scale;

		// How much break down protection by slot
		final float slotScale;

		switch (slot) {
		case CHEST:
			slotScale = (8f/20f);
			break;
		case FEET:
			slotScale = (3f/20f);
			break;
		case HEAD:
			slotScale = (3f/20f);
			break;
		case LEGS:
			slotScale = (6f/20f);
			break;
		default:
			slotScale = .5f;
		}

		return fullProt * typeScale * slotScale;
	}

	private static final double calcMagicReduct(EquipmentSlot slot, EMagicElement element, Type type) {
		// each piece will give (.1, .15, .25) of their type depending on level.
		return (type == Type.NOVICE ? .1 : (type == Type.ADEPT ? .15 : .25));
	}

	public static final double CalcMagicSetReductTotal(EMagicElement armorElement, Type type, int setCount,
			@Nullable EMagicElement targetElement) {
		if (setCount < 1 || setCount > 4) {
			return 0;
		}

		// Fire will resist 2 fire damage (total 3 with max level set). [0, .5, 1, 2]
		// Earth will resist 1 earth damage (total 2 with max level set) AND .5 in all
		// other elements. [0, .25, .5, 1][0, 0, 0, .5]
		// Ender will resist 5 ender damage (total 6 with max level set) but -.5 in all
		// others. [0, 1, 3, 5][0, -.1, -.3, -.5]
		// Wind will resist 1 wind (total 2 with full set)
		// Lightning will resist 3 lightning (total 4)
		// Ice will resist .5 in all
		final double[] setTotalFire = { 0, .5, 1, 2 };
		final double[] setTotalEarth = { 0, .125, .25, .5 };
		final double[] setTotalEarthBonus = { 0, 0, 0, .5 };
		final double[] setTotalEnder = { 0, 1, 3, 5.5 };
		final double[] setTotalEnderBonus = { 0, -.1, -.3, -.5 };

		final double[] setTotalWind = { 0, 0, 0, 1 };
		final double[] setTotalLightning = { 0, 0, 0, 3 };
		final double[] setTotalIce = { 0, 0, 0, 1.5 };

		final double reduc;

		if (armorElement == EMagicElement.FIRE) {
			// Only affect fire
			if (targetElement == EMagicElement.FIRE) {
				reduc = setTotalFire[setCount - 1]; // (1/4 per piece split evenly)
			} else {
				reduc = 0;
			}
		} else if (armorElement == EMagicElement.EARTH) {
			if (targetElement == EMagicElement.EARTH) {
				reduc = setTotalEarth[setCount - 1];
			} else if (targetElement == null) {
				reduc = setTotalEarthBonus[setCount - 1];
			} else {
				reduc = 0;
			}
		} else if (armorElement == EMagicElement.ENDER) {
			if (targetElement == EMagicElement.ENDER) {
				reduc = setTotalEnder[setCount - 1];
			} else if (targetElement == null) {
				reduc = setTotalEnderBonus[setCount - 1];
			} else {
				reduc = 0;
			}
		} else if (armorElement == EMagicElement.WIND) {
			if (targetElement == EMagicElement.WIND) {
				reduc = setTotalWind[setCount - 1];
			} else {
				reduc = 0;
			}
		} else if (armorElement == EMagicElement.LIGHTNING) {
			if (targetElement == EMagicElement.LIGHTNING) {
				reduc = setTotalLightning[setCount - 1];
			} else {
				reduc = 0;
			}
		} else if (armorElement == EMagicElement.ICE) {
			if (targetElement == null) {
				reduc = setTotalIce[setCount - 1];
			} else {
				reduc = 0;
			}
		} else {
			reduc = 0;
		}

		return reduc;
	}

	private static int calcArmorDurability(EquipmentSlot slot, EMagicElement element, Type type) {
		float mod = 1f;
		switch (element) {
		case EARTH:
			mod = 1.1f;
			break;
		case PHYSICAL:
			mod = 1.2f;
			break;
		case FIRE:
			mod = 0.9f;
			break;
		case ENDER:
			mod = 0.8f;
			break;
		default:
			break;
		}

		int diamond = ArmorMaterials.DIAMOND.getDurabilityForSlot(slot);
		double amt = diamond * 1.25 * type.scale; // 80, 90, 100% of (125% of diamond)

		return (int) Math.floor(amt * mod);
	}

	private static double calcArmorSpeedBoost(EquipmentSlot slot, EMagicElement element, Type type) {
		if (type != Type.MASTER) {
			return 0;
		}

		switch (element) {
		case EARTH:
		case ENDER:
		case FIRE:
		case ICE:
		case LIGHTNING:
		case PHYSICAL:
			return .05; // +5% per piecce. 20% for 4 pieces (same as speed I)
		case WIND:
			return .075; // +7.5% per piecce. 30% for 4 pieces (halfway between speed I and II)
		}

		return 0;
	}

	private static double calcArmorJumpBoost(EquipmentSlot slot, EMagicElement element, Type type) {
		if (type != Type.MASTER) {
			return 0;
		}

		switch (element) {
		case EARTH:
		case ENDER:
		case FIRE:
		case ICE:
		case LIGHTNING:
		case PHYSICAL:
			return .2; // same as jump-boost 2
		case WIND:
			return .3;
		}

		return 0;
	}

	public static double CalcArmorMagicBoostTotal(EMagicElement element, Type type, int setCount) {
		if (setCount < 2 || setCount > 4) {
			return 0;
		}

		double total = 0;
		switch (element) {
		case EARTH:
		case ICE:
		case PHYSICAL:
		case WIND:
			total = 0;
			break;
		case FIRE:
		case ENDER:
			total = 10;
			break;
		case LIGHTNING:
			total = 25;
			break;
		}

		// This has the effect of making 2 be like 1/4, 3 being 2/4, and 4 being 4/4
		final int mult = (setCount == 4 ? 4 : setCount - 1);
		return mult * (total / 4);
	}

	private static int calcArmorToughness(EquipmentSlot slot, EMagicElement element, Type type) {
		return type.ordinal() + 2;
	}

	private final Type type;
	private int armor; // Can't use vanilla; it's final
	private int armorToughness;
	private double magicResistAmount;
	private double magicReducAmount;
	private EMagicElement element;
	private double jumpBoost;
	private double speedBoost;

	// TODO: move?
	@OnlyIn(Dist.CLIENT)
	private static List<EnchantedArmorBaseModel<LivingEntity>> armorModels;

	public ElementalArmor(EMagicElement element, EquipmentSlot slot, Type type, Item.Properties builder) {
		super(ArmorMaterials.IRON, slot, builder.durability(calcArmorDurability(slot, element, type)));

		this.type = type;
		this.element = element;
		this.armor = calcArmor(slot, element, type);
		this.magicResistAmount = (Math.round((double) calcMagicResistBase(slot, element, type) * 100.0D));
		this.magicReducAmount = calcMagicReduct(slot, element, type);
		this.jumpBoost = calcArmorJumpBoost(slot, element, type);
		this.speedBoost = calcArmorSpeedBoost(slot, element, type);
		this.armorToughness = calcArmorToughness(slot, element, type);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();

		if (equipmentSlot == this.slot) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.putAll(multimap);
			builder.put(Attributes.ARMOR, new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()],
					"Armor modifier", (double) this.armor, AttributeModifier.Operation.ADDITION));
			builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()],
					"Armor toughness", this.armorToughness, AttributeModifier.Operation.ADDITION));
			builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(ARMOR_SPEED_MODS[equipmentSlot.getIndex()],
					"Armor speed boost", (double) this.speedBoost, AttributeModifier.Operation.MULTIPLY_TOTAL));
			builder.put(NostrumAttributes.magicResist, new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()],
					"Magic Resist", (double) this.magicResistAmount, AttributeModifier.Operation.ADDITION));
			builder.put(NostrumAttributes.GetReduceAttribute(this.element),
					new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Magic Reduction",
							(double) this.magicReducAmount, AttributeModifier.Operation.ADDITION));
			multimap = builder.build();
		}

		return multimap;
	}

	@Override
	public int getEnchantmentValue() {
		return 16;
	}

	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return false;
	}

	@Override
	public SpellAction getTriggerAction(LivingEntity user, boolean offense, @Nonnull ItemStack stack) {
		if (offense)
			return null;

		if (!GetArmorEffectsEnabled(user)) {
			return null;
		}

		SpellAction action = null;
		switch (element) {
		case EARTH:
//			if (NostrumMagica.rand.nextFloat() <= 0.15f * (float) (Math.min(2, level) + 1))
			action = new SpellAction().status(NostrumEffects.rooted, (int) (20 * 5 * type.scale), 0);
			break;
		case ENDER:
//			if (NostrumMagica.rand.nextFloat() <= 0.15f * (float) (Math.min(2, level) + 1))
			action = new SpellAction().phase(Math.min(2, type.ordinal()));
			break;
		case FIRE:
//			if (NostrumMagica.rand.nextFloat() <= 0.35f * (float) (Math.min(2, level) + 1))
			action = new SpellAction().burn(5 * 20);
			break;
		case PHYSICAL:
		case WIND:
		case LIGHTNING:
		case ICE:
		default:
			break;

		}

		return action;
	}

	@Override
	public boolean shouldTrigger(boolean offense, @Nonnull ItemStack stack) {
		final float chancePer = (this.element == EMagicElement.FIRE ? .2f : .15f);
		return !offense && NostrumMagica.rand.nextFloat() <= chancePer * (float) (Math.min(2, type.scale) + 1);
	}

	public static ElementalArmor get(EMagicElement element, EquipmentSlot slot, Type type) {
		ElementalArmor armor = null;

		switch (element) {
		case EARTH:
			switch (slot) {
			case HEAD:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorEarthHeadNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorEarthHeadAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorEarthHeadMaster;
					break;
				}
				break;
			case CHEST:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorEarthChestNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorEarthChestAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorEarthChestMaster;
					break;
				}
				break;
			case LEGS:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorEarthLegsNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorEarthLegsAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorEarthLegsMaster;
					break;
				}
				break;
			case FEET:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorEarthFeetNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorEarthFeetAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorEarthFeetMaster;
					break;
				}
				break;
			case MAINHAND:
			case OFFHAND:
				break;
			}
			break;
		case ENDER:
			switch (slot) {
			case HEAD:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorEnderHeadNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorEnderHeadAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorEnderHeadMaster;
					break;
				}
				break;
			case CHEST:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorEnderChestNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorEnderChestAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorEnderChestMaster;
					break;
				}
				break;
			case LEGS:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorEnderLegsNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorEnderLegsAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorEnderLegsMaster;
					break;
				}
				break;
			case FEET:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorEnderFeetNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorEnderFeetAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorEnderFeetMaster;
					break;
				}
				break;
			case MAINHAND:
			case OFFHAND:
				break;
			}
			break;
		case FIRE:
			switch (slot) {
			case HEAD:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorFireHeadNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorFireHeadAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorFireHeadMaster;
					break;
				}
				break;
			case CHEST:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorFireChestNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorFireChestAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorFireChestMaster;
					break;
				}
				break;
			case LEGS:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorFireLegsNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorFireLegsAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorFireLegsMaster;
					break;
				}
				break;
			case FEET:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorFireFeetNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorFireFeetAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorFireFeetMaster;
					break;
				}
				break;
			case MAINHAND:
			case OFFHAND:
				break;
			}
			break;
		case ICE:
			switch (slot) {
			case HEAD:
				switch (type) {
				case NOVICE:
					// armor = NostrumItems.enchantedArmorIceHeadNovice;
					break;
				case ADEPT:
					// armor = NostrumItems.enchantedArmorIceHeadAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorIceHeadMaster;
					break;
				}
				break;
			case CHEST:
				switch (type) {
				case NOVICE:
					// armor = NostrumItems.enchantedArmorIceChestNovice;
					break;
				case ADEPT:
					// armor = NostrumItems.enchantedArmorIceChestAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorIceChestMaster;
					break;
				}
				break;
			case LEGS:
				switch (type) {
				case NOVICE:
					// armor = NostrumItems.enchantedArmorIceLegsNovice;
					break;
				case ADEPT:
					// armor = NostrumItems.enchantedArmorIceLegsAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorIceLegsMaster;
					break;
				}
				break;
			case FEET:
				switch (type) {
				case NOVICE:
					// armor = NostrumItems.enchantedArmorIceFeetNovice;
					break;
				case ADEPT:
					// armor = NostrumItems.enchantedArmorIceFeetAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorIceFeetMaster;
					break;
				}
				break;
			case MAINHAND:
			case OFFHAND:
				break;
			}
			break;
		case LIGHTNING:
			switch (slot) {
			case HEAD:
				switch (type) {
				case NOVICE:
					// armor = NostrumItems.enchantedArmorLightningHeadNovice;
					break;
				case ADEPT:
					// armor = NostrumItems.enchantedArmorLightningHeadAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorLightningHeadMaster;
					break;
				}
				break;
			case CHEST:
				switch (type) {
				case NOVICE:
					// armor = NostrumItems.enchantedArmorLightningChestNovice;
					break;
				case ADEPT:
					// armor = NostrumItems.enchantedArmorLightningChestAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorLightningChestMaster;
					break;
				}
				break;
			case LEGS:
				switch (type) {
				case NOVICE:
					// armor = NostrumItems.enchantedArmorLightningLegsNovice;
					break;
				case ADEPT:
					// armor = NostrumItems.enchantedArmorLightningLegsAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorLightningLegsMaster;
					break;
				}
				break;
			case FEET:
				switch (type) {
				case NOVICE:
					// armor = NostrumItems.enchantedArmorLightningFeetNovice;
					break;
				case ADEPT:
					// armor = NostrumItems.enchantedArmorLightningFeetAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorLightningFeetMaster;
					break;
				}
				break;
			case MAINHAND:
			case OFFHAND:
				break;
			}
			break;
		case PHYSICAL:
			switch (slot) {
			case HEAD:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorPhysicalHeadNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorPhysicalHeadAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorPhysicalHeadMaster;
					break;
				}
				break;
			case CHEST:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorPhysicalChestNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorPhysicalChestAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorPhysicalChestMaster;
					break;
				}
				break;
			case LEGS:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorPhysicalLegsNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorPhysicalLegsAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorPhysicalLegsMaster;
					break;
				}
				break;
			case FEET:
				switch (type) {
				case NOVICE:
					armor = NostrumItems.enchantedArmorPhysicalFeetNovice;
					break;
				case ADEPT:
					armor = NostrumItems.enchantedArmorPhysicalFeetAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorPhysicalFeetMaster;
					break;
				}
				break;
			case MAINHAND:
			case OFFHAND:
				break;
			}
			break;
		case WIND:
			switch (slot) {
			case HEAD:
				switch (type) {
				case NOVICE:
					// armor = NostrumItems.enchantedArmorWindHeadNovice;
					break;
				case ADEPT:
					// armor = NostrumItems.enchantedArmorWindHeadAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorWindHeadMaster;
					break;
				}
				break;
			case CHEST:
				switch (type) {
				case NOVICE:
					// armor = NostrumItems.enchantedArmorWindChestNovice;
					break;
				case ADEPT:
					// armor = NostrumItems.enchantedArmorWindChestAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorWindChestMaster;
					break;
				}
				break;
			case LEGS:
				switch (type) {
				case NOVICE:
					// armor = NostrumItems.enchantedArmorWindLegsNovice;
					break;
				case ADEPT:
					// armor = NostrumItems.enchantedArmorWindLegsAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorWindLegsMaster;
					break;
				}
				break;
			case FEET:
				switch (type) {
				case NOVICE:
					// armor = NostrumItems.enchantedArmorWindFeetNovice;
					break;
				case ADEPT:
					// armor = NostrumItems.enchantedArmorWindFeetAdept;
					break;
				case MASTER:
					armor = NostrumItems.enchantedArmorWindFeetMaster;
					break;
				}
				break;
			case MAINHAND:
			case OFFHAND:
				break;
			}
			break;
		}

		return armor;
	}

	public Type getType() {
		return type;
	}

	public EMagicElement getElement() {
		return element;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		if (!(stack.getItem() instanceof ElementalArmor)) {
			return null;
		}

		// Could support overlay. For now, have unique brightness-adjusted textures for
		// each.
		if (type != null && type.equalsIgnoreCase("overlay")) {
			// return NostrumMagica.MODID + ":textures/models/armor/none.png";
			return NostrumMagica.MODID + ":textures/models/armor/magic_armor_" + element.name().toLowerCase()
					+ "_overlay.png";
		}

		return NostrumMagica.MODID + ":textures/models/armor/magic_armor_" + element.name().toLowerCase() + ".png";
	}
	
	@Override
	public void initializeClient(Consumer<IItemRenderProperties> props) {
		super.initializeClient(props);
		props.accept(new IItemRenderProperties() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public HumanoidModel getArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
					HumanoidModel defaultModel) {
				if (armorModels == null) {
					armorModels = new ArrayList<EnchantedArmorBaseModel<LivingEntity>>(5);
					armorModels.add(0, new EnchantedArmorBaseModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(NostrumModelLayers.ElemArmor_0)));
					armorModels.add(1, new EnchantedArmorBaseModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(NostrumModelLayers.ElemArmor_1)));
					armorModels.add(2, new EnchantedArmorBaseModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(NostrumModelLayers.ElemArmor_2)));
					armorModels.add(3, new EnchantedArmorBaseModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(NostrumModelLayers.ElemArmor_3)));
					armorModels.add(4, new EnchantedArmorBaseModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(NostrumModelLayers.ElemArmor_4)));
				}
				
				final int setCount = getSetPieces(entity);
				final int index = (setCount - 1) + (type == Type.MASTER ? 1 : 0); // Boost 1 if ultimate armor
				EnchantedArmorBaseModel<LivingEntity> model = armorModels.get(index % armorModels.size());
				model.setVisibleFrom(slot);

				return model;
			}
		});
	}

	public static int GetSetCount(LivingEntity entity, EMagicElement element, Type type) {
		int count = 0;

		if (entity != null) {
			for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST,
					EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
				ItemStack inSlot = entity.getItemBySlot(slot);
				if (inSlot.isEmpty() || !(inSlot.getItem() instanceof ElementalArmor)) {
					continue;
				}

				ElementalArmor item = (ElementalArmor) inSlot.getItem();
				if (item.getElement() == element && item.getType() == type) {
					count++;
				}
			}
		}

		return count;
	}

	public static int GetSetPieces(LivingEntity entity, ElementalArmor armor) {
		final EMagicElement myElem = armor.getElement();
		final Type myType = armor.getType();
		return GetSetCount(entity, myElem, myType);
	}

	public int getSetPieces(LivingEntity entity) {
		return GetSetPieces(entity, this);
	}

	@Override
	public int getColor(ItemStack stack) {
		// This is brightness. Different elements already tint their textures. We just
		// make brighter with level.
		switch (type) {
		default:
		case NOVICE:
			return 0xFF3F3F3F;
		case ADEPT:
			return 0xFF7F7F7F;
		case MASTER:
			return 0xFFFFFFFF;
		}
	}

	public static List<ElementalArmor> getAll() {
		List<ElementalArmor> list = new LinkedList<>();

		for (EMagicElement element : EMagicElement.values()) {
			for (EquipmentSlot slot : EquipmentSlot.values())
				if (slot.getType() == EquipmentSlot.Type.ARMOR) {
					for (Type type : Type.values()) {
						list.add(get(element, slot, type));
					}
				}
		}

		return list;
	}

	protected void onArmorDisplayTick(Level world, Player player, ItemStack itemStack, int setCount) {
		final int displayLevel = (Math.min(2, type.ordinal()) + 1) * (setCount * setCount);

		if (NostrumMagica.rand.nextInt(400) > displayLevel) {
			return;
		}

		final double dx;
		final double dy;
		final double dz;
		final ParticleOptions effect;
		final int mult;
		final float rangeMod;
		switch (element) {
		case EARTH:
			effect = ParticleTypes.MYCELIUM;
			dx = dy = dz = 0;
			mult = 1;
			rangeMod = 1;
			break;
		case FIRE:
			effect = ParticleTypes.FLAME;
			dx = dz = 0;
			dy = .025;
			mult = 1;
			rangeMod = 2;
			break;
		case ENDER:
			effect = ParticleTypes.PORTAL;
			dx = dy = dz = 0;
			mult = 2;
			rangeMod = 1.5f;
			break;
		case ICE:
			effect = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SNOW_BLOCK.defaultBlockState());
			dx = dz = 0;
			dy = .025;
			mult = 1;
			rangeMod = 2;
			break;
		case LIGHTNING:
			effect = null;
			dx = dz = dy = mult = 0;
			rangeMod = 0;
			NostrumParticles.LIGHTNING_STATIC.spawn(world,
					new SpawnParams(1, player.getX(), player.getY() + 1, player.getZ(), 1, 20 * 1, 0,
							new Vec3(0, 0.01 * (NostrumMagica.rand.nextBoolean() ? 1 : -1), 0), null).color(.8f, 1f,
									1f, 0f));
			break;
		case PHYSICAL:
		case WIND:
		default:
			effect = null;
			dx = dy = dz = 0;
			mult = 0;
			rangeMod = 0;
			break;
		}

		if (effect == null) {
			return;
		}

		for (int i = 0; i < mult; i++) {
			final float rd = NostrumMagica.rand.nextFloat();
			final float radius = .5f + (NostrumMagica.rand.nextFloat() * (.5f * rangeMod));
			final double px = (player.getX() + radius * Math.cos(rd * Math.PI * 2));
			final double py = (player.getY() + (NostrumMagica.rand.nextFloat() * 2));
			final double pz = (player.getZ() + radius * Math.sin(rd * Math.PI * 2));
			world.addParticle(effect, px, py, pz, dx, dy, dz);
		}
	}

	protected void onServerTick(Level world, Player player, ItemStack stack, int setCount) {
		if (setCount == 4 && this.type == Type.MASTER && this.slot == EquipmentSlot.CHEST) {
			if (element == EMagicElement.ICE) {
				if (player.isOnGround() && !ArmorCheckFlying(player) && GetArmorEffectsEnabled(player)) {
					final BlockPos pos = player.blockPosition();
					if (world.isEmptyBlock(pos)) {
						BlockState belowState = world.getBlockState(pos.below());
						if (belowState.getMaterial().blocksMotion()) {
							world.setBlockAndUpdate(pos, NostrumBlocks.mysticSnowLayer.defaultBlockState());
						}
					}
				}
			} else if (element == EMagicElement.WIND) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				if (attr != null && attr.getMana() > 0 && player.isSprinting() && !ArmorCheckFlying(player) && GetArmorEffectsEnabled(player)) {
					if (!player.hasEffect(MobEffects.MOVEMENT_SPEED) || player.tickCount % 10 == 0) {
						player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 0));
					}
					if (!player.hasEffect(MobEffects.JUMP) || player.tickCount % 10 == 0) {
						player.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 1));
					}

					// Refresh nearby tornados
					if (player.isOnGround())
						for (AreaEffectEntity cloud : world.getEntitiesOfClass(AreaEffectEntity.class,
								(new AABB(0, 0, 0, 1, 1, 1))
										.move(player.getX(), player.getY(), player.getZ()).inflate(5),
								(effect) -> {
									// lol
									return effect != null && (effect.getCustomParticle() == ParticleTypes.SWEEP_ATTACK
											|| effect.getParticle() == ParticleTypes.SWEEP_ATTACK);
								})) {
							cloud.addTime(1, true);
						}

					if (player.tickCount % 3 == 0) {
						attr.addMana(-1);
						NostrumMagica.instance.proxy.sendMana(player);
					}
				}
			} else if (element == EMagicElement.EARTH) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				if (attr != null && attr.getMana() >= EARTH_GROW_COST && !ArmorCheckFlying(player) && GetArmorEffectsEnabled(player)) {
					if (player.tickCount % 40 == 0) {
						// Attempt bonemeal
						if (DoEarthGrow(world, player.blockPosition()) != null) {
							attr.addMana(-EARTH_GROW_COST);
							NostrumMagica.instance.proxy.sendMana(player);
						}
					}
				}
			}
		}
	}

	@Override
	public void onArmorTick(ItemStack itemStack, Level world, Player player) {
		super.onArmorTick(itemStack, world, player);

		final int setCount = getSetPieces(player);
		if (!world.isClientSide) {
			onServerTick(world, player, itemStack, setCount);
		} else {
			onArmorDisplayTick(world, player, itemStack, setCount);
		}
	}

	public static final boolean EntityHasEnchantedArmor(LivingEntity entity) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() != EquipmentSlot.Type.ARMOR) {
				continue;
			}

			ItemStack inSlot = entity.getItemBySlot(slot);
			if (!inSlot.isEmpty() && inSlot.getItem() instanceof ElementalArmor) {
				return true;
			}
		}

		return false;
	}

	// Updates all entities' current set bonuses (or lack there-of) from enchanted
	// armor
	public static void ServerWorldTick(ServerLevel world) {
		world.getEntities().get(EntityTypeTest.forClass(LivingEntity.class), (living) -> {
			if (living.isFallFlying() && living.isShiftKeyDown() && living instanceof ServerPlayer) {
				((ServerPlayer) living).stopFallFlying();
			}
		});
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		
		if (ElementalArmor.GetHasWingUpgrade(stack)) {
			tooltip.add(new TranslatableComponent("info.armor.wing_upgrade").withStyle(ChatFormatting.GOLD));
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderElyta(LivingEntity entity, ItemStack stack) {
		return stack != entity.getMainHandItem() && stack != entity.getOffhandItem() && hasElytra(entity)
				&& (element == EMagicElement.ICE || element == EMagicElement.LIGHTNING || element == EMagicElement.WIND)
				&& (!(entity instanceof Player) || !(shouldRenderDragonWings(stack, (Player) entity)));
	}

	protected boolean hasElytra(LivingEntity entity) {
		if (this.type == Type.MASTER && this.slot == EquipmentSlot.CHEST) {
			// Check if full set is available
			return (4 == getSetPieces(entity));
		}
		return false;
	}

	protected static boolean HasElytra(LivingEntity entity) {
		ElementalArmor piece = getChestPiece(entity);
		if (piece == null) {
			return false;
		}

		return piece.hasElytra(entity);
	}

	protected static @Nullable ElementalArmor getChestPiece(LivingEntity entity) {
		ItemStack chestpiece = entity.getItemBySlot(EquipmentSlot.CHEST);
		if (chestpiece.isEmpty() || !(chestpiece.getItem() instanceof ElementalArmor)) {
			return null;
		}

		return (ElementalArmor) chestpiece.getItem();
	}

	private static final int EARTH_SCAN_RANGE_XZ = 21;
	private static final int EARTH_SCAN_RANGE_Y = 3;
	private static final List<BlockPos> EARTH_SCAN_POS = new ArrayList<>(
			EARTH_SCAN_RANGE_XZ * EARTH_SCAN_RANGE_XZ * EARTH_SCAN_RANGE_Y);

	static {
		final int xzRadius = (EARTH_SCAN_RANGE_XZ / 2); // int div
		final int yRadius = (EARTH_SCAN_RANGE_Y / 2); // int div
		for (int x = -xzRadius; x <= xzRadius; x++)
			for (int z = -xzRadius; z <= xzRadius; z++)
				for (int y = -yRadius; y <= yRadius; y++) {
					EARTH_SCAN_POS.add(new BlockPos(x, y, z));
				}
	}

	protected static final int MANA_JUMP_COST = 50;
	protected static final int MANA_DRAGON_FLIGHT = 1;
	protected static final int WIND_TORNADO_COST = 50;
	protected static final int WIND_WHIRLWIND_COST = 20;
	protected static final int ENDER_DASH_COST = 20;
	protected static final int EARTH_GROW_COST = 5;

	protected boolean hasManaJump(LivingEntity entity) {
		if (this.type == Type.MASTER && this.slot == EquipmentSlot.CHEST) {
			// Check if full set is available and if we have enough mana
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
			if (attr == null || attr.getMana() < MANA_JUMP_COST) {
				return false;
			}
			return (4 == getSetPieces(entity));
		}
		return false;
	}

	protected static boolean HasManaJump(LivingEntity entity) {
		ElementalArmor piece = getChestPiece(entity);
		return piece == null ? false : piece.hasManaJump(entity);
	}

	protected boolean hasWindTornado(LivingEntity entity) {
		if (this.type == Type.MASTER && this.slot == EquipmentSlot.CHEST && this.element == EMagicElement.WIND) {
			// Check if full set is available and if we have enough mana
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
			if (attr == null || attr.getMana() < WIND_TORNADO_COST) {
				return false;
			}
			return (4 == getSetPieces(entity));
		}
		return false;
	}

	protected static boolean HasWindTornado(LivingEntity entity) {
		ElementalArmor piece = getChestPiece(entity);
		return piece == null ? false : piece.hasWindTornado(entity);
	}

	protected boolean hasEnderDash(LivingEntity entity) {
		if (this.type == Type.MASTER && this.slot == EquipmentSlot.CHEST && this.element == EMagicElement.ENDER) {
			// Check if full set is available and if we have enough mana
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
			if (attr == null || attr.getMana() < ENDER_DASH_COST) {
				return false;
			}
			return (4 == getSetPieces(entity));
		}
		return false;
	}

	protected static boolean HasEnderDash(LivingEntity entity) {
		ElementalArmor piece = getChestPiece(entity);
		return piece == null ? false : piece.hasEnderDash(entity);
	}

	protected boolean hasDragonFlight(LivingEntity entity) {
		if (this.type == Type.MASTER && this.slot == EquipmentSlot.CHEST) {
			boolean hasRightElement = element == EMagicElement.ENDER || element == EMagicElement.EARTH
					|| element == EMagicElement.FIRE || element == EMagicElement.PHYSICAL;
			if (!hasRightElement) {
				ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
				hasRightElement = ElementalArmor.GetHasWingUpgrade(chest);
			}
			if (hasRightElement) {
				// Check if full set is available and if we have enough mana
				INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
				if (attr == null || attr.getMana() < MANA_DRAGON_FLIGHT) {
					return false;
				}
				return (4 == getSetPieces(entity));
			}
		}

		return false;
	}

	protected static boolean HasDragonFlight(LivingEntity entity) {
		ElementalArmor piece = getChestPiece(entity);
		return piece == null ? false : piece.hasDragonFlight(entity);
	}

	protected static void consumeManaJump(LivingEntity entity) {
		if (entity instanceof Player && ((Player) entity).isCreative()) {
			return;
		}

		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null) {
			attr.addMana(-MANA_JUMP_COST);
		}
	}

	protected static void consumeWindTornado(LivingEntity entity) {
		if (entity instanceof Player && ((Player) entity).isCreative()) {
			return;
		}

		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null) {
			attr.addMana(-WIND_TORNADO_COST);
		}
	}

	protected static void consumeWindJumpWhirlwind(LivingEntity entity) {
		if (entity instanceof Player && ((Player) entity).isCreative()) {
			return;
		}

		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null) {
			attr.addMana(-WIND_WHIRLWIND_COST);
		}
	}

	protected static void consumeEnderDash(LivingEntity entity) {
		if (entity instanceof Player && ((Player) entity).isCreative()) {
			return;
		}

		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null) {
			attr.addMana(-ENDER_DASH_COST);
		}
	}

	protected static void consumeDragonFlight(LivingEntity entity) {
		if (entity instanceof Player && ((Player) entity).isCreative()) {
			return;
		}

		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null) {
			attr.addMana(-MANA_DRAGON_FLIGHT);
		}
	}

	private static boolean lastTickGround = false; // For checking if the player just jumped this tick
	private static boolean backPressedEarly = false;
	private static long lastBackMSecs = -1;
	private static boolean leftPressedEarly = false;
	private static long lastLeftMSecs = -1;
	private static boolean rightPressedEarly = false;
	private static long lastRightMSecs = -1;

	private static KeyMapping bindingEnderLeft;
	private static KeyMapping bindingEnderRight;
	private static KeyMapping bindingEnderBack;
	// private static KeyBinding bindingSummonTornado;
	private static KeyMapping bindingSummonJumpWhirlwind;
	private static KeyMapping bindingToggleArmorEffect;

	public static final void ClientInit() {
		bindingEnderLeft = new KeyMapping("key.dash.left.desc", GLFW.GLFW_KEY_KP_7, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingEnderLeft);

		bindingEnderRight = new KeyMapping("key.dash.right.desc", GLFW.GLFW_KEY_KP_9, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingEnderRight);

		bindingEnderBack = new KeyMapping("key.dash.back.desc", GLFW.GLFW_KEY_KP_2, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingEnderBack);

		bindingSummonJumpWhirlwind = new KeyMapping("key.wind.jump.desc", GLFW.GLFW_KEY_M, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingSummonJumpWhirlwind);

		bindingToggleArmorEffect = new KeyMapping("key.armor.toggle.desc", GLFW.GLFW_KEY_KP_5,
				"key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingToggleArmorEffect);

	}

	protected static void clientDashSide(LivingEntity ent, boolean right) {
		consumeEnderDash(ent);
		NetworkHandler.sendToServer(new EnchantedArmorStateUpdate(ArmorState.ENDER_DASH_SIDE, right, 0));
	}

	protected static void clientDashBack(LivingEntity ent) {
		consumeEnderDash(ent);
		NetworkHandler.sendToServer(new EnchantedArmorStateUpdate(ArmorState.ENDER_DASH_BACK, false, 0));
	}

	@SubscribeEvent
	public static void onKey(KeyInputEvent event) {
		Player player = NostrumMagica.instance.proxy.getPlayer();
		if (bindingEnderLeft.consumeClick()) {
			clientDashSide(player, false);
		} else if (bindingEnderRight.consumeClick()) {
			clientDashSide(player, true);
		} else if (bindingEnderBack.consumeClick()) {
			clientDashBack(player);
		} else if (bindingSummonJumpWhirlwind.consumeClick()) {
			NetworkHandler.sendToServer(new EnchantedArmorStateUpdate(ArmorState.WIND_JUMP_WHIRLWIND, false, 0));
		} else if (bindingToggleArmorEffect.consumeClick()) {
			NostrumMagicaSounds.UI_TICK.playClient(player);
			final boolean enabled = !GetArmorEffectsEnabled(player);
			SetArmorEffectsEnabled(player, enabled);
			NetworkHandler.sendToServer(new EnchantedArmorStateUpdate(ArmorState.EFFECT_TOGGLE, enabled, 0));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientJump(PlayerJumpEvent.Post event) {
		final LocalPlayer player = (LocalPlayer) event.getPlayer();
		final boolean flying = ArmorCheckFlying(player);
		
		boolean hasJump = true;
		
		// Start flying (this logic is meant to match the elytra check)
		if (!flying && hasJump && !player.isOnGround() && !player.getAbilities().flying && player.isFallFlying()) {
			// Does this armor support flying?
			if (HasElytra(player)) {
				SetArmorFlying(player, true);
				SendUpdates(player, null);
				hasJump = false;
			}
		}
		
		// Mana jump
		final double MANA_JUMP_AMT = flying ? .6 : .4;
		if (hasJump && flying && !player.isOnGround() && !lastTickGround && !player.getAbilities().flying
				&& player.getDeltaMovement().y < MANA_JUMP_AMT) {
			// Does this armor have mana jump?
			if (HasManaJump(player)) {
				consumeManaJump(player);
				player.setDeltaMovement(player.getDeltaMovement().add(0, MANA_JUMP_AMT, 0));
				hasJump = false; // Consumed
				NetworkHandler.sendToServer(new EnchantedArmorStateUpdate(ArmorState.JUMP, true, 0));
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		Minecraft mc = Minecraft.getInstance();
		final LocalPlayer player = mc.player;
		if (player == null) {
			return;
		}

		if (event.phase != TickEvent.Phase.END) {
			backPressedEarly = player.input == null ? false : player.input.down;
			leftPressedEarly = player.input == null ? false : player.input.left;
			rightPressedEarly = player.input == null ? false : player.input.right;
			return;
		}

		if (player.input == null) {
			return;
		}

		final boolean backPress = (player.input.down && !backPressedEarly);
		final boolean leftPress = (player.input.left && !leftPressedEarly);
		final boolean rightPress = (player.input.right && !rightPressedEarly);
		final boolean doubleBack;
		final boolean doubleLeft;
		final boolean doubleRight;
		final long msecAllowance = 200;
		final long nowMsecs = System.currentTimeMillis();
		if (backPress) {
			doubleBack = (nowMsecs - lastBackMSecs < msecAllowance);
			lastBackMSecs = nowMsecs;
			lastLeftMSecs = 0;
			lastRightMSecs = 0;
		} else {
			doubleBack = false;
		}
		if (leftPress) {
			doubleLeft = (nowMsecs - lastLeftMSecs < msecAllowance);
			lastLeftMSecs = nowMsecs;
			lastBackMSecs = 0;
			lastRightMSecs = 0;
		} else {
			doubleLeft = false;
		}
		if (rightPress) {
			doubleRight = (nowMsecs - lastRightMSecs < msecAllowance);
			lastRightMSecs = nowMsecs;
			lastLeftMSecs = 0;
			lastBackMSecs = 0;
		} else {
			doubleRight = false;
		}

		final boolean flying = ArmorCheckFlying(player);

		// If we've landed, turn off flying
		if (flying && (player.isOnGround() || player.isShiftKeyDown() || player.isPassenger() || player.isInWater()
				|| player.isInLava())) {
			SetArmorFlying(player, false);
			SendUpdates(player, null);
			return;
		}

		// Dragon flying
		if (flying && !player.isOnGround() && !player.getAbilities().flying && player.input.up) {
			// Does this armor have dragon flying?
			if (HasDragonFlight(player)) {
				// Check if magnitude of flying is low and if so, boost it with magic
				final double curMagnitudeSq = (player.getDeltaMovement().x * player.getDeltaMovement().x
						+ player.getDeltaMovement().y * player.getDeltaMovement().y + player.getDeltaMovement().z * player.getDeltaMovement().z);
				if (curMagnitudeSq < .4) {
					// How much should we scale?
					final double curMagnitude = Math.sqrt(curMagnitudeSq);
					final double target = 0.63;
					final double scale = target / curMagnitude;
					final double origX = player.getDeltaMovement().x;
					final double origY = player.getDeltaMovement().y;
					final double origZ = player.getDeltaMovement().z;
					player.setDeltaMovement(player.getDeltaMovement().scale(scale));

					final double dx = Math.abs(player.getDeltaMovement().x - origX);
					final double dy = Math.abs(player.getDeltaMovement().y - origY);
					final double dz = Math.abs(player.getDeltaMovement().z - origZ);

					// We take mana depending on how 'up' we're being propeled
					final float vertScale = (dx == 0 && dy == 0 && dz == 0 ? 0f : (float) (dy / (dx + dy + dz)));
					final boolean deduct = vertScale == 0f ? false : (NostrumMagica.rand.nextFloat() < vertScale * 3);
					if (deduct) {
						consumeDragonFlight(player);
						NetworkHandler
								.sendToServer(new EnchantedArmorStateUpdate(ArmorState.DRAGON_FLIGHT_TICK, deduct, 0));
					}
				}
			}
		}

		// Double-press abilities
		if (!flying && !player.getAbilities().flying) {
			// just for testing
			if (doubleBack) {
				if (HasWindTornado(player)) {
					consumeWindTornado(player);
					NetworkHandler.sendToServer(new EnchantedArmorStateUpdate(ArmorState.WIND_TORNADO, true, 0));
					return;
				}

				if (ModConfig.config.doubleEnderDash() && HasEnderDash(player)) {
					clientDashBack(player);
					return;
				}
			}

			if (doubleLeft) {
				if (ModConfig.config.doubleEnderDash() && HasEnderDash(player)) {
					clientDashSide(player, false);
					return;
				}
			}

			if (doubleRight) {
				if (ModConfig.config.doubleEnderDash() && HasEnderDash(player)) {
					clientDashSide(player, true);
					return;
				}
			}
		}

		lastTickGround = player.isOnGround();
	}

	@SubscribeEvent
	public static void onTrack(PlayerEvent.StartTracking event) {
		// Server-side. A player has started tr acking an entity. Send the 'start' state
		// of their armor, if any
		// TODO if there was more than flying, we'd want to iterate every possible type
		// and send updates for each
		// (which would clear an entity's old state if something was active when they
		// left and they just came back)

		if (event.getTarget() instanceof LivingEntity) {
			SendUpdates((LivingEntity) event.getTarget(), event.getPlayer());
		}
	}

	protected static final void SendUpdates(LivingEntity entity, @Nullable Player toPlayer) {
		// Note: We only do players for now
		if (entity instanceof Player) {
			final Player player = (Player) entity;
			final EnchantedArmorStateUpdate message = new EnchantedArmorStateUpdate(ArmorState.FLYING,
					ArmorCheckFlying(player), player.getId());
			if (player.level.isClientSide) {
				assert (player == NostrumMagica.instance.proxy.getPlayer());
				NetworkHandler.sendToServer(message);
			} else if (toPlayer != null) {
				NetworkHandler.sendTo(message, (ServerPlayer) toPlayer);
			} else {
				NetworkHandler.sendToDimension(message, DimensionUtils.GetDimension(player));
			}
		}
	}

	public static void worldUnload() {
		synchronized (ArmorFlyingMap) {
			ArmorFlyingMap.clear();
		}
	}

	private static enum FlyingTrackedData {
		LAST_WING_FLAP,
	}

	private static final float WING_FLAP_DURATION = 20f;

	// Note: We only care about players. This could be expanded but would also need
	// better cleanup
	// to avoid infinite RAM spam
	private static final Map<UUID, Map<FlyingTrackedData, Integer>> ArmorFlyingMap = new HashMap<>();

	private static final Map<UUID, Boolean> ArmorHitEffectMap = new HashMap<>();

	protected static boolean ArmorCheckFlying(LivingEntity ent) {
		final UUID id = ent.getUUID();
		boolean ret = false;
		synchronized (ArmorFlyingMap) {
			if (ArmorFlyingMap.containsKey(id)) {
				// We encode 'flying at all?' as 'any entry exists'
				return true;
			}
		}
		return ret;
	}

	public static void SetArmorFlying(LivingEntity ent, boolean flying) {
		synchronized (ArmorFlyingMap) {
			if (!flying) {
				ArmorFlyingMap.remove(ent.getUUID());
				ent.fallDistance = 0;
			} else {
				if (!ArmorFlyingMap.containsKey(ent.getUUID())) {
					ArmorFlyingMap.put(ent.getUUID(), new EnumMap<>(FlyingTrackedData.class));
				}
			}
		}
	}

	protected static int ArmorGetLastFlapTick(LivingEntity ent) {
		final UUID id = ent.getUUID();
		synchronized (ArmorFlyingMap) {
			Map<FlyingTrackedData, Integer> map = ArmorFlyingMap.get(id);
			if (map != null) {
				Integer val = map.get(FlyingTrackedData.LAST_WING_FLAP);
				return val == null ? 0 : val.intValue();
			}
		}

		return 0;
	}

	public static boolean SetArmorWingFlap(LivingEntity ent) {
		// Only actually update if no flap is ongoing
		final UUID id = ent.getUUID();
		final int curTicks = ent.tickCount;
		boolean changed = false;
		synchronized (ArmorFlyingMap) {
			Map<FlyingTrackedData, Integer> map = ArmorFlyingMap.get(id);
			final int lastTicks;
			if (map != null) {
				Integer val = map.get(FlyingTrackedData.LAST_WING_FLAP);
				lastTicks = val == null ? 0 : val.intValue();
			} else {
				map = new EnumMap<>(FlyingTrackedData.class);
				ArmorFlyingMap.put(id, map);
				lastTicks = 0;
			}
			if (curTicks > lastTicks + WING_FLAP_DURATION) {
				map.put(FlyingTrackedData.LAST_WING_FLAP, curTicks);
				changed = true;
			}
		}

		return changed;
	}

	public static float GetWingFlap(LivingEntity ent, float partialTicks) {
		double curTicks = ent.tickCount + partialTicks; // Expand to double
		double flapStartTicks = ArmorGetLastFlapTick(ent);
		return Math.max(0.0f, Math.min(1.0f, (float) (curTicks - flapStartTicks) / WING_FLAP_DURATION));
	}

	public static final void SetArmorEffectsEnabled(LivingEntity ent, boolean enabled) {
		final UUID key = ent.getUUID();
		if (enabled) {
			ArmorHitEffectMap.put(key, enabled);
		} else {
			ArmorHitEffectMap.remove(key);
		}
	}

	public static final boolean GetArmorEffectsEnabled(LivingEntity ent) {
		Boolean val = ArmorHitEffectMap.get(ent.getUUID());
		return (val != null && val.booleanValue());
	}

	private static final String NBT_WING_UPGRADE = "dragonwing_upgrade";

	public static final void SetHasWingUpgrade(ItemStack stack, boolean upgraded) {
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
		}

		nbt.putBoolean(NBT_WING_UPGRADE, upgraded);

		stack.setTag(nbt);
	}

	public static final boolean GetHasWingUpgrade(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof ElementalArmor
				&& ((ElementalArmor) stack.getItem()).getSlot() == EquipmentSlot.CHEST && stack.hasTag()
				&& stack.getTag().getBoolean(NBT_WING_UPGRADE);
	}

	private static final boolean DoEnderDash(LivingEntity entity, Vec3 dir) {
		if (DimensionUtils.IsSorceryDim(entity.level)) {
			return false;
		}

		final float dashDist = 4.0f;
		final Vec3 idealVec = entity.position().add(dashDist * dir.x, dashDist * dir.y, dashDist * dir.z);

		// Do three traces from y=0, y=1, and y=2. Take best one
		Vec3 bestResult = null;
		double bestDist = -1;
		final Vec3 startPos = entity.position();
		for (int y = -1; y <= 4; y++) {
			final Vec3 end = idealVec.add(0, y, 0);
			HitResult mop = RayTrace.raytrace(entity.level, entity, startPos.add(0, y, 0), end, (ent) -> {
				return false;
			});

			final Vec3 spot;
			if (mop != null && mop.getType() == HitResult.Type.BLOCK) {
				spot = mop.getLocation();
			} else {
				// Didn't hit anything. Count as ideal
				spot = end;
			}

			final double dist = startPos.add(0, y, 0).distanceTo(spot);
			if (dist > bestDist) {
				bestDist = dist;
				bestResult = spot;
			}
		}

		if (bestResult != null) {
			NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(entity, bestResult.x, bestResult.y,
					bestResult.z, entity);
			if (!event.isCanceled()) {
				if (entity.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), false)) {
					entity.level.playSound(null, startPos.x, startPos.y, startPos.z,
							SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 1f);
					NostrumMagica.fireTeleprotedOtherEvent(event.getEntity(), entity, event.getPrev(),
							event.getTarget());
					return true;
				}
			}
		}
		return false;
	}

	protected static final boolean CanGrow(BlockState state, BonemealableBlock growable) {
		// Don't grow mushrooms and stuff... althoug why not?
		return growable instanceof CropBlock || growable instanceof SaplingBlock
				|| growable instanceof BambooSaplingBlock;
	}

	public static synchronized final @Nullable BlockPos DoEarthGrow(Level world, BlockPos center) {
		Collections.shuffle(EARTH_SCAN_POS);

		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (BlockPos offset : EARTH_SCAN_POS) {
			cursor.set(center.getX() + offset.getX(), center.getY() + offset.getY(), center.getZ() + offset.getZ());
			BlockState state = world.getBlockState(cursor);
			if (state != null && state.getBlock() instanceof BonemealableBlock) {
				BonemealableBlock growable = (BonemealableBlock) state.getBlock();
				if (!CanGrow(state, growable)) {
					continue;
				}
				if (growable.isValidBonemealTarget(world, cursor, state, false)
						&& growable.isBonemealSuccess(world, NostrumMagica.rand, cursor, state)) {
					// Only grow 1/4th the time
					if (NostrumMagica.rand.nextBoolean() && NostrumMagica.rand.nextBoolean()) {
						growable.performBonemeal((ServerLevel) world, NostrumMagica.rand, cursor, state);
					}

					((ServerLevel) world).sendParticles(ParticleTypes.HAPPY_VILLAGER,
							cursor.getX() + .5 + (-.5 + NostrumMagica.rand.nextDouble()), cursor.getY() + .5,
							cursor.getZ() + .5 + (-.5 + NostrumMagica.rand.nextDouble()), 2, .2, .2, .2, 0);
					return cursor.immutable();
				}
			}
		}

		return null;
	}

	public static final boolean DoEarthDig(Level world, Player player, BlockPos pos, Direction face) {
		if (!player.getMainHandItem().isEmpty()) {
			return false;
		}

		if (player.getAttackStrengthScale(0.5F) < .95) {
			return false;
		}

		BlockState state = world.getBlockState(pos);
		if (state == null) {
			return false;
		}

		Block block = state.getBlock();
		if (block != Blocks.STONE && block != Blocks.SANDSTONE && block != Blocks.COBBLESTONE) {
			return false;
		}

		// Can break. Break neighbors, too
		final BlockPos[] positions = (face == Direction.UP || face == Direction.DOWN
				? new BlockPos[] { pos.north().east(), pos.north(), pos.north().west(), pos.east(), pos, pos.west(),
						pos.south().east(), pos.south(), pos.south().west() }
				: face == Direction.NORTH || face == Direction.SOUTH
						? new BlockPos[] { pos.above().east(), pos.above(), pos.above().west(), pos.east(), pos, pos.west(),
								pos.below().east(), pos.below(), pos.below().west() }
						: /* face == Direction.EAST || face == Direction.WEST */ new BlockPos[] { pos.above().north(),
								pos.above(), pos.above().south(), pos.north(), pos, pos.south(), pos.below().north(),
								pos.below(), pos.below().south() });

		for (BlockPos at : positions) {
			state = world.getBlockState(at);
			if (state == null) {
				continue;
			}

			block = state.getBlock();
			if (block != Blocks.STONE && block != Blocks.SANDSTONE && block != Blocks.COBBLESTONE) {
				continue;
			}

			// TODO does this work even though block is destroyed rig ht after entities are
			// created inside it?
			Block.dropResources(state, world, at);
			world.destroyBlock(at, false);
		}

		return true;
	}

	public static final void HandleStateUpdate(ArmorState state, LivingEntity ent, boolean data) {
		ItemStack chest = ent.getItemBySlot(EquipmentSlot.CHEST);
		if (chest.isEmpty() || !(chest.getItem() instanceof ElementalArmor)) {
			return;
		}

		final ElementalArmor armor = (ElementalArmor) chest.getItem();

		switch (state) {
		case FLYING:
			SetArmorFlying(ent, data);
			// Packet handler takes care of forwarding to other entities if this was just
			// received on the server
			break;
		case JUMP:
			// Deduct mana
			if (!ent.level.isClientSide) {
				ElementalArmor.consumeManaJump(ent);
			}
			break;
		case ENDER_DASH_BACK:
			if (!ent.level.isClientSide && armor.hasEnderDash(ent) && !DimensionUtils.IsSorceryDim(ent.level)) {
				// If sneaking, attempt to teleport to a rod ball
				if (ent.isShiftKeyDown()) {
					ItemStack held = ent.getMainHandItem();
					if (held.isEmpty() || !(held.getItem() instanceof AspectedEnderWeapon)) {
						held = ent.getOffhandItem();
					}

					if (held.isEmpty() || !(held.getItem() instanceof AspectedEnderWeapon)) {
						;
					} else {
						AspectedEnderWeapon.AttemptCasterTeleport(ent, held);
					}
				} else {
					final Vec3 realLook = ent.getLookAngle();
					final Vec3 fakeLook = new Vec3(realLook.x, 0, realLook.z);
					Vec3 dir = fakeLook.scale(-1);
					if (DoEnderDash(ent, dir)) {
						ElementalArmor.consumeEnderDash(ent);
					} else {
						if (ent instanceof Player) {
							NostrumMagica.instance.proxy.sendMana((Player) ent);
						}
					}
				}
			}
			break;
		case ENDER_DASH_SIDE:
			if (!ent.level.isClientSide && armor.hasEnderDash(ent) && !DimensionUtils.IsSorceryDim(ent.level)) {
				final Vec3 realLook = ent.getLookAngle();
				final Vec3 fakeLook = new Vec3(realLook.x, 0, realLook.z);
				final Vec3 dir = fakeLook.yRot((float) ((Math.PI / 2) * (data ? -1 : 1)));
				if (DoEnderDash(ent, dir)) {
					consumeEnderDash(ent);
				}
			}
			break;
		case WIND_TORNADO:
			if (!ent.level.isClientSide && armor.hasWindTornado(ent)) {
				consumeWindTornado(ent);
				AreaEffectEntity cloud = new AreaEffectEntity(NostrumEntityTypes.areaEffect, ent.level, ent.getX(),
						ent.getY(), ent.getZ());
				cloud.setOwner(ent);

				cloud.setHeight(5f);
				cloud.setRadius(7.5f);
				cloud.setDuration(0);
				cloud.setWaitTime(20 * 5 + 10);
				cloud.setWaiting(true);
				cloud.addEffect((IAreaEntityEffect) (worldIn, entity) -> {
					if (entity.noPhysics || entity.isNoGravity()) {
						return;
					}

					// Never effect summoner
					if (entity == ent) {
						return;
					}

					// Projectiles get turned downward
					if (entity instanceof Projectile) {
						LivingEntity shooter = Projectiles.getShooter(entity);
						if (shooter == ent) {
							// Let summoner's projectiles go unharmed
							return;
						}

						entity.setDeltaMovement(entity.getDeltaMovement().multiply(.2, 1, .2).add(0, -.3, 0));
//						entity.getMotion().y -= 0.3;
//						entity.getMotion().x *= .2;
//						entity.getMotion().z *= .2;
						entity.hurtMarked = true;
						return;
					}

//					// upward effect
//					final int period = 20;
//					final float prog = ((float) (entity.ticksExisted % period) / (float) period);
//					final double dy = (Math.sin(prog * 2 * Math.PI) + 1) / 2;
//					final Vector3d target = new Vector3d(cloud.getPosX(), cloud.getPosY() + 2 + dy, cloud.getPosZ());
//					final Vector3d diff = target.subtract(entity.getPositionVec());
//					entity.getMotion().x = 0;//diff.x/ 2;
//					entity.getMotion().y = diff.y/ 2;
//					entity.getMotion().z = 0;//diff.z/ 2;
//					entity.velocityChanged = true;
//					//entity.getPosY() = 2 + dy;
//					//entity.setPositionAndUpdate(cloud.getPosX(), cloud.getPosY() + 2 + dy, cloud.getPosZ());

					// Downward suppresive effect
					entity.setDeltaMovement(entity.getDeltaMovement().multiply(.2, 1, .2).add(0, -.3, 0));
//					entity.getMotion().x *= .2;
//					entity.getMotion().z *= .2;
//					entity.getMotion().y -= 0.3;

					// Hurt unfriendlies, too
					if (entity.tickCount % 20 == 0) {
						if (entity instanceof LivingEntity && !NostrumMagica.IsSameTeam((LivingEntity) entity, ent)) {
							LivingEntity living = (LivingEntity) entity;
							entity.invulnerableTime = 0;
							SpellDamage.DamageEntity(living, EMagicElement.WIND, .25f, ent);
							entity.invulnerableTime = 0;

//							NostrumParticles.GLOW_ORB.spawn(living.getEntityWorld(), new NostrumParticles.SpawnParams(
//									 10,
//									 living.getPosX(), entity.getPosY() + entity.height/2f, entity.getPosZ(), entity.width * 2,
//									 10, 5,
//									 living.getEntityId())
//									 .color(EMagicElement.WIND.getColor()));
						}
					}
				});
				cloud.setEffectDelay(0);

//				cloud.setCustomParticle(ParticleTypes.SWEEP_ATTACK);
//				cloud.setCustomParticleParam1(10);
//				cloud.setCustomParticleFrequency(.2f);
				cloud.setParticle(ParticleTypes.MYCELIUM);
				cloud.setWaiting(true);
				cloud.addVFXFunc((worldIn, ticksExisted, cloudIn) -> {
					final int count = 40;
					AspectedWeapon.spawnWhirlwindParticle(worldIn, count, cloudIn.position(), cloudIn, 0xA0C0EEC0,
							.65f);
				});
				cloud.setCustomParticle(ParticleTypes.SWEEP_ATTACK);
				// cloud.setCustomParticleParam1(10);
				cloud.setCustomParticleFrequency(.05f);

				ent.level.addFreshEntity(cloud);
			}
			break;
		case DRAGON_FLIGHT_TICK:
			// Deduct mana
			if (data) {
				if (!ent.level.isClientSide) {
					ElementalArmor.consumeDragonFlight(ent);
				} else {
					if (SetArmorWingFlap(ent)) {
						if (ent instanceof Player) {
							NostrumMagicaSounds.WING_FLAP.playClient((Player) ent);
						}
					}
				}
			}
			break;
		case EFFECT_TOGGLE:
			SetArmorEffectsEnabled(ent, data);
			break;
		case WIND_JUMP_WHIRLWIND:
			if (!ent.level.isClientSide && armor.hasWindTornado(ent)) {
				Player playerIn = (Player) ent;
				ElementalArmor.consumeWindJumpWhirlwind(ent);
				final float maxDist = 20;
				HitResult mop = RayTrace.raytrace(playerIn.level, playerIn,
						playerIn.position().add(0, playerIn.getEyeHeight(), 0), playerIn.getLookAngle(), maxDist,
						(e) -> {
							return e != playerIn;
						});
				if (mop != null && mop.getType() != HitResult.Type.MISS) {
					final Vec3 at = (mop.getType() == HitResult.Type.ENTITY
							? RayTrace.entFromRaytrace(mop).position()
							: mop.getLocation());
					AspectedWeapon.spawnJumpVortex(playerIn.level, playerIn, at, AspectedWeapon.Type.MASTER);
				}
			}
			break;
		}
	}

	@SubscribeEvent
	public static void onJump(LivingJumpEvent event) {
		// Full true/corrupt sets give jump boost
		if (event.isCanceled()) {
			return;
		}

		LivingEntity ent = event.getEntityLiving();
		ElementalArmor armor = getChestPiece(ent);
		if (armor != null && armor.getType() == Type.MASTER && armor.getSetPieces(ent) == 4) {
			// Jump-boost gives an extra .1 per level. We want 2-block height so we do .2
			Vec3 motion = ent.getDeltaMovement();
			ent.setDeltaMovement(motion.x, motion.y + armor.jumpBoost, motion.z);
		}
	}

	@SubscribeEvent
	public static void onJump(LivingFallEvent event) {
		// Full true/corrupt sets give jump boost
		if (event.isCanceled()) {
			return;
		}

		LivingEntity ent = event.getEntityLiving();

		ElementalArmor armor = getChestPiece(ent);
		if (armor != null && armor.getType() == Type.MASTER && armor.getSetPieces(ent) == 4) {
			// Jump-boost gives an extra .1 per level. We want 2-block height so we do .2
			final float amt = (float) (armor.jumpBoost / .1f);
			event.setDistance(Math.max(0f, event.getDistance() - amt));
		}
	}

	@Override
	public boolean shouldRenderDragonWings(ItemStack stack, Player player) {
		final boolean flying = player.isFallFlying();
		// Maybe should have an interface?
		if (ElementalArmor.GetSetCount(player, EMagicElement.PHYSICAL, Type.MASTER) == 4
				|| ElementalArmor.GetSetCount(player, EMagicElement.EARTH, Type.MASTER) == 4
				|| ElementalArmor.GetSetCount(player, EMagicElement.FIRE, Type.MASTER) == 4
				|| ElementalArmor.GetSetCount(player, EMagicElement.ENDER, Type.MASTER) == 4
				|| (ElementalArmor.GetHasWingUpgrade(stack)
						&& (ElementalArmor.GetSetCount(player, EMagicElement.ICE, Type.MASTER) == 4
								|| ElementalArmor.GetSetCount(player, EMagicElement.WIND, Type.MASTER) == 4
								|| ElementalArmor.GetSetCount(player, EMagicElement.LIGHTNING, Type.MASTER) == 4)

				)) {
			if (flying) {
				return true;
			}

			ItemStack cape = AetherCloakLayer.ShouldRender(player);
			return cape.isEmpty() || !((ICapeProvider) cape.getItem()).shouldPreventOtherRenders(player, cape);
		}
		return false;
	}

	@Override
	public int getDragonWingColor(ItemStack stack, Player player) {
		return this.element.getColor();
	}
}
