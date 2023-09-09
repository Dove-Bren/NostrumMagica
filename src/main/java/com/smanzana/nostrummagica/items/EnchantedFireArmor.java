package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;

public class EnchantedFireArmor extends EnchantedArmor {

	public static final String ID_PREFIX = "armor_fire_";
	public static final String ID_HELM_NOVICE = ID_PREFIX + "helm_novice";
	public static final String ID_HELM_ADEPT = ID_PREFIX + "helm_adept";
	public static final String ID_HELM_MASTER = ID_PREFIX + "helm_master";
	public static final String ID_HELM_TRUE = ID_PREFIX + "helm_true";
	
	public static final String ID_CHEST_NOVICE = ID_PREFIX + "chest_novice";
	public static final String ID_CHEST_ADEPT = ID_PREFIX + "chest_adept";
	public static final String ID_CHEST_MASTER = ID_PREFIX + "chest_master";
	public static final String ID_CHEST_TRUE = ID_PREFIX + "chest_true";
	
	public static final String ID_LEGS_NOVICE = ID_PREFIX + "legs_novice";
	public static final String ID_LEGS_ADEPT = ID_PREFIX + "legs_adept";
	public static final String ID_LEGS_MASTER = ID_PREFIX + "legs_master";
	public static final String ID_LEGS_TRUE = ID_PREFIX + "legs_true";
	
	public static final String ID_FEET_NOVICE = ID_PREFIX + "feet_novice";
	public static final String ID_FEET_ADEPT = ID_PREFIX + "feet_adept";
	public static final String ID_FEET_MASTER = ID_PREFIX + "feet_master";
	public static final String ID_FEET_TRUE = ID_PREFIX + "feet_true";
	
	public EnchantedFireArmor(EquipmentSlotType slot, Type type, Item.Properties properties) {
		super(EMagicElement.FIRE, slot, type, properties);
	}
	
}
