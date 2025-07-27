package com.smanzana.nostrummagica.pet;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IPetWithSoul {

	public @Nonnull UUID getPetSoulID();
	
	public @Nonnull UUID getWorldID();
	
	public CompoundTag serializeNBT();

	
	/**
	 * Convenience wrapper for recreating a generic entity out of a saved snapshot nbt.
	 * Individuals items etc. that may do the spawning should wrap this and, say, set the new
	 * worldID on the entity.
	 * This does NOT add the entity to the world.
	 * @param world
	 * @param pos
	 * @param snapshot
	 * @return
	 */
	public static @Nullable Entity CreatePetFromSnapshot(Level world, Vec3 pos, CompoundTag snapshot) {
		// return AnvilChunkLoader.readWorldEntityPos(snapshot, world, pos.x, pos.y, pos.z, worldSpawn);
		
		// Could use "EntityType.loadEntityRecursive" which is more like readWorldEntityPos in that it handles passengers,
		// But we don't want to support passengers.
		// Want to use "EntityType.loadEntity" but it's private, so do our own exception handling
		Entity ent;
		try {
			ent = EntityType.create(snapshot, world).orElse(null);
			if (ent != null) {
				ent.setPos(pos.x, pos.y, pos.z);
			}
		} catch (Exception e) {
			NostrumMagica.logger.error("Failed to spawn pet from snapshot: " + e.getMessage());
			e.printStackTrace();
			ent = null;
		}
		
		return ent;
	}
}
