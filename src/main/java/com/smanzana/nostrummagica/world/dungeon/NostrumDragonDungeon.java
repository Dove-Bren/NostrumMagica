package com.smanzana.nostrummagica.world.dungeon;

import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoom;

import net.minecraft.world.IWorld;

public class NostrumDragonDungeon extends NostrumDungeon {

	public NostrumDragonDungeon(IDungeonRoom starting, IDungeonRoom ending) {
		super(starting, ending, 4, 1);
	}
	
	@Override
	public void spawn(IWorld world, DungeonExitPoint start) {
		super.spawn(world, start);
	}

}
