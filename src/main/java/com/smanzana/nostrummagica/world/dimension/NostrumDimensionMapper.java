package com.smanzana.nostrummagica.world.dimension;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;

/**
 * Maps between player and their unique offset in the Sorcery dimension.
 * @author Skyler
 *
 */
public class NostrumDimensionMapper extends WorldSavedData {
	
	public static class NostrumDimensionOffset {
		
		private static final String NBT_X = "x";
		private static final String NBT_Z = "z";
		
		public int offsetX; // expressed in "OFFSET_CHUNK_LEN"
		public int offsetZ;
		
		public NostrumDimensionOffset(int x, int z) {
			offsetX = x;
			offsetZ = z;
		}
		
		public int minBlockX() {
			return offsetX * OFFSET_CHUNK_LEN * 16;
		}
		
		public int minBlockZ() {
			return offsetZ * OFFSET_CHUNK_LEN * 16;
		}
		
		public int maxBlockX() {
			return ((offsetX+1) * OFFSET_CHUNK_LEN * 16) - 1;
		}
		
		public int maxBlockZ() {
			return ((offsetZ + 1) * OFFSET_CHUNK_LEN * 16) - 1;
		}
		
		public BlockPos getCenterPos(int y) {
			return new BlockPos(
					((offsetX * OFFSET_CHUNK_LEN) + (OFFSET_CHUNK_LEN / 2)) * 16,
					y,
					((offsetZ * OFFSET_CHUNK_LEN) + (OFFSET_CHUNK_LEN / 2)) * 16);
		}
		
		/**
		 * When we find conflicted offsets, we need to move over. We do so by shifting mostly in X to tryh and find a free spot.
		 */
		private void bump() {
			this.offsetX += 14;
			this.offsetZ += 5;
		}
		
		private NBTTagCompound asNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger(NBT_X, this.offsetX);
			nbt.setInteger(NBT_Z, this.offsetZ);
			return nbt;
		}
		
		private static NostrumDimensionOffset fromNBT(NBTTagCompound nbt) {
			int x = nbt.getInteger(NBT_X);
			int z = nbt.getInteger(NBT_Z);
			return new NostrumDimensionOffset(x, z);
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof NostrumDimensionOffset) {
				NostrumDimensionOffset other = (NostrumDimensionOffset) o;
				if (other.offsetX == this.offsetX && other.offsetZ == this.offsetZ) {
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public int hashCode() {
			return (offsetX & offsetZ) ^ ((offsetX << 8) & (offsetZ >> 8));
		}
	}
	
	private static boolean registered = false;
	
	public static void registerDimensions() {
		if (!registered) {
			registered = true;
			NostrumEmptyDimension.register(ModConfig.config.sorceryDimensionIndex(), "SorceryDim");
		}
	}
	
	public static final int OFFSET_CHUNK_LEN = (5120 / 16);
	public static final String DATA_NAME = NostrumMagica.MODID + "_dimension_mappings";
	
	/**
	 * Generate an offset for a given uuid.
	 * This offset MAY NOT be free.
	 * @param uuid
	 * @return
	 */
	public static NostrumDimensionOffset GetDefaultOffset(UUID uuid) {
		int hash = uuid.hashCode();
		hash &= System.currentTimeMillis() & (0xFFFFFFFF); //d1a76729-1ccb-3de3-b2b3-c7efa54f7c6f
		
		// X will be least and second-most significant bytes added (overflow discarded)
		// Z will be same but with most significant and second-least significant
		int x = 255 & (((hash & 0x00FF0000) >> 16) + (hash & 0x000000FF));
		int z = 255 & (((hash & 0xFF000000) >> 24) + ((hash & 0x0000FF00) >> 24));
		return new NostrumDimensionOffset(x, z);
	}
	
	private Map<UUID, NostrumDimensionOffset> map;
	
	public NostrumDimensionMapper() {
		this(DATA_NAME);
	}

	public NostrumDimensionMapper(String name) {
		super(name);
		
		this.map = new HashMap<>();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		for (String key : nbt.getKeySet()) {
			UUID id;
			
			try {
				id = UUID.fromString(key);
			} catch (Exception e) {
				id = null;
			}
			
			if (id == null) {
				NostrumMagica.logger.warn("Failed to parse Dimension mapping with key: " + key);
				continue;
			}
			
			NBTTagCompound tag = nbt.getCompoundTag(key);
			this.map.put(id, NostrumDimensionOffset.fromNBT(tag));
		}
		
		NostrumMagica.logger.info("Loaded " + map.size() + " dimension offsets");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		int count = 0;
		for (Entry<UUID, NostrumDimensionOffset> row : map.entrySet()) {
			NBTTagCompound tag = row.getValue().asNBT();
			compound.setTag(row.getKey().toString(), tag);
			count++;
		}
		
		NostrumMagica.logger.info("Saved " + count + " dynamic dimension offsets");
		return compound;
	}
	
	public NostrumDimensionOffset register(UUID id) {
		
		NostrumDimensionOffset existing = this.getOffset(id);
		if (existing != null) {
			return existing;
		}
		
		existing = GetDefaultOffset(id);
		
		while (map.containsValue(existing)) {
			existing.bump();
		}
		map.put(id, existing);
		this.markDirty();
		
		return existing;
	}
	
	public void unregisterAll() {
		map.clear();
	}

	@Nullable
	public NostrumDimensionOffset getOffset(UUID id) {
		return map.get(id);
	}
}
