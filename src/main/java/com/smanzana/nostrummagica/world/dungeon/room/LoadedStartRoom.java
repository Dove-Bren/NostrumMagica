package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry.DungeonRoomRecord;

import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;

public class LoadedStartRoom extends LoadedRoom {
	
	protected final LoadedRoom entry;
	
	public LoadedStartRoom(DungeonRoomRecord lobbyRecord, DungeonRoomRecord entryRecord) {
		super(lobbyRecord);
		this.entry = new LoadedRoom(entryRecord);
	}
	
	@Override
	public int getDifficulty() {
		return 0;
	}

	@Override
	public boolean hasEnemies() {
		return true;
	}

	@Override
	public void spawn(IWorld world, DungeonExitPoint start, @Nullable MutableBoundingBox bounds, UUID dungeonID) {
		super.spawn(world, start, bounds, dungeonID);
		
		RoomExtendedEntranceStaircase stairs = new RoomExtendedEntranceStaircase(false, entry);
		//DungeonExitPoint adj = new DungeonExitPoint(start.getPos().add(0, 6, 0), start.getFacing());
		stairs.spawn(world, start, bounds, dungeonID);
	}
}
