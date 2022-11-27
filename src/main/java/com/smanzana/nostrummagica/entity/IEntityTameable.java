package com.smanzana.nostrummagica.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;

/**
 * Like Vanilla's EntityTameable, but an interface instead
 * @author Skyler
 *
 */
public interface IEntityTameable extends IEntityOwnable {

	/**
	 * Returns whether this entity has been tamed and, thus, has an owner.
	 * @return
	 */
	public boolean isEntityTamed();
	
	/**
	 * Updated getOwner call. We must be owned by an EntityLivingBase
	 */
	default public EntityLivingBase getLivingOwner() {
		Entity owner = this.getOwner();
		if (owner instanceof EntityLivingBase) {
			return (EntityLivingBase) owner;
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
