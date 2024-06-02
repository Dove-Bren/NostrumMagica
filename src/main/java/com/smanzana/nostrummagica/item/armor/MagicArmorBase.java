package com.smanzana.nostrummagica.item.armor;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;

public class MagicArmorBase extends ArmorItem implements ILoreTagged {

//	public static MagicArmorBase helm() {
//		if (helm == null) {
//			helm = new MagicArmorBase("magichelmbase", EquipmentSlotType.HEAD); 
//		}
//		return helm;
//	}
//	
//	public static MagicArmorBase chest() {
//		if (chest == null) {
//			chest = new MagicArmorBase("magicchestbase", EquipmentSlotType.CHEST); 
//		}
//		return chest;
//	}
//	
//	public static MagicArmorBase legs() {
//		if (legs == null) {
//			legs = new MagicArmorBase("magicleggingsbase", EquipmentSlotType.LEGS); 
//		}
//		return legs;
//	}
//	
//	public static MagicArmorBase feet() {
//		if (feet == null) {
//			feet = new MagicArmorBase("magicfeetbase", EquipmentSlotType.FEET); 
//		}
//		return feet;
//	}
	
	private static final String ID_PREFIX = "magicarmor_";
	public static final String ID_HELM = ID_PREFIX + "helm";
	public static final String ID_CHEST = ID_PREFIX + "chest";
	public static final String ID_LEGS = ID_PREFIX + "legs";
	public static final String ID_FEET = ID_PREFIX + "feet";

	public MagicArmorBase(EquipmentSlotType slot, Item.Properties properties) {
		super(ArmorMaterial.LEATHER, slot, properties.maxDamage(5));
	}
	
//	public String getModelID() {
//		return id;
//	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_magic_armor";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magic Armor";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Wrapping iron armor with Void Crystals makes ethereal armor.", "The armor is incredibly fragile and not effective by itself.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Wrapping iron armor with Void Crystals makes ethereal armor.", "The armor is incredibly fragile and shouldn't be used by itself.", "It can be enchanted to carry an element and provide unique effects.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	public static MagicArmorBase get(EquipmentSlotType slot) {
		switch (slot) {
		case CHEST:
			return NostrumItems.magicArmorBaseChest;
		case FEET:
			return NostrumItems.magicArmorBaseFeet;
		case HEAD:
			return NostrumItems.magicArmorBaseHelm;
		case LEGS:
			return NostrumItems.magicArmorBaseLegs;
		default:
			break;
		}
		
		return null;
	}

}
