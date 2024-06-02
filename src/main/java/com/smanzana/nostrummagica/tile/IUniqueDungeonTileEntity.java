package com.smanzana.nostrummagica.tile;

import java.util.UUID;

/**
 * This tile entity wants to do something unique per every dungeon it's in, such as generating new
 * world key pairs for locks and keys so that ones from other instances of this tile entity in other
 * dungeons won't overlap.
 * @author Skyler
 *
 */
public interface IUniqueDungeonTileEntity {

	/**
	 * Called after this tile entity has been placed in the world as a result of dungeon placement.
	 * dungeonID is the same for all rooms in the entire dungeon, while room is unique per room.
	 * So keys that should match across rooms but not other copies of the tile entity in other dungeons
	 * should uniquify themselves with the dungeonID, whereas entities that should be unique per room
	 * should use the roomID.
	 * @param dungeonID
	 * @param roomID
	 * @param isWorldGen
	 */
	public void onDungeonSpawn(UUID dungeonID, UUID roomID, boolean isWorldGen);
	
}
