package com.smanzana.nostrummagica.spells.components;

import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class Transmutation {

	private static Set<Item> items;
	
	private static Set<Block> blocks;
	
	private static boolean initted = false;
	private static final void init() {
		if (initted)
			return;
		
		initted = true;
		
		items = Sets.newLinkedHashSet();
		items.add(Items.BEEF);
		items.add(Items.APPLE);
		items.add(Items.POTATO);
		items.add(Items.IRON_HELMET);
		items.add(Items.ENDER_PEARL);
		items.add(Items.CARROT);
		items.add(Items.BREAD);
		items.add(Items.COMPASS);
		items.add(Items.BRICK);
		items.add(Items.BONE);
		items.add(Items.EMERALD);
		items.add(Items.COAL);
		items.add(Items.EGG);
		items.add(Items.GOLD_INGOT);
		items.add(Items.REDSTONE);
		items.add(Items.BOOK);
		items.add(Items.QUARTZ);
		items.add(Items.CHAINMAIL_CHESTPLATE);
		items.add(Items.NETHER_WART);
		items.add(Items.IRON_INGOT);
		items.add(Items.DIAMOND_AXE);
		items.add(Items.DIAMOND_PICKAXE);
		items.add(Items.MELON_SEEDS);
		items.add(Items.SUGAR_CANE);
		items.add(Items.PRISMARINE_CRYSTALS);
		items.add(Items.BREWING_STAND);
		items.add(Items.ENDER_EYE);
		items.add(Items.DIAMOND);
		items.add(Items.CHAINMAIL_BOOTS);
		items.add(Items.WOODEN_SWORD);
		items.add(Items.GLOWSTONE_DUST);
		items.add(Items.CLAY_BALL);
		items.add(Items.CLOCK);
		items.add(Items.COMPARATOR);
		items.add(Items.COOKIE);
		items.add(Items.EXPERIENCE_BOTTLE);
		items.add(Items.FEATHER);
		items.add(Items.SPIDER_EYE);
		items.add(Items.STRING);
		
		blocks = Sets.newLinkedHashSet();
				
		blocks.add(Blocks.BOOKSHELF);
		blocks.add(Blocks.CACTUS);
		blocks.add(Blocks.COAL_ORE);
		blocks.add(Blocks.END_STONE);
		blocks.add(Blocks.DIRT);
		blocks.add(Blocks.ICE);
		blocks.add(Blocks.NOTE_BLOCK);
		blocks.add(Blocks.NETHERRACK);
		blocks.add(Blocks.SAND);
		blocks.add(Blocks.IRON_BARS);
		blocks.add(Blocks.DROPPER);
		blocks.add(Blocks.MOSSY_COBBLESTONE);
		blocks.add(Blocks.STONE);
		blocks.add(Blocks.NETHERRACK);
		blocks.add(Blocks.OAK_LOG);
		blocks.add(Blocks.PUMPKIN);
		blocks.add(Blocks.NETHER_QUARTZ_ORE);
		blocks.add(Blocks.OAK_PLANKS);
		blocks.add(Blocks.QUARTZ_STAIRS);
		blocks.add(Blocks.OAK_FENCE);
		blocks.add(Blocks.ACACIA_FENCE);
		blocks.add(Blocks.REDSTONE_ORE);
		blocks.add(Blocks.LAPIS_ORE);
		blocks.add(Blocks.CRAFTING_TABLE);
		blocks.add(Blocks.GOLD_ORE);
		blocks.add(Blocks.GRAVEL);
		blocks.add(Blocks.TERRACOTTA);
		blocks.add(Blocks.IRON_ORE);
	}
	
	public static final boolean IsTransmutable(Item item) {
		return GetTransmutationResult(item, 0) != null;
	}
	
	public static final boolean IsTransmutable(Block block) {
		return GetTransmutationResult(block, 0) != null;
	}
	
	/**
	 * 
	 * @param item
	 * @param level 1 through 3 inclusive
	 * @return
	 */
	public static final @Nullable Item GetTransmutationResult(Item item, int level) {
		init();
		
		final @Nullable Item ret;
		if (items.contains(item)) {
			Iterator<Item> it = items.iterator();
			Item next = it.next();
			while (next != item)
				next = it.next();
			
			// Now calculate offset
			int hop = 4 - (level > 3 ? 3 : level);
			for (int i = 0; i < hop; i++) {
				if (!it.hasNext())
					it = items.iterator();
				next = it.next();
			}
			
			ret = next;
		} else {
			// Try to go through blocks and see if it's in there
			Iterator<Block> it = blocks.iterator();
			Block next = it.next();
			while (next.asItem() != item) { // TODO this only works for vanilla? That ok?
				if (!it.hasNext()) {
					next = null;
					break;
				}
				next = it.next();
			}
			
			if (next != null) {
				// Now calculate offset
				int hop = 4 - (level > 3 ? 3 : level);
				for (int i = 0; i < hop; i++) {
					if (!it.hasNext())
						it = blocks.iterator();
					next = it.next();
				}
				
				ret = next.asItem();
			} else {
				ret = null;
			}
		}
		
		return ret;
	}
	
	/**
	 * 
	 * @param block
	 * @param level 1 through 3 inclusive
	 * @return
	 */
	public static final @Nullable Block GetTransmutationResult(Block block, int level) {
		init();
		final Block ret;
		
		Iterator<Block> it = blocks.iterator();
		Block next = it.next();
		while (next != block) { // TODO this only works for vanilla? That ok?
			if (!it.hasNext()) {
				next = null;
				break;
			}
			next = it.next();
		}
		
		if (next != null) {
			// Now calculate offset
			int hop = 4 - (level > 3 ? 3 : level);
			for (int i = 0; i < hop; i++) {
				if (!it.hasNext())
					it = blocks.iterator();
				next = it.next();
			}
			ret = next;
		} else {
			ret = null;
		}
		
		return ret;
	}
	
}
