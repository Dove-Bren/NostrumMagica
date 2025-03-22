package com.smanzana.nostrummagica.tile;

/**
 * Just a convenience rerouter for ticking method, since tickers are now block side.
 * Used same name as 1.16.5 ticking interface
 */
public interface TickableBlockEntity {

	public void tick();
	
	public static <T extends TickableBlockEntity> void Tick(T entity) {
		entity.tick();
	}
	
}
