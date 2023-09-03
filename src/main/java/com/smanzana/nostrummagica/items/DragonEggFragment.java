package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.item.Item;
import net.minecraft.item.Rarity;

public class DragonEggFragment extends Item implements ILoreTagged {

	public static final String ID = "dragon_egg_part";
	
	private DragonEggFragment() {
		super(NostrumItems.PropBase().maxStackSize(3).rarity(Rarity.RARE));
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_egg_part";
	}

	@Override
	public String getLoreDisplayName() {
		return "Dragon Egg Fragment";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("What is this? A fragment of an egg? It seems to contain some small amount of life force still...");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("A fragment of a dragon egg.", "If you combine multiple, you may be able to restore some of its lost life force.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
}
