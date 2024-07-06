package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.DungeonDoorTileEntity;
import com.smanzana.nostrummagica.world.NostrumWorldKey;
import com.smanzana.nostrummagica.world.dungeon.DungeonRecord;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
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
	
	public static class Small extends DungeonDoorBlock {
		
		public static final String ID = "dungeon_door_small";
		
		public Small() {
			super();
		}
	}
	
	public static class Large extends DungeonDoorBlock {
		
		public static final String ID = "dungeon_door_large";
		
		public Large() {
			super();
		}
	}
}
