package com.smanzana.nostrummagica.items;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicPotency;
import com.smanzana.nostrummagica.attributes.AttributeMagicReduction;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.model.ModelEnchantedArmorBase;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.render.LayerAetherCloak;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.entity.EntityAreaEffect;
import com.smanzana.nostrummagica.entity.EntityAreaEffect.IAreaEntityEffect;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.integration.caelus.NostrumElytraWrapper;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.EnchantedArmorStateUpdate;
import com.smanzana.nostrummagica.network.messages.EnchantedArmorStateUpdate.ArmorState;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.MagicDamageSource;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.utils.NonNullEnumMap;
import com.smanzana.nostrummagica.utils.Projectiles;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.block.BambooSaplingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SaplingBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.EquipmentSlotType.Group;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class EnchantedArmor extends ArmorItem implements EnchantedEquipment, IDragonWingRenderItem, IDyeableArmorItem, IElytraRenderer {
	
	public static enum Type {
		NOVICE(0),
		ADEPT(1),
		MASTER(2),
		TRUE(3);
		
		public final int scale;
		
		private Type(int scale) {
			this.scale = scale;
		}
		
		public final @Nullable Type getNext() {
			switch (this) {
			case NOVICE:
				return ADEPT;
			case ADEPT:
				return MASTER;
			case MASTER:
				return TRUE;
			case TRUE:
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
			case TRUE:
				return MASTER;
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
	private static final UUID[] ARMOR_MODIFIERS = new UUID[] {UUID.fromString("922AB274-1111-56FE-19AE-365AA9758B8B"), UUID.fromString("D1459204-0E61-4716-A129-61666D432E0D"), UUID.fromString("1F7D236D-1118-6524-3375-34814505B28E"), UUID.fromString("2A632266-F4E1-2E67-7836-64FD783B7A50")};
	
	// UUID for speed and jump-boost modifiers
	private static final UUID[] ARMOR_SPEED_MODS = new UUID[] {UUID.fromString("822AB274-1111-56FE-19AE-365AA9758B8B"), UUID.fromString("C1459204-0E61-4716-A129-61666D432E0D"), UUID.fromString("2F7D236D-1118-6524-3375-34814505B28E"), UUID.fromString("3A632266-F4E1-2E67-7836-64FD783B7A50")};
	//private static final UUID[] ARMOR_JUMP_MODS = new UUID[] {UUID.fromString("722AB274-1111-56FE-19AE-365AA9758B8B"), UUID.fromString("B1459204-0E61-4716-A129-61666D432E0D"), UUID.fromString("3F7D236D-1118-6524-3375-34814505B28E"), UUID.fromString("4A632266-F4E1-2E67-7836-64FD783B7A50")};
	
	// UUIDs for set-based modifiers.
	// Each corresponds to a slot that's adding its bonus
	private static final UUID[] SET_MODIFIERS = new UUID[] {UUID.fromString("29F29D77-7DC5-4B68-970F-853633662A72"), UUID.fromString("A84E2267-7D48-4943-9C1C-25A022E85930"), UUID.fromString("D48816AD-B00D-4098-B686-2FC24436CD56"), UUID.fromString("104C2D3C-3987-4D56-9727-FFBEE388F6AF")};
	
	// UUIDs for magic potency modifiers
	private static final UUID[] ARMOR_MAGICPOT_MODS = new UUID[] {UUID.fromString("85c5a784-4ee6-4e2d-ae1b-dd6d006ab724"), UUID.fromString("12fd1eae-bb2f-4e80-89db-38bef660c664"), UUID.fromString("3eea62eb-b9c1-4859-a4d6-35e2edbd4c49"), UUID.fromString("471dd1cf-9ba1-44ce-bba9-3cf9315d784c")};
	
	// UUID and modifiers for turning on elytra flying capability
	private static final UUID ARMOR_ELYTRA_ID = UUID.fromString("146B0D42-6A18-11EE-8C99-0242AC120002");
	private static final AttributeModifier ARMOR_ELYTRA_MODIFIER = NostrumElytraWrapper.MakeHasElytraModifier(ARMOR_ELYTRA_ID, true);
	private static final AttributeModifier ARMOR_NO_ELYTRA_MODIFIER = NostrumElytraWrapper.MakeHasElytraModifier(ARMOR_ELYTRA_ID, false);
	
	private static int calcArmor(EquipmentSlotType slot, EMagicElement element, Type type) {
		
		// Ratio compared to BASE
		// BASE is 14, 18, 22 for the whole set (with rounding errors)
		float mod;
		
		switch (element) {
						// 14, 18, 22  BASE
		case EARTH:
			mod = (22f/24f); // 13, 16.5, 20
			break;
		case ENDER:
			mod = (20f/24f); // 12, 15, 18
			break;
		case FIRE:
			mod = (20f/24f); // 12, 15, 18
			break;
		case PHYSICAL:
			mod = 1f; // 15, 18.75, 22.5
			break;
		case ICE:
			mod = (20f/24f);
			break;
		case LIGHTNING:
			mod = (17.5f/24f);
			break;
		case WIND:
			mod = (17.5f/24f);
			break;
		default:
			mod = 0.5f;
		}
		
		int base;
		
		switch (slot) {
		case CHEST:
			base = 8 + (ModConfig.config.usingAdvancedArmors() ? 4 : 0);
			break;
		case FEET:
			base = 2;
			break;
		case HEAD:
			base = 2;
			break;
		case LEGS:
			base = 6;
			break;
		default:
			base = 0;
		}
		
		if (base != 0)
			base += Math.min(2, type.scale) - 1;
		
		if (true) {//ModConfig.config.usingAdvancedArmors()) {
			base += 1;
		}
		
		return Math.max(1, (int) ((float) base * mod));
	}
	
	// Calcs magic resist, but as if it were armor which is base 20/25
	private static float calcMagicResistBase(EquipmentSlotType slot, EMagicElement element, Type type) {
		
		float mod;
		// 14, 18, 22  BASE
		switch (element) {
		case EARTH:
			mod = (9f/24f);
			break;
		case ENDER:
			mod = (11f/24f);
			break;
		case FIRE:
			mod = .5f;
			break;
		case PHYSICAL:
			if (type == Type.TRUE) {
				mod = /* (12.6363f / 24f) */ (12.5f/22f); // Want to actually hit 50%
			} else {
				mod = (5/24f);
			}
			break;
		case ICE:
			mod = (15f/22f); // 60%
			break;
		case LIGHTNING:
			mod = (12f/22f); // 48%
			break;
		case WIND:
			mod = (11f/24f); // 40%
			break;
		default:
			mod = 0.25f;
		}
		
		int base;
		
		switch (slot) {
		case CHEST:
			base = 8;
			break;
		case FEET:
			base = 2;
			break;
		case HEAD:
			base = 2;
			break;
		case LEGS:
			base = 6;
			break;
		default:
			base = 0;
		}
		
		if (base != 0)
			base += Math.min(2, type.scale) - 1;
		
		return Math.max(1f, ((float) base * mod));
	}
	
	private static final double calcMagicReduct(EquipmentSlotType slot, EMagicElement element, Type type) {
		// each piece will give (.1, .15, .25) of their type depending on level.
		return (type == Type.NOVICE ? .1 : (type == Type.ADEPT ? .15 : .25));
	}
	
	private static final double calcMagicSetReductTotal(EMagicElement armorElement, int setCount, EMagicElement targetElement) {
		if (setCount < 1 || setCount > 4) {
			return 0;
		}
		
		// Fire will resist 2 fire damage (total 3 with max level set). [0, .5, 1, 2]
		// Earth will resist 1 earth damage (total 2 with max level set) AND .5 in all other elements. [0, .25, .5, 1][0, 0, 0, .5]
		// Ender will resist 5 ender damage (total 6 with max level set) but -.5 in all others. [0, 1, 3, 5][0, -.1, -.3, -.5]
		// Wind will resist 1 wind (total 2 with full set)
		// Lightning will resist 3 lightning (total 4)
		// Ice will resist .5 in all
		final double[] setTotalFire = {0, .5, 1, 2};
		final double[] setTotalEarth = {0, .125, .25, 1};
		final double[] setTotalEarthBonus = {0, 0, 0, .5};
		final double[] setTotalEnder = {0, 1, 3, 5};
		final double[] setTotalEnderBonus = {0, -.1, -.3, -.5};
		
		final double[] setTotalWind = {0, 0, 0, 1};
		final double[] setTotalLightning = {0, 0, 0, 3};
		final double[] setTotalIce = {0, 0, 0, 1.5};
		
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
			} else {
				reduc = setTotalEarthBonus[setCount - 1];
			}
		} else if (armorElement == EMagicElement.ENDER) {
			if (targetElement == EMagicElement.ENDER) {
				reduc = setTotalEnder[setCount - 1];
			} else {
				reduc = setTotalEnderBonus[setCount - 1];
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
			reduc = setTotalIce[setCount - 1];
		} else {
			reduc = 0;
		}
		
		return reduc;
	}
	
	private static final double calcMagicSetReduct(EquipmentSlotType slot, EMagicElement armorElement, int setCount, EMagicElement targetElement) {
		return calcMagicSetReductTotal(armorElement, setCount, targetElement) / setCount; // split evenly amonst all [setCount] pieces.
		// COULD make different pieces make up bigger chunks of the pie but ehh
	}
	
	private static int calcArmorDurability(EquipmentSlotType slot, EMagicElement element, Type type) {
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
		
		int iron = ArmorMaterial.DIAMOND.getDurability(slot);
		double amt = iron * Math.pow(1.5, type.scale-1);
		
		return (int) Math.floor(amt * mod);
	}
	
	private static double calcArmorSpeedBoost(EquipmentSlotType slot, EMagicElement element, Type type) {
		if (type != Type.TRUE) {
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
	
	private static double calcArmorJumpBoost(EquipmentSlotType slot, EMagicElement element, Type type) {
		if (type != Type.TRUE) {
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
	
	private static double calcArmorMagicBoostTotal(EMagicElement element, int setCount) {
		if (setCount < 1 || setCount > 4) {
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
		
		return total;
	}
	
	private static double calcArmorMagicBoost(EquipmentSlotType slot, EMagicElement element, int setCount) {
		return calcArmorMagicBoostTotal(element, setCount) / setCount;
	}
	
	private final Type type;
	private int armor; // Can't use vanilla; it's final
	private double magicResistAmount;
	private double magicReducAmount;
	private EMagicElement element;
	private double jumpBoost;
	private double speedBoost;

	// TODO: move?
	@OnlyIn(Dist.CLIENT)
	private static List<ModelEnchantedArmorBase<LivingEntity>> armorModels = null;
	
	public EnchantedArmor(EMagicElement element, EquipmentSlotType slot, Type type, Item.Properties builder) {
		super(ArmorMaterial.IRON, slot, builder.maxDamage(calcArmorDurability(slot, element, type)));
		
		this.type = type;
		this.element = element;
		this.armor = calcArmor(slot, element, type);
		this.magicResistAmount = (Math.round((double) calcMagicResistBase(slot, element, type) * 4.0D)); // Return is out of 25, so x 4 for %
		this.magicReducAmount = calcMagicReduct(slot, element, type);
		this.jumpBoost = calcArmorJumpBoost(slot, element, type);
		this.speedBoost = calcArmorSpeedBoost(slot, element, type);
		
		// TODO move somewhere else?
		if (!NostrumMagica.instance.proxy.isServer()) {
			if (armorModels == null) {
				armorModels = new ArrayList<ModelEnchantedArmorBase<LivingEntity>>(5);
				for (int i = 0; i < 5; i++) {
					armorModels.add(i, new ModelEnchantedArmorBase<LivingEntity>(1f, i));
				}
			}
		}
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
//		super.getSubItems(tab, subItems); // Base armor
//		
//		// Corrupted armors should also present the upgraded flight version
//		final boolean hasFlight = element == EMagicElement.ENDER || element == EMagicElement.EARTH || element == EMagicElement.FIRE || element == EMagicElement.PHYSICAL; 
//		if (!hasFlight && this.getEquipmentSlot() == EquipmentSlotType.CHEST) {
//			ItemStack modStack = new ItemStack(this);
//			SetHasWingUpgrade(modStack, true);
//			subItems.add(modStack);
//		}
//	}
	
	@Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();	

        if (equipmentSlot == this.slot)
        {
            multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor modifier", (double)this.armor, AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor toughness", 4, AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(ARMOR_SPEED_MODS[equipmentSlot.getIndex()], "Armor speed boost", (double)this.speedBoost, AttributeModifier.Operation.MULTIPLY_TOTAL));
            multimap.put(AttributeMagicResist.instance().getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Magic Resist", (double)this.magicResistAmount, AttributeModifier.Operation.ADDITION));
            multimap.put(AttributeMagicReduction.instance(this.element).getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Magic Reduction", (double)this.magicReducAmount, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
    }
	
	@Override
	public int getItemEnchantability() {
		return 16;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }

	@Override
	public SpellAction getTriggerAction(LivingEntity user, boolean offense, @Nonnull ItemStack stack) {
		if (offense)
			return null;
		
		if (!GetArmorHitEffectsEnabled(user)) {
			return null;
		}
		
		SpellAction action = null;
		switch (element) {
		case EARTH:
//			if (NostrumMagica.rand.nextFloat() <= 0.15f * (float) (Math.min(2, level) + 1))
			action = new SpellAction(user).status(NostrumEffects.rooted, 20 * 5 * (Math.min(2, type.scale) + 1), 0);
			break;
		case ENDER:
//			if (NostrumMagica.rand.nextFloat() <= 0.15f * (float) (Math.min(2, level) + 1))
			action = new SpellAction(user).phase(Math.min(2, type.scale));
			break;
		case FIRE:
//			if (NostrumMagica.rand.nextFloat() <= 0.35f * (float) (Math.min(2, level) + 1))
			action = new SpellAction(user).burn(5 * 20);
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
	
	public static EnchantedArmor get(EMagicElement element, EquipmentSlotType slot, Type type) {
		EnchantedArmor armor = null;
		
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
				case TRUE:
					armor = NostrumItems.enchantedArmorEarthHeadTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorEarthChestTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorEarthLegsTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorEarthFeetTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorEnderHeadTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorEnderChestTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorEnderLegsTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorEnderFeetTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorFireHeadTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorFireChestTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorFireLegsTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorFireFeetTrue;
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
					//armor = NostrumItems.enchantedArmorIceHeadNovice;
					break;
				case ADEPT:
					//armor = NostrumItems.enchantedArmorIceHeadAdept;
					break;
				case MASTER:
					//armor = NostrumItems.enchantedArmorIceHeadMaster;
					break;
				case TRUE:
					armor = NostrumItems.enchantedArmorIceHeadTrue;
					break;
				}
				break;
			case CHEST:
				switch (type) {
				case NOVICE:
					//armor = NostrumItems.enchantedArmorIceChestNovice;
					break;
				case ADEPT:
					//armor = NostrumItems.enchantedArmorIceChestAdept;
					break;
				case MASTER:
					//armor = NostrumItems.enchantedArmorIceChestMaster;
					break;
				case TRUE:
					armor = NostrumItems.enchantedArmorIceChestTrue;
					break;
				}
				break;
			case LEGS:
				switch (type) {
				case NOVICE:
					//armor = NostrumItems.enchantedArmorIceLegsNovice;
					break;
				case ADEPT:
					//armor = NostrumItems.enchantedArmorIceLegsAdept;
					break;
				case MASTER:
					//armor = NostrumItems.enchantedArmorIceLegsMaster;
					break;
				case TRUE:
					armor = NostrumItems.enchantedArmorIceLegsTrue;
					break;
				}
				break;
			case FEET:
				switch (type) {
				case NOVICE:
					//armor = NostrumItems.enchantedArmorIceFeetNovice;
					break;
				case ADEPT:
					//armor = NostrumItems.enchantedArmorIceFeetAdept;
					break;
				case MASTER:
					//armor = NostrumItems.enchantedArmorIceFeetMaster;
					break;
				case TRUE:
					armor = NostrumItems.enchantedArmorIceFeetTrue;
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
					//armor = NostrumItems.enchantedArmorLightningHeadNovice;
					break;
				case ADEPT:
					//armor = NostrumItems.enchantedArmorLightningHeadAdept;
					break;
				case MASTER:
					//armor = NostrumItems.enchantedArmorLightningHeadMaster;
					break;
				case TRUE:
					armor = NostrumItems.enchantedArmorLightningHeadTrue;
					break;
				}
				break;
			case CHEST:
				switch (type) {
				case NOVICE:
					//armor = NostrumItems.enchantedArmorLightningChestNovice;
					break;
				case ADEPT:
					//armor = NostrumItems.enchantedArmorLightningChestAdept;
					break;
				case MASTER:
					//armor = NostrumItems.enchantedArmorLightningChestMaster;
					break;
				case TRUE:
					armor = NostrumItems.enchantedArmorLightningChestTrue;
					break;
				}
				break;
			case LEGS:
				switch (type) {
				case NOVICE:
					//armor = NostrumItems.enchantedArmorLightningLegsNovice;
					break;
				case ADEPT:
					//armor = NostrumItems.enchantedArmorLightningLegsAdept;
					break;
				case MASTER:
					//armor = NostrumItems.enchantedArmorLightningLegsMaster;
					break;
				case TRUE:
					armor = NostrumItems.enchantedArmorLightningLegsTrue;
					break;
				}
				break;
			case FEET:
				switch (type) {
				case NOVICE:
					//armor = NostrumItems.enchantedArmorLightningFeetNovice;
					break;
				case ADEPT:
					//armor = NostrumItems.enchantedArmorLightningFeetAdept;
					break;
				case MASTER:
					//armor = NostrumItems.enchantedArmorLightningFeetMaster;
					break;
				case TRUE:
					armor = NostrumItems.enchantedArmorLightningFeetTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorPhysicalHeadTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorPhysicalChestTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorPhysicalLegsTrue;
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
				case TRUE:
					armor = NostrumItems.enchantedArmorPhysicalFeetTrue;
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
					//armor = NostrumItems.enchantedArmorWindHeadNovice;
					break;
				case ADEPT:
					//armor = NostrumItems.enchantedArmorWindHeadAdept;
					break;
				case MASTER:
					//armor = NostrumItems.enchantedArmorWindHeadMaster;
					break;
				case TRUE:
					armor = NostrumItems.enchantedArmorWindHeadTrue;
					break;
				}
				break;
			case CHEST:
				switch (type) {
				case NOVICE:
					//armor = NostrumItems.enchantedArmorWindChestNovice;
					break;
				case ADEPT:
					//armor = NostrumItems.enchantedArmorWindChestAdept;
					break;
				case MASTER:
					//armor = NostrumItems.enchantedArmorWindChestMaster;
					break;
				case TRUE:
					armor = NostrumItems.enchantedArmorWindChestTrue;
					break;
				}
				break;
			case LEGS:
				switch (type) {
				case NOVICE:
					//armor = NostrumItems.enchantedArmorWindLegsNovice;
					break;
				case ADEPT:
					//armor = NostrumItems.enchantedArmorWindLegsAdept;
					break;
				case MASTER:
					//armor = NostrumItems.enchantedArmorWindLegsMaster;
					break;
				case TRUE:
					armor = NostrumItems.enchantedArmorWindLegsTrue;
					break;
				}
				break;
			case FEET:
				switch (type) {
				case NOVICE:
					//armor = NostrumItems.enchantedArmorWindFeetNovice;
					break;
				case ADEPT:
					//armor = NostrumItems.enchantedArmorWindFeetAdept;
					break;
				case MASTER:
					//armor = NostrumItems.enchantedArmorWindFeetMaster;
					break;
				case TRUE:
					armor = NostrumItems.enchantedArmorWindFeetTrue;
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
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
		if (!(stack.getItem() instanceof EnchantedArmor)) {
			return null;
		}
		
		// Could support overlay. For now, have unique brightness-adjusted textures for each.
		if (type != null && type.equalsIgnoreCase("overlay")) {
			//return NostrumMagica.MODID + ":textures/models/armor/none.png";
			return NostrumMagica.MODID + ":textures/models/armor/magic_armor_" + element.name().toLowerCase() + "_overlay.png";
		}
		
		return NostrumMagica.MODID + ":textures/models/armor/magic_armor_" + element.name().toLowerCase() + ".png"; 
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@OnlyIn(Dist.CLIENT)
	public BipedModel getArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlotType slot, BipedModel defaultModel) {
		final int setCount = getSetPieces(entity);
		final int index = (setCount - 1) + (type == Type.TRUE ? 1 : 0); // Boost 1 if ultimate armor
		ModelEnchantedArmorBase<LivingEntity> model = armorModels.get(index % armorModels.size());
		model.setVisibleFrom(slot);
		
		return model;
	}
	
	public static int GetSetCount(LivingEntity entity, EMagicElement element, Type type) {
		int count = 0;
		
		if (entity != null) {
			for (EquipmentSlotType slot : new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET}) {
				ItemStack inSlot = entity.getItemStackFromSlot(slot);
				if (inSlot.isEmpty() || !(inSlot.getItem() instanceof EnchantedArmor)) {
					continue;
				}
				
				EnchantedArmor item = (EnchantedArmor) inSlot.getItem();
				if (item.getElement() == element && item.getType() == type) {
					count++;
				}
			}
		}
		
		return count;
	}
	
	public static int GetSetPieces(LivingEntity entity, EnchantedArmor armor) {
		final EMagicElement myElem = armor.getElement();
		final Type myType = armor.getType();
		return GetSetCount(entity, myElem, myType);
	}
	
	public int getSetPieces(LivingEntity entity) {
		return GetSetPieces(entity, this);
	}
	
	@Override
	public int getColor(ItemStack stack) {
		// This is brightness. Different elements already tint their textures. We just make brighter with level.
		switch (type) {
		default:
		case NOVICE:
			return 0xFF3F3F3F;
		case ADEPT:
			return 0xFF7F7F7F;
		case MASTER:
		case TRUE:
			return 0xFFFFFFFF;
		}
	}
	
	public static List<EnchantedArmor> getAll() {
		List<EnchantedArmor> list = new LinkedList<>();
		
		for (EMagicElement element : EMagicElement.values()) {
			for (EquipmentSlotType slot : EquipmentSlotType.values())
			if (slot.getSlotType() == EquipmentSlotType.Group.ARMOR) {
				for (Type type : Type.values()) {
					list.add(get(element, slot, type));
				}
			}
		}
		
		return list;
	}
	
	// Physical (and earth?) capped to 20 effectively. Consider adding physical bonus like -1 to all damage?
//	@Override
//	public ArmorProperties getProperties(LivingEntity player, ItemStack armor, DamageSource source, double damage,
//			int slot) {
//		if (source.isDamageAbsolute() || source.isUnblockable()) {
//			return new ArmorProperties(1, 0.0, 0);
//		}
//		
//		// This is deducted in addition to amount from attributes -- which cap out at diamond level.
//		// Subtract diamond level when calculating ratio
//		final int extraArmorPts = this.armor - ArmorMaterial.DIAMOND.getDamageReductionAmount(armorType);
//		return new ArmorProperties(1, Math.max(0, (double) extraArmorPts / 25.0), Integer.MAX_VALUE);
//	}

	protected void onArmorDisplayTick(World world, PlayerEntity player, ItemStack itemStack, int setCount) {
		final int displayLevel = (Math.min(2, type.scale) + 1) * (setCount * setCount);
		
//		if (setCount == 4 && element == EMagicElement.ICE && level == 3) {
//			RenderFuncs.renderWeather(player.getPosition(), Minecraft.getInstance().getRenderPartialTicks(), true);
//		}
		
		if (NostrumMagica.rand.nextInt(400) > displayLevel) {
			return;
		}
		
		final double dx;
		final double dy;
		final double dz;
		final IParticleData effect;
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
			effect = new BlockParticleData(ParticleTypes.FALLING_DUST, Blocks.SNOW_BLOCK.getDefaultState());
			dx = dz = 0;
			dy = .025;
			mult = 1;
			rangeMod = 2;
			break;
		case LIGHTNING:
//			effect = ParticleTypes.FALLING_DUST;
//			dx = dz = 0;
//			dy = -.025;
//			mult = 1;
//			rangeMod = 1;
//			data = new int[] {Block.getStateId(Blocks.GOLD_BLOCK.getDefaultState())};
			effect = null;
			dx = dz = dy = mult = 0;
			rangeMod = 0;
			//int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, 
			//Vec3d velocity, boolean unused
			NostrumParticles.LIGHTNING_STATIC.spawn(world, new SpawnParams(
					1, player.posX, player.posY + 1, player.posZ, 1, 20 * 1, 0, new Vec3d(0, 0.01 * (NostrumMagica.rand.nextBoolean() ? 1 : -1), 0), null
					).color(.8f, 1f, 1f, 0f));
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
			final double px = (player.posX + radius * Math.cos(rd * Math.PI * 2));
			final double py = (player.posY + (NostrumMagica.rand.nextFloat() * 2));
			final double pz = (player.posZ + radius * Math.sin(rd * Math.PI * 2));
			world.addParticle(effect, px, py, pz, dx, dy, dz);
		}
	}
	
	protected void onServerTick(World world, PlayerEntity player, ItemStack stack, int setCount) {
		if (setCount == 4 && this.type == Type.TRUE && this.slot == EquipmentSlotType.CHEST) {
			if (element == EMagicElement.ICE) {
				if (player.onGround && !ArmorCheckFlying(player)) {
					final BlockPos pos = player.getPosition();
					if (world.isAirBlock(pos)) {
						BlockState belowState = world.getBlockState(pos.down());
						if (belowState.getMaterial().blocksMovement()) {
							world.setBlockState(pos, Blocks.SNOW.getDefaultState());
						}
					}
				}
			} else if (element == EMagicElement.WIND) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				if (attr != null && attr.getMana() > 0 && player.isSprinting() && !ArmorCheckFlying(player)) {
					if (!player.isPotionActive(Effects.SPEED) || player.ticksExisted % 10 == 0) {
						player.addPotionEffect(new EffectInstance(Effects.SPEED, 20, 0));
					}
					if (!player.isPotionActive(Effects.JUMP_BOOST) || player.ticksExisted % 10 == 0) {
						player.addPotionEffect(new EffectInstance(Effects.JUMP_BOOST, 20, 1));
					}
					
					// Refresh nearby tornados
					if (player.onGround)
					for (EntityAreaEffect cloud : world.getEntitiesWithinAABB(EntityAreaEffect.class, (new AxisAlignedBB(0, 0, 0, 1, 1, 1)).offset(player.posX, player.posY, player.posZ).grow(5), (effect) -> {
						// lol
						return effect != null
								&& (effect.getCustomParticle() == ParticleTypes.SWEEP_ATTACK || effect.getParticleData() == ParticleTypes.SWEEP_ATTACK);
					})) {
						cloud.addTime(1, true);
					}
					
					if (player.ticksExisted % 3 == 0) {
						attr.addMana(-1);
						NostrumMagica.instance.proxy.sendMana(player);
					}
				}
			} else if (element == EMagicElement.EARTH) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				if (attr != null && attr.getMana() >= EARTH_GROW_COST && !ArmorCheckFlying(player)) {
					if (player.ticksExisted % 40 == 0) {
						// Attempt bonemeal
						if (DoEarthGrow(world, player.getPosition()) != null) {
							attr.addMana(-EARTH_GROW_COST);
							NostrumMagica.instance.proxy.sendMana(player);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onArmorTick(ItemStack itemStack, World world, PlayerEntity player) {
		super.onArmorTick(itemStack, world, player);
		
		final int setCount = getSetPieces(player);
		if (!world.isRemote) {
			onServerTick(world, player, itemStack, setCount);
		} else {
			onArmorDisplayTick(world, player, itemStack, setCount);
		}
	}
	
	// hacky little map to avoid thrashing attributes every tick
	protected static final Map<LivingEntity, Map<EquipmentSlotType, ItemStack>> LastEquipState = new HashMap<>();
	
	protected static Map<EquipmentSlotType, ItemStack> GetLastTickState(LivingEntity entity) {
		Map<EquipmentSlotType, ItemStack> map = LastEquipState.get(entity);
		if (map == null) {
			map = new NonNullEnumMap<>(EquipmentSlotType.class, ItemStack.EMPTY);
			LastEquipState.put(entity, map);
		}
		return map;
	}
	
	protected static boolean EntityChangedEquipment(LivingEntity entity) {
		Map<EquipmentSlotType, ItemStack> map = GetLastTickState(entity);
		for (EquipmentSlotType slot : EquipmentSlotType.values()) {
			if (slot.getSlotType() != Group.ARMOR) {
				continue;
			}
			
			@Nonnull final ItemStack inSlot = entity.getItemStackFromSlot(slot);
			@Nonnull final ItemStack lastTick = map.get(slot);
			
			// Same check Vanilla uses to apply attributes
			if (!ItemStack.areItemStacksEqual(inSlot, lastTick)) {
				return true;
			}
		}
		return false;
	}
	
	public static final boolean EntityHasEnchantedArmor(LivingEntity entity) {
		for (EquipmentSlotType slot : EquipmentSlotType.values()) {
			if (slot.getSlotType() != Group.ARMOR) {
				continue;
			}
			
			ItemStack inSlot = entity.getItemStackFromSlot(slot);
			if (!inSlot.isEmpty()  && inSlot.getItem() instanceof EnchantedArmor) {
				return true;
			}
		}
		
		return false;
	}
	
	protected static void UpdateEntity(LivingEntity entity) {
		
		if (!entity.isAlive()) {
			LastEquipState.remove(entity);
			return;
		}
		
		// Only do any of this if they have a piece of enchanted armor (or they used to)
		if (!LastEquipState.containsKey(entity) && !EntityHasEnchantedArmor(entity)) {
			return;
		}
		
		// Check and change attributes
		if (EntityChangedEquipment(entity)) {
			// Figure out attributes and set.
			// Also capture current armor status and cache it.
			Map<EquipmentSlotType, ItemStack> cacheMap = new EnumMap<>(EquipmentSlotType.class);
			Multimap<String, AttributeModifier> attribMap = HashMultimap.<String, AttributeModifier>create();	

			for (EquipmentSlotType slot : EquipmentSlotType.values()) {
				if (slot.getSlotType() != Group.ARMOR) {
					continue;
				}
				
				ItemStack inSlot = entity.getItemStackFromSlot(slot);
				final int setCount;
				final @Nullable EMagicElement armorElem;
				if (!inSlot.isEmpty()) {
					inSlot = inSlot.copy();
					
					if (inSlot.getItem() instanceof EnchantedArmor) {
						EnchantedArmor armorType = (EnchantedArmor) inSlot.getItem();
						setCount = GetSetPieces(entity, armorType);
						armorElem = armorType.getElement();
					} else {
						setCount = 0;
						armorElem = null;
					}
				} else {
					setCount = 0;
					armorElem = null;
				}
				
				// Figure out how much this SHOULD be giving
				// Important to do this even with 0 to remove previous bonuses
				for (EMagicElement elem : EMagicElement.values()) {
					final double reduct = (setCount > 0 && armorElem != null)
							? calcMagicSetReduct(slot, armorElem, setCount, elem)
							: 0;
					attribMap.put(AttributeMagicReduction.instance(elem).getName(), new AttributeModifier(SET_MODIFIERS[slot.getIndex()], "Magic Reduction (Set)", reduct, AttributeModifier.Operation.ADDITION));
				}
				final double boost = (setCount > 0 && armorElem != null)
						? calcArmorMagicBoost(slot, armorElem, setCount)
						: 0;
				attribMap.put(AttributeMagicPotency.instance().getName(), new AttributeModifier(ARMOR_MAGICPOT_MODS[slot.getIndex()], "Magic Potency (Set)", boost, AttributeModifier.Operation.ADDITION));
				
				if (slot == EquipmentSlotType.CHEST) {
					boolean has = (inSlot.getItem() instanceof EnchantedArmor) ? ((EnchantedArmor) inSlot.getItem()).hasElytra(entity) : false;
					NostrumElytraWrapper.AddElytraModifier(attribMap,
							 has ? ARMOR_ELYTRA_MODIFIER : ARMOR_NO_ELYTRA_MODIFIER);
				}
				
				
				// Add captured value to map
				cacheMap.put(slot, inSlot);
			}
			
			// Update attributes
			entity.getAttributes().applyAttributeModifiers(attribMap);
			
			// Create and save new map
			LastEquipState.put(entity, cacheMap);
		}
		
		// Check for world-changing full set bonuses
		// Note: Cheat and just look at helm. if helm isn't right, full set isn't set anyways
		@Nonnull ItemStack helm = entity.getItemStackFromSlot(EquipmentSlotType.HEAD);
		if (!helm.isEmpty() && helm.getItem() instanceof EnchantedArmor) {
			EnchantedArmor type = (EnchantedArmor) helm.getItem();
			final int setCount = GetSetPieces(entity, type);
			if (setCount == 4) {
				// Full set!
				final EMagicElement element = type.getElement();
				
				if (element == EMagicElement.FIRE) {
					// Fire prevents fire.
					// Level 1(0) reduces fire time (25% reduction by 50% of the time reducing by another tick)
					// Level 2(1) halves fire time
					// Level 3 and 4(2/3) prevents fire all-together
					if (type.getType() == Type.MASTER || type.getType() == Type.TRUE) {
						if (entity.isBurning()) {
							entity.extinguish();
						}
					} else {
						if (type.getType() == Type.ADEPT || NostrumMagica.rand.nextBoolean()) {
							try {
								Field fireField = ObfuscationReflectionHelper.findField(Entity.class, "field_190534_ay");
								fireField.setAccessible(true);
								
								int val = fireField.getInt(entity);
								
								if (val > 0) {
									// On fire so decrease
									
									// Decrease every other 20 so damage ticks aren't doubled.
									// Do this by checking if divisible by 40 (true every 2 %20).
									// (We skip odds to get to evens to simplify logic)
									if (val % 2 == 0) {
										if (val % 20 != 0 || val % 40 == 0) {
											fireField.setInt(entity, val - 1);
										}
									} else {
										; // Skip so that next tick is even
									}
								}
								
								fireField.setAccessible(false);
							} catch (Exception e) {
								; // This will happen every tick, so don't log
							}
						}
					}
				}
			}
		}
	}
	
	// Updates all entities' current set bonuses (or lack there-of) from enchanted armor
	public static void ServerWorldTick(ServerWorld world) {
		world.getEntities().forEach((ent)-> {
			if (ent instanceof LivingEntity) {
				UpdateEntity((LivingEntity) ent);
			}
		});
	}
	
	public static Map<IAttribute, Double> FindCurrentSetBonus(@Nullable Map<IAttribute, Double> map, LivingEntity entity, EMagicElement element, Type type) {
		if (map == null) {
			map = new HashMap<>();
		}
		
		final int setCount = GetSetCount(entity, element, type);
		
		for (EquipmentSlotType slot : EquipmentSlotType.values()) {
			if (slot.getSlotType() != Group.ARMOR) {
				continue;
			}
			
			@Nonnull ItemStack inSlot = entity.getItemStackFromSlot(slot);
			if (inSlot.isEmpty() || !(inSlot.getItem() instanceof EnchantedArmor)) {
				continue;
			}
			
			EnchantedArmor armorType = (EnchantedArmor) inSlot.getItem();
			final Type inSlotType = armorType.getType();
			final EMagicElement inSlotElement = armorType.getElement();
			if (inSlotType != type || inSlotElement != element) {
				continue;
			}
			
			for (EMagicElement elem : EMagicElement.values()) {
				final double reduct = calcMagicSetReduct(slot, element, setCount, elem);
				if (reduct != 0) {
					final AttributeMagicReduction inst = AttributeMagicReduction.instance(elem);
					Double cur = map.get(inst);
					if (cur == null) {
						cur = 0.0;
					}
					
					cur = cur + reduct;
						
					map.put(inst, cur);
				}
			}

			final double boost = calcArmorMagicBoost(slot, element, setCount);
			if (boost != 0) {
				final AttributeMagicPotency inst = AttributeMagicPotency.instance();
				Double cur = map.get(inst);
				if (cur == null) {
					cur = 0.0;
				}
				
				cur = cur + boost;
					
				map.put(inst, cur);
			}
		}
		
		return map;
	}
	
	private Map<IAttribute, Double> setMapInst = new HashMap<>();
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		final boolean showFull = Screen.hasShiftDown();
		final @Nullable PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		final int setCount = this.getSetPieces(player);
		
		final String setName = I18n.format("item.armor.set." + element.name().toLowerCase() + "." + type.name().toLowerCase() + ".name", new Object[0]);
		if (showFull) {
			tooltip.add(new TranslationTextComponent("info.armor.set_total", setName, TextFormatting.DARK_PURPLE, TextFormatting.RESET));
		} else {
			
			final String countFormat = "" + (setCount == 4 ? TextFormatting.GOLD : TextFormatting.YELLOW);
			tooltip.add(new TranslationTextComponent("info.armor.set_status", setName, setCount, countFormat, "" + TextFormatting.RESET));
		}
		
		if (player != null) {
		
			synchronized(setMapInst) {
				setMapInst.clear();
				
				if (showFull) {
					// Show total
					for (EMagicElement targElem : EMagicElement.values()) {
						final double reduc = calcMagicSetReductTotal(element, 4, targElem);
						setMapInst.put(AttributeMagicReduction.instance(targElem), reduc);
					}
					final double boost = calcArmorMagicBoostTotal(element, 4);
					setMapInst.put(AttributeMagicPotency.instance(), boost);
				} else {
					// Show current
					FindCurrentSetBonus(setMapInst, player, element, type); // puts into setMapInst
				}
				
				if (!setMapInst.isEmpty()) {
					for (Entry<IAttribute, Double> entry : setMapInst.entrySet()) {
						Double val = entry.getValue();
						if (val == null || val == 0) {
							continue;
						}
						
						// Formatting here copied from Vanilla
						if (val > 0) {
							tooltip.add((new TranslationTextComponent("attribute.modifier.plus.0", ItemStack.DECIMALFORMAT.format(val), new TranslationTextComponent("attribute.name." + (String)entry.getKey().getName()))).applyTextStyle(TextFormatting.BLUE));
						} else {
							val = -val;
							tooltip.add((new TranslationTextComponent("attribute.modifier.take.0", ItemStack.DECIMALFORMAT.format(val), new TranslationTextComponent("attribute.name." + (String)entry.getKey().getName()))).applyTextStyle(TextFormatting.RED));
						}
					}
				}
			}
			
			// Also show special bonuses
			// TODO make this a bit more... extensible?
			if (showFull || setCount == 4) {
				final String key = "info.armor.set_bonus." + element.name().toLowerCase() + "." + type.name().toLowerCase();
				if (I18n.hasKey(key)) {
					final String full = I18n.format(key, new Object[0]);
					for (String line : full.split("\\|"))
					tooltip.add(new StringTextComponent(line).applyTextStyle(TextFormatting.DARK_PURPLE));
				}
			}
		}
		
		if (EnchantedArmor.GetHasWingUpgrade(stack)) {
			tooltip.add(new TranslationTextComponent("info.armor.wing_upgrade").applyTextStyle(TextFormatting.GOLD));
		}
	}

//	@Override
//	public boolean isElytraFlying(LivingEntity entity, ItemStack stack) {
//		return ArmorCheckFlying(entity);
//	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderElyta(LivingEntity entity, ItemStack stack) {
		return hasElytra(entity)
				&& (element == EMagicElement.ICE || element == EMagicElement.LIGHTNING || element == EMagicElement.WIND)
				&& (!(entity instanceof PlayerEntity) || !(shouldRenderDragonWings(stack, (PlayerEntity) entity)));
	}
	
	protected boolean hasElytra(LivingEntity entity) {
		if (this.type == Type.TRUE && this.slot == EquipmentSlotType.CHEST) {
			// Check if full set is available
			return (4 == getSetPieces(entity)); 
		}
		return false;
	}
	
	private static final int EARTH_SCAN_RANGE_XZ = 21;
	private static final int EARTH_SCAN_RANGE_Y = 3;
	private static final List<BlockPos> EARTH_SCAN_POS = new ArrayList<>(EARTH_SCAN_RANGE_XZ * EARTH_SCAN_RANGE_XZ * EARTH_SCAN_RANGE_Y);
	
	{
		final int xzRadius = (EARTH_SCAN_RANGE_XZ/2); // int div
		final int yRadius = (EARTH_SCAN_RANGE_Y/2); // int div
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
		if (this.type == Type.TRUE && this.slot == EquipmentSlotType.CHEST) {
			// Check if full set is available and if we have enough mana
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
			if (attr == null || attr.getMana() < MANA_JUMP_COST) {
				return false;
			}
			return (4 == getSetPieces(entity)); 
		}
		return false;
	}
	
	protected boolean hasWindTornado(LivingEntity entity) {
		if (this.type == Type.TRUE && this.slot == EquipmentSlotType.CHEST && this.element == EMagicElement.WIND) {
			// Check if full set is available and if we have enough mana
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
			if (attr == null || attr.getMana() < WIND_TORNADO_COST) {
				return false;
			}
			return (4 == getSetPieces(entity)); 
		}
		return false;
	}
	
	protected boolean hasEnderDash(LivingEntity entity) {
		if (this.type == Type.TRUE && this.slot == EquipmentSlotType.CHEST && this.element == EMagicElement.ENDER) {
			// Check if full set is available and if we have enough mana
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
			if (attr == null || attr.getMana() < ENDER_DASH_COST) {
				return false;
			}
			return (4 == getSetPieces(entity)); 
		}
		return false;
	}
	
	protected boolean hasDragonFlight(LivingEntity entity) {
		if (this.type == Type.TRUE && this.slot == EquipmentSlotType.CHEST) {
			boolean hasRightElement = element == EMagicElement.ENDER || element == EMagicElement.EARTH || element == EMagicElement.FIRE || element == EMagicElement.PHYSICAL;
			if (!hasRightElement) {
				ItemStack chest = entity.getItemStackFromSlot(EquipmentSlotType.CHEST);
				hasRightElement = EnchantedArmor.GetHasWingUpgrade(chest);
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
	
	protected void consumeManaJump(LivingEntity entity) {
		if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) {
			return;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null) {
			attr.addMana(-MANA_JUMP_COST);
		}
	}
	
	protected void consumeWindTornado(LivingEntity entity) {
		if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) {
			return;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null) {
			attr.addMana(-WIND_TORNADO_COST);
		}
	}
	
	protected void consumeWindJumpWhirlwind(LivingEntity entity) {
		if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) {
			return;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null) {
			attr.addMana(-WIND_WHIRLWIND_COST);
		}
	}
	
	protected void consumeEnderDash(LivingEntity entity) {
		if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) {
			return;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null) {
			attr.addMana(-ENDER_DASH_COST);
		}
	}
	
	protected void consumeDragonFlight(LivingEntity entity) {
		if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) {
			return;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null) {
			attr.addMana(-MANA_DRAGON_FLIGHT);
		}
	}
	
	private boolean jumpPressedEarly = false; // For telling whether jump remains pressed or released and pressed again
	private boolean lastTickGround = false; // For checking if the player just jumped this tick
	private boolean backPressedEarly = false;
	private long lastBackMSecs = -1;
	private boolean leftPressedEarly = false;
	private long lastLeftMSecs = -1;
	private boolean rightPressedEarly = false;
	private long lastRightMSecs = -1;
	
	private static KeyBinding bindingEnderLeft;
	private static KeyBinding bindingEnderRight;
	private static KeyBinding bindingEnderBack;
	// private static KeyBinding bindingSummonTornado;
	private static KeyBinding bindingSummonJumpWhirlwind;
	private static KeyBinding bindingToggleArmorEffect;
	
	public static final void ClientInit() {
		bindingEnderLeft = new KeyBinding("key.dash.left.desc", GLFW.GLFW_KEY_KP_7, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingEnderLeft);

		bindingEnderRight = new KeyBinding("key.dash.right.desc", GLFW.GLFW_KEY_KP_9, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingEnderRight);

		bindingEnderBack = new KeyBinding("key.dash.back.desc", GLFW.GLFW_KEY_KP_2, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingEnderBack);

		bindingSummonJumpWhirlwind = new KeyBinding("key.wind.jump.desc", GLFW.GLFW_KEY_M, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingSummonJumpWhirlwind);

		bindingToggleArmorEffect = new KeyBinding("key.armor.toggle.desc", GLFW.GLFW_KEY_KP_5, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingToggleArmorEffect);
		
	}
	
	protected void clientDashSide(LivingEntity ent, boolean right) {
		this.consumeEnderDash(ent);
		NetworkHandler.sendToServer(new EnchantedArmorStateUpdate(ArmorState.ENDER_DASH_SIDE, right, 0));
	}
	
	protected void clientDashBack(LivingEntity ent) {
		this.consumeEnderDash(ent);
		NetworkHandler.sendToServer(new EnchantedArmorStateUpdate(ArmorState.ENDER_DASH_BACK, false, 0));
	}
	
	@SubscribeEvent
	public void onKey(KeyInputEvent event) {
		PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		if (bindingEnderLeft.isPressed()) {
			clientDashSide(player, false);
		} else if (bindingEnderRight.isPressed()) {
			clientDashSide(player, true);
		} else if (bindingEnderBack.isPressed()) {
			clientDashBack(player);
		} else if (bindingSummonJumpWhirlwind.isPressed()) {
			NetworkHandler.sendToServer(new EnchantedArmorStateUpdate(ArmorState.WIND_JUMP_WHIRLWIND, false, 0));
		} else if (bindingToggleArmorEffect.isPressed()) {
			NostrumMagicaSounds.UI_TICK.playClient(player);
			final boolean enabled = !GetArmorHitEffectsEnabled(player);
			SetArmorHitEffectsEnabled(player, enabled);
			NetworkHandler.sendToServer(new EnchantedArmorStateUpdate(ArmorState.EFFECT_TOGGLE, enabled, 0));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		Minecraft mc = Minecraft.getInstance();
		final ClientPlayerEntity player = mc.player;
		if (player == null) {
			return;
		}

		if (event.phase != TickEvent.Phase.END) {
			jumpPressedEarly = player.movementInput == null ? false : player.movementInput.jump;
			backPressedEarly = player.movementInput == null ? false : player.movementInput.backKeyDown;
			leftPressedEarly = player.movementInput == null ? false : player.movementInput.leftKeyDown;
			rightPressedEarly = player.movementInput == null ? false : player.movementInput.rightKeyDown;
			return;
		}
		
		if (player.movementInput == null) {
			return;
		}
		
		final boolean backPress = (player.movementInput.backKeyDown && !backPressedEarly);
		final boolean leftPress = (player.movementInput.leftKeyDown && !leftPressedEarly);
		final boolean rightPress = (player.movementInput.rightKeyDown && !rightPressedEarly);
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
		if (flying && (player.onGround || player.isSneaking() || player.isPassenger() || player.isInWater() || player.isInLava())) {
			SetArmorFlying(player, false);
			SendUpdates(player, null);
			return;
		}
		
		boolean hasJump = player.movementInput.jump && !jumpPressedEarly;
		
		// Start flying
		if (!flying && hasJump && !player.onGround && player.getMotion().y < 0 && !player.abilities.isFlying) {
			// Does this armor support flying?
			if (this.hasElytra(player)) {
				SetArmorFlying(player, true);
				SendUpdates(player, null);
				hasJump = false; // Consumed
			}
		}
		
		// Mana jump
		final double MANA_JUMP_AMT = flying ? .6 : .4;
		if (hasJump && flying && !player.onGround && !lastTickGround && !player.abilities.isFlying && player.getMotion().y < MANA_JUMP_AMT) {
			// Does this armor have mana jump?
			if (this.hasManaJump(player)) {
				this.consumeManaJump(player);
				player.setMotion(player.getMotion().add(0, MANA_JUMP_AMT, 0));
				hasJump = false; // Consumed
				NetworkHandler.sendToServer(new EnchantedArmorStateUpdate(ArmorState.JUMP, true, 0));
			}
		}
		
		// Dragon flying
		if (flying && !player.onGround && !player.abilities.isFlying && player.movementInput.forwardKeyDown) {
			// Does this armor have dragon flying?
			if (this.hasDragonFlight(player)) {
				// Check if magnitude of flying is low and if so, boost it with magic
				final double curMagnitudeSq = (player.getMotion().x * player.getMotion().x + player.getMotion().y * player.getMotion().y + player.getMotion().z * player.getMotion().z);
				if (curMagnitudeSq < .4) {
					// How much should we scale?
					final double curMagnitude = Math.sqrt(curMagnitudeSq);
					final double target = 0.63;
					final double scale = target / curMagnitude;
					final double origX = player.getMotion().x;
					final double origY = player.getMotion().y;
					final double origZ = player.getMotion().z;
					player.setMotion(player.getMotion().scale(scale));
					
					final double dx = Math.abs(player.getMotion().x - origX);
					final double dy = Math.abs(player.getMotion().y - origY);
					final double dz = Math.abs(player.getMotion().z - origZ);
					
					// We take mana depending on how 'up' we're being propeled
					final float vertScale = (dx == 0 && dy == 0 && dz == 0 ? 0f : (float) (dy / (dx + dy + dz)));
					final boolean deduct = vertScale == 0f ? false : (random.nextFloat() < vertScale * 3);
					if (deduct) {
						this.consumeDragonFlight(player);
						NetworkHandler.sendToServer(new EnchantedArmorStateUpdate(ArmorState.DRAGON_FLIGHT_TICK, deduct, 0));
					}
				}
			}
		}
		
		// Double-press abilities
		if (!flying && !player.abilities.isFlying) {
			// just for testing
			if (doubleBack) {
				if (this.hasWindTornado(player)) {
					this.consumeWindTornado(player);
					NetworkHandler.sendToServer(new EnchantedArmorStateUpdate(ArmorState.WIND_TORNADO, true, 0));
					return;
				}
				
				if (ModConfig.config.doubleEnderDash() && this.hasEnderDash(player)) {
					clientDashBack(player);
					return;
				}
			}
			
			if (doubleLeft) {
				if (ModConfig.config.doubleEnderDash() && this.hasEnderDash(player)) {
					clientDashSide(player, false);
					return;
				}
			}
			
			if (doubleRight) {
				if (ModConfig.config.doubleEnderDash() && this.hasEnderDash(player)) {
					clientDashSide(player, true);
					return;
				}
			}
		}
		
		lastTickGround = player.onGround;
	}
	
	@SubscribeEvent
	public void onTrack(PlayerEvent.StartTracking event) {
		// Server-side. A player has started tr acking an entity. Send the 'start' state of their armor, if any
		// TODO if there was more than flying, we'd want to iterate every possible type and send updates for each
		// (which would clear an entity's old state if something was active when they left and they just came back)
		
		if (event.getTarget() instanceof LivingEntity) {
			SendUpdates((LivingEntity) event.getTarget(), event.getPlayer());
		}
	}
	
	protected static final void SendUpdates(LivingEntity entity, @Nullable PlayerEntity toPlayer) {
		// Note: We only do players for now
		if (entity instanceof PlayerEntity) {
			final PlayerEntity player = (PlayerEntity) entity;
			final EnchantedArmorStateUpdate message = new EnchantedArmorStateUpdate(ArmorState.FLYING, ArmorCheckFlying(player), player.getEntityId());
			if (player.world.isRemote) {
				assert(player == NostrumMagica.instance.proxy.getPlayer());
				NetworkHandler.sendToServer(message);
			} else if (toPlayer != null) {
				NetworkHandler.sendTo(message, (ServerPlayerEntity) toPlayer);
			} else {
				NetworkHandler.sendToDimension(message, player.dimension);
			}
		}
	}
	
	public static void worldUnload() {
		synchronized(ArmorFlyingMap) {
			ArmorFlyingMap.clear();
		}
	}
	
	private static enum FlyingTrackedData {
		LAST_WING_FLAP,
	}
	private static final float WING_FLAP_DURATION = 20f; 
	
	// Note: We only care about players. This could be expanded but would also need better cleanup 
	// to avoid infinite RAM spam
	private static final Map<UUID, Map<FlyingTrackedData, Integer>> ArmorFlyingMap = new HashMap<>();
	
	private static final Map<UUID, Boolean> ArmorHitEffectMap = new HashMap<>();
	
	protected static boolean ArmorCheckFlying(LivingEntity ent) {
		final UUID id = ent.getUniqueID();
		boolean ret = false;
		synchronized(ArmorFlyingMap) {
			if (ArmorFlyingMap.containsKey(id)) {
				// We encode 'flying at all?' as 'any entry exists'
				return true;
			}
		}
		return ret;
	}
	
	public static void SetArmorFlying(LivingEntity ent, boolean flying) {
		synchronized(ArmorFlyingMap) {
			if (!flying) {
				ArmorFlyingMap.remove(ent.getUniqueID());
				ent.fallDistance = 0;
			} else {			
				if (!ArmorFlyingMap.containsKey(ent.getUniqueID())) {
					ArmorFlyingMap.put(ent.getUniqueID(), new EnumMap<>(FlyingTrackedData.class));
				}
			}
		}
	}
	
	protected static int ArmorGetLastFlapTick(LivingEntity ent) {
		final UUID id = ent.getUniqueID();
		synchronized(ArmorFlyingMap) {
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
		final UUID id = ent.getUniqueID();
		final int curTicks = ent.ticksExisted;
		boolean changed = false;
		synchronized(ArmorFlyingMap) {
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
		double curTicks = ent.ticksExisted + partialTicks; // Expand to double
		double flapStartTicks = ArmorGetLastFlapTick(ent);
		return Math.max(0.0f, Math.min(1.0f, (float) (curTicks - flapStartTicks) / WING_FLAP_DURATION));
	}
	
	public static final void SetArmorHitEffectsEnabled(LivingEntity ent, boolean enabled) {
		final UUID key = ent.getUniqueID();
		if (enabled) {
			ArmorHitEffectMap.put(key, enabled);
		} else {
			ArmorHitEffectMap.remove(key);
		}
	}
	
	public static final boolean GetArmorHitEffectsEnabled(LivingEntity ent) {
		Boolean val = ArmorHitEffectMap.get(ent.getUniqueID());
		return (val != null && val.booleanValue());
	}
	
	private static final String NBT_WING_UPGRADE = "dragonwing_upgrade";
	
	public static final void SetHasWingUpgrade(ItemStack stack, boolean upgraded) {
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		nbt.putBoolean(NBT_WING_UPGRADE, upgraded);
		
		stack.setTag(nbt);
	}
	
	public static final boolean GetHasWingUpgrade(ItemStack stack) {
		return !stack.isEmpty()
				&& stack.getItem() instanceof EnchantedArmor
				&& ((EnchantedArmor) stack.getItem()).getEquipmentSlot() == EquipmentSlotType.CHEST
				&& stack.hasTag()
				&& stack.getTag().getBoolean(NBT_WING_UPGRADE);
	}
	
	private static final boolean DoEnderDash(LivingEntity entity, Vec3d dir) {
		final float dashDist = 4.0f;
		final Vec3d idealVec = entity.getPositionVector().add(dashDist * dir.x, dashDist * dir.y, dashDist * dir.z);
		
		// Do three traces from y=0, y=1, and y=2. Take best one
		Vec3d bestResult = null;
		double bestDist = -1;
		final Vec3d startPos = entity.getPositionVector();
		for (int y = -1; y <= 4; y++) {
			final Vec3d end = idealVec.add(0, y, 0);
			RayTraceResult mop = RayTrace.raytrace(entity.world, entity, startPos.add(0, y, 0), end, (ent) -> {
				return false;
			});
			
			final Vec3d spot;
			if (mop != null && mop.getType() == RayTraceResult.Type.BLOCK) {
				spot = mop.getHitVec();
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
		
		if (bestResult != null && entity.attemptTeleport(bestResult.x, bestResult.y, bestResult.z, false)) {
			entity.world.playSound(null, startPos.x, startPos.y, startPos.z,
					SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS,
					1f, 1f);
			return true;
		}
		return false;
	}
	
	protected static final boolean CanGrow(BlockState state, IGrowable growable) {
		// Don't grow mushrooms and stuff... althoug why not?
		return growable instanceof CropsBlock
				|| growable instanceof SaplingBlock
				|| growable instanceof BambooSaplingBlock;
	}
	
	public static synchronized final @Nullable BlockPos DoEarthGrow(World world, BlockPos center) {
		Collections.shuffle(EARTH_SCAN_POS);
		
		MutableBlockPos cursor = new MutableBlockPos();
		for (BlockPos offset : EARTH_SCAN_POS) {
			cursor.setPos(center.getX() + offset.getX(), center.getY() + offset.getY(), center.getZ() + offset.getZ());
			BlockState state = world.getBlockState(cursor);
			if (state != null && state.getBlock() instanceof IGrowable) {
				IGrowable growable = (IGrowable) state.getBlock();
				if (!CanGrow(state, growable)) {
					continue;
				}
				if (growable.canGrow(world, cursor, state, false) && growable.canUseBonemeal(world, random, cursor, state)) {
					// Only grow 1/4th the time
					if (random.nextBoolean() && random.nextBoolean()) {
						growable.grow(world, random, cursor, state);
					}
					
					((ServerWorld) world).spawnParticle(ParticleTypes.HAPPY_VILLAGER,
							cursor.getX() + .5 + (-.5 + random.nextDouble()),
							cursor.getY() + .5,
							cursor.getZ() + .5 + (-.5 + random.nextDouble()),
							2,
							.2, .2, .2, 0);
					return cursor.toImmutable();
				}
			}
		}
		
		return null;
	}
	
	public static final boolean DoEarthDig(World world, PlayerEntity player, BlockPos pos, Direction face) {
		if (!player.getHeldItemMainhand().isEmpty()) {
			return false;
		}
		
		if (player.getCooledAttackStrength(0.5F) < .95) {
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
		final BlockPos[] positions = (
					face == Direction.UP || face == Direction.DOWN ? new BlockPos[] {
							pos.north().east(), pos.north(), pos.north().west(),
							pos.east(), pos, pos.west(),
							pos.south().east(), pos.south(), pos.south().west()
					}
					: face == Direction.NORTH || face == Direction.SOUTH ? new BlockPos[] {
							pos.up().east(), pos.up(), pos.up().west(),
							pos.east(), pos, pos.west(),
							pos.down().east(), pos.down(), pos.down().west()
					}
					: /* face == Direction.EAST || face == Direction.WEST*/ new BlockPos[] {
							pos.up().north(), pos.up(), pos.up().south(),
							pos.north(), pos, pos.south(),
							pos.down().north(), pos.down(), pos.down().south()
					}
				);
		
		for (BlockPos at : positions) {
			state = world.getBlockState(at);
			if (state == null) {
				continue;
			}
			
			block = state.getBlock();
			if (block != Blocks.STONE && block != Blocks.SANDSTONE && block != Blocks.COBBLESTONE) {
				continue;
			}
			
			// TODO does this work even though block is destroyed rig ht after entities are created inside it?
			Block.spawnDrops(state, world, at);
			world.destroyBlock(at, false);
		}
		
		return true;
	}
	
	public static final void HandleStateUpdate(ArmorState state, LivingEntity ent, boolean data) {
		ItemStack chest = ent.getItemStackFromSlot(EquipmentSlotType.CHEST);
		if (chest.isEmpty() || !(chest.getItem() instanceof EnchantedArmor)) {
			return;
		}
		
		final EnchantedArmor armor = (EnchantedArmor) chest.getItem();
		
		switch (state) {
		case FLYING:
			SetArmorFlying(ent, data);
			// Packet handler takes care of forwarding to other entities if this was just received on the server
			break;
		case JUMP:
			// Deduct mana
			if (!ent.world.isRemote) {
				armor.consumeManaJump(ent);
			}
			break;
		case ENDER_DASH_BACK:
			if (!ent.world.isRemote && armor.hasEnderDash(ent)) {
				final Vec3d realLook = ent.getLookVec();
				final Vec3d fakeLook = new Vec3d(realLook.x, 0, realLook.z);
				Vec3d dir = fakeLook.scale(-1);
				if (DoEnderDash(ent, dir)) {
					armor.consumeEnderDash(ent);
				} else {
					if (ent instanceof PlayerEntity) {
						NostrumMagica.instance.proxy.sendMana((PlayerEntity) ent);
					}
				}
			}
			break;
		case ENDER_DASH_SIDE:
			if (!ent.world.isRemote && armor.hasEnderDash(ent)) {
				final Vec3d realLook = ent.getLookVec();
				final Vec3d fakeLook = new Vec3d(realLook.x, 0, realLook.z);
				final Vec3d dir = fakeLook.rotateYaw((float) ((Math.PI / 2) * (data ? -1 : 1)));
				if (DoEnderDash(ent, dir)) {
					armor.consumeEnderDash(ent);
				}
			}
			break;
		case WIND_TORNADO:
			if (!ent.world.isRemote && armor.hasWindTornado(ent)) {
				armor.consumeWindTornado(ent);
				EntityAreaEffect cloud = new EntityAreaEffect(NostrumEntityTypes.areaEffect, ent.world, ent.posX, ent.posY, ent.posZ);
				cloud.setOwner(ent);
				
				cloud.setHeight(5f);
				cloud.setRadius(7.5f);
				cloud.setDuration(0);
				cloud.setWaitTime(20 * 5 + 10);
				cloud.setIgnoreRadius(true);
				cloud.addEffect((IAreaEntityEffect)(worldIn, entity) -> {
					if (entity.noClip || entity.hasNoGravity()) {
						return;
					}
					
					// Never effect summoner
					if (entity == ent) {
						return;
					}
					
					// Projectiles get turned downward
					if (entity instanceof IProjectile) {
						LivingEntity shooter = Projectiles.getShooter(entity);
						if (shooter == ent) {
							// Let summoner's projectiles go unharmed
							return;
						}
						
						entity.setMotion(entity.getMotion()
								.mul(.2, 1, .2)
								.add(0, -.3, 0)
								);
//						entity.getMotion().y -= 0.3;
//						entity.getMotion().x *= .2;
//						entity.getMotion().z *= .2;
						entity.velocityChanged = true;
						return;
					}
					
//					// upward effect
//					final int period = 20;
//					final float prog = ((float) (entity.ticksExisted % period) / (float) period);
//					final double dy = (Math.sin(prog * 2 * Math.PI) + 1) / 2;
//					final Vec3d target = new Vec3d(cloud.posX, cloud.posY + 2 + dy, cloud.posZ);
//					final Vec3d diff = target.subtract(entity.getPositionVector());
//					entity.getMotion().x = 0;//diff.x/ 2;
//					entity.getMotion().y = diff.y/ 2;
//					entity.getMotion().z = 0;//diff.z/ 2;
//					entity.velocityChanged = true;
//					//entity.posY = 2 + dy;
//					//entity.setPositionAndUpdate(cloud.posX, cloud.posY + 2 + dy, cloud.posZ);
					
					// Downward suppresive effect
					entity.setMotion(entity.getMotion()
							.mul(.2, 1, .2)
							.add(0, -.3, 0)
							);
//					entity.getMotion().x *= .2;
//					entity.getMotion().z *= .2;
//					entity.getMotion().y -= 0.3;
					
					
					// Hurt unfriendlies, too
					if (entity.ticksExisted % 20 == 0) {
						if (entity instanceof LivingEntity && !NostrumMagica.IsSameTeam((LivingEntity)entity, ent)) {
							LivingEntity living = (LivingEntity) entity;
							entity.hurtResistantTime = 0;
							entity.attackEntityFrom(new MagicDamageSource(ent, EMagicElement.WIND),
									SpellAction.calcDamage(ent, living, .25f, EMagicElement.WIND));
							entity.hurtResistantTime = 0;
							
//							NostrumParticles.GLOW_ORB.spawn(living.getEntityWorld(), new NostrumParticles.SpawnParams(
//									 10,
//									 living.posX, entity.posY + entity.height/2f, entity.posZ, entity.width * 2,
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
				cloud.setParticleData(ParticleTypes.MYCELIUM);
				cloud.setIgnoreRadius(true);
				cloud.addVFXFunc((worldIn, ticksExisted, cloudIn) -> {
					final int count = 40;
					EnchantedWeapon.spawnWhirlwindParticle(worldIn, count, cloudIn.getPositionVector(), cloudIn, 0xA0C0EEC0, .65f);
				});
				cloud.setCustomParticle(ParticleTypes.SWEEP_ATTACK);
				//cloud.setCustomParticleParam1(10);
				cloud.setCustomParticleFrequency(.05f);
				
				ent.world.addEntity(cloud);
			}
			break;
		case DRAGON_FLIGHT_TICK:
			// Deduct mana
			if (data) {
				if (!ent.world.isRemote) {
					armor.consumeDragonFlight(ent);
				} else {
					if (SetArmorWingFlap(ent)) {
						if (ent instanceof PlayerEntity) {
							NostrumMagicaSounds.WING_FLAP.playClient((PlayerEntity) ent);
						}
					}
				}
			}
			break;
		case EFFECT_TOGGLE:
			SetArmorHitEffectsEnabled(ent, data);
			break;
		case WIND_JUMP_WHIRLWIND:
			if (!ent.world.isRemote && armor.hasWindTornado(ent)) {
				PlayerEntity playerIn = (PlayerEntity) ent;
				armor.consumeWindJumpWhirlwind(ent);
				final float maxDist = 20;
				RayTraceResult mop = RayTrace.raytrace(playerIn.world, playerIn, playerIn.getPositionVector().add(0, playerIn.getEyeHeight(), 0), playerIn.getLookVec(), maxDist, (e) -> { return e != playerIn;});
				if (mop != null && mop.getType() != RayTraceResult.Type.MISS) {
					final Vec3d at = (mop.getType() == RayTraceResult.Type.ENTITY ? RayTrace.entFromRaytrace(mop).getPositionVec() : mop.getHitVec());
					EnchantedWeapon.spawnJumpVortex(playerIn.world, playerIn, at, EnchantedWeapon.Type.MASTER);
				}
			}
			break;
		}
	}
	
	@SubscribeEvent
	public void onJump(LivingJumpEvent event) {
		// Full true/corrupt sets give jump boost
		if (event.isCanceled()) {
			return;
		}

		LivingEntity ent = event.getEntityLiving();
		@Nonnull ItemStack chestplate = ent.getItemStackFromSlot(EquipmentSlotType.CHEST);
		if (chestplate.isEmpty() || !(chestplate.getItem() instanceof EnchantedArmor)) {
			return;
		}
		
		EnchantedArmor armor = (EnchantedArmor) chestplate.getItem();
		if (armor.getType() == Type.TRUE && armor == this && armor.getSetPieces(ent) == 4) {
			// Jump-boost gives an extra .1 per level. We want 2-block height so we do .2
			Vec3d motion = ent.getMotion();
			ent.setMotion(motion.x, motion.y + armor.jumpBoost, motion.z);
		}
	}
	
	@SubscribeEvent
	public void onJump(LivingFallEvent event) {
		// Full true/corrupt sets give jump boost
		if (event.isCanceled()) {
			return;
		}

		LivingEntity ent = event.getEntityLiving();
		@Nonnull ItemStack chestplate = ent.getItemStackFromSlot(EquipmentSlotType.CHEST);
		if (chestplate.isEmpty() || !(chestplate.getItem() instanceof EnchantedArmor)) {
			return;
		}
		
		EnchantedArmor armor = (EnchantedArmor) chestplate.getItem();
		if (armor.getType() == Type.TRUE && armor == this && armor.getSetPieces(ent) == 4) {
			// Jump-boost gives an extra .1 per level. We want 2-block height so we do .2
			final float amt = (float) (armor.jumpBoost / .1f);
			event.setDistance(Math.max(0f, event.getDistance() - amt));
		}
	}

	@Override
	public boolean shouldRenderDragonWings(ItemStack stack, PlayerEntity player) {
		final boolean flying = player.isElytraFlying();
		// Maybe should have an interface?
		if (
				EnchantedArmor.GetSetCount(player, EMagicElement.PHYSICAL, Type.TRUE) == 4
				|| EnchantedArmor.GetSetCount(player, EMagicElement.EARTH, Type.TRUE) == 4
				|| EnchantedArmor.GetSetCount(player, EMagicElement.FIRE, Type.TRUE) == 4
				|| EnchantedArmor.GetSetCount(player, EMagicElement.ENDER, Type.TRUE) == 4
				|| (
					EnchantedArmor.GetHasWingUpgrade(stack) && (
						EnchantedArmor.GetSetCount(player, EMagicElement.ICE, Type.TRUE) == 4
						|| EnchantedArmor.GetSetCount(player, EMagicElement.WIND, Type.TRUE) == 4
						|| EnchantedArmor.GetSetCount(player, EMagicElement.LIGHTNING, Type.TRUE) == 4
					)
					
				)
				) {
			if (flying) {
				return true;
			}
			
			ItemStack cape = LayerAetherCloak.ShouldRender(player);
			return cape.isEmpty() || !((ICapeProvider) cape.getItem()).shouldPreventOtherRenders(player, cape);
		}
		return false;
	}

	@Override
	public int getDragonWingColor(ItemStack stack, PlayerEntity player) {
		return this.element.getColor();
	}
}
