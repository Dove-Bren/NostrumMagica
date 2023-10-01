package com.smanzana.nostrummagica.spells;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public enum EAlteration {

	INFLICT("Inflict", 10, ReagentType.GRAVE_DUST),
	RESIST("Resist", 30, ReagentType.CRYSTABLOOM),
	SUPPORT("Support", 15, ReagentType.GINSENG),
	GROWTH("Growth", 25, ReagentType.SKY_ASH),
	ENCHANT("Enchant", 40, ReagentType.BLACK_PEARL),
	CONJURE("Conjure", 30, ReagentType.MANDRAKE_ROOT),
	SUMMON("Summon", 50, ReagentType.MANI_DUST),
	RUIN("Ruin", 80, ReagentType.SPIDER_SILK);
	
	private ResourceLocation glyph;
	private String name;
	private int cost;
	private ReagentType reagent;
	
	private EAlteration(String base, int cost, ReagentType reagent) {
		this.name = base;
		this.glyph = new ResourceLocation(NostrumMagica.MODID, base.toLowerCase());
		this.reagent = reagent;
	}

	public ResourceLocation getGlyph() {
		return glyph;
	}

	public String getName() {
		return name;
	}
	
	public int getCost() {
		return cost;
	}
	
	/**
	 * Return a list of reagents required.
	 * Both type and count of the itemstacks will be respected.
	 * @return
	 */
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY, ReagentItem.CreateStack(reagent, 1));
	}
	
}
