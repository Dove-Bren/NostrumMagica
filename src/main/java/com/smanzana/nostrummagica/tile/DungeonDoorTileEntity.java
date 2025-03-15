package com.smanzana.nostrummagica.tile;

import java.util.UUID;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.nostrummagica.block.dungeon.DungeonDoorBlock;

public class DungeonDoorTileEntity extends LockedDoorTileEntity {

	public DungeonDoorTileEntity() {
		super(NostrumTileEntities.DungeonDoorTileEntityType);
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
	public void onRoomBlueprintSpawn(UUID dungeonID, UUID roomID, boolean isWorldGen) {
		; // Don't change key; let dungeon stamp in key
	}
	
	public boolean isLarge() {
		return this.getBlockState().getBlock() instanceof DungeonDoorBlock.Large;
	}
}