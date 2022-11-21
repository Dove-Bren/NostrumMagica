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

import org.lwjgl.input.Keyboard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicReduction;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.model.ModelEnchantedArmorBase;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.render.LayerAetherCloak;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.EntityAreaEffect;
import com.smanzana.nostrummagica.entity.EntityAreaEffect.IAreaEntityEffect;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.EnchantedArmorStateUpdate;
import com.smanzana.nostrummagica.network.messages.EnchantedArmorStateUpdate.ArmorState;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.MagicDamageSource;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.utils.NonNullEnumMap;
import com.smanzana.nostrummagica.utils.Projectiles;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.EntityEquipmentSlot.Type;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

public class EnchantedArmor extends ItemArmor implements EnchantedEquipment, ISpecialArmor, IElytraProvider, IDragonWingRenderItem {

	private static Map<EMagicElement, Map<EntityEquipmentSlot, Map<Integer, EnchantedArmor>>> items;
	
	public static final void registerArmors(final IForgeRegistry<Item> registry) {
		items = new EnumMap<EMagicElement, Map<EntityEquipmentSlot, Map<Integer, EnchantedArmor>>>(EMagicElement.class);
		for (EMagicElement element : EMagicElement.values()) {
			items.put(element, new EnumMap<EntityEquipmentSlot, Map<Integer, EnchantedArmor>>(EntityEquipmentSlot.class));
			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
				if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
				items.get(element).put(slot, new HashMap<Integer, EnchantedArmor>());
					for (int i = 0; i < 4; i++) {
						if (!isArmorElement(element) && i != 3) {
							continue; // Corrupted armors are only lvl 3
						}
						ResourceLocation location = new ResourceLocation(NostrumMagica.MODID, "armor_" + slot.name().toLowerCase() + "_" + element.name().toLowerCase() + (i + 1));
						EnchantedArmor armor =  new EnchantedArmor(location.getResourcePath(), slot, element, i);
						armor.setUnlocalizedName(location.getResourcePath());
						armor.setRegistryName(location);
						registry.register(armor);
						items.get(element).get(slot).put(i + 1, armor);
						MinecraftForge.EVENT_BUS.register(armor);
					}
				}
			}
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
	
	private static int calcArmor(EntityEquipmentSlot slot, EMagicElement element, int level) {
		
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
			base += Math.min(2, level) - 1;
		
		if (true) {//ModConfig.config.usingAdvancedArmors()) {
			base += 1;
		}
		
		return Math.max(1, (int) ((float) base * mod));
	}
	
