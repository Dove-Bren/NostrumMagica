package com.smanzana.nostrummagica.tile;

import com.smanzana.nostrummagica.world.NostrumKeyRegistry.NostrumWorldKey;

public interface IWorldKeyHolder {

	public boolean hasWorldKey();
	
	public NostrumWorldKey getWorldKey();
	
	public void setWorldKey(NostrumWorldKey key);
	
}
