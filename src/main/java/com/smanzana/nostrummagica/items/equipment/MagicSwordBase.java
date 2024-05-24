package com.smanzana.nostrummagica.items.equipment;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;

public class MagicSwordBase extends SwordItem implements ILoreTagged {

	public static final String ID = "magicswordbase";
	
	public MagicSwordBase() {
		super(ItemTier.WOOD, 1, -2.4F, NostrumItems.PropEquipment().maxDamage(5));
	}
	
	public String getModelID() {
		return "magicswordbase";
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_magic_weapon";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magic Weapons";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Inserting void crystals into an iron sword creates an Ethereal sword.", "Until you find a way to imbue it with the power of an element, it's largely useless.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Inserting void crystals into an iron sword creates an Ethereal sword.", "Casting an enchantment on it provides unique effects.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}

}
