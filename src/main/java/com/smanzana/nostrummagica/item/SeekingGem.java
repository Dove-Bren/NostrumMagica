package com.smanzana.nostrummagica.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.autodungeons.world.dungeon.DungeonRecord;
import com.smanzana.nostrummagica.block.dungeon.DungeonKeyChestBlock;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.DungeonKeyChestTileEntity;
import com.smanzana.nostrummagica.util.WorldUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.World;

/**
 * Solidified position crystal for obelisk linking
 * @author Skyler
 *
 */
public class SeekingGem extends Item implements ILoreTagged {
	
	public static interface IChestFilter {
		public boolean test(World world, BlockPos pos, BlockState state);
	}
	
	protected static final IChestFilter IS_DUNGEONCHEST = (world, pos, state) -> state.getBlock() instanceof DungeonKeyChestBlock && !state.get(DungeonKeyChestBlock.OPEN);
	
	public static @Nullable BlockPos FindKeyChest(World world, MutableBoundingBox bounds, BlockPos center, @Nullable IChestFilter chestFilterIn) {
		final IChestFilter chestFilter = chestFilterIn != null
				? chestFilterIn
				: IS_DUNGEONCHEST; 
		
		final BlockPos min = new BlockPos(bounds.minX, bounds.minY, bounds.minZ);
		final BlockPos max = new BlockPos(bounds.maxX, bounds.maxY, bounds.maxZ);
		List<BlockPos> matches = new ArrayList<>();
		WorldUtil.ScanBlocks(world, min, max, (worldIn, pos) -> {
			BlockState state = world.getBlockState(pos);
			if (chestFilter.test(world, pos, state)) {
				matches.add(pos);
			}
			return true;
		});
		
		BlockPos closest = null;
		int minLength = Integer.MAX_VALUE;
		for (BlockPos candidate : matches) {
			final int dist = center.manhattanDistance(candidate);
			if (dist < minLength) {
				closest = center;
				minLength = dist;
			}
		}
		
		return closest;
	}

	public static final String ID = "seeking_gem";
	
	public SeekingGem(Item.Properties props) {
		super(props);
	}
	
	protected @Nullable BlockPos attemptDungeonSeek(PlayerEntity playerIn, World worldIn, ItemStack gem, DungeonRecord dungeon) {
		if (dungeon != null && dungeon.currentRoom != null) {
			return FindKeyChest(worldIn, dungeon.currentRoom.getBounds(), playerIn.getPosition(), (world, pos, state) -> {
				if (!IS_DUNGEONCHEST.test(world, pos, state)) {
					return false;
				}
				
				// Also make sure its key matches the dungeon!
				TileEntity te = world.getTileEntity(pos);
				if (te == null || !(te instanceof DungeonKeyChestTileEntity)) {
					return false;
				}
				
				final WorldKey hasKey = ((DungeonKeyChestTileEntity) te).getWorldKey();
				return hasKey != null && (hasKey.equals(dungeon.instance.getLargeKey()) || hasKey.equals(dungeon.instance.getSmallKey()));
			});
		}
		
		return null;
	}
	
	protected boolean doSeek(World world, PlayerEntity player, ItemStack stack) {
		// Only do work on client side!
		if (world.isRemote) {
			@Nullable BlockPos nearest = attemptDungeonSeek(player, world, stack, AutoDungeons.GetDungeonTracker().getDungeon(player));
			if (nearest != null) {
				NostrumMagicaSounds.AMBIENT_WOOSH3.playClient(world, nearest.getX() + .5, nearest.getY() + .5, nearest.getZ() + .5);
			} else {
				NostrumMagicaSounds.CAST_FAIL.playClient(player);
			}
			player.getCooldownTracker().setCooldown(stack.getItem(), 20); // Jut client side
			return nearest != null;
		}
		return false;
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		final World worldIn = context.getWorld();
		final PlayerEntity playerIn = context.getPlayer();
		doSeek(worldIn, playerIn, context.getItem());
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		doSeek(worldIn, playerIn, playerIn.getHeldItem(hand));
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, playerIn.getHeldItem(hand));
	}

	@Override
	public String getLoreKey() {
		return "nostrum_seeking_gem";
	}

	@Override
	public String getLoreDisplayName() {
		return "Seeking Gems";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Seeking Gems hold a will to seek.", "They are sometimes used in spells and rituals that have to do with finding.", "There may be other uses...");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Seeking Gems hold a will to seek.", "They are sometimes used in spells and rituals that have to do with finding.", "They can also be used in a dungeon to help locate any nearby dungeon key chests!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