	// Calcs magic resist, but as if it were armor which is base 20/25
	private static float calcMagicResistBase(EntityEquipmentSlot slot, EMagicElement element, int level) {
		
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
			if (level >= 3) {
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
			base += Math.min(2, level) - 1;
		
		return Math.max(1f, ((float) base * mod));
	}
	
	private static final double calcMagicReduct(EntityEquipmentSlot slot, EMagicElement element, int level) {
		// each piece will give (.1, .15, .25) of their type depending on level.
		return (level == 0 ? .1 : (level == 1 ? .15 : .25));
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
		final double[] setTotalIce = {0, 0, 0, .5};
		
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
	
	private static final double calcMagicSetReduct(EntityEquipmentSlot slot, EMagicElement armorElement, int setCount, EMagicElement targetElement) {
		return calcMagicSetReductTotal(armorElement, setCount, targetElement) / setCount; // split evenly amonst all [setCount] pieces.
		// COULD make different pieces make up bigger chunks of the pie but ehh
	}
	
	private static int calcArmorDurability(EntityEquipmentSlot slot, EMagicElement element, int level) {
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
		double amt = iron * Math.pow(1.5, level-1);
		
		return (int) Math.floor(amt * mod);
	}
	
	private static double calcArmorSpeedBoost(EntityEquipmentSlot slot, EMagicElement element, int level) {
		if (level < 3) {
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
	
	private static double calcArmorJumpBoost(EntityEquipmentSlot slot, EMagicElement element, int level) {
		if (level < 3) {
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
	
	private int level;
	private int armor; // Can't use vanilla; it's final
	private double magicResistAmount;
	private double magicReducAmount;
	private EMagicElement element;
	private double jumpBoost;
	private double speedBoost;
	
	private String modelID;
	
	@SideOnly(Side.CLIENT)
	private static ModelEnchantedArmorBase armorModels[];
	
	public EnchantedArmor(String modelID, EntityEquipmentSlot type, EMagicElement element, int level) {
		super(ArmorMaterial.IRON, 0, type);
		
		this.level = level;
		this.element = element;
		this.modelID = modelID;
		
		this.setCreativeTab(NostrumMagica.creativeTab);
		
		this.armor = calcArmor(type, element, level);
		this.magicResistAmount = (Math.round((double) calcMagicResistBase(type, element, level) * 4.0D)); // Return is out of 25, so x 4 for %
		this.magicReducAmount = calcMagicReduct(type, element, level);
		this.jumpBoost = calcArmorJumpBoost(type, element, level);
		this.speedBoost = calcArmorSpeedBoost(type, element, level);
		
		this.setMaxDamage(calcArmorDurability(type, element, level));
		
		if (!NostrumMagica.proxy.isServer()) {
			if (armorModels == null) {
				armorModels = new ModelEnchantedArmorBase[5];
				for (int i = 0; i < 5; i++) {
					armorModels[i] = new ModelEnchantedArmorBase(1f, i);
				}
			}
		}
	}
	
	@Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();	

        if (equipmentSlot == this.armorType)
        {
            multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor modifier", (double)this.armor, 0));
            multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor toughness", 4, 0));
            multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(ARMOR_SPEED_MODS[equipmentSlot.getIndex()], "Armor speed boost", (double)this.speedBoost, 2));
            multimap.put(AttributeMagicResist.instance().getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Magic Resist", (double)this.magicResistAmount, 0));
            multimap.put(AttributeMagicReduction.instance(this.element).getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Magic Reduction", (double)this.magicReducAmount, 0));
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
	public SpellAction getTriggerAction(EntityLivingBase user, boolean offense, @Nonnull ItemStack stack) {
		if (offense)
			return null;
		
		if (!GetArmorHitEffectsEnabled(user)) {
			return null;
		}
		
		SpellAction action = null;
		switch (element) {
		case EARTH:
//			if (NostrumMagica.rand.nextFloat() <= 0.15f * (float) (Math.min(2, level) + 1))
			action = new SpellAction(user).status(RootedPotion.instance(), 20 * 5 * (Math.min(2, level) + 1), 0);
			break;
		case ENDER:
//			if (NostrumMagica.rand.nextFloat() <= 0.15f * (float) (Math.min(2, level) + 1))
			action = new SpellAction(user).phase(Math.min(2, level));
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
		return !offense && NostrumMagica.rand.nextFloat() <= chancePer * (float) (Math.min(2, level) + 1);
	}
	
	public static EnchantedArmor get(EMagicElement element, EntityEquipmentSlot slot, int level) {
		if (items.containsKey(element) && items.get(element).containsKey(slot))
			return items.get(element).get(slot).get(level);
		
		return null;
	}
	
	public int getLevel() {
		return level;
	}
	
	public EMagicElement getElement() {
		return element;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
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
	
	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
		final int setCount = getSetPieces(entity);
		final int index = (setCount - 1) + (this.level >= 3 ? 1 : 0); // Boost 1 if ultimate armor
		ModelEnchantedArmorBase model = armorModels[index % armorModels.length];
		model.setVisibleFrom(slot);
		
		return model;
	}
	
	public static int GetSetCount(EntityLivingBase entity, EMagicElement element, int level) {
		int count = 0;
		
		if (entity != null) {
			for (EntityEquipmentSlot slot : new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET}) {
				ItemStack inSlot = entity.getItemStackFromSlot(slot);
				if (inSlot.isEmpty() || !(inSlot.getItem() instanceof EnchantedArmor)) {
					continue;
				}
				
				EnchantedArmor item = (EnchantedArmor) inSlot.getItem();
				if (item.getElement() == element && item.getLevel() == level) {
					count++;
				}
			}
		}
		
		return count;
	}
	
	public static int GetSetPieces(EntityLivingBase entity, EnchantedArmor armor) {
		final EMagicElement myElem = armor.getElement();
		final int myLevel = armor.getLevel();
		return GetSetCount(entity, myElem, myLevel);
	}
	
	public int getSetPieces(EntityLivingBase entity) {
		return GetSetPieces(entity, this);
	}
	
	@Override
	public boolean hasOverlay(ItemStack item) {
		return true;
	}
	
	@Override
	public int getColor(ItemStack stack) {
		// This is brightness. Different elements already tint their textures. We just make brighter with level.
		switch (level) {
		default:
		case 0:
			return 0xFF3F3F3F;
		case 1:
			return 0xFF7F7F7F;
		case 2:
		case 3:
			return 0xFFFFFFFF;
		}
	}
	
	public static List<EnchantedArmor> getAll() {
		List<EnchantedArmor> list = new LinkedList<>();
		
		for (EMagicElement element : EMagicElement.values()) {
			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
			if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
				list.addAll(items.get(element).get(slot).values());
			}
		}
		
		return list;
	}
	
	public String getModelID() {
		return modelID;
	}

	@Override
	public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage,
			int slot) {
		if (source.isDamageAbsolute() || source.isUnblockable()) {
			return new ArmorProperties(1, 0.0, 0);
		}
		
		// This is deducted in addition to amount from attributes -- which cap out at diamond level.
		// Subtract diamond level when calculating ratio
		final int extraArmorPts = this.armor - ArmorMaterial.DIAMOND.getDamageReductionAmount(armorType);
		return new ArmorProperties(1, Math.max(0, (double) extraArmorPts / 25.0), Integer.MAX_VALUE);
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
		return 0; // this.armor; this is now "extra" on top of attributes
	}

	@Override
	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
		stack.damageItem(damage, entity);
	}
	
	protected void onArmorDisplayTick(World world, EntityPlayer player, ItemStack itemStack, int setCount) {
		final int displayLevel = (Math.min(2, level) + 1) * (setCount * setCount);
		
//		if (setCount == 4 && element == EMagicElement.ICE && level == 3) {
//			RenderFuncs.renderWeather(player.getPosition(), Minecraft.getMinecraft().getRenderPartialTicks(), true);
//		}
		
		if (NostrumMagica.rand.nextInt(400) > displayLevel) {
			return;
		}
		
		final double dx;
		final double dy;
		final double dz;
		final EnumParticleTypes effect;
		final int mult;
		final float rangeMod;
		int[] data = new int[0];
		switch (element) {
		case EARTH:
			effect = EnumParticleTypes.SUSPENDED_DEPTH;
			dx = dy = dz = 0;
			mult = 1;
			rangeMod = 1;
			break;
		case FIRE:
			effect = EnumParticleTypes.FLAME;
			dx = dz = 0;
			dy = .025;
			mult = 1;
			rangeMod = 2;
			break;
		case ENDER:
			effect = EnumParticleTypes.PORTAL;
			dx = dy = dz = 0;
			mult = 2;
			rangeMod = 1.5f;
			break;
		case ICE:
			effect = EnumParticleTypes.FALLING_DUST;
			dx = dz = 0;
			dy = .025;
			mult = 1;
			rangeMod = 2;
			data = new int[] {Block.getStateId(Blocks.SNOW.getDefaultState())};
			break;
		case LIGHTNING:
//			effect = EnumParticleTypes.FALLING_DUST;
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
			world.spawnParticle(effect, px, py, pz, dx, dy, dz, data);
		}
	}
	
	protected void onServerTick(World world, EntityPlayer player, ItemStack stack, int setCount) {
		if (setCount == 4 && this.level == 3 && this.armorType == EntityEquipmentSlot.CHEST) {
			if (element == EMagicElement.ICE) {
				if (player.onGround && !ArmorCheckFlying(player)) {
					final BlockPos pos = player.getPosition();
					if (world.isAirBlock(pos)) {
						IBlockState belowState = world.getBlockState(pos.down());
						if (belowState.getMaterial().blocksMovement()) {
							world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState());
						}
					}
				}
			} else if (element == EMagicElement.WIND) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				if (attr != null && attr.getMana() > 0 && player.isSprinting() && !ArmorCheckFlying(player)) {
					final Potion potSpeed = Potion.getPotionFromResourceLocation("speed");
					final Potion potJump = Potion.getPotionFromResourceLocation("jump_boost");
					if (!player.isPotionActive(potSpeed) || player.ticksExisted % 10 == 0) {
						player.addPotionEffect(new PotionEffect(potSpeed, 20, 0));
					}
					if (!player.isPotionActive(potJump) || player.ticksExisted % 10 == 0) {
						player.addPotionEffect(new PotionEffect(potJump, 20, 1));
					}
					
					// Refresh nearby tornados
					if (player.onGround)
					for (EntityAreaEffect cloud : world.getEntitiesWithinAABB(EntityAreaEffect.class, (new AxisAlignedBB(0, 0, 0, 1, 1, 1)).offset(player.posX, player.posY, player.posZ).grow(5), (effect) -> {
						return effect != null
								&& (effect.getCustomParticle() == EnumParticleTypes.SWEEP_ATTACK || effect.getParticle() == EnumParticleTypes.SWEEP_ATTACK);
					})) {
						cloud.addTime(1, true);
					}
					
					if (player.ticksExisted % 3 == 0) {
						attr.addMana(-1);
						NostrumMagica.proxy.sendMana(player);
					}
				}
			} else if (element == EMagicElement.EARTH) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				if (attr != null && attr.getMana() >= EARTH_GROW_COST && !ArmorCheckFlying(player)) {
					if (player.ticksExisted % 40 == 0) {
						// Attempt bonemeal
						if (DoEarthGrow(world, player.getPosition()) != null) {
							attr.addMana(-EARTH_GROW_COST);
							NostrumMagica.proxy.sendMana(player);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
		super.onArmorTick(world, player, itemStack);
		
		final int setCount = getSetPieces(player);
		if (!world.isRemote) {
			onServerTick(world, player, itemStack, setCount);
		} else {
			onArmorDisplayTick(world, player, itemStack, setCount);
		}
	}
	
	// hacky little map to avoid thrashing attributes every tick
	protected static final Map<EntityLivingBase, Map<EntityEquipmentSlot, ItemStack>> LastEquipState = new HashMap<>();
	
	protected static Map<EntityEquipmentSlot, ItemStack> GetLastTickState(EntityLivingBase entity) {
		Map<EntityEquipmentSlot, ItemStack> map = LastEquipState.get(entity);
		if (map == null) {
			map = new NonNullEnumMap<>(EntityEquipmentSlot.class, ItemStack.EMPTY);
			LastEquipState.put(entity, map);
		}
		return map;
	}
	
	protected static boolean EntityChangedEquipment(EntityLivingBase entity) {
		Map<EntityEquipmentSlot, ItemStack> map = GetLastTickState(entity);
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			if (slot.getSlotType() != Type.ARMOR) {
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
	
	public static final boolean EntityHasEnchantedArmor(EntityLivingBase entity) {
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			if (slot.getSlotType() != Type.ARMOR) {
				continue;
			}
			
			ItemStack inSlot = entity.getItemStackFromSlot(slot);
			if (!inSlot.isEmpty()  && inSlot.getItem() instanceof EnchantedArmor) {
				return true;
			}
		}
		
		return false;
	}
	
	protected static void UpdateEntity(EntityLivingBase entity) {
		
		if (entity.isDead) {
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
			Map<EntityEquipmentSlot, ItemStack> cacheMap = new EnumMap<>(EntityEquipmentSlot.class);
			Multimap<String, AttributeModifier> attribMap = HashMultimap.<String, AttributeModifier>create();	

			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
				if (slot.getSlotType() != Type.ARMOR) {
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
				for (EMagicElement elem : EMagicElement.values()) {
					final double reduct = (setCount > 0 && armorElem != null)
							? calcMagicSetReduct(slot, armorElem, setCount, elem)
							: 0;
						
					// Important to do this even with 0 to remove previous bonuses
					attribMap.put(AttributeMagicReduction.instance(elem).getName(), new AttributeModifier(SET_MODIFIERS[slot.getIndex()], "Magic Reduction (Set)", reduct, 0));
				}
				
				// Add captured value to map
				cacheMap.put(slot, inSlot);
			}
			
			// Update attributes
			entity.getAttributeMap().applyAttributeModifiers(attribMap);
			
			// Create and save new map
			LastEquipState.put(entity, cacheMap);
		}
		
		// Check for world-changing full set bonuses
		// Note: Cheat and just look at helm. if helm isn't right, full set isn't set anyways
		@Nonnull ItemStack helm = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		if (!helm.isEmpty() && helm.getItem() instanceof EnchantedArmor) {
			EnchantedArmor type = (EnchantedArmor) helm.getItem();
			final int setCount = GetSetPieces(entity, type);
			if (setCount == 4) {
				// Full set!
				final EMagicElement element = type.getElement();
				final int level = type.getLevel();
				
				if (element == EMagicElement.FIRE) {
					// Fire prevents fire.
					// Level 1(0) reduces fire time (25% reduction by 50% of the time reducing by another tick)
					// Level 2(1) halves fire time
					// Level 3 and 4(2/3) prevents fire all-together
					if (level >= 2) {
						if (entity.isBurning()) {
							entity.extinguish();
						}
					} else {
						if (level == 1 || NostrumMagica.rand.nextBoolean()) {
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
	public static void ServerWorldTick(World world) {
		for (Entity ent : world.loadedEntityList) {
			if (ent instanceof EntityLivingBase) {
				UpdateEntity((EntityLivingBase) ent);
			}
		}
	}
	
	public static Map<IAttribute, Double> FindCurrentSetBonus(@Nullable Map<IAttribute, Double> map, EntityLivingBase entity, EMagicElement element, int level) {
		if (map == null) {
			map = new HashMap<>();
		}
		
		final int setCount = GetSetCount(entity, element, level);
		
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			if (slot.getSlotType() != Type.ARMOR) {
				continue;
			}
			
			@Nonnull ItemStack inSlot = entity.getItemStackFromSlot(slot);
			if (inSlot.isEmpty() || !(inSlot.getItem() instanceof EnchantedArmor)) {
				continue;
			}
			
			EnchantedArmor armorType = (EnchantedArmor) inSlot.getItem();
			final int inSlotLevel = armorType.getLevel();
			final EMagicElement inSlotElement = armorType.getElement();
			if (inSlotLevel != level || inSlotElement != element) {
				continue;
			}
			
			for (EMagicElement elem : EMagicElement.values()) {
				final double reduct = calcMagicSetReduct(slot, element, setCount, elem);
				if (reduct == 0) {
					continue;
				}
				
				final AttributeMagicReduction inst = AttributeMagicReduction.instance(elem);
				Double cur = map.get(inst);
				if (cur == null) {
					cur = 0.0;
				}
				
				cur = cur + reduct;
					
				map.put(inst, cur);
			}
		}
		
		return map;
	}
	
	private Map<IAttribute, Double> setMapInst = new HashMap<>();
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		final boolean showFull = Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode());
		final @Nullable EntityPlayer player = NostrumMagica.proxy.getPlayer();
		final int setCount = this.getSetPieces(player);
		
		final String setName = I18n.format("item.armor.set." + element.name().toLowerCase() + "." + level + ".name", new Object[0]);
		if (showFull) {
			tooltip.add(I18n.format("info.armor.set_total", setName, ChatFormatting.DARK_PURPLE, ChatFormatting.RESET));
		} else {
			
			final String countFormat = "" + (setCount == 4 ? ChatFormatting.GOLD : ChatFormatting.YELLOW);
			tooltip.add(I18n.format("info.armor.set_status", setName, setCount, countFormat, "" + ChatFormatting.RESET));
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
				} else {
					// Show current
					FindCurrentSetBonus(setMapInst, player, element, level); // puts into setMapInst
				}
				
				if (!setMapInst.isEmpty()) {
					for (Entry<IAttribute, Double> entry : setMapInst.entrySet()) {
						Double val = entry.getValue();
						if (val == null || val == 0) {
							continue;
						}
						
						// Formatting here copied from Vanilla
						if (val > 0) {
							tooltip.add(TextFormatting.BLUE + " " + I18n.format("attribute.modifier.plus.0", String.format("%.2f", val), I18n.format("attribute.name." + (String)entry.getKey().getName())));
						} else {
							val = -val;
							tooltip.add(TextFormatting.RED + " " + I18n.format("attribute.modifier.take.0", String.format("%.2f", val), I18n.format("attribute.name." + (String)entry.getKey().getName())));
						}
					}
				}
			}
			
			// Also show special bonuses
			// TODO make this a bit more... extensible?
			if (showFull || setCount == 4) {
				final String key = "info.armor.set_bonus." + element.name().toLowerCase() + "." + level;
				if (I18n.hasKey(key)) {
					final String full = I18n.format(key, new Object[0]);
					for (String line : full.split("\\|"))
					tooltip.add(ChatFormatting.DARK_PURPLE + " "
							+ line
							+ ChatFormatting.RESET);
				}
			}
		}
		
		if (EnchantedArmor.GetHasWingUpgrade(stack)) {
			tooltip.add(ChatFormatting.GOLD + I18n.format("info.armor.wing_upgrade") + ChatFormatting.RESET);
		}
	}

	@Override
	public boolean isElytraFlying(EntityLivingBase entity, ItemStack stack) {
		return ArmorCheckFlying(entity);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldRenderElyta(EntityLivingBase entity, ItemStack stack) {
		return hasElytra(entity)
				&& (element == EMagicElement.ICE || element == EMagicElement.LIGHTNING || element == EMagicElement.WIND)
				&& (!(entity instanceof EntityPlayer) || !(shouldRenderDragonWings(stack, (EntityPlayer) entity)));
	}
	
	protected boolean hasElytra(EntityLivingBase entity) {
		if (this.level == 3 && this.armorType == EntityEquipmentSlot.CHEST) {
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
	protected static final int ENDER_DASH_COST = 20;
	protected static final int EARTH_GROW_COST = 5;
	
	protected boolean hasManaJump(EntityLivingBase entity) {
		if (this.level == 3 && this.armorType == EntityEquipmentSlot.CHEST) {
			// Check if full set is available and if we have enough mana
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
			if (attr == null || attr.getMana() < MANA_JUMP_COST) {
				return false;
			}
			return (4 == getSetPieces(entity)); 
		}
		return false;
	}
	
	protected boolean hasWindTornado(EntityLivingBase entity) {
		if (this.level == 3 && this.armorType == EntityEquipmentSlot.CHEST && this.element == EMagicElement.WIND) {
			// Check if full set is available and if we have enough mana
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
			if (attr == null || attr.getMana() < WIND_TORNADO_COST) {
				return false;
			}
			return (4 == getSetPieces(entity)); 
		}
		return false;
	}
	
	protected boolean hasEnderDash(EntityLivingBase entity) {
		if (this.level == 3 && this.armorType == EntityEquipmentSlot.CHEST && this.element == EMagicElement.ENDER) {
			// Check if full set is available and if we have enough mana
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
			if (attr == null || attr.getMana() < ENDER_DASH_COST) {
				return false;
			}
			return (4 == getSetPieces(entity)); 
		}
		return false;
	}
	
	protected boolean hasDragonFlight(EntityLivingBase entity) {
		if (this.level == 3 && this.armorType == EntityEquipmentSlot.CHEST) {
			boolean hasRightElement = element == EMagicElement.ENDER || element == EMagicElement.EARTH || element == EMagicElement.FIRE || element == EMagicElement.PHYSICAL;
			if (!hasRightElement) {
				ItemStack chest = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
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
	
	protected void consumeManaJump(EntityLivingBase entity) {
		if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative()) {
			return;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null) {
			attr.addMana(-MANA_JUMP_COST);
		}
	}
	
	protected void consumeWindTornado(EntityLivingBase entity) {
		if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative()) {
			return;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null) {
			attr.addMana(-WIND_TORNADO_COST);
		}
	}
	
	protected void consumeEnderDash(EntityLivingBase entity) {
		if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative()) {
			return;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null) {
			attr.addMana(-ENDER_DASH_COST);
		}
	}
	
	protected void consumeDragonFlight(EntityLivingBase entity) {
		if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative()) {
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
	private static KeyBinding bindingToggleArmorEffect;
	
	public static final void ClientInit() {
		bindingEnderLeft = new KeyBinding("key.dash.left.desc", Keyboard.KEY_NUMPAD7, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingEnderLeft);

		bindingEnderRight = new KeyBinding("key.dash.right.desc", Keyboard.KEY_NUMPAD9, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingEnderRight);

		bindingEnderBack = new KeyBinding("key.dash.back.desc", Keyboard.KEY_NUMPAD2, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingEnderBack);

		bindingToggleArmorEffect = new KeyBinding("key.armor.toggle.desc", Keyboard.KEY_NUMPAD5, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingToggleArmorEffect);
		
	}
	
	protected void clientDashSide(EntityLivingBase ent, boolean right) {
		this.consumeEnderDash(ent);
		NetworkHandler.getSyncChannel().sendToServer(new EnchantedArmorStateUpdate(ArmorState.ENDER_DASH_SIDE, right, 0));
	}
	
	protected void clientDashBack(EntityLivingBase ent) {
		this.consumeEnderDash(ent);
		NetworkHandler.getSyncChannel().sendToServer(new EnchantedArmorStateUpdate(ArmorState.ENDER_DASH_BACK, false, 0));
	}
	
	@SubscribeEvent
	public void onKey(KeyInputEvent event) {
		EntityPlayer player = NostrumMagica.proxy.getPlayer();
		if (bindingEnderLeft.isPressed()) {
			clientDashSide(player, false);
		} else if (bindingEnderRight.isPressed()) {
			clientDashSide(player, true);
		} else if (bindingEnderBack.isPressed()) {
			clientDashBack(player);
		} else if (bindingToggleArmorEffect.isPressed()) {
			NostrumMagicaSounds.UI_TICK.playClient(player);
			final boolean enabled = !GetArmorHitEffectsEnabled(player);
			SetArmorHitEffectsEnabled(player, enabled);
			NetworkHandler.getSyncChannel().sendToServer(new EnchantedArmorStateUpdate(ArmorState.EFFECT_TOGGLE, enabled, 0));
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		final EntityPlayerSP player = Minecraft.getMinecraft().player;
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
		if (flying && (player.onGround || player.isSneaking() || player.isRiding() || player.isInWater() || player.isInLava())) {
			SetArmorFlying(player, false);
			SendUpdates(player, null);
			return;
		}
		
		boolean hasJump = player.movementInput.jump && !jumpPressedEarly;
		
		// Start flying
		if (!flying && hasJump && !player.onGround && player.motionY < 0 && !player.capabilities.isFlying) {
			// Does this armor support flying?
			if (this.hasElytra(player)) {
				SetArmorFlying(player, true);
				SendUpdates(player, null);
				hasJump = false; // Consumed
			}
		}
		
		// Mana jump
		final double MANA_JUMP_AMT = flying ? .6 : .4;
		if (hasJump && flying && !player.onGround && !lastTickGround && !player.capabilities.isFlying && player.motionY < MANA_JUMP_AMT) {
			// Does this armor have mana jump?
			if (this.hasManaJump(player)) {
				this.consumeManaJump(player);
				player.motionY += MANA_JUMP_AMT;
				hasJump = false; // Consumed
				NetworkHandler.getSyncChannel().sendToServer(new EnchantedArmorStateUpdate(ArmorState.JUMP, true, 0));
			}
		}
		
		// Dragon flying
		if (flying && !player.onGround && !player.capabilities.isFlying && player.movementInput.forwardKeyDown) {
			// Does this armor have dragon flying?
			if (this.hasDragonFlight(player)) {
				// Check if magnitude of flying is low and if so, boost it with magic
				final double curMagnitudeSq = (player.motionX * player.motionX + player.motionY * player.motionY + player.motionZ * player.motionZ);
				if (curMagnitudeSq < .4) {
					// How much should we scale?
					final double curMagnitude = Math.sqrt(curMagnitudeSq);
					final double target = 0.63;
					final double scale = target / curMagnitude;
					final double origX = player.motionX;
					final double origY = player.motionY;
					final double origZ = player.motionZ;
					player.motionX *= scale;
					player.motionY *= scale;
					player.motionZ *= scale;
					
					final double dx = Math.abs(player.motionX - origX);
					final double dy = Math.abs(player.motionY - origY);
					final double dz = Math.abs(player.motionZ - origZ);
					
					// We take mana depending on how 'up' we're being propeled
					final float vertScale = (dx == 0 && dy == 0 && dz == 0 ? 0f : (float) (dy / (dx + dy + dz)));
					final boolean deduct = vertScale == 0f ? false : (itemRand.nextFloat() < vertScale * 3);
					if (deduct) {
						this.consumeDragonFlight(player);
						NetworkHandler.getSyncChannel().sendToServer(new EnchantedArmorStateUpdate(ArmorState.DRAGON_FLIGHT_TICK, deduct, 0));
					}
				}
			}
		}
		
		// Double-press abilities
		if (!flying && !player.capabilities.isFlying) {
			// just for testing
			if (doubleBack) {
				if (this.hasWindTornado(player)) {
					this.consumeWindTornado(player);
					NetworkHandler.getSyncChannel().sendToServer(new EnchantedArmorStateUpdate(ArmorState.WIND_TORNADO, true, 0));
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
		
		if (event.getTarget() instanceof EntityLivingBase) {
			SendUpdates((EntityLivingBase) event.getTarget(), event.getEntityPlayer());
		}
	}
	
	protected static final void SendUpdates(EntityLivingBase entity, @Nullable EntityPlayer toPlayer) {
		// Note: We only do players for now
		if (entity instanceof EntityPlayer) {
			final EntityPlayer player = (EntityPlayer) entity;
			final EnchantedArmorStateUpdate message = new EnchantedArmorStateUpdate(ArmorState.FLYING, ArmorCheckFlying(player), player.getEntityId());
			if (player.world.isRemote) {
				assert(player == NostrumMagica.proxy.getPlayer());
				NetworkHandler.getSyncChannel().sendToServer(message);
			} else if (toPlayer != null) {
				NetworkHandler.getSyncChannel().sendTo(message, (EntityPlayerMP) toPlayer);
			} else {
				NetworkHandler.getSyncChannel().sendToDimension(message, player.dimension);
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
	
	protected static boolean ArmorCheckFlying(EntityLivingBase ent) {
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
	
	public static void SetArmorFlying(EntityLivingBase ent, boolean flying) {
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
	
	protected static int ArmorGetLastFlapTick(EntityLivingBase ent) {
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
	
	public static boolean SetArmorWingFlap(EntityLivingBase ent) {
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
	
	public static float GetWingFlap(EntityLivingBase ent, float partialTicks) {
		double curTicks = ent.ticksExisted + partialTicks; // Expand to double
		double flapStartTicks = ArmorGetLastFlapTick(ent);
		return Math.max(0.0f, Math.min(1.0f, (float) (curTicks - flapStartTicks) / WING_FLAP_DURATION));
	}
	
	public static final void SetArmorHitEffectsEnabled(EntityLivingBase ent, boolean enabled) {
		final UUID key = ent.getUniqueID();
		if (enabled) {
			ArmorHitEffectMap.put(key, enabled);
		} else {
			ArmorHitEffectMap.remove(key);
		}
	}
	
	public static final boolean GetArmorHitEffectsEnabled(EntityLivingBase ent) {
		Boolean val = ArmorHitEffectMap.get(ent.getUniqueID());
		return (val != null && val.booleanValue());
	}
	
	private static final String NBT_WING_UPGRADE = "dragonwing_upgrade";
	
	public static final void SetHasWingUpgrade(ItemStack stack, boolean upgraded) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		nbt.setBoolean(NBT_WING_UPGRADE, upgraded);
		
		stack.setTagCompound(nbt);
	}
	
	public static final boolean GetHasWingUpgrade(ItemStack stack) {
		return !stack.isEmpty()
				&& stack.getItem() instanceof EnchantedArmor
				&& ((EnchantedArmor) stack.getItem()).getEquipmentSlot() == EntityEquipmentSlot.CHEST
				&& stack.hasTagCompound()
				&& stack.getTagCompound().getBoolean(NBT_WING_UPGRADE);
	}
	
	private static final boolean DoEnderDash(EntityLivingBase entity, Vec3d dir) {
		final float dashDist = 4.0f;
		final Vec3d idealVec = entity.getPositionVector().addVector(dashDist * dir.x, dashDist * dir.y, dashDist * dir.z);
		
		// Do three traces from y=0, y=1, and y=2. Take best one
		Vec3d bestResult = null;
		double bestDist = -1;
		final Vec3d startPos = entity.getPositionVector();
		for (int y = -1; y <= 4; y++) {
			final Vec3d end = idealVec.addVector(0, y, 0);
			RayTraceResult mop = RayTrace.raytrace(entity.world, startPos.addVector(0, y, 0), end, (ent) -> {
				return false;
			});
			
			final Vec3d spot;
			if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
				spot = mop.hitVec;
			} else {
				// Didn't hit anything. Count as ideal
				spot = end;
			}
			
			final double dist = startPos.addVector(0, y, 0).distanceTo(spot);
			if (dist > bestDist) {
				bestDist = dist;
				bestResult = spot;
			}
		}
		
		if (bestResult != null && entity.attemptTeleport(bestResult.x, bestResult.y, bestResult.z)) {
			entity.world.playSound(null, startPos.x, startPos.y, startPos.z,
					SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS,
					1f, 1f);
			return true;
		}
		return false;
	}
	
	public static synchronized final @Nullable BlockPos DoEarthGrow(World world, BlockPos center) {
		Collections.shuffle(EARTH_SCAN_POS);
		
		MutableBlockPos cursor = new MutableBlockPos();
		for (BlockPos offset : EARTH_SCAN_POS) {
			cursor.setPos(center.getX() + offset.getX(), center.getY() + offset.getY(), center.getZ() + offset.getZ());
			IBlockState state = world.getBlockState(cursor);
			if (state != null && state.getBlock() instanceof IGrowable) {
				IGrowable growable = (IGrowable) state.getBlock();
				if (!(growable instanceof BlockCrops) && !(growable instanceof BlockSapling)) {
					continue;
				}
				if (growable.canGrow(world, cursor, state, false) && growable.canUseBonemeal(world, itemRand, cursor, state)) {
					// Only grow 1/4th the time
					if (itemRand.nextBoolean() && itemRand.nextBoolean()) {
						growable.grow(world, itemRand, cursor, state);
					}
					
					((WorldServer) world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
							cursor.getX() + .5 + (-.5 + itemRand.nextDouble()),
							cursor.getY() + .5,
							cursor.getZ() + .5 + (-.5 + itemRand.nextDouble()),
							2,
							.2, .2, .2, 0, new int[0]);
					return cursor.toImmutable();
				}
			}
		}
		
		return null;
	}
	
	public static final boolean DoEarthDig(World world, EntityPlayer player, BlockPos pos, EnumFacing face) {
		if (!player.getHeldItemMainhand().isEmpty()) {
			return false;
		}
		
		if (player.getCooledAttackStrength(0.5F) < .95) {
			return false;
		}
		
		IBlockState state = world.getBlockState(pos);
		if (state == null) {
			return false;
		}
		
		Block block = state.getBlock();
		if (block != Blocks.STONE && block != Blocks.SANDSTONE && block != Blocks.COBBLESTONE) {
			return false;
		}
		
		// Can break. Break neighbors, too
		final BlockPos[] positions = (
					face == EnumFacing.UP || face == EnumFacing.DOWN ? new BlockPos[] {
							pos.north().east(), pos.north(), pos.north().west(),
							pos.east(), pos, pos.west(),
							pos.south().east(), pos.south(), pos.south().west()
					}
					: face == EnumFacing.NORTH || face == EnumFacing.SOUTH ? new BlockPos[] {
							pos.up().east(), pos.up(), pos.up().west(),
							pos.east(), pos, pos.west(),
							pos.down().east(), pos.down(), pos.down().west()
					}
					: /* face == EnumFacing.EAST || face == EnumFacing.WEST*/ new BlockPos[] {
							pos.up().north(), pos.up(), pos.up().south(),
							pos.north(), pos, pos.south(),
							pos.down().north(), pos.down(), pos.down().south()
					}
				);
		
		NonNullList<ItemStack> drops = NonNullList.create();
		for (BlockPos at : positions) {
			state = world.getBlockState(at);
			if (state == null) {
				continue;
			}
			
			block = state.getBlock();
			if (block != Blocks.STONE && block != Blocks.SANDSTONE && block != Blocks.COBBLESTONE) {
				continue;
			}
			
			state.getBlock().getDrops(drops, world, at, state, 0); // Fortune?
			world.destroyBlock(at, false);
			for (ItemStack stack : drops) {
				world.spawnEntity(new EntityItem(world, at.getX() + .5, at.getY() + .5, at.getZ() + .5, stack));
			}
		}
		
		return true;
	}
	
	public static final void HandleStateUpdate(ArmorState state, EntityLivingBase ent, boolean data) {
		ItemStack chest = ent.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
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
					if (ent instanceof EntityPlayer) {
						NostrumMagica.proxy.sendMana((EntityPlayer) ent);
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
				EntityAreaEffect cloud = new EntityAreaEffect(ent.world, ent.posX, ent.posY, ent.posZ);
				cloud.setOwner(ent);
				
				cloud.height = 5f;
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
						EntityLivingBase shooter = Projectiles.getShooter(entity);
						if (shooter == ent) {
							// Let summoner's projectiles go unharmed
							return;
						}
						
						entity.motionY -= 0.3;
						entity.motionX *= .2;
						entity.motionZ *= .2;
						entity.velocityChanged = true;
						return;
					}
					
//					// upward effect
//					final int period = 20;
//					final float prog = ((float) (entity.ticksExisted % period) / (float) period);
//					final double dy = (Math.sin(prog * 2 * Math.PI) + 1) / 2;
//					final Vec3d target = new Vec3d(cloud.posX, cloud.posY + 2 + dy, cloud.posZ);
//					final Vec3d diff = target.subtract(entity.getPositionVector());
//					entity.motionX = 0;//diff.x/ 2;
//					entity.motionY = diff.y/ 2;
//					entity.motionZ = 0;//diff.z/ 2;
//					entity.velocityChanged = true;
//					//entity.posY = 2 + dy;
//					//entity.setPositionAndUpdate(cloud.posX, cloud.posY + 2 + dy, cloud.posZ);
					
					// Downward suppresive effect
					entity.motionX *= .2;
					entity.motionZ *= .2;
					entity.motionY -= 0.3;
					
					
					// Hurt unfriendlies, too
					if (entity.ticksExisted % 20 == 0) {
						if (entity instanceof EntityLivingBase && !NostrumMagica.IsSameTeam((EntityLivingBase)entity, ent)) {
							EntityLivingBase living = (EntityLivingBase) entity;
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
				
				
//				cloud.setCustomParticle(EnumParticleTypes.SWEEP_ATTACK);
//				cloud.setCustomParticleParam1(10);
//				cloud.setCustomParticleFrequency(.2f);
				cloud.setParticle(EnumParticleTypes.SUSPENDED);
				cloud.setIgnoreRadius(true);
				cloud.addVFXFunc((worldIn, ticksExisted, cloudIn) -> {
					final int count = 40;
					EnchantedWeapon.spawnWhirlwindParticle(worldIn, count, cloudIn.getPositionVector(), cloudIn, 0xA0C0EEC0, .65f);
				});
				cloud.setCustomParticle(EnumParticleTypes.SWEEP_ATTACK);
				cloud.setCustomParticleParam1(10);
				cloud.setCustomParticleFrequency(.05f);
				
				ent.world.spawnEntity(cloud);
			}
			break;
		case DRAGON_FLIGHT_TICK:
			// Deduct mana
			if (data) {
				if (!ent.world.isRemote) {
					armor.consumeDragonFlight(ent);
				} else {
					if (SetArmorWingFlap(ent)) {
						if (ent instanceof EntityPlayer) {
							NostrumMagicaSounds.WING_FLAP.playClient((EntityPlayer) ent);
						}
					}
				}
			}
			break;
		case EFFECT_TOGGLE:
			SetArmorHitEffectsEnabled(ent, data);
			break;
		}
	}
	
	@SubscribeEvent
	public void onJump(LivingJumpEvent event) {
		// Full true/corrupt sets give jump boost
		if (event.isCanceled()) {
			return;
		}

		EntityLivingBase ent = event.getEntityLiving();
		@Nonnull ItemStack chestplate = ent.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (chestplate.isEmpty() || !(chestplate.getItem() instanceof EnchantedArmor)) {
			return;
		}
		
		EnchantedArmor armor = (EnchantedArmor) chestplate.getItem();
		if (armor.level >= 3 && armor == this && armor.getSetPieces(ent) == 4) {
			// Jump-boost gives an extra .1 per level. We want 2-block height so we do .2
			ent.motionY += armor.jumpBoost;
		}
	}
	
	@SubscribeEvent
	public void onJump(LivingFallEvent event) {
		// Full true/corrupt sets give jump boost
		if (event.isCanceled()) {
			return;
		}

		EntityLivingBase ent = event.getEntityLiving();
		@Nonnull ItemStack chestplate = ent.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (chestplate.isEmpty() || !(chestplate.getItem() instanceof EnchantedArmor)) {
			return;
		}
		
		EnchantedArmor armor = (EnchantedArmor) chestplate.getItem();
		if (armor.level >= 3 && armor == this && armor.getSetPieces(ent) == 4) {
			// Jump-boost gives an extra .1 per level. We want 2-block height so we do .2
			final float amt = (float) (armor.jumpBoost / .1f);
			event.setDistance(Math.max(0f, event.getDistance() - amt));
		}
	}

	@Override
	public boolean shouldRenderDragonWings(ItemStack stack, EntityPlayer player) {
		final boolean flying = player.isElytraFlying();
		// Maybe should have an interface?
		if (
				EnchantedArmor.GetSetCount(player, EMagicElement.PHYSICAL, 3) == 4
				|| EnchantedArmor.GetSetCount(player, EMagicElement.EARTH, 3) == 4
				|| EnchantedArmor.GetSetCount(player, EMagicElement.FIRE, 3) == 4
				|| EnchantedArmor.GetSetCount(player, EMagicElement.ENDER, 3) == 4
				|| (
					EnchantedArmor.GetHasWingUpgrade(stack) && (
						EnchantedArmor.GetSetCount(player, EMagicElement.ICE, 3) == 4
						|| EnchantedArmor.GetSetCount(player, EMagicElement.WIND, 3) == 4
						|| EnchantedArmor.GetSetCount(player, EMagicElement.LIGHTNING, 3) == 4
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
	public int getDragonWingColor(ItemStack stack, EntityPlayer player) {
		return this.element.getColor();
	}
}
