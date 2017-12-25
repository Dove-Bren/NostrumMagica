package com.smanzana.nostrummagica.spells;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.smanzana.nostrummagica.NostrumMagica;

import java.util.Random;

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
	
	public int register(Spell spell) {
		int id = newID();
		registry.put(id, spell);
		
		return id;
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
					Spell.fromNBT(nbt.getCompoundTag(key)));
		}
	}
	
	public NBTTagCompound save() {
		NBTTagCompound nbt = new NBTTagCompound();
		
		for (Entry<Integer, Spell> entry : registry.entrySet()) {
			nbt.setTag(entry.getKey() + "", entry.getValue().toNBT());
		}
		
		return nbt;
	}
	
	
}
