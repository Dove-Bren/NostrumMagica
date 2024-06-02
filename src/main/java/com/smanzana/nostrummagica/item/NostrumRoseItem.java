package com.smanzana.nostrummagica.item;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.item.Item;
import net.minecraft.item.Rarity;

public class NostrumRoseItem extends Item implements ILoreTagged {

	public static final String ID_BLOOD_ROSE = "rose_blood";
	public static final String ID_ELDRICH_ROSE = "rose_eldrich";
	public static final String ID_PALE_ROSE = "rose_pale";
	
	public NostrumRoseItem() {
		super(NostrumItems.PropLowStack().rarity(Rarity.RARE));
	}
	
//	@Override
//	public String getUnlocalizedName(ItemStack stack) {
//		int i = stack.getMetadata();
//		
//		RoseType type = getTypeFromMeta(i);
//		return "item." + type.getUnlocalizedKey();
//	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_rose_item";
	}

	@Override
	public String getLoreDisplayName() {
		return "Mystic Roses";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("It appears that some golems carry roses full of power... But what are they for?");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Some golems carry Mystic Roses.", "The roses can be used to created items that increase your abilities as a mage.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
