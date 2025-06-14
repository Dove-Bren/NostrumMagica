package com.smanzana.nostrummagica.tile;

import java.util.UUID;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.world.dungeon.DungeonInstance;
import com.smanzana.nostrummagica.block.dungeon.DungeonDoorBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DungeonDoorTileEntity extends LockedDoorTileEntity {

	public DungeonDoorTileEntity(BlockPos pos, BlockState state) {
		super(NostrumBlockEntities.DungeonDoor, pos, state);
	}
	
	@Override
	protected void checkBlockState() {
		boolean worldUnlockable = level.getBlockState(worldPosition).getValue(DungeonDoorBlock.UNLOCKABLE);
		boolean tileUnlockable = AutoDungeons.GetWorldKeys().hasKey(this.getWorldKey()); 
		if (worldUnlockable != tileUnlockable) {
			level.setBlock(worldPosition, level.getBlockState(worldPosition).setValue(DungeonDoorBlock.UNLOCKABLE, tileUnlockable), 3);
		}
	}
	
	@Override
	public void onRoomBlueprintSpawn(DungeonInstance dungeonInstance, UUID roomID, boolean isWorldGen) {
		// Set key to dungeon keys
		this.setWorldKey(((DungeonDoorBlock) this.getBlockState().getBlock()).pickDungeonKey(dungeonInstance), isWorldGen);
	}
	
	public boolean isLarge() {
		return this.getBlockState().getBlock() instanceof DungeonDoorBlock.Large;
	}
}