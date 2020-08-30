package com.smanzana.nostrummagica.client.gui.infoscreen;

/**
 * Indicates this item/block/etc can be looked up in the info screen.
 * @author Skyler
 *
 */
public interface InfoScreenIndexed {

	/**
	 * Return the UNIQUE index key for this item/block/entity/etc.
	 * @return
	 */
	public String getInfoScreenKey();
	
}
