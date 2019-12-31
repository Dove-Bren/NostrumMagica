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
 * Maps between players and their unique dimensions.
 * Note: Dimensions capped by some number in config.
 * @author Skyler
 *
 */
public class NostrumDimensionMapper extends WorldSavedData {
	
	private static boolean registered = false;
	
	public static void registerDimensions() {
		if (!registered) {
			registered = true;
			for (int i = 0; i < ModConfig.config.dimensionCount(); i++) {
				NostrumEmptyDimension.register(ModConfig.config.dimensionStartIndex() + i, i + "");
			}
		}
	}
	
	public static final String DATA_NAME = NostrumMagica.MODID + "_dimension_mappings";
	
	private Map<UUID, Integer> map;
	private int highestDimension;
	
	public NostrumDimensionMapper() {
		this(DATA_NAME);
	}

	public NostrumDimensionMapper(String name) {
		super(name);
		
		this.map = new HashMap<>();
		highestDimension = ModConfig.config.dimensionStartIndex();
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
		
		System.out.println("After load, max is: " + highestDimension);//TODO
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		int count = 0;
		for (Entry<UUID, Integer> row : map.entrySet()) {
			compound.setInteger(row.getKey().toString(), row.getValue());
			count++;
		}
		
		NostrumMagica.logger.info("Saved " + count + " dynamic dimensions");
		return compound;
	}
	
	@Nullable
	public Integer lookup(UUID id) {
		return map.get(id);
	}
	
	public int register(UUID id) {
		Integer existing = this.lookup(id);
		if (existing != null) {
			return existing;
		}
		
		int dimension = ++highestDimension;
		while (!registerInternal(id, dimension)) {
			System.out.println("*");//TODO
			dimension = ++highestDimension;
		}
		this.markDirty();
		
		System.out.println("After register, max is: " + highestDimension);//TODO
		return dimension;
	}
	
	private boolean registerInternal(UUID id, int dimension) {
//		if (!NostrumEmptyDimension.register(dimension, id.toString())) {
//			return false;
//		}
		map.put(id, dimension);
		return true;
	}
	
	public void unregisterAll() {
		map.clear();
		highestDimension = ModConfig.config.dimensionStartIndex();
		System.out.println("After unregister, max is: " + highestDimension);//TODO
	}

//	@SideOnly(Side.CLIENT)
//	public void override(EntityPlayer thePlayer, int dimension) {
//		//this.unregisterAll();
//		this.registerInternal(thePlayer.getUniqueID(), dimension);
//	}
}
