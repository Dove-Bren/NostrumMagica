package com.smanzana.nostrummagica.spells;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Tags spells with IDs so that looking them up from the tome is easier.
 * Spells are written out server-side on exit and loaded back up on startup.
 * They are added upon creation to the registry as they made.<br />
 * The spell registry is initialized in preinit. Do not attempt to use it until
 * at least init
 * @author Skyler
 *
 */
public class SpellRegistry {

	private Map<Integer, Spell> registry;
	private List<Integer> transients;
	private static final Random rand = new Random();
	
	public SpellRegistry() {
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
		synchronized(this) {
			int id = register(spell);
			transients.add(id);
			return id;
		}
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
	}
	
	public Spell lookup(int id) {
		synchronized(this) {
			return registry.get(id);
		}
	}
	
	public void loadFromNBT(NBTTagCompound nbt) {
		synchronized(this) {
			for (String key : nbt.getKeySet()) {
				int id;
				try {
					id = Integer.parseInt(key);
				} catch (NumberFormatException e) {
					NostrumMagica.logger.error("Failed to parse id for spell: " + key
						+ ". Spell will be ignored. This is very bad!");
					continue;
				}
				
				registry.put(id,
						Spell.fromNBT(nbt.getCompoundTag(key), id));
			}
		}
	}
	
	public NBTTagCompound save() {
		NBTTagCompound nbt = new NBTTagCompound();
		
		synchronized(this) {
			for (Entry<Integer, Spell> entry : registry.entrySet()) {
				if (transients.isEmpty() || !transients.contains(entry.getKey()))
					if (!entry.getValue().isEmpty())
						nbt.setTag(entry.getKey() + "", entry.getValue().toNBT());
			}
		}
		
		return nbt;
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
	}
	
	
}
