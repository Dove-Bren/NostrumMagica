package com.smanzana.nostrummagica.items.armor;

import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class MagicLightningArmor extends MagicArmor {

	public static final String ID_PREFIX = "armor_lightning_";
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
	
	public MagicLightningArmor(EquipmentSlotType slot, Type type, Item.Properties properties) {
		super(EMagicElement.LIGHTNING, slot, type, properties);
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			items.add(new ItemStack(this));
			
			// Add an upgraded copy of true chestplates
			if (this.slot == EquipmentSlotType.CHEST && this.getType() == Type.TRUE) {
				ItemStack stack = new ItemStack(this);
				MagicArmor.SetHasWingUpgrade(stack, true);
				items.add(stack);
			}
		}
	}
	
}
