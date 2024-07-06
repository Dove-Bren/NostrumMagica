package com.smanzana.nostrummagica.tile;

import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.dungeon.DungeonDoorBlock;

public class DungeonDoorTileEntity extends LockedDoorTileEntity {

	public DungeonDoorTileEntity() {
		super(NostrumTileEntities.DungeonDoorTileEntityType);
	}
	
	@Override
	protected void checkBlockState() {
		boolean worldUnlockable = world.getBlockState(pos).get(DungeonDoorBlock.UNLOCKABLE);
		boolean tileUnlockable = NostrumMagica.instance.getWorldKeys().hasKey(this.getWorldKey()); 
		if (worldUnlockable != tileUnlockable) {
			world.setBlockState(pos, world.getBlockState(pos).with(DungeonDoorBlock.UNLOCKABLE, tileUnlockable), 3);
		}
	}
	
	@Override
	public void onDungeonSpawn(UUID dungeonID, UUID roomID, boolean isWorldGen) {
		; // Don't change key; let dungeon stamp in key
	}
	
	public boolean isLarge() {
		return this.getBlockState().getBlock() instanceof DungeonDoorBlock.Large;
	}
}