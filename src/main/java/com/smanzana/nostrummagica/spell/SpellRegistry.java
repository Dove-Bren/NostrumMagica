package com.smanzana.nostrummagica.spell;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;

/**
 * Tags spells with IDs so that looking them up from the tome is easier.
 * Spells are written out server-side on exit and loaded back up on startup.
 * They are added upon creation to the registry as they made.<br />
 * The spell registry is initialized in preinit. Do not attempt to use it until
 * at least init
 * @author Skyler
 *
 */
public class SpellRegistry extends WorldSavedData {

	public static final String DATA_NAME =  NostrumMagica.MODID + "_SpellData";
	
	private Map<Integer, Spell> registry;
	private List<Integer> transients;
	private static final Random rand = new Random();
	
	public SpellRegistry() {
		this(DATA_NAME);
	}
	
	public SpellRegistry(String name) {
		super(name);

		registry = new HashMap<>();
		transients = new LinkedList<>();
	}
	
	private int newID() {
		int id = rand.nextInt();
		int tries = 0;
		while (registry.containsKey(id) && tries < 1000) {
			id = rand.nextInt();
		}
		
		if (registry.containsKey(id)) {
			// Do sequential. We have to find one.
			id = rand.nextInt();
			while (registry.containsKey(id))
				id++;
		}
		
		return id;
	}
	
	/**
	 * Registers a new spell, generating a new ID and returning it.
	 * The constructor of a spell already does this. So don't call it.
	 * @param spell
	 * @return
	 */
	public int register(Spell spell) {
		int id;
		synchronized(this) {
			id = newID();
			registry.put(id, spell);
		}
		
		this.markDirty();
		return id;
	}
	
	/**
	 * Registers a spell like normal, except marks it transient.
	 * This means it will not be saved out to disk when the server is saved.
	 * This is meant to be used for spells used by AI, etc that are constructed
	 * each time the server starts up
	 * @param spell
	 * @return
	 */
	public int registerTransient(Spell spell) {
		int id;
		synchronized(this) {
			id = register(spell);
			transients.add(id);
		}
		
		this.markDirty();
		return id;
	}
	
	/**
	 * Used to inject overrides from the server after the client has
	 * requested information about spells
	 * @param id
	 * @param spell
	 */
	public void override(int id, Spell spell) {
		synchronized(this) {
			registry.put(id, spell);
		}
		
		this.markDirty();
	}
	
	public void removeTransientStatus(Spell spell) {
		synchronized(this) {
			this.transients.remove((Integer) spell.getRegistryID());
		}
		
		this.markDirty();
	}
	
	public Spell lookup(int id) {
		synchronized(this) {
			return registry.get(id);
		}
	}
	
	public List<Spell> getAllSpells() {
		synchronized(this) {
			return Lists.newArrayList(registry.values());
		}
	}

	public void clear() {
		synchronized(this) {
			registry.clear();
		}
		
		this.markDirty();
	}
	
	public void evict(Spell spell) {
		int id = spell.getRegistryID();
		
		synchronized(this) {
			registry.remove(id);
		}
		
		this.markDirty();
	}

	@Override
	public void read(CompoundNBT nbt) {
		synchronized(this) {
			this.registry.clear();
			this.transients.clear();
			
			for (String key : nbt.keySet()) {
				int id;
				try {
					id = Integer.parseInt(key);
				} catch (NumberFormatException e) {
					NostrumMagica.logger.error("Failed to parse id for spell: " + key
						+ ". Spell will be ignored. This is very bad!");
					continue;
				}
				
				registry.put(id,
						Spell.fromNBT(nbt.getCompound(key), id));
			}
			
			NostrumMagica.logger.info("Loaded spell registry (" + registry.size() + " spells)");
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		
		NostrumMagica.logger.info("Saving Spell registry");
		
		synchronized(this) {
			for (Entry<Integer, Spell> entry : registry.entrySet()) {
				if (transients.isEmpty() || !transients.contains(entry.getKey()))
					if (!entry.getValue().isEmpty())
						nbt.put(entry.getKey() + "", entry.getValue().toNBT());
			}
		}
		
		return nbt;
	}
	
}
