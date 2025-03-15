package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.entity.plantboss.PlantBossEntity.PlantBossTreeType;

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
		buf.writeEnum(value);
	}

	@Override
	public PlantBossTreeType read(PacketBuffer buf)  {
		return buf.readEnum(PlantBossTreeType.class);
	}

	@Override
	public DataParameter<PlantBossTreeType> createAccessor(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public PlantBossTreeType copy(PlantBossTreeType value) {
		return value;
	}
}