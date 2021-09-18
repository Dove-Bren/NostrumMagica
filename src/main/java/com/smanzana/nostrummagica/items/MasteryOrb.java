package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.item.Item;

public class MasteryOrb extends Item implements ILoreTagged {

	private static MasteryOrb instance = null;
	
	public static MasteryOrb instance() {
		if (instance == null)
			instance = new MasteryOrb();
		
		return instance;
	}
	
	public static final String id = "mastery_orb";
	
	private MasteryOrb() {
		super();
		this.setUnlocalizedName(id);
		this.setRegistryName(NostrumMagica.MODID, id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(8);
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
