package com.smanzana.nostrummagica.entity;

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
	public boolean isTamed();
	
	/**
	 * Updated getOwner call. We must be owned by an EntityLivingBase
	 */
	@Override
	public EntityLivingBase getOwner();

	/**
	 * Returns whether the entity is sitting.
	 * Not all entities must implement sitting. Simply always return false.
	 * @return
	 */
	public boolean isSitting();
	
}
