package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonRoomInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.IWorldHeightReader;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry.DungeonRoomRecord;

import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;

public class LoadedStartRoom extends LoadedRoom implements IDungeonStartRoom {
	
	protected final LoadedRoom entry;
	protected final RoomExtendedEntranceStaircase stairs;
	
	public LoadedStartRoom(DungeonRoomRecord lobbyRecord, DungeonRoomRecord entryRecord) {
		super(lobbyRecord);
		this.entry = new LoadedRoom(entryRecord);
		this.stairs = new RoomExtendedEntranceStaircase(false);
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
		
		//DungeonExitPoint adj = new DungeonExitPoint(start.getPos().add(0, 6, 0), start.getFacing());
		stairs.spawn(world, start, bounds, dungeonID);
	}

	@Override
	public List<DungeonRoomInstance> generateExtraPieces(IWorldHeightReader world, DungeonExitPoint start, Random rand, DungeonInstance instance) {
		// Stairs and entry room
		return Lists.newArrayList(
				new DungeonRoomInstance(start, stairs, false, instance, UUID.randomUUID()),
				new DungeonRoomInstance(stairs.getEntryStart(world, start), entry, false, instance, UUID.randomUUID())
				);
	}
}
