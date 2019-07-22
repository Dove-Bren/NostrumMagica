package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.item.Item;

public class DragonEggFragment extends Item implements ILoreTagged {

	private static DragonEggFragment instance = null;
	
	public static DragonEggFragment instance() {
		if (instance == null)
			instance = new DragonEggFragment();
		
		return instance;
	}
	
	public static void init() {
		;
	}
	
	public static final String id = "dragon_egg_part";
	
	private DragonEggFragment() {
		super();
		this.setUnlocalizedName(id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(3);
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
