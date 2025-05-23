package com.smanzana.nostrummagica.pet;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Registry for pet souls.
 * Some pets can give their materialized soul to their owners, allowing for ressurection should they die.
 * This class manages saving those pets, and has an ID system to help solve pet duplication (like when pets are unloaded).
 * 
 * Pets should be registered when they're made immortal.
 * Every time a pet is loaded, it should look up the current world ID for it and remove itself if they do not match.
 * Any time a pet is re-added to the world, it's given a new world ID.
 * @author Skyler
 *
 */
public class PetSoulRegistry extends SavedData {
	
	private static final class SoulEntry {
		
		private static final String NBT_ENTRY_WORLDID = "worldID";
		private static final String NBT_ENTRY_SNAPSHOT = "snapshot";
		
		protected UUID worldID;
		protected @Nullable CompoundTag snapshot;
		
		public SoulEntry() {
			this(UUID.randomUUID());
		}
		
		public SoulEntry(UUID worldID) {
			this(worldID, null);
		}
		
		protected SoulEntry(UUID worldID, @Nullable CompoundTag snapshot) {
			setWorldID(worldID);
			setSnapshot(snapshot);
		}
		
		public void setWorldID(UUID worldID) {
			this.worldID = worldID;
		}
		
		public UUID getWorldID() {
			return this.worldID;
		}
		
		public void setSnapshot(CompoundTag nbt) {
			if (nbt != null) {
				this.snapshot = nbt.copy();
			} else {
				this.snapshot = nbt;
			}
		}
		
		public @Nullable CompoundTag getSnapshot() {
			return snapshot;
		}
		
		public CompoundTag writeToNBT(@Nullable CompoundTag nbt) {
			if (nbt == null) {
				nbt = new CompoundTag();
			}
			
			nbt.putUUID(NBT_ENTRY_WORLDID, worldID);
			if (this.snapshot != null) {
				nbt.put(NBT_ENTRY_SNAPSHOT, snapshot);
			}
			
			return nbt;
		}
		
		public static SoulEntry FromNBT(CompoundTag nbt) {
			UUID worldID = nbt.hasUUID(NBT_ENTRY_WORLDID) ? nbt.getUUID(NBT_ENTRY_WORLDID) : null;
			if (worldID == null) {
				NostrumMagica.logger.warn("Failed to deserialize PetSoul key UUID!");
				worldID = UUID.randomUUID();
			}
			
			@Nullable CompoundTag snapshot = nbt.getCompound(NBT_ENTRY_SNAPSHOT);
			
			return new SoulEntry(worldID, snapshot);
		}
	}

	public static final String DATA_NAME =  NostrumMagica.MODID + "_PetSoulData";
	
	private Map<UUID, SoulEntry> soulMap;
	
	public PetSoulRegistry() {
		super();
		
		this.soulMap = new HashMap<>();
	}
	
	public static PetSoulRegistry Load(CompoundTag nbt) {
		PetSoulRegistry registry = new PetSoulRegistry();
		registry.load(nbt);
		return registry;
	}

	public void load(CompoundTag nbt) {
		synchronized(soulMap) {
			soulMap.clear();
			
			// NBT has each entry in the map as a single element, where the key name
			// is the stringified UUID key and the value is nbt-ified SoulEntries.
			for (String key : nbt.getAllKeys()) {
				UUID id = null;
				try {
					id = UUID.fromString(key);
				} catch (Exception e) {
					NostrumMagica.logger.error("Failed to parse UUID key: " + key);
					e.printStackTrace();
					id = null;
				}
				
				if (id == null) {
					continue;
				}
				
				SoulEntry entry = SoulEntry.FromNBT(nbt.getCompound(key));
				if (entry == null) {
					NostrumMagica.logger.warn("Ignoring entry for key " + key);
					continue;
				}
				
				this.soulMap.put(id, entry);
			}
		}
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		synchronized(soulMap) {
			for (Entry<UUID, SoulEntry> entry : soulMap.entrySet()) {
				compound.put(
						entry.getKey().toString(),
						entry.getValue().writeToNBT(null)
					);
			}
		}
		return compound;
	}
	
	/**
	 * Registers a pet with a soul to be tracked across death and server restarts.
	 * Should only ever be called ONCE for each unique pet.
	 * Returns the WorldID to assign to the pet.
	 * @param pet
	 * @return
	 */
	public UUID registerPet(IPetWithSoul pet) {
		UUID key = pet.getPetSoulID();
		SoulEntry entry = new SoulEntry();
		
		synchronized(soulMap) {
			if (soulMap.containsKey(key)) {
				NostrumMagica.logger.error("Got another registration for ID " + key.toString() + "! Multiple registration attempts? Key collision??");
				(new Exception()).printStackTrace();
			}
			
			soulMap.put(key, entry);
		}
		this.setDirty();
		return entry.getWorldID();
	}
	
	protected @Nullable UUID getCurrentWorldID(IPetWithSoul pet) {
		UUID key = pet.getPetSoulID();
		SoulEntry entry = null;
		UUID ret = null;
		
		synchronized(soulMap) {
			entry = soulMap.get(key);
		}
		
		if (entry != null) {
			ret = entry.getWorldID();
		}
		return ret;
	}
	
	/**
	 * Check if the provided worldID matches what's registered as the current one.
	 * A false here likely means the calling instance is an old one and should be discarded.
	 * @param pet
	 * @param currentID
	 * @return
	 */
	public boolean checkCurrentWorldID(IPetWithSoul pet) {
		final UUID id = getCurrentWorldID(pet);
		final UUID currentID = pet.getWorldID();
		
		if (currentID == null || id == null) {
			return currentID == null && id == null;
		}
		
		return currentID.equals(id);
	}
	
	public void removePet(IPetWithSoul pet) {
		UUID key = pet.getPetSoulID();
		synchronized(soulMap) {
			soulMap.remove(key);
		}
		this.setDirty();
	}
	
	/**
	 * Rotate out the world ID for the pet to a new randomly-generated one.
	 * This method returns the new ID for storage.
	 * @param pet
	 * @return
	 */
	public UUID rotateWorldID(IPetWithSoul pet) {
		return rotateWorldID(pet.getPetSoulID());
	}
	
	public UUID rotateWorldID(UUID rawPetID) {
		final UUID newWorldID = UUID.randomUUID();
		synchronized(soulMap) {
			SoulEntry entry = soulMap.get(rawPetID);
			if (entry == null) {
				entry = new SoulEntry(newWorldID);
				soulMap.put(rawPetID, entry);
			} else {
				entry.setWorldID(newWorldID);
			}
		}
		this.setDirty();
		return newWorldID;
	}
	
	public void snapshotPet(IPetWithSoul pet) {
		final UUID key = pet.getPetSoulID();
		final CompoundTag snapshot = pet.serializeNBT();
		synchronized(soulMap) {
			SoulEntry entry = soulMap.get(key);
			if (entry == null) {
				entry = new SoulEntry();
				soulMap.put(key, entry);
			}
			entry.setSnapshot(snapshot);
		}
		this.setDirty();
	}
	
	public @Nullable CompoundTag getPetSnapshot(IPetWithSoul pet) {
		return this.getPetSnapshot(pet.getPetSoulID());
	}
	
	public @Nullable CompoundTag getPetSnapshot(UUID rawPetID) {
		CompoundTag ret = null;
		synchronized(soulMap) {
			SoulEntry entry = soulMap.get(rawPetID);
			if (entry != null) {
				ret = entry.getSnapshot();
			}
		}
		return ret;
	}
}
