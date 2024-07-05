package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonRoomInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.IWorldHeightReader;

public interface IDungeonStartRoom extends IDungeonRoom {
	
	public List<DungeonRoomInstance> generateExtraPieces(IWorldHeightReader world, DungeonExitPoint start, Random rand, DungeonInstance instance);

}
