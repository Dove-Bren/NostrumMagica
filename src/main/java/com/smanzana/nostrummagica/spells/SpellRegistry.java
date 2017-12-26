package com.smanzana.nostrummagica.spells;

import java.util.HashMap;
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
 * They are added upon creation to the registry as they made.
 * @author Skyler
 *
 */
public class SpellRegistry {

	private Map<Integer, Spell> registry;
	private static final Random rand = new Random();
	
	public SpellRegistry() {
		registry = new HashMap<>();
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
		int id = newID();
		registry.put(id, spell);
		
		return id;
	}
	
	/**
	 * Used to inject overrides from the server after the client has
	 * requested information about spells
	 * @param id
	 * @param spell
	 */
	public void override(int id, Spell spell) {
		registry.put(id, spell);
	}
	
	public Spell lookup(int id) {
		return registry.get(id);
	}
	
	public void loadFromNBT(NBTTagCompound nbt) {
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
	
	public NBTTagCompound save() {
		NBTTagCompound nbt = new NBTTagCompound();
		
		for (Entry<Integer, Spell> entry : registry.entrySet()) {
			nbt.setTag(entry.getKey() + "", entry.getValue().toNBT());
		}
		
		return nbt;
	}

	public List<Spell> getAllSpells() {
		return Lists.newArrayList(registry.values());
	}

	public void clear() {
		registry.clear();
	}
	
	
}
