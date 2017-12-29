package com.smanzana.nostrummagica.items;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellAction;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class EnchantedArmor extends ItemArmor implements EnchantedEquipment {

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
							EnchantedArmor armor =  new EnchantedArmor(location.getResourcePath(), slot, element, i + 1);
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
	private static final UUID[] ARMOR_MODIFIERS = new UUID[] {UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};
	
	private static int calcArmor(EntityEquipmentSlot slot, EMagicElement element, int level) {
		
		float mod;
		
		switch (element) {
		case EARTH:
			mod = 0.8f;
			break;
		case ENDER:
			mod = 0.9f;
			break;
		case FIRE:
			mod = 1.15f;
			break;
		case PHYSICAL:
			mod = 1.5f;
			break;
		default:
			mod = 1.0f;
		}
		
		int base;
		
		switch (slot) {
		case CHEST:
			base = 6;
			break;
		case FEET:
			base = 2;
			break;
		case HEAD:
			base = 2;
			break;
		case LEGS:
			base = 5;
			break;
		default:
			base = 0;
		}
		
		if (base != 0)
			base += Math.pow(2, level);
		
		return (int) ((float) base * mod);
	}
	
	private int level;
	private int armor; // Can't use vanilla; it's final
	private EMagicElement element;
	
	private String modelID;
	
	public EnchantedArmor(String modelID, EntityEquipmentSlot type, EMagicElement element, int level) {
		super(ArmorMaterial.IRON, 0, type);
		
		this.level = level;
		this.element = element;
		this.modelID = modelID;
		
		this.setCreativeTab(NostrumMagica.creativeTab);
		
		this.armor = calcArmor(type, element, level);
	}
	
	@Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

        if (equipmentSlot == this.armorType)
        {
            multimap.put(SharedMonsterAttributes.ARMOR.getAttributeUnlocalizedName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor modifier", (double)this.armor, 0));
            multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getAttributeUnlocalizedName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor toughness", 1, 0));
        }

        return multimap;
    }
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }

	@Override
	public SpellAction getTriggerAction(EntityLivingBase user, boolean offense) {
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
	public boolean shouldTrigger(boolean offense) {
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
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		if (!(stack.getItem() instanceof EnchantedArmor)) {
			return null;
		}
		
		if (type != null && type.equalsIgnoreCase("overlay")) {
			return NostrumMagica.MODID + ":textures/models/armor/none.png";
		}
		
		return NostrumMagica.MODID + ":textures/models/armor/magic" + level + "_layer_" + (slot == EntityEquipmentSlot.LEGS ? 2 : 1) + ".png"; 
	}
	
	@Override
	public boolean hasOverlay(ItemStack item) {
		return true;
	}
	
	@Override
	public int getColor(ItemStack stack) {
		switch (element) {
		case EARTH:
			return 0xFF874A0D;
		case ENDER:
			return 0xFF3D003D;
		case FIRE:
			return 0xFFAD1F00;
		case ICE:
			return 0xFF6ED1EA;
		case LIGHTNING:
			return 0xFFDBE045;
		case PHYSICAL:
			return 0xFF91917F;
		case WIND:
			return 0xFF4C8E29;
		}
		
		return 0xFF00FF00;
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
	
}
