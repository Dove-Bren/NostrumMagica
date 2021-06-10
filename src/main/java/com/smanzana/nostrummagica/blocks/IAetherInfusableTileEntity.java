package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuser.AetherInfuserTileEntity;

/**
 * Marks that this entity might have some behavior based on receiving aether from a nearby aether infuser
 * @author Skyler
 *
 */
public interface IAetherInfusableTileEntity {

	/**
	 * Quick check whether this tile entity can even attempt to accept any aether.
	 * No work should be done by this func besides checking if things look roughly right.
	 * @param source
	 * @param maxAether
	 * @return
	 */
	public boolean canAcceptAetherInfuse(AetherInfuserTileEntity source, int maxAether);
	
	/**
	 * Attempt to accept some aether from an Aether Infuser.
	 * Not all aether must be taken. Instead, return what couldn't be used.
	 * @param source
	 * @param maxAether Max aether the infuser can provide in this call
	 * @return
	 */
	public int acceptAetherInfuse(AetherInfuserTileEntity source, int maxAether);
	
}
