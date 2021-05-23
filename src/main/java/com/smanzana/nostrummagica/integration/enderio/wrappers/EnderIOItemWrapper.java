package com.smanzana.nostrummagica.integration.enderio.wrappers;

import crazypants.enderio.ModObject;
import net.minecraft.item.Item;

public class EnderIOItemWrapper {

	public static Item GetItem(EnderIOItemType type) {
		switch (type) {
		case TRAVEL_STAFF:
			return ModObject.itemTravelStaff.getItem();
		default:
			return null;
		}
	}
	
}
