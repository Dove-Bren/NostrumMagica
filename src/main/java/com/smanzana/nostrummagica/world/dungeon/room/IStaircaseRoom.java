package com.smanzana.nostrummagica.world.dungeon.room;

import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.IWorldHeightReader;

public interface IStaircaseRoom extends IDungeonRoom {

	public DungeonExitPoint getEntryStart(IWorldHeightReader world, DungeonExitPoint start);
	
}
