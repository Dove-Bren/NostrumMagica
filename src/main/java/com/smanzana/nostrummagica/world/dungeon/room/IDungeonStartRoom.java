package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonRoomInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.IWorldHeightReader;

public interface IDungeonStartRoom extends IDungeonRoom {
	
	public List<DungeonRoomInstance> generateExtraPieces(IWorldHeightReader world, BlueprintLocation start, Random rand, DungeonInstance instance);

}
