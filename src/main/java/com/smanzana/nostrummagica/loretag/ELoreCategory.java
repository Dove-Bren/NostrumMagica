package com.smanzana.nostrummagica.loretag;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.item.NostrumItems;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public enum ELoreCategory {

		ITEM(() -> NostrumItems.mageBlade),
		BLOCK(() -> Blocks.GRASS_BLOCK.asItem()),
		ENTITY(() -> Items.SKELETON_SKULL),
		DUNGEON(() -> NostrumBlocks.dungeonBars.asItem()),
		;
	
	private final Supplier<ItemStack> iconSupplier;
	private final Component title;
	
	private ELoreCategory(Supplier<Item> itemSupplier) {
		this(() -> new ItemStack(itemSupplier.get()), 1);
	}
	
	private ELoreCategory(Supplier<ItemStack> iconSupplier, int unused) {
		this.iconSupplier = iconSupplier;
		this.title = new TranslatableComponent("lore_category." + name().toLowerCase() + ".name");
	}

	public ItemStack makeIcon() {
		return iconSupplier.get();
	}
	
	public Component getTitle() {
		return title;
	}
	
}
