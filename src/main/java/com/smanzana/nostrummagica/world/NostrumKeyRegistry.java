package com.smanzana.nostrummagica.world;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Allows blocks/items to grant 'keys' that can be used on other doors/chests to unlock them,
 * even when those things may not be loaded right now.
 * This is intended to be world-wide key-to-object mapping where keys are UUIDs, rather than something
 * cooler and more RPG-like where you have small keys and you consume a small key to open a door.
 * In a way, this is the same as the trigger mechanic on switch blocks etc. except the thing it's triggering
 * doesn't have to be loaded and respond right then.
 * @author Skyler
 *
 */
public class NostrumKeyRegistry extends WorldSavedData {
	
	public static class NostrumWorldKey {
		
		private static final String NBT_ID = "key_id";
		;//private static final String NBT_COLOR = "color";
		
		private final UUID id;
		;//private final int color;
		
		public NostrumWorldKey(@Nonnull UUID id) {;//, int colorARGB) {
			this.id = id;
			;//this.color = colorARGB;
		}
		
		public NostrumWorldKey() {
			this(UUID.randomUUID());;//, 0xFF000000 | NostrumMagica.rand.nextInt());
		}
		
		public CompoundNBT asNBT() {
			CompoundNBT nbt = new CompoundNBT();
			nbt.putUniqueId(NBT_ID, id);
			;//nbt.putInt(NBT_COLOR, color);
			return nbt;
		}
		
		public static NostrumWorldKey fromNBT(CompoundNBT nbt) {
			UUID id = nbt.getUniqueId(NBT_ID);
			;//int color = nbt.getInt(NBT_COLOR);
			return new NostrumWorldKey(id);//, color);
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof NostrumWorldKey) {
				NostrumWorldKey other = (NostrumWorldKey) o;
				if (other.id.equals(this.id)) {;// && other.color == this.color) {
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public int hashCode() {
			return id.hashCode();// * 37 + Integer.hashCode(color);
		}
		
		@Override
		public String toString() {
			return this.id.toString();// + " - " + this.color;
		}
	}
	
	public static final String DATA_NAME = NostrumMagica.MODID + "_world_keys";
	private static final String NBT_LIST = "key_list";
	
	private final Set<NostrumWorldKey> keys;
	
	public NostrumKeyRegistry() {
		this(DATA_NAME);
	}

	public NostrumKeyRegistry(String name) {
		super(name);
		
		this.keys = new HashSet<>();
	}

	@Override
	public void read(CompoundNBT nbt) {
		keys.clear();
		
		ListNBT list = nbt.getList(NBT_LIST, NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundNBT tag = list.getCompound(i);
			keys.add(NostrumWorldKey.fromNBT(tag));
		}
		
		NostrumMagica.logger.info("Loaded " + keys.size() + " world keys");
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		ListNBT list = new ListNBT();
		for (NostrumWorldKey key : keys) {
			CompoundNBT tag = key.asNBT();
			list.add(tag);
		}
		compound.put(NBT_LIST, list);
		
		NostrumMagica.logger.info("Saved " + keys.size() + " world keys");
		return compound;
	}
	
	public NostrumWorldKey addKey(NostrumWorldKey key) {
		keys.add(key);
		this.markDirty();
		return key;
	}
	
	public NostrumWorldKey addKey() {
		return addKey(new NostrumWorldKey());
	}
	
	public boolean hasKey(NostrumWorldKey key) {
		return keys.contains(key);
	}
	
	public boolean consumeKey(NostrumWorldKey key) {
		return keys.remove(key);
	}
	
	public void clearKeys() {
		keys.clear();
	}
}
