package com.smanzana.nostrummagica.spell;

import java.util.List;
import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum EAlteration {

	INFLICT(5, 1, ReagentType.GRAVE_DUST),
	RESIST(10, 1, ReagentType.CRYSTABLOOM),
	SUPPORT(10, 1, ReagentType.GINSENG),
	GROWTH(15, 1, ReagentType.SKY_ASH),
	ENCHANT(15, 1, ReagentType.BLACK_PEARL),
	CONJURE(20, 1, ReagentType.MANDRAKE_ROOT),
	SUMMON(30, 2, ReagentType.MANI_DUST),
	RUIN(15, 2, ReagentType.SPIDER_SILK),
	CORRUPT(15, 1, ReagentType.GRAVE_DUST, () -> new ItemStack(Items.PHANTOM_MEMBRANE)),
	;
	
	private final ResourceLocation glyph;
	private final int cost;
	private final int weight; // Should be 1+
	private final ReagentType reagent;
	private final Supplier<ItemStack> craftItemSupplier;
	private ItemStack craftItem;
	private final Component name;
	private final Component description;
	private final List<Component> tooltip;
	
	private EAlteration(int cost, int weight, ReagentType reagent) {
		this(cost, weight, reagent, () -> ReagentItem.CreateStack(reagent, 1));
	}
	
	private EAlteration(int cost, int weight, ReagentType reagent, Supplier<ItemStack> craftItem) {
		this.glyph = new ResourceLocation(NostrumMagica.MODID, name().toLowerCase());
		this.reagent = reagent;
		this.craftItemSupplier = craftItem;
		this.craftItem = ItemStack.EMPTY;
		
		this.cost = cost;
		this.weight = weight;
		
		this.name = new TranslatableComponent("alteration." + name().toLowerCase() + ".name");
		this.description = new TranslatableComponent("alteration." + name().toLowerCase() + ".desc");
		this.tooltip = List.of(
				name.copy().withStyle(ChatFormatting.BOLD),
				description
			);
	}

	public ResourceLocation getGlyph() {
		return glyph;
	}

	public String getBareName() {
		return name.getString();
	}
	
	public Component getDisplayName() {
		return this.name;
	}
	
	public Component getDescription() {
		return this.description;
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

	public List<Component> getTooltip() {
		return tooltip;
	}
	
}
