package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlankScroll extends Item {

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
	
}
