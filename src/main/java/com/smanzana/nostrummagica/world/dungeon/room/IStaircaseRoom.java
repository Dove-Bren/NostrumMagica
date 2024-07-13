package com.smanzana.nostrummagica.world.dungeon.room;

import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.IWorldHeightReader;

public interface IStaircaseRoom extends IDungeonRoom {

	public BlueprintLocation getEntryStart(IWorldHeightReader world, BlueprintLocation start);
	
}
