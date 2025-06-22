package com.smanzana.nostrummagica.item.equipment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.util.WorldUtil;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.autodungeons.world.dungeon.DungeonRecord;
import com.smanzana.nostrummagica.block.dungeon.DungeonKeyChestBlock;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.DungeonKeyChestTileEntity;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

/**
 * Solidified position crystal for obelisk linking
 * @author Skyler
 *
 */
public class SeekingGem extends Item implements ILoreTagged {
	
	public static interface IChestFilter {
		public boolean test(Level world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEnt);
	}
	
	protected static final IChestFilter IS_DUNGEONCHEST = (world, pos, state, ent) -> state.getBlock() instanceof DungeonKeyChestBlock && !state.getValue(DungeonKeyChestBlock.OPEN);
	protected static final IChestFilter IS_EMPTYCHEST = (world, pos, state, ent) -> state.getBlock() instanceof ChestBlock && ent != null && ent instanceof ChestBlockEntity chest && chest.lootTable == null && chest.isEmpty();
	protected static final IChestFilter IS_FILLEDCHEST = (world, pos, state, ent) -> state.getBlock() instanceof ChestBlock && ent != null && ent instanceof ChestBlockEntity chest && chest.lootTable == null && !chest.isEmpty();
	protected static final IChestFilter IS_LOOTCHEST = (world, pos, state, ent) -> state.getBlock() instanceof ChestBlock && ent != null && ent instanceof ChestBlockEntity chest && chest.lootTable != null;
	
	public static void FindChests(Level world, BoundingBox bounds, BlockPos center, @Nullable IChestFilter chestFilterIn, List<BlockPos> matches) {
		final IChestFilter chestFilter = chestFilterIn != null
				? chestFilterIn
				: IS_DUNGEONCHEST; 
		
		final BlockPos min = new BlockPos(bounds.minX(), bounds.minY(), bounds.minZ());
		final BlockPos max = new BlockPos(bounds.maxX(), bounds.maxY(), bounds.maxZ());
		WorldUtil.ScanBlocks(world, min, max, (worldIn, pos) -> {
			BlockState state = world.getBlockState(pos);
			BlockEntity ent = world.getBlockEntity(pos);
			if (chestFilter.test(world, pos, state, ent)) {
				matches.add(pos.immutable());
			}
			return true;
		});
	}
	
