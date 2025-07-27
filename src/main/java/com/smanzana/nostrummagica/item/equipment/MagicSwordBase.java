package com.smanzana.nostrummagica.item.equipment;

import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class MagicSwordBase extends SwordItem implements ILoreTagged {

	public static final String ID = "magicswordbase";
	
	public MagicSwordBase() {
		super(Tiers.WOOD, 1, -2.4F, NostrumItems.PropEquipment().durability(5));
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
	public ELoreCategory getCategory() {
		return ELoreCategory.ITEM;
	}

}
