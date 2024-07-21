package com.smanzana.nostrummagica.item.armor;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;

public class MagicEarthArmor extends MagicArmor {

	public static final String ID_PREFIX = "armor_earth_";
	public static final String ID_HELM_NOVICE = ID_PREFIX + "helm_novice";
	public static final String ID_HELM_ADEPT = ID_PREFIX + "helm_adept";
	public static final String ID_HELM_MASTER = ID_PREFIX + "helm_master";
		
	public static final String ID_CHEST_NOVICE = ID_PREFIX + "chest_novice";
	public static final String ID_CHEST_ADEPT = ID_PREFIX + "chest_adept";
	public static final String ID_CHEST_MASTER = ID_PREFIX + "chest_master";
		
	public static final String ID_LEGS_NOVICE = ID_PREFIX + "legs_novice";
	public static final String ID_LEGS_ADEPT = ID_PREFIX + "legs_adept";
	public static final String ID_LEGS_MASTER = ID_PREFIX + "legs_master";
		
	public static final String ID_FEET_NOVICE = ID_PREFIX + "feet_novice";
	public static final String ID_FEET_ADEPT = ID_PREFIX + "feet_adept";
	public static final String ID_FEET_MASTER = ID_PREFIX + "feet_master";
		
	public MagicEarthArmor(EquipmentSlotType slot, Type type, Item.Properties properties) {
		super(EMagicElement.EARTH, slot, type, properties);
	}
	
}