	public static @Nullable BlockPos FindClosestChest(Level world, BoundingBox bounds, BlockPos center, @Nullable IChestFilter chestFilterIn) {
		List<BlockPos> matches = new ArrayList<>();
		FindChests(world, bounds, center, chestFilterIn, matches);
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
	
	protected static enum SeekTargetType {
		KEY_CHEST,
		LOOT_CHEST,
		NON_EMPTY_CHEST,
		EMPTY_CHEST
	}
	
	protected static record SeekTarget(SeekTargetType type, BlockPos pos) {}
	
	protected @Nullable SeekTarget attemptDungeonSeek(Player playerIn, Level worldIn, ItemStack gem, DungeonRecord dungeon) {
		if (dungeon != null && dungeon.currentRoom != null) {
			// Sorcery dimension has prefilled non-empty chests and key chests. Overworld dungeons have key chests and loot chests. Find all three.
			// Since we have to lambda, might as well track directly and avoid a second loop and re-check
			Set<BlockPos> keyChests = new HashSet<>();
			Set<BlockPos> lootChests = new HashSet<>();
			Set<BlockPos> filledChests = new HashSet<>();
			
			IChestFilter filter = (world, pos, state, ent) -> {
				if (IS_DUNGEONCHEST.test(world, pos, state, ent)) {
					// Also make sure its key matches the dungeon!
					BlockEntity te = world.getBlockEntity(pos);
					if (te == null || !(te instanceof DungeonKeyChestTileEntity)) {
						return false;
					}
					
					final WorldKey hasKey = ((DungeonKeyChestTileEntity) te).getWorldKey();
					if (hasKey != null && (hasKey.equals(dungeon.instance.getLargeKey()) || hasKey.equals(dungeon.instance.getSmallKey()))) {
						keyChests.add(pos.immutable());
						return true;
					}
					return false;
				}
				
				if (IS_LOOTCHEST.test(world, pos, state, ent)) {
					lootChests.add(pos.immutable());
					return true;
				}
				
				if (IS_FILLEDCHEST.test(world, pos, state, ent)) {
					filledChests.add(pos.immutable());
					return true;
				}
				
				return false;
			};
			@Nullable BlockPos foundPos = FindClosestChest(worldIn, dungeon.currentRoom.getBounds(), playerIn.blockPosition(), filter);
			
			if (foundPos != null) {
				final SeekTargetType type;
				if (keyChests.contains(foundPos)) {
					type = SeekTargetType.KEY_CHEST;
				} else if (lootChests.contains(foundPos)) {
					type = SeekTargetType.LOOT_CHEST;
				} else {
					type = SeekTargetType.NON_EMPTY_CHEST;
				}
				return new SeekTarget(type, foundPos);
			} else {
				return null;
			}
		}
		
		// Not in a dungeon. Since this is client-side, we don't know if we're in a structure either...
		// So fall back to just cuboid scan
		final BoundingBox fallbackBox = BoundingBox.fromCorners(playerIn.blockPosition().below(10).west(20).north(20), playerIn.blockPosition().above(10).east(20).south(20));
		final boolean searchForEmpty = playerIn.isCreative() && playerIn.isCrouching();
		@Nullable BlockPos foundPos = FindClosestChest(worldIn, fallbackBox, playerIn.blockPosition(), searchForEmpty ? IS_EMPTYCHEST : IS_LOOTCHEST);
		
		return foundPos == null ? null : new SeekTarget(searchForEmpty ? SeekTargetType.EMPTY_CHEST : SeekTargetType.LOOT_CHEST, foundPos);
	}
	
	protected boolean doSeek(Level world, Player player, ItemStack stack) {
		
		// Only do work on client side... except client side doesn't have chest info besides dungeon chests. Yikes?
		if (!world.isClientSide) {
			DungeonRecord dungeon = AutoDungeons.GetDungeonTracker().getDungeon(player);
			@Nullable SeekTarget nearest = attemptDungeonSeek(player, world, stack, dungeon);
			if (nearest != null) {
				final BlockPos nearestPos = nearest.pos;
				switch (nearest.type) {
				case EMPTY_CHEST:
					NostrumMagicaSounds.DAMAGE_ICE.play(world, nearestPos.getX() + .5, nearestPos.getY() + .5, nearestPos.getZ() + .5);
					break;
				case KEY_CHEST:
					NostrumMagicaSounds.AMBIENT_WOOSH3.play(world, nearestPos.getX() + .5, nearestPos.getY() + .5, nearestPos.getZ() + .5);
					break;
				case LOOT_CHEST:
					world.playSound(null, nearestPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1f, 1f);
					break;
				case NON_EMPTY_CHEST:
					world.playSound(null, nearestPos, SoundEvents.NOTE_BLOCK_PLING, SoundSource.PLAYERS, 1f, 1f);
					break;
				}
				
				if (player.isCreative()) {
					NostrumParticles.GLOW_TRAIL.spawn(world, new SpawnParams(1, player.getX() + .5, player.getY() + .5, player.getZ() + .5,
							0, 300, 0, new TargetLocation(Vec3.atCenterOf(nearestPos))
							).setTargetBehavior(new ParticleTargetBehavior().joinMode(true)).color(1f, .8f, 1f, .3f));
				}
			} else {
				NostrumMagicaSounds.CAST_FAIL.playClient(player);
			}
			player.getCooldowns().addCooldown(stack.getItem(), 20); // Jut client side
			return nearest != null;
		}
		
		return true;
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
