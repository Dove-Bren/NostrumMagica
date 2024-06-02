package com.smanzana.nostrummagica.item;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class NostrumResourceCrystal extends BlockItem implements ILoreTagged {

	public static final String ID_CRYSTAL_SMALL = "crystal_small";
	public static final String ID_CRYSTAL_MEDIUM = "crystal_medium";
	public static final String ID_CRYSTAL_LARGE = "crystal_large";
	
	public NostrumResourceCrystal(@Nonnull Block blockToPlace, Item.Properties properties) {
		super(blockToPlace, properties);
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_crystals";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magic Crystals";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Mani crystals are crystaline stores of power.", "You can already tell they will be useful crafting components!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Mani, Kani, and Vani crystals store varying amounts of power.", "The sheer amount of magic energy that radiates off of each can be felt!", "What's more, the gems seem to attract defending Wisps when placed in the world!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
