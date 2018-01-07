package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.lore.ILoreTagged;
import com.smanzana.nostrummagica.lore.Lore;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlankScroll extends Item implements ILoreTagged {

	private static BlankScroll instance = null;
	
	public static BlankScroll instance() {
		if (instance == null)
			instance = new BlankScroll();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.addShapelessRecipe(new ItemStack(instance),
				ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1),
				Items.PAPER,
				Items.PAPER,
				ReagentItem.instance().getReagent(ReagentType.CRYSTABLOOM, 1));
	}
	
	public static final String id = "blank_scroll";
	
	private BlankScroll() {
		super();
		this.setUnlocalizedName(id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(64);
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
	
}
