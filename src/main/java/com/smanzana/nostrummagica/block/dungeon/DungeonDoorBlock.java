package com.smanzana.nostrummagica.block.dungeon;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.DungeonDoorTileEntity;
import com.smanzana.nostrummagica.util.WorldUtil;
import com.smanzana.nostrummagica.world.NostrumWorldKey;
import com.smanzana.nostrummagica.world.dungeon.DungeonRecord;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonInstance;

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

public abstract class DungeonDoorBlock extends LockedDoorBlock {

	public static BooleanProperty UNLOCKABLE = LockedChestBlock.UNLOCKABLE;
	
	public DungeonDoorBlock() {
		super();

		this.setDefaultState(this.getDefaultState().with(UNLOCKABLE, false));
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if (!this.isMaster(state))
			return null;
		
		return new DungeonDoorTileEntity();
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (!worldIn.isRemote()) {
			BlockPos master = this.getMasterPos(worldIn, state, pos);
			if (master != null) {
				DungeonDoorTileEntity door = (DungeonDoorTileEntity) worldIn.getTileEntity(master);
				final ItemStack heldItem = playerIn.getHeldItem(hand);
				
				if (playerIn.isCreative() && heldItem.isEmpty() && playerIn.isSneaking()) {
					// Try to take key from dungeon
					DungeonRecord record = NostrumMagica.dungeonTracker.getDungeon(playerIn);
					if (record != null) {
						NostrumWorldKey key = door.isLarge() ? record.instance.getLargeKey() : record.instance.getSmallKey();
						door.setWorldKey(key);
						playerIn.sendMessage(new StringTextComponent("Set to dungeon key"), Util.DUMMY_UUID);
					} else {
						playerIn.sendMessage(new StringTextComponent("Not in a dungeon, so no key to set"), Util.DUMMY_UUID);
					}
					return ActionResultType.SUCCESS;
				} else {
					door.attemptUnlock(playerIn);
				}
			}
		}
		
		return ActionResultType.SUCCESS;
	}
	
	protected abstract NostrumWorldKey pickDungeonKey(DungeonInstance dungeon);
	
	public void spawnDungeonDoor(IWorld worldIn, BlockPos start, Direction facing, @Nullable MutableBoundingBox bounds, DungeonInstance dungeon) {
		BlockState state = this.getMaster(facing);
		worldIn.setBlockState(start, state, 3);
		this.spawnDoor(worldIn, start, state, bounds);
		
		DungeonDoorTileEntity door = (DungeonDoorTileEntity) worldIn.getTileEntity(start);
		door.setWorldKey(pickDungeonKey(dungeon), WorldUtil.IsWorldGen(worldIn));
	}
	
	public void overrideDungeonKey(IWorld worldIn, BlockPos masterPos, DungeonInstance dungeon) {
		DungeonDoorTileEntity door = (DungeonDoorTileEntity) worldIn.getTileEntity(masterPos);
		door.setWorldKey(pickDungeonKey(dungeon), WorldUtil.IsWorldGen(worldIn));
	}
	
	public static class Small extends DungeonDoorBlock {
		
		public static final String ID = "dungeon_door_small";
		
		public Small() {
			super();
		}

		@Override
		protected NostrumWorldKey pickDungeonKey(DungeonInstance dungeon) {
			return dungeon.getSmallKey();
		}
	}
	
	public static class Large extends DungeonDoorBlock {
		
		public static final String ID = "dungeon_door_large";
		
		public Large() {
			super();
		}

		@Override
		protected NostrumWorldKey pickDungeonKey(DungeonInstance dungeon) {
			return dungeon.getLargeKey();
		}
	}
}
