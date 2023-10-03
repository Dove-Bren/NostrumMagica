package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.item.BlockItem;

public class SpellTableItem extends BlockItem implements ILoreTagged {

	public static final String ID = "spell_table";

	public SpellTableItem() {
		super(NostrumBlocks.spellTable, NostrumItems.PropBase());
	}
	
	// TODO why does mirror and this have lore? Should put on block...
	
	@Override
	public String getLoreKey() {
		return "nostrum_spell_table";
	}

	@Override
	public String getLoreDisplayName() {
		return "Spell Table";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Spell Tables are used to create spells.", "Combine spell runes, blank scrolls, and reagents to create Spell Scrolls.", "Spells must begin with a trigger. After that, any triggers or shapes afterwards can be used.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Spell Tables are used to create spells.", "Combine spell runes, blank scrolls, and reagents to create Spell Scrolls.", "Spells must begin with a trigger. After that, any triggers or shapes afterwards can be used.", "Reagents must be slotted into the table in order to be used.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_SPELLS;
	}
}
