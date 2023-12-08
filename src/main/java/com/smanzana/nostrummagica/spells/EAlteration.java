package com.smanzana.nostrummagica.spells;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
	RUIN("Ruin", 80, ReagentType.SPIDER_SILK),
	CORRUPT("Corrupt", 45, ReagentType.GRAVE_DUST, () -> new ItemStack(Items.PHANTOM_MEMBRANE)),
	;
	
	private final ResourceLocation glyph;
	private final String name;
	private final int cost;
	private final ReagentType reagent;
	private final Supplier<ItemStack> craftItemSupplier;
	private ItemStack craftItem;
	
	private EAlteration(String base, int cost, ReagentType reagent) {
		this(base, cost, reagent, () -> ReagentItem.CreateStack(reagent, 1));
	}
	
	private EAlteration(String base, int cost, ReagentType reagent, Supplier<ItemStack> craftItem) {
		this.name = base;
		this.glyph = new ResourceLocation(NostrumMagica.MODID, base.toLowerCase());
		this.reagent = reagent;
		this.craftItemSupplier = craftItem;
		this.craftItem = ItemStack.EMPTY;
		
		this.cost = cost; // This used to always be 0! Whoops!
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
	
	public ItemStack getCraftItem() {
		if (craftItem.isEmpty()) {
			craftItem = craftItemSupplier.get();
		}
		return craftItem;
	}
	
}
