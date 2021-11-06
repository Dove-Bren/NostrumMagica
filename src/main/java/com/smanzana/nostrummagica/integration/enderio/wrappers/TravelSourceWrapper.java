package com.smanzana.nostrummagica.integration.enderio.wrappers;

import crazypants.enderio.api.teleport.TravelSource;

public enum TravelSourceWrapper {

	BLOCK(TravelSource.BLOCK),
	STAFF(TravelSource.STAFF),	
	STAFF_BLINK(TravelSource.STAFF_BLINK),
	TELEPAD(TravelSource.TELEPAD);
	
	private final TravelSource source;
	
	private TravelSourceWrapper(TravelSource source) {
		this.source = source;
	}
	
	public TravelSource getSource() {
		return this.source;
	}
	
}
