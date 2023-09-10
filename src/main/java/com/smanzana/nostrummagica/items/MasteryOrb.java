package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.item.Item;
import net.minecraft.item.Rarity;

public class MasteryOrb extends Item implements ILoreTagged {

	public static final String ID = "mastery_orb";
	
	public MasteryOrb() {
		super(NostrumItems.PropLowStack().rarity(Rarity.RARE));
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_mastery_orb";
	}

	@Override
	public String getLoreDisplayName() {
		return "Mastery Orb";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("These orbs seem to be filled with some sort of unaspected potency.", "You can't quite deduce what they could be used for...");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("These orbs are filled with a large amount of unaspected potency.", "By offering them up at Elemental shrines, you can raise your mastery of the elements.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_SPELLS;
	}
	
}
