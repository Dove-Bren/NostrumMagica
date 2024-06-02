package com.smanzana.nostrummagica.tile;

import net.minecraft.util.Direction;

/**
 * Marks a tile entity that stores information that depends on rotation (like block offsets) that
 * wants to know which rotation it's spawned with when spawned from a blueprint
 * @author Skyler
 *
 */
public interface IOrientedTileEntity {

	/**
	 * Called once after tile entity is set in the world.
	 * Rotation is relative, not absolute, with 'north' being '0 rotation'.
	 * That is, if the tile entity was facing east and the blueprint is being spawned
	 * with an overall rotation of 1 turn clockwise, this will be east (1 rotation clockwise), not
	 * south.
	 * Consider using RoomBlueprint.applyRotation to transform offsets.
	 * @param rotation
	 */
	public void setSpawnedFromRotation(Direction rotation, boolean isWorldGen);
	
}
