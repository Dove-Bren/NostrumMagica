package com.smanzana.nostrummagica.loretag;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/**
 * Subclass of ILoreTagged for entities that want an entity display in the info screen.
 * @author Skyler
 *
 */
public interface IEntityLoreTagged<E extends Entity> extends ILoreTagged {
	
	public EntityType<? extends E> getEntityType();
	
	public default @Nonnull E makeEntity(World world) {
		return this.getEntityType().create(world);
	}
}
