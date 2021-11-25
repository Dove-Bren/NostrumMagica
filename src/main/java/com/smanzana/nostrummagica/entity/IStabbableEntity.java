package com.smanzana.nostrummagica.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Entity might have a special interaction with the SoulDagger.
 */
public interface IStabbableEntity {

	/**
	 * Called (server-side) when an entity is stabbed with a SoulDagger.
	 * Return whether regular stabbing damage should BE SKIPPED afterwards.
	 * Returning false means treat like a normal entity.
	 * @param stabber
	 * @param stabbingItem
	 * @return
	 */
	public boolean onSoulStab(EntityLivingBase stabber, ItemStack stabbingItem);
	
}
