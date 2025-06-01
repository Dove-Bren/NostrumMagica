package com.smanzana.nostrummagica.capabilities;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.ItemImbuement;

public interface IImbuedProjectile {
	
	public void setImbuement(@Nullable ItemImbuement imbuement);
	
	public @Nullable ItemImbuement getImbuement();
	
}
