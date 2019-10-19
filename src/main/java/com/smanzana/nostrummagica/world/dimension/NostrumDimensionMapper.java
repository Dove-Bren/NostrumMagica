package com.smanzana.nostrummagica.world.dimension;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

/**
 * Maps between players and their unique dimensions
 * @author Skyler
 *
 */
public class NostrumDimensionMapper extends WorldSavedData {
	
	public static final String DATA_NAME = NostrumMagica.MODID + "_dimension_mappings";
	
	private Map<UUID, Integer> map;
	private int highestDimension;
	
	public NostrumDimensionMapper() {
		this(DATA_NAME);
	}

	public NostrumDimensionMapper(String name) {
		super(name);
		
		this.map = new HashMap<>();
		highestDimension = ModConfig.config.dimensionIndex();
	}
	
	public void registerDimensions() {
		for (Entry<UUID, Integer> row : map.entrySet()) {
			NostrumEmptyDimension.register(row.getValue(), row.getKey().toString());
		}
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
			
			int dim = nbt.getInteger(key);
			this.map.put(id, dim);
			if (dim > highestDimension) {
				highestDimension = dim;
			}
		}
		
		this.registerDimensions();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		for (Entry<UUID, Integer> row : map.entrySet()) {
			compound.setInteger(row.getKey().toString(), row.getValue());
		}
		
		return compound;
	}
	
	@Nullable
	public Integer lookup(UUID id) {
		return map.get(id);
	}
	
	public int register(UUID id) {
		int dimension;
		Integer existing = this.lookup(id);
		if (existing != null) {
			dimension = existing;
		} else {
			dimension = ++highestDimension;
		}
		
		map.put(id, dimension);
		NostrumEmptyDimension.register(dimension, id.toString());
		this.markDirty();
		
		return dimension;
	}

}
