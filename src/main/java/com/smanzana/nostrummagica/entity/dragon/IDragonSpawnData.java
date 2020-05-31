package com.smanzana.nostrummagica.entity.dragon;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class IDragonSpawnData<T extends ITameDragon> {
	
	public static interface IDragonSpawnFactory {
		public IDragonSpawnData<?> create(NBTTagCompound nbt);
	}
	
	private static Map<String, IDragonSpawnFactory> spawnMap = null;
	
	public static void register(String key, IDragonSpawnFactory factory) {
		if (spawnMap == null) {
			spawnMap = new HashMap<>();
		}
		
		spawnMap.put(key, factory);
	}
	
	public static IDragonSpawnFactory lookupFactory(String key) {
		if (spawnMap == null) {
			spawnMap = new HashMap<>();
		}
		
		return spawnMap.get(key);
	}
	
	public abstract void writeToNBT(NBTTagCompound nbt);
	public abstract T spawnDragon(World world, double x, double y, double z);
	public abstract String getKey();
}
