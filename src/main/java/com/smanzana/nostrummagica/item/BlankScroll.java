package com.smanzana.nostrummagica.item;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.world.item.Item;

public class BlankScroll extends Item implements ILoreTagged {

	public static final String ID = "blank_scroll";
	
	public BlankScroll() {
		super(NostrumItems.PropBase());
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_blank_scroll";
	}

	@Override
	public String getLoreDisplayName() {
		return "Blank Scroll";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Blank scrolls are made by taking paper and sprinkling ground up magical reagents on them.", "If you had a table, some runes, and the reagents, you might be able to put the scroll to good use...");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Blank scrolls are used to make spells.", "By arranging runes and reagents on a Spell table, spells can be drafted onto the scroll to create a Spell Scroll.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
}
