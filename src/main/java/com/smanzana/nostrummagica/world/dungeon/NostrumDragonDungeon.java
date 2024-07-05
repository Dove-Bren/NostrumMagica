package com.smanzana.nostrummagica.world.dungeon;

import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoom;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonStartRoom;

public class NostrumDragonDungeon extends NostrumDungeon {

	public NostrumDragonDungeon(IDungeonStartRoom starting, IDungeonRoom ending) {
		super(starting, ending, 4, 1);
	}
}
