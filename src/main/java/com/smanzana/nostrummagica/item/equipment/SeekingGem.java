package com.smanzana.nostrummagica.item.equipment;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.util.WorldUtil;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.autodungeons.world.dungeon.DungeonRecord;
import com.smanzana.nostrummagica.block.dungeon.DungeonKeyChestBlock;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.DungeonKeyChestTileEntity;

import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Solidified position crystal for obelisk linking
 * @author Skyler
 *
 */
public class SeekingGem extends Item implements ILoreTagged {
	
	public static interface IChestFilter {
		public boolean test(Level world, BlockPos pos, BlockState state);
	}
	
	protected static final IChestFilter IS_DUNGEONCHEST = (world, pos, state) -> state.getBlock() instanceof DungeonKeyChestBlock && !state.getValue(DungeonKeyChestBlock.OPEN);
	
	public static @Nullable BlockPos FindKeyChest(Level world, BoundingBox bounds, BlockPos center, @Nullable IChestFilter chestFilterIn) {
		final IChestFilter chestFilter = chestFilterIn != null
				? chestFilterIn
				: IS_DUNGEONCHEST; 
		
		final BlockPos min = new BlockPos(bounds.minX(), bounds.minY(), bounds.minZ());
		final BlockPos max = new BlockPos(bounds.maxX(), bounds.maxY(), bounds.maxZ());
		List<BlockPos> matches = new ArrayList<>();
		WorldUtil.ScanBlocks(world, min, max, (worldIn, pos) -> {
			BlockState state = world.getBlockState(pos);
			if (chestFilter.test(world, pos, state)) {
				matches.add(pos.immutable());
			}
			return true;
		});
		
		BlockPos closest = null;
		int minLength = Integer.MAX_VALUE;
		for (BlockPos candidate : matches) {
			final int dist = center.distManhattan(candidate);
			if (dist < minLength) {
				closest = candidate;
				minLength = dist;
			}
		}
		
		return closest;
	}

	public static final String ID = "seeking_gem";
	
	public SeekingGem(Item.Properties props) {
		super(props);
	}
	
	protected @Nullable BlockPos attemptDungeonSeek(Player playerIn, Level worldIn, ItemStack gem, DungeonRecord dungeon) {
		if (dungeon != null && dungeon.currentRoom != null) {
			return FindKeyChest(worldIn, dungeon.currentRoom.getBounds(), playerIn.blockPosition(), (world, pos, state) -> {
				if (!IS_DUNGEONCHEST.test(world, pos, state)) {
					return false;
				}
				
				// Also make sure its key matches the dungeon!
				BlockEntity te = world.getBlockEntity(pos);
				if (te == null || !(te instanceof DungeonKeyChestTileEntity)) {
					return false;
				}
				
				final WorldKey hasKey = ((DungeonKeyChestTileEntity) te).getWorldKey();
				return hasKey != null && (hasKey.equals(dungeon.instance.getLargeKey()) || hasKey.equals(dungeon.instance.getSmallKey()));
			});
		}
		
		return null;
	}
	
	protected boolean doSeek(Level world, Player player, ItemStack stack) {
		DungeonRecord dungeon = AutoDungeons.GetDungeonTracker().getDungeon(player);
		if (dungeon != null) {
			// Only do work on client side!
			if (world.isClientSide) {
				@Nullable BlockPos nearest = attemptDungeonSeek(player, world, stack, dungeon);
				if (nearest != null) {
					NostrumMagicaSounds.AMBIENT_WOOSH3.playClient(world, nearest.getX() + .5, nearest.getY() + .5, nearest.getZ() + .5);
					
//					NostrumParticles.GLOW_TRAIL.spawn(world, new SpawnParams(1, player.getX() + .5, player.getY() + .5, player.getZ() + .5,
//							0, 300, 0, new TargetLocation(Vec3.atCenterOf(nearest))
//							).setTargetBehavior(new ParticleTargetBehavior().joinMode(true)).color(1f, .8f, 1f, .3f));
				} else {
					NostrumMagicaSounds.CAST_FAIL.playClient(player);
				}
				player.getCooldowns().addCooldown(stack.getItem(), 20); // Jut client side
				return nearest != null;
			}
			
			return true;
		} else {
			if (world.isClientSide) {
				player.sendMessage(new TextComponent("It doesn't seem to do anything here..."), Util.NIL_UUID);
			}
		}
		return false;
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		final Level worldIn = context.getLevel();
		final Player playerIn = context.getPlayer();
		if (doSeek(worldIn, playerIn, context.getItemInHand())) {
			if (playerIn instanceof ServerPlayer) {
				CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)playerIn, context.getItemInHand());
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		if (doSeek(worldIn, playerIn, playerIn.getItemInHand(hand))) {
			if (playerIn instanceof ServerPlayer) {
				CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)playerIn, playerIn.getItemInHand(hand));
			}
		}
		return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, playerIn.getItemInHand(hand));
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
