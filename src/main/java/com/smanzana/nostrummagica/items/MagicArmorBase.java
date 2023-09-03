package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;

public class MagicArmorBase extends ItemArmor implements ILoreTagged {

	private static MagicArmorBase helm;
	private static MagicArmorBase chest;
	private static MagicArmorBase legs;
	private static MagicArmorBase feet;
	
	public static MagicArmorBase helm() {
		if (helm == null) {
			helm = new MagicArmorBase("magichelmbase", EquipmentSlotType.HEAD); 
		}
		return helm;
	}
	
	public static MagicArmorBase chest() {
		if (chest == null) {
			chest = new MagicArmorBase("magicchestbase", EquipmentSlotType.CHEST); 
		}
		return chest;
	}
	
	public static MagicArmorBase legs() {
		if (legs == null) {
			legs = new MagicArmorBase("magicleggingsbase", EquipmentSlotType.LEGS); 
		}
		return legs;
	}
	
	public static MagicArmorBase feet() {
		if (feet == null) {
			feet = new MagicArmorBase("magicfeetbase", EquipmentSlotType.FEET); 
		}
		return feet;
	}
	
	private String id;

	public MagicArmorBase(String id, EquipmentSlotType slot) {
		super(ArmorMaterial.LEATHER, 0, slot);
		this.setUnlocalizedName(id);
		this.setRegistryName(new ResourceLocation(NostrumMagica.MODID, id));
		this.setMaxDamage(5);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.id = id;
	}
	
	public String getModelID() {
		return id;
	}
	
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
			return chest;
		case FEET:
			return feet;
		case HEAD:
			return helm;
		case LEGS:
			return legs;
		default:
			break;
		}
		
		return null;
	}

}
