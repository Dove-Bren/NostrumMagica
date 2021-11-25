package com.smanzana.nostrummagica.pet;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

public interface IPetWithSoul {

	public @Nonnull UUID getPetSoulID();
	
	public @Nonnull UUID getWorldID();
	
	public NBTTagCompound serializeNBT();

	
	/**
	 * Convenience wrapper for spawning a generic entity out of a saved snapshot nbt.
	 * Individuals items etc. that may do the spawning should wrap this and, say, set the new
	 * worldID on the entity.
	 * @param world
	 * @param pos
	 * @param snapshot
	 * @return
	 */
	public static @Nullable Entity SpawnPetFromSnapshot(World world, Vec3d pos, NBTTagCompound snapshot, boolean worldSpawn) {
		return AnvilChunkLoader.readWorldEntityPos(snapshot, world, pos.x, pos.y, pos.z, worldSpawn);
	}
}
