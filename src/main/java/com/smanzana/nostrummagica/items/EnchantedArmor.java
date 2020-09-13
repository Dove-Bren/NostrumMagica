package com.smanzana.nostrummagica.items;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.client.model.ModelEnchantedArmorBase;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellAction;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EnchantedArmor extends ItemArmor implements EnchantedEquipment, ISpecialArmor {

	private static Map<EMagicElement, Map<EntityEquipmentSlot, Map<Integer, EnchantedArmor>>> items;
	
	public static final void registerArmors() {
		items = new EnumMap<EMagicElement, Map<EntityEquipmentSlot, Map<Integer, EnchantedArmor>>>(EMagicElement.class);
		for (EMagicElement element : EMagicElement.values()) {
			if (isArmorElement(element)) {
				items.put(element, new EnumMap<EntityEquipmentSlot, Map<Integer, EnchantedArmor>>(EntityEquipmentSlot.class));
				for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
					if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
						items.get(element).put(slot, new HashMap<Integer, EnchantedArmor>());
						for (int i = 0; i < 3; i++) {
							ResourceLocation location = new ResourceLocation(NostrumMagica.MODID, "armor_" + slot.name().toLowerCase() + "_" + element.name().toLowerCase() + (i + 1));
							EnchantedArmor armor =  new EnchantedArmor(location.getResourcePath(), slot, element, i);
							armor.setUnlocalizedName(location.getResourcePath());
							GameRegistry.register(armor, location);
							items.get(element).get(slot).put(i + 1, armor);
						}
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
	
	// Vanilla UUIDS. can get out of sync. :(
	//private static final UUID[] ARMOR_MODIFIERS = new UUID[] {UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};
	private static final UUID[] ARMOR_MODIFIERS = new UUID[] {UUID.fromString("922AB274-1111-56FE-19AE-365AA9758B8B"), UUID.fromString("D1459204-0E61-4716-A129-61666D432E0D"), UUID.fromString("1F7D236D-1118-6524-3375-34814505B28E"), UUID.fromString("2A632266-F4E1-2E67-7836-64FD783B7A50")};
	
	private static int calcArmor(EntityEquipmentSlot slot, EMagicElement element, int level) {
		
		// Ratio compared to BASE
		// BASE is 14, 18, 22 for the whole set (with rounding errors)
		float mod;
		
		switch (element) {
						// 14, 18, 22  BASE
		case EARTH:
			mod = (22f/24f); // 11, 16.7, 18
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
		default:
			mod = 0.5f;
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
			base += level - 1;
		
		return Math.max(1, (int) ((float) base * mod));
	}
	
	// Calcs magic resist, but as if it were armor which is base 20/25
	private static int calcMagicResistBase(EntityEquipmentSlot slot, EMagicElement element, int level) {
		
		float mod;
		
		switch (element) {
		case EARTH:
			mod = (18f/24f);
			break;
		case ENDER:
			mod = (22f/24f);
			break;
		case FIRE:
			mod = 1f;
			break;
		case PHYSICAL:
			mod = (10/24f);
			break;
		default:
			mod = 0.5f;
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
			base += level - 1;
		
		return Math.max(1, (int) ((float) base * mod));
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
	
	private int level;
	private int armor; // Can't use vanilla; it's final
	private double magicResistAmount;
	private EMagicElement element;
	
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
		this.magicResistAmount = ((double) calcMagicResistBase(type, element, level) * 2.0D); // (/50 so max is 48%, then * 100 for %, so *2)
		
		this.setMaxDamage(calcArmorDurability(type, element, level));
		
		if (!NostrumMagica.proxy.isServer()) {
			if (armorModels == null) {
				armorModels = new ModelEnchantedArmorBase[4];
				for (int i = 0; i < 4; i++) {
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
            multimap.put(SharedMonsterAttributes.ARMOR.getAttributeUnlocalizedName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor modifier", (double)this.armor, 0));
            multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getAttributeUnlocalizedName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor toughness", 1, 0));
            multimap.put(AttributeMagicResist.instance().getAttributeUnlocalizedName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Magic Resist", (double)this.magicResistAmount, 0));
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
	public SpellAction getTriggerAction(EntityLivingBase user, boolean offense, ItemStack stack) {
		if (offense)
			return null;
		
		SpellAction action = null;
		switch (element) {
		case EARTH:
			if (NostrumMagica.rand.nextFloat() <= 0.15f * (float) level)
				action = new SpellAction(user).status(RootedPotion.instance(), 20 * 2, 0);
			break;
		case ENDER:
			if (NostrumMagica.rand.nextFloat() <= 0.15f * (float) level)
				action = new SpellAction(user).phase(level);
			break;
		case FIRE:
			if (NostrumMagica.rand.nextFloat() <= 0.35f * (float) level)
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
	public boolean shouldTrigger(boolean offense, ItemStack stack) {
		return !offense;
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
		
		int setCount = getSetPieces(entity, stack);
		ModelEnchantedArmorBase model = armorModels[Math.max(0, Math.min(3, setCount - 1))];
		model.setVisibleFrom(slot);
		
		return model;
	}
	
	public int getSetPieces(EntityLivingBase entity, ItemStack stack) {
		int count = 0;
		EMagicElement myElem = ((EnchantedArmor)stack.getItem()).getElement();
		int myLevel = ((EnchantedArmor)stack.getItem()).getLevel();
		
		for (EntityEquipmentSlot slot : new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET}) {
			ItemStack inSlot = entity.getItemStackFromSlot(slot);
			if (inSlot == stack) {
				count++;
				continue;
			}
			
			if (inSlot == null || !(inSlot.getItem() instanceof EnchantedArmor)) {
				continue;
			}
			
			EnchantedArmor item = (EnchantedArmor) inSlot.getItem();
			if (item.getElement() == myElem && item.getLevel() == myLevel) {
				count++;
			}
		}
		
		return count;
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
			return 0xFFFFFFFF;
		}
	}
	
	public static List<EnchantedArmor> getAll() {
		List<EnchantedArmor> list = new LinkedList<>();
		
		for (EMagicElement element : EMagicElement.values())
		if (isArmorElement(element)) {
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
		return new ArmorProperties(1, (double) this.armor / 25.0, Integer.MAX_VALUE);
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
		return this.armor;
	}

	@Override
	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
		stack.damageItem(damage, entity);
	}
	
}
