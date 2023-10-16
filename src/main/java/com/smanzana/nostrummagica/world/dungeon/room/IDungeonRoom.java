package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.List;

import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.world.IWorld;

public interface IDungeonRoom {
	
	/**
	 * Check whether the room can be spawned at the indicated position with the
	 * given direction. This should check all affected blocks and make sure that
	 * no unbreakable blocks, etc are overlapped
	 * @param world
	 * @return
	 */
	public boolean canSpawnAt(IWorld world, DungeonExitPoint start);
	
	/**
	 * Return the number of exits this room has
	 * @return
	 */
	public int getNumExits();
	
	/**
	 * Return a list of exists that would be generated if the room was
	 * placed at the given position and facing
	 * @return
	 */
	public List<DungeonExitPoint> getExits(DungeonExitPoint start);
	
	/**
	 * Returns the difficulty of the given room, which is used when figuring outa
	 * which room to spawn
	 * @return difficulty on scale from 1 to 10
	 */
	public int getDifficulty();
	
	//public boolean hasPuzzle();
	
	public boolean supportsDoor();
	
	public boolean supportsKey();
	
	// If supportsKey returns false, expected to return null
	public DungeonExitPoint getKeyLocation(DungeonExitPoint start);
	
	public List<DungeonExitPoint> getTreasureLocations(DungeonExitPoint start);
	
	public boolean hasEnemies();
	
	public boolean hasTraps();
	
	public void spawn(NostrumDungeon dungeon, IWorld world, DungeonExitPoint start);
	
}
