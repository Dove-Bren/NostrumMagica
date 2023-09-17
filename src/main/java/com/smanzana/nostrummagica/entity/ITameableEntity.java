package com.smanzana.nostrummagica.entity;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/**
 * Like Vanilla's EntityTameable, but an interface instead
 * @author Skyler
 *
 */
public interface ITameableEntity {

	public @Nullable Entity getOwner();
	
	/**
	 * Returns whether this entity has been tamed and, thus, has an owner.
	 * @return
	 */
	public boolean isEntityTamed();
	
	/**
	 * Updated getOwner call. We must be owned by an LivingEntity
	 */
	default public LivingEntity getLivingOwner() {
		Entity owner = this.getOwner();
		if (owner instanceof LivingEntity) {
			return (LivingEntity) owner;
		}
		return null;
	}

	/**
	 * Returns whether the entity is sitting.
	 * Not all entities must implement sitting. Simply always return false.
	 * @return
	 */
	public boolean isEntitySitting();
	
}
