package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonRoomInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.IWorldHeightReader;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;

public class BlueprintStartRoom extends BlueprintDungeonRoom implements IDungeonStartRoom {
	
	protected final BlueprintDungeonRoom entry;
	protected final RoomExtendedEntranceStaircase stairs;
	
	public BlueprintStartRoom(ResourceLocation lobbyID, ResourceLocation entryID) {
		super(lobbyID);
		this.entry = new BlueprintDungeonRoom(entryID);
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
	public void spawn(IWorld world, BlueprintLocation start, @Nullable MutableBoundingBox bounds, UUID dungeonID) {
		super.spawn(world, start, bounds, dungeonID);
		
		//DungeonExitPoint adj = new DungeonExitPoint(start.getPos().add(0, 6, 0), start.getFacing());
		stairs.spawn(world, start, bounds, dungeonID);
	}

	@Override
	public List<DungeonRoomInstance> generateExtraPieces(IWorldHeightReader world, BlueprintLocation start, Random rand, DungeonInstance instance) {
		// Stairs and entry room
		return Lists.newArrayList(
				new DungeonRoomInstance(start, stairs, false, false, instance, UUID.randomUUID()),
				new DungeonRoomInstance(stairs.getEntryStart(world, start), entry, false, false, instance, UUID.randomUUID())
				);
	}
}
