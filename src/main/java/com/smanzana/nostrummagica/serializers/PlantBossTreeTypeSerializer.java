package com.smanzana.nostrummagica.serializers;

import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss.PlantBossTreeType;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public final class PlantBossTreeTypeSerializer implements IDataSerializer<PlantBossTreeType> {
	
	public static PlantBossTreeTypeSerializer instance = new PlantBossTreeTypeSerializer();
	
	private PlantBossTreeTypeSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, PlantBossTreeType value) {
		buf.writeEnumValue(value);
	}

	@Override
	public PlantBossTreeType read(PacketBuffer buf)  {
		return buf.readEnumValue(PlantBossTreeType.class);
	}

	@Override
	public DataParameter<PlantBossTreeType> createKey(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public PlantBossTreeType copyValue(PlantBossTreeType value) {
		return value;
	}
}