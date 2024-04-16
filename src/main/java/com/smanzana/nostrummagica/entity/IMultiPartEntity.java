package com.smanzana.nostrummagica.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public interface IMultiPartEntity {

	World getWorld();

    boolean attackEntityFromPart(MultiPartEntityPart<?> part, DamageSource source, float damage);
    
    Entity[] getEnityParts();
    
    // Called on the client when a client part uses the parent ID to find and match with the parent.
    // Parent's can use this to check if the part is really one of theirs (not an orphan) and to
    // keep their own track of children if needed.
    default boolean attachClientEntity(IMultiPartEntityPart<?> part) { return true; };
	
}
