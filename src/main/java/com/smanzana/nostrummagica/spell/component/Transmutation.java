package com.smanzana.nostrummagica.spell.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class Transmutation {

	private static List<ItemTransmutationSource> items;
	
	private static List<BlockTransmutationSource> blocks;
	
	private static long inittedSeed = -1;
	private static final void init(long seed) {
		if (inittedSeed == seed && items != null && blocks != null) // check lists are there, as -1 (default) is a valid seed too!
			return;
		
		NostrumMagica.logger.info("Creating transmutation list for seed " + seed);
		inittedSeed = seed;
		
//		items = new ArrayList<>(NostrumTags.Items.TransmutableItem.getAllElements());
//		
		
//		blocks = new ArrayList<>(NostrumTags.Blocks.TransmutableBlock.getAllElements());
		
		
//		blocks = Sets.newLinkedHashSet();
		blocks = new ArrayList<>();
		blocks.add(new BlockTransmutationSource(Blocks.GRASS_BLOCK));
		blocks.add(new BlockTransmutationSource(Blocks.BOOKSHELF));
		blocks.add(new BlockTransmutationSource(Blocks.CACTUS));
		blocks.add(new BlockTransmutationSource(Blocks.COAL_ORE));
		blocks.add(new BlockTransmutationSource(Blocks.END_STONE));
		blocks.add(new BlockTransmutationSource(Blocks.DIRT));
		blocks.add(new BlockTransmutationSource(Blocks.ICE));
		blocks.add(new BlockTransmutationSource(Blocks.NOTE_BLOCK));
		blocks.add(new BlockTransmutationSource(Blocks.SAND));
		blocks.add(new BlockTransmutationSource(Blocks.IRON_BARS));
		blocks.add(new BlockTransmutationSource(Blocks.DROPPER));
		blocks.add(new BlockTransmutationSource(Blocks.MOSSY_COBBLESTONE));
		blocks.add(new BlockTransmutationSource(Blocks.STONE));
		blocks.add(new BlockTransmutationSource(Blocks.NETHERRACK));
		blocks.add(new BlockTransmutationSource(Blocks.OAK_LOG));
		blocks.add(new BlockTransmutationSource(Blocks.PUMPKIN));
		blocks.add(new BlockTransmutationSource(Blocks.NETHER_QUARTZ_ORE));
		blocks.add(new BlockTransmutationSource(Blocks.OAK_PLANKS));
		blocks.add(new BlockTransmutationSource(Blocks.QUARTZ_STAIRS));
		blocks.add(new BlockTransmutationSource(Blocks.OAK_FENCE));
		blocks.add(new BlockTransmutationSource(Blocks.ACACIA_FENCE));
		blocks.add(new BlockTransmutationSource(Blocks.REDSTONE_ORE));
		blocks.add(new BlockTransmutationSource(Blocks.GRASS));
		blocks.add(new BlockTransmutationSource(Blocks.LAPIS_ORE));
		blocks.add(new BlockTransmutationSource(Blocks.CRAFTING_TABLE));
		blocks.add(new BlockTransmutationSource(Blocks.GOLD_ORE));
		blocks.add(new BlockTransmutationSource(Blocks.GRAVEL));
		blocks.add(new BlockTransmutationSource(Blocks.TERRACOTTA));
		blocks.add(new BlockTransmutationSource(Blocks.IRON_ORE));
		blocks.add(new BlockTransmutationSource(Blocks.BLACKSTONE));
		blocks.add(new BlockTransmutationSource(Blocks.POLISHED_BLACKSTONE));
		blocks.add(new BlockTransmutationSource(Blocks.NETHER_GOLD_ORE));
		blocks.add(new BlockTransmutationSource(Blocks.WARPED_PLANKS));
		blocks.add(new BlockTransmutationSource(Blocks.WARPED_STEM));
		blocks.add(new BlockTransmutationSource(Blocks.ACACIA_LOG));
		blocks.add(new BlockTransmutationSource(Blocks.DARK_OAK_LOG));
		blocks.add(new BlockTransmutationSource(Blocks.SPRUCE_LOG));
		blocks.add(new BlockTransmutationSource(Blocks.JUNGLE_LOG));
		blocks.add(new BlockTransmutationSource(Blocks.BIRCH_LOG));
		blocks.add(new BlockTransmutationSource(Blocks.STRIPPED_OAK_LOG));
		blocks.add(new BlockTransmutationSource(Blocks.RED_NETHER_BRICKS));
		blocks.add(new BlockTransmutationSource(Blocks.SOUL_SAND));
		blocks.add(new BlockTransmutationSource(Blocks.SOUL_SOIL));
		blocks.add(new BlockTransmutationSource(Blocks.HONEYCOMB_BLOCK));
		

		items = new ArrayList<>();
		items.add(new ItemTransmutationSource(Items.BEEF));
		items.add(new ItemTransmutationSource(Items.APPLE));
		items.add(new ItemTransmutationSource(Items.POTATO));
		items.add(new ItemTransmutationSource(Items.IRON_HELMET));
		items.add(new ItemTransmutationSource(Items.ENDER_PEARL));
		items.add(new ItemTransmutationSource(Items.CARROT));
		items.add(new ItemTransmutationSource(Items.BREAD));
		items.add(new ItemTransmutationSource(Items.COMPASS));
		items.add(new ItemTransmutationSource(Items.BRICK));
		items.add(new ItemTransmutationSource(Items.BONE));
		items.add(new ItemTransmutationSource(Items.EMERALD));
		items.add(new ItemTransmutationSource(Items.COAL));
		items.add(new ItemTransmutationSource(Items.EGG));
		items.add(new ItemTransmutationSource(Items.GOLD_INGOT));
		items.add(new ItemTransmutationSource(Items.REDSTONE));
		items.add(new ItemTransmutationSource(Items.BOOK));
		items.add(new ItemTransmutationSource(Items.QUARTZ));
		items.add(new ItemTransmutationSource(Items.CHAINMAIL_CHESTPLATE));
		items.add(new ItemTransmutationSource(Items.NETHER_WART));
		items.add(new ItemTransmutationSource(Items.IRON_INGOT));
		items.add(new ItemTransmutationSource(Items.DIAMOND_AXE));
		items.add(new ItemTransmutationSource(Items.DIAMOND_PICKAXE));
		items.add(new ItemTransmutationSource(Items.MELON_SEEDS));
		items.add(new ItemTransmutationSource(Items.SUGAR_CANE));
		items.add(new ItemTransmutationSource(Items.PRISMARINE_CRYSTALS));
		items.add(new ItemTransmutationSource(Items.BREWING_STAND));
		items.add(new ItemTransmutationSource(Items.ENDER_EYE));
		items.add(new ItemTransmutationSource(Items.DIAMOND));
		items.add(new ItemTransmutationSource(Items.CHAINMAIL_BOOTS));
		items.add(new ItemTransmutationSource(Items.WOODEN_SWORD));
		items.add(new ItemTransmutationSource(Items.GLOWSTONE_DUST));
		items.add(new ItemTransmutationSource(Items.CLAY_BALL));
		items.add(new ItemTransmutationSource(Items.CLOCK));
		items.add(new ItemTransmutationSource(Items.COMPARATOR));
		items.add(new ItemTransmutationSource(Items.COOKIE));
		items.add(new ItemTransmutationSource(Items.EXPERIENCE_BOTTLE));
		items.add(new ItemTransmutationSource(Items.FEATHER));
		items.add(new ItemTransmutationSource(Items.SPIDER_EYE));
		items.add(new ItemTransmutationSource(Items.STRING));
		items.add(new ItemTransmutationSource(Items.GOLD_NUGGET));
		items.add(new ItemTransmutationSource(Items.IRON_NUGGET));
		items.add(new ItemTransmutationSource(Items.HONEY_BOTTLE));
		items.add(new ItemTransmutationSource(Items.HONEYCOMB));
		
		for (BlockTransmutationSource blockSource : blocks) {
			items.add(new ItemTransmutationSource(blockSource.block.asItem()));
		}
		
		final Random rand = new Random(seed);
		Collections.shuffle(items, rand);
		
		rand.setSeed(seed); // Set back for consistent results regardless of item list size
		Collections.shuffle(blocks, rand);
	}
	
	public static final boolean IsTransmutable(Item item) {
		return GetTransmutationResult(item, 0).valid;
	}
	
	public static final boolean IsTransmutable(Block block) {
		return GetTransmutationResult(block, 0).valid;
	}
	
	protected static final long GetSeedToUse() {
		// I want the transmute list to be based on seed, but by the time we're generating recipes
		// and registering JEI there isn't a world.
		// I had some success just forcing JEI to refresh but it doesn't sync well with multiplayer.
		// So just use a hardcoded seed for now.
		return 0x54311233;
		
//		// If dedicated server, use loaded world seed if there is one.
//		if (NostrumMagica.instance.proxy.getPlayer() != null) {
//			World world = NostrumMagica.instance.proxy.getPlayer().world;
//			if (world == null || world.getServer() == null) {
//				return -1;
//			}
//			return world.getServer().getWorld(World.OVERWORLD).getSeed(); // Not sure if seed is always the same? might change CLIENT list per dimension? lol
//		} else {
//			try {
//				long seed = ServerLifecycleHooks.getCurrentServer().getWorld(World.OVERWORLD).getSeed();
//				return seed;
//			} catch (Exception e) {
//				e.printStackTrace();
//				NostrumMagica.logger.error("Failed to find world seed for generating transmutations");
//			}
//		}
//		
//		return -1;
	}
	
	protected static final @Nullable ItemTransmutationSource getItemSource(Item item) {
		init(GetSeedToUse());
		
		for (ItemTransmutationSource source : items) {
			if (source.item == item) {
				return source;
			}
		}
		
		return null;
	}
	
	protected static final @Nullable BlockTransmutationSource getBlockSource(Block block) {
		init(GetSeedToUse());
		
		for (BlockTransmutationSource source : blocks) {
			if (source.block == block) {
				return source;
			}
		}
		
		return null;
	}
	
	protected static final @Nonnull ItemTransmutationSource getResult(ItemTransmutationSource source, int level) {
		final ItemTransmutationSource ret;
		
		Iterator<ItemTransmutationSource> it = items.iterator();
		ItemTransmutationSource next = null;
		while (it.hasNext()) {
			next = it.next();
			
			if (next == source) {
				break;
			}
		}
		
		if (next == source) {
			// Found it
			
			// Now calculate offset
			int hop = 4 - (level > 3 ? 3 : level);
			for (int i = 0; i < hop; i++) {
				if (!it.hasNext())
					it = items.iterator();
				next = it.next();
			}
			
			ret = next;
		} else {
			// Didn't find it
			NostrumMagica.logger.warn("Failed to find item source in item list: " + source.getName());
			ret = source;
		}
		
		return ret;
	}
	
	protected static final @Nonnull BlockTransmutationSource getResult(BlockTransmutationSource source, int level) {
		final BlockTransmutationSource ret;
		
		Iterator<BlockTransmutationSource> it = blocks.iterator();
		BlockTransmutationSource next = null;
		while (it.hasNext()) {
			next = it.next();
			
			if (next == source) {
				break;
			}
		}
		
		if (next == source) {
			// Found it
			
			// Now calculate offset
			int hop = 4 - (level > 3 ? 3 : level);
			for (int i = 0; i < hop; i++) {
				if (!it.hasNext())
					it = blocks.iterator();
				next = it.next();
			}
			
			ret = next;
		} else {
			// Didn't find it
			NostrumMagica.logger.warn("Failed to find item source in block list: " + source.getName());
			ret = source;
		}
		
		return ret;
	}
	
	/**
	 * 
	 * @param item
	 * @param level 1 through 3 inclusive
	 * @return
	 */
	public static final TransmuteResult<Item> GetTransmutationResult(Item item, int level) {
		final @Nullable Item ret;
		final @Nullable TransmutationSource source;
		
		final @Nullable ItemTransmutationSource itemSource = getItemSource(item);
		if (itemSource != null) {
			source = itemSource;
			ret = itemSource.findResultItem(level);
		} else {
			source = null;
			ret = null;
		}
		
		if (ret == null) {
			return new TransmuteResult<>();
		} else {
			return new TransmuteResult<>(ret, source, level);
		}
	}
	
	/**
	 * 
	 * @param block
	 * @param level 1 through 3 inclusive
	 * @return
	 */
	public static final TransmuteResult<Block> GetTransmutationResult(Block block, int level) {
		final @Nullable Block ret;
		final @Nullable TransmutationSource source;
		
		final @Nullable BlockTransmutationSource blockSource = getBlockSource(block);
		
		if (blockSource != null) {
			source = blockSource;
			ret = blockSource.findResultBlock(level);
		} else {
			source = null;
			ret = null;
		}
		
		if (ret == null) {
			return new TransmuteResult<>();
		} else {
			return new TransmuteResult<>(ret, source, level);
		}
	}
	
	public static final class TransmuteResult<T> {
		public final boolean valid;
		public final T output;
		public final TransmutationSource source;
		public final int level;
		
		public TransmuteResult() {
			this.valid = false;
			this.output = null;
			this.source = null;
			this.level = 0;
		}
		
		public TransmuteResult(T output, TransmutationSource source, int level) {
			this.valid = true;
			this.output = output;
			this.source = source;
			this.level = level;
		}
	}
	
	public static final class TransmutationRecipe {
		
		private final ItemStack output;
		private final TransmutationSource source;
		private final boolean revealedRecipe;
		private final int jumpLevel;
		
		public TransmutationRecipe(ItemStack output, TransmutationSource source, int jumpLevel, boolean isRevealed) {
			this.output = output;
			this.source = source;
			this.jumpLevel = jumpLevel;
			this.revealedRecipe = isRevealed;
		}
		
		public @Nonnull ItemStack getOutput() {
			return output;
		}
		
		public @Nullable Ingredient getRevealedIngredient() {
			return this.revealedRecipe ? source.getRevealedIngredient() : null;
		}
		
		public @Nullable TransmutationSource getSource() {
			return source;
		}
		
		public int getLevel() {
			return this.jumpLevel;
		}
		
		public boolean isRevealed(@Nullable Player player) {
			if (player == null) {
				return !this.revealedRecipe; // If no player, default to only the non-revealed recipes
			}
			
			return this.revealedRecipe == source.isRevealed(player, this.jumpLevel);
		}
		
		public static final List<TransmutationRecipe> GetItemRecipes() {
			init(GetSeedToUse());
			
			// Get all sources first
			List<ItemTransmutationSource> sources = TransmutationSource.GetItems();
			
			// For each source, create six recipes:
			// A revealed and unrevealed one for all three jump levels
			List<TransmutationRecipe> recipes = new ArrayList<>(sources.size() * 6);
			for (TransmutationSource source : sources) {
				for (int i = 1; i <= 3; i++) {
					final ItemStack result = source.findResult(i);
					
					recipes.add(new TransmutationRecipe(result, source, i, true));
					recipes.add(new TransmutationRecipe(result, source, i, false));
				}
			}
			
			return recipes;
		}
		
		public static final List<TransmutationRecipe> GetBlocksRecipes() {
			init(GetSeedToUse());
			
			// Get all sources first
			List<BlockTransmutationSource> sources = TransmutationSource.GetBlocks();
			
			// For each source, create six recipes:
			// A revealed and unrevealed one for all three jump levels
			List<TransmutationRecipe> recipes = new ArrayList<>(sources.size() * 6);
			for (BlockTransmutationSource source : sources) {
				for (int i = 1; i <= 3; i++) {
					final ItemStack result = source.findResult(i);
					
					recipes.add(new TransmutationRecipe(result, source, i, true));
					recipes.add(new TransmutationRecipe(result, source, i, false));
				}
			}
			
			return recipes;
		}
		
	}
	
	public abstract static class TransmutationSource {
		
		protected TransmutationSource() {
			
		}
		
		public boolean isRevealed(@Nonnull Player player, int level) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr != null) {
				return attr.hasTransmuteKnowledge(this.getName(), level);
			}
			
			return false;
		}
		
		public abstract String getName();
		
		public abstract TransmutationSource copy();
		
		public abstract @Nonnull Ingredient getRevealedIngredient();
		
		public ItemStack findResult(int jumpLevel) {
			return new ItemStack(findResultItem(jumpLevel));
		}
		public abstract Item findResultItem(int jumpLevel);
		
		public static final List<ItemTransmutationSource> GetItems() {
			init(GetSeedToUse());
			
			List<ItemTransmutationSource> sources = new ArrayList<>(128);
			for (ItemTransmutationSource item : Transmutation.items) {
				sources.add(item);
			}
			
			return sources;
		}
		
		public static final List<BlockTransmutationSource> GetBlocks() {
			init(GetSeedToUse());
			
			List<BlockTransmutationSource> sources = new ArrayList<>(128);
			for (BlockTransmutationSource item : Transmutation.blocks) {
				sources.add(item);
			}
			
			return sources;
		}
		
		public static final List<TransmutationSource> GetAll() {
			init(GetSeedToUse());
			
			List<TransmutationSource> sources = new ArrayList<>(128);
			for (ItemTransmutationSource item : Transmutation.items) {
				sources.add(item);
			}
			for (BlockTransmutationSource item : Transmutation.blocks) {
				sources.add(item);
			}
			
			return sources;
		}
	}
	
	protected static class ItemTransmutationSource extends TransmutationSource {
		
		private final Item item;
		private final Ingredient inputIngredient;
		
		public ItemTransmutationSource(Item item) {
			super();
			this.item = item;
			this.inputIngredient = Ingredient.of(item);
		}

		@Override
		public String getName() {
			return "transmute_source.item." + item.getRegistryName().getNamespace() + "." + item.getRegistryName().getPath();
		}

		@Override
		public TransmutationSource copy() {
			return new ItemTransmutationSource(this.item);
		}

		@Override
		public Ingredient getRevealedIngredient() {
			return inputIngredient;
		}
		
		@Override
		public Item findResultItem(int level) {
			init(GetSeedToUse());
			
			return Transmutation.getResult(this, level).item;
		}

	}
	
	protected static class BlockTransmutationSource extends TransmutationSource {
		
		private final Block block;
		private final Ingredient inputIngredient;
		
		public BlockTransmutationSource(Block block) {
			super();
			this.block = block;
			this.inputIngredient = Ingredient.of(block);
		}

		@Override
		public String getName() {
			return "transmute_source.block." + block.getRegistryName().getNamespace() + "." + block.getRegistryName().getPath();
		}

		@Override
		public TransmutationSource copy() {
			return new BlockTransmutationSource(this.block);
		}

		@Override
		public Ingredient getRevealedIngredient() {
			return inputIngredient;
		}
		
		@Override
		public Item findResultItem(int jumpLevel) {
			return findResultBlock(jumpLevel).asItem();
		}
		
		public Block findResultBlock(int jumpLevel) {
			init(GetSeedToUse());
			
			return Transmutation.getResult(this, jumpLevel).block;
		}
		
	}
		
}
