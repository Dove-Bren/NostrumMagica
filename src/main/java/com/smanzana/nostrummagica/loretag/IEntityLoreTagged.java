package com.smanzana.nostrummagica.loretag;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Subclass of ILoreTagged for entities that want an entity display in the info screen.
 * @author Skyler
 *
 */
public interface IEntityLoreTagged<E extends Entity> extends ILoreTagged {
	
	public EntityType<? extends E> getEntityType();
	
	public default @Nonnull E makeEntity(Level world) {
		return this.getEntityType().create(world);
	}
}
