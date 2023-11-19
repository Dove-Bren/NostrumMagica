package com.smanzana.nostrummagica.tiles;

import com.smanzana.nostrummagica.world.NostrumKeyRegistry.NostrumWorldKey;

public interface IWorldKeyHolder {

	public boolean hasWorldKey();
	
	public NostrumWorldKey getWorldKey();
	
	public void setWorldKey(NostrumWorldKey key);
	
}
