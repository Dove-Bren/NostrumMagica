package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.MinecraftForge;

public class MagicArmorBase extends ItemArmor implements ILoreTagged {

	public static MagicArmorBase helm;
	public static MagicArmorBase chest;
	public static MagicArmorBase legs;
	public static MagicArmorBase feet;
	
	public static void init() {
		helm = new MagicArmorBase("magichelmbase", EntityEquipmentSlot.HEAD);
		chest = new MagicArmorBase("magicchestbase", EntityEquipmentSlot.CHEST);
		legs = new MagicArmorBase("magicleggingsbase", EntityEquipmentSlot.LEGS);
		feet = new MagicArmorBase("magicfeetbase", EntityEquipmentSlot.FEET);
	}
		
	private String id;

	public MagicArmorBase(String id, EntityEquipmentSlot slot) {
		super(ArmorMaterial.LEATHER, 0, slot);
		this.setUnlocalizedName(id);
		this.setMaxDamage(5);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.id = id;
		MinecraftForge.EVENT_BUS.register(this);
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

}
