package com.smanzana.nostrummagica.utils;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class Entities {

	public static @Nullable Entity FindEntity(World world, UUID id) {
		if (world instanceof ClientWorld) {
			Iterable<Entity> entities = ((ClientWorld)world).getAllEntities();
			for (Entity ent : entities) {
				if (ent.getUniqueID().equals(id)) {
					return ent;
				}
			}
		} else if (world instanceof ServerWorld) {
			return ((ServerWorld) world).getEntityByUuid(id);
		}
		
		return null;
	}
	
}
