package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonRoomInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.IWorldHeightReader;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoomRef.DungeonLobbyRoomRef;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoomRef.DungeonRoomRef;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoomRef.DungeonStaircaseRoomRef;

import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;

public class DungeonStartRoom {
	
	protected final DungeonLobbyRoomRef lobby;
	protected final DungeonRoomRef entry;
	protected final DungeonStaircaseRoomRef stairs;
	
	public DungeonStartRoom(DungeonLobbyRoomRef lobby, DungeonRoomRef entry, DungeonStaircaseRoomRef stairs) {
		this.lobby = lobby;
		this.entry = entry;
		this.stairs = stairs;
	}
	
	public List<DungeonRoomInstance> generateExtraPieces(IWorldHeightReader world, BlueprintLocation start, Random rand, DungeonInstance instance) {
		// Stairs and entry room
		BlueprintLocation adj = new BlueprintLocation(start.getPos().add(lobby.get().getStairOffset()), start.getFacing());
		return Lists.newArrayList(
				new DungeonRoomInstance(adj, stairs.get(), false, false, instance, UUID.randomUUID()),
				new DungeonRoomInstance(stairs.get().getEntryStart(world, adj), entry, false, false, instance, UUID.randomUUID())
				);
	}

	public boolean canSpawnAt(IWorld world, BlueprintLocation start) {
		return lobby.canSpawnAt(world, start);
	}

	public List<BlueprintLocation> getExits(BlueprintLocation start) {
		return lobby.getExits(start);
	}

	public MutableBoundingBox getBounds(BlueprintLocation entry) {
		return lobby.getBounds(entry);
	}

	public boolean supportsDoor() {
		return lobby.supportsDoor();
	}

	public boolean supportsKey() {
		return lobby.supportsKey();
	}

	public BlueprintLocation getKeyLocation(BlueprintLocation start) {
		return lobby.getKeyLocation(start);
	}

	public boolean supportsTreasure() {
		return lobby.supportsTreasure();
	}

	public List<BlueprintLocation> getTreasureLocations(BlueprintLocation start) {
		return lobby.getTreasureLocations(start);
	}

	public DungeonLobbyRoomRef getLobby() {
		return lobby;
	}
}
