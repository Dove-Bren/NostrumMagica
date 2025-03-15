package com.smanzana.nostrummagica.block.dungeon;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.block.ILargeDoorMarker;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.autodungeons.world.dungeon.DungeonInstance;
import com.smanzana.autodungeons.world.dungeon.DungeonRecord;
import com.smanzana.autodungeons.world.dungeon.DungeonRoomInstance;
import com.smanzana.nostrummagica.tile.DungeonDoorTileEntity;
import com.smanzana.nostrummagica.tile.DungeonKeyChestTileEntity;
import com.smanzana.nostrummagica.util.WorldUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public abstract class DungeonDoorBlock extends LockedDoorBlock implements ILargeDoorMarker {

	public static BooleanProperty UNLOCKABLE = LockedChestBlock.UNLOCKABLE;
	
	public DungeonDoorBlock() {
		super();

		this.registerDefaultState(this.defaultBlockState().setValue(UNLOCKABLE, false));
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if (!this.isMaster(state))
			return null;
		
		return new DungeonDoorTileEntity();
	}
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (!worldIn.isClientSide()) {
			BlockPos master = this.getMasterPos(worldIn, state, pos);
			if (master != null) {
				DungeonDoorTileEntity door = (DungeonDoorTileEntity) worldIn.getBlockEntity(master);
				final ItemStack heldItem = playerIn.getItemInHand(hand);
				
				if (playerIn.isCreative() && heldItem.isEmpty() && playerIn.isShiftKeyDown()) {
					// Try to take key from dungeon
					DungeonRecord record = AutoDungeons.GetDungeonTracker().getDungeon(playerIn);
					if (record != null) {
						WorldKey key = door.isLarge() ? record.instance.getLargeKey() : record.instance.getSmallKey();
						door.setWorldKey(key);
						playerIn.sendMessage(new StringTextComponent("Set to dungeon key"), Util.NIL_UUID);
					} else {
						playerIn.sendMessage(new StringTextComponent("Not in a dungeon, so no key to set"), Util.NIL_UUID);
					}
					return ActionResultType.SUCCESS;
				} else {
					door.attemptUnlock(playerIn);
				}
			}
		}
		
		return ActionResultType.SUCCESS;
	}
	
	protected abstract WorldKey pickDungeonKey(DungeonInstance dungeon);
	
	public void spawnDungeonDoor(IWorld worldIn, BlockPos start, Direction facing, @Nullable MutableBoundingBox bounds, DungeonInstance dungeon) {
		final boolean isWorldGen = WorldUtil.IsWorldGen(worldIn);
		// This is pretty dumb, but terrain gen will 'defer' tile entities under normal circumstances. By default,
		// our setBlockState below will during world gen, too.
		// BUT if something earlier in gen has done a 'getTileEntity' after something even earlier
		// caused a deferred one, it will cache it and not allow any TE changes during generation.
		// Specifically WorldGenRegion will push into the deferred, and then read it when getTileEntity is called.
		// DungeonChests run into an issue where LootUtil has already forced a chest TE to generate, and so our
		// blockstate change here doesn't cause a TE refresh.
		// So we're going to force it.
		if (isWorldGen && worldIn.getBlockEntity(start) != null && !(worldIn.getBlockEntity(start) instanceof DungeonKeyChestTileEntity)) {
			worldIn.removeBlock(start, false);
		}
		
		BlockState state = this.getMaster(facing);
		worldIn.setBlock(start, state, 3);
		this.spawnDoor(worldIn, start, state, bounds);
		
		DungeonDoorTileEntity door = (DungeonDoorTileEntity) worldIn.getBlockEntity(start);
		door.setWorldKey(pickDungeonKey(dungeon), WorldUtil.IsWorldGen(worldIn));
	}
	
	public void overrideDungeonKey(IWorld worldIn, BlockPos masterPos, DungeonInstance dungeon) {
		DungeonDoorTileEntity door = (DungeonDoorTileEntity) worldIn.getBlockEntity(masterPos);
		if (door == null) {
			System.out.println("No door where it said there would be! " + masterPos);
			System.out.println("Instead, there is: " + worldIn.getBlockState(masterPos));
		}
		door.setWorldKey(pickDungeonKey(dungeon), WorldUtil.IsWorldGen(worldIn));
	}
	
	@Override
	public boolean isLargeDoor(BlockState state) {
		return state.getValue(MASTER);
	}
	
	@Override
	public void setKey(IWorld worldIn, BlockState state, BlockPos masterPos, WorldKey key, DungeonRoomInstance dungeon, @Nullable MutableBoundingBox bounds) {
		DungeonDoorTileEntity door = (DungeonDoorTileEntity) worldIn.getBlockEntity(masterPos);
		door.setWorldKey(key, WorldUtil.IsWorldGen(worldIn));
	}
	
	public static class Small extends DungeonDoorBlock {
		
		public static final String ID = "dungeon_door_small";
		
		public Small() {
			super();
		}

		@Override
		protected WorldKey pickDungeonKey(DungeonInstance dungeon) {
			return dungeon.getSmallKey();
		}
	}
	
	public static class Large extends DungeonDoorBlock {
		
		public static final String ID = "dungeon_door_large";
		
		public Large() {
			super();
		}

		@Override
		protected WorldKey pickDungeonKey(DungeonInstance dungeon) {
			return dungeon.getLargeKey();
		}
	}
}
