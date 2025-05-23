package com.smanzana.nostrummagica.spell;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.StringTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;

public enum EAlteration {

	INFLICT("Inflict", 5, 1, ReagentType.GRAVE_DUST),
	RESIST("Resist", 10, 1, ReagentType.CRYSTABLOOM),
	SUPPORT("Support", 10, 1, ReagentType.GINSENG),
	GROWTH("Growth", 15, 1, ReagentType.SKY_ASH),
	ENCHANT("Enchant", 15, 1, ReagentType.BLACK_PEARL),
	CONJURE("Conjure", 20, 1, ReagentType.MANDRAKE_ROOT),
	SUMMON("Summon", 30, 2, ReagentType.MANI_DUST),
	RUIN("Ruin", 15, 2, ReagentType.SPIDER_SILK),
	CORRUPT("Corrupt", 15, 1, ReagentType.GRAVE_DUST, () -> new ItemStack(Items.PHANTOM_MEMBRANE)),
	;
	
	private final ResourceLocation glyph;
	private final String name;
	private final int cost;
	private final int weight; // Should be 1+
	private final ReagentType reagent;
	private final Supplier<ItemStack> craftItemSupplier;
	private ItemStack craftItem;
	
	private EAlteration(String base, int cost, int weight, ReagentType reagent) {
		this(base, cost, weight, reagent, () -> ReagentItem.CreateStack(reagent, 1));
	}
	
	private EAlteration(String base, int cost, int weight, ReagentType reagent, Supplier<ItemStack> craftItem) {
		this.name = base;
		this.glyph = new ResourceLocation(NostrumMagica.MODID, base.toLowerCase());
		this.reagent = reagent;
		this.craftItemSupplier = craftItem;
		this.craftItem = ItemStack.EMPTY;
		
		this.cost = cost;
		this.weight = weight;
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
	
	public int getWeight() {
		return this.weight;
	}
	
	/**
	 * Return a list of reagents required.
	 * Both type and count of the itemstacks will be respected.
	 * @return
	 */
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.of(ItemStack.EMPTY, ReagentItem.CreateStack(reagent, 1));
	}
	
	public ItemStack getCraftItem() {
		if (craftItem.isEmpty()) {
			craftItem = craftItemSupplier.get();
		}
		return craftItem;
	}
	
	public Tag toNBT() {
		return StringTag.valueOf(this.name().toLowerCase());
	}
	
	public static final EAlteration FromNBT(Tag nbt) {
		EAlteration alteration = EAlteration.INFLICT;
		try {
			alteration = EAlteration.valueOf(((StringTag) nbt).getAsString().toUpperCase());
		} catch (Exception e) {
			;
		}
		return alteration;
	}
	
}
