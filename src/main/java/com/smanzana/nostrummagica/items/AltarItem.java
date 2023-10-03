package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.item.BlockItem;

public class AltarItem extends BlockItem implements ILoreTagged {

	public static final String ID = "altar_item";

	public AltarItem() {
		super(NostrumBlocks.altar, NostrumItems.PropLowStack());
	}
	
	@Override
	public String getLoreKey() {
		return "altar_item";
	}

	@Override
	public String getLoreDisplayName() {
		return "Ritual Altar";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Altars can be used to hold items.", "There's probably a better use for them...");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Ritual Altars hold items for display or use in a ritual.", "Only tier III rituals use altars.", "Up to 5 altars can be used in a single ritual.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.RITUALS;
	}
	
}
