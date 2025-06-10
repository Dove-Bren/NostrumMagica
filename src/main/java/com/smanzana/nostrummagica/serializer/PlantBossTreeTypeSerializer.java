package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.entity.boss.plantboss.PlantBossEntity.PlantBossTreeType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class PlantBossTreeTypeSerializer implements EntityDataSerializer<PlantBossTreeType> {
	
	public static PlantBossTreeTypeSerializer instance = new PlantBossTreeTypeSerializer();
	
	private PlantBossTreeTypeSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, PlantBossTreeType value) {
		buf.writeEnum(value);
	}

	@Override
	public PlantBossTreeType read(FriendlyByteBuf buf)  {
		return buf.readEnum(PlantBossTreeType.class);
	}

	@Override
	public EntityDataAccessor<PlantBossTreeType> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public PlantBossTreeType copy(PlantBossTreeType value) {
		return value;
	}
}