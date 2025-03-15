package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.ArcaneWolfElementalType;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public final class ArcaneWolfElementalTypeSerializer implements IDataSerializer<ArcaneWolfElementalType> {
	
	public static ArcaneWolfElementalTypeSerializer instance = new ArcaneWolfElementalTypeSerializer();
	
	private ArcaneWolfElementalTypeSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, ArcaneWolfElementalType value) {
		buf.writeEnum(value);
	}

	@Override
	public ArcaneWolfElementalType read(PacketBuffer buf) {
		return buf.readEnum(ArcaneWolfElementalType.class);
	}

	@Override
	public DataParameter<ArcaneWolfElementalType> createAccessor(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public ArcaneWolfElementalType copy(ArcaneWolfElementalType value) {
		return value;
	}
}