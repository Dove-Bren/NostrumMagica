package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.Entity;

public interface IEnchantableEntity {

	/**
	 * Return whether this entity is enchantable.
	 * @param entity
	 * @return
	 */
	public boolean canEnchant(Entity entity, EMagicElement element, int power);

	public boolean attemptEnchant(Entity entity, EMagicElement element, int power);
	
}
