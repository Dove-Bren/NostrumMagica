package com.smanzana.nostrummagica.serializers;

import java.io.IOException;

import com.smanzana.nostrummagica.entity.EntityArcaneWolf.ArcaneWolfElementalType;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;

public final class ArcaneWolfElementalTypeSerializer implements DataSerializer<ArcaneWolfElementalType> {
	
	public static ArcaneWolfElementalTypeSerializer instance = new ArcaneWolfElementalTypeSerializer();
	
	private ArcaneWolfElementalTypeSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, ArcaneWolfElementalType value) {
		buf.writeEnumValue(value);
	}

	@Override
	public ArcaneWolfElementalType read(PacketBuffer buf) throws IOException {
		return buf.readEnumValue(ArcaneWolfElementalType.class);
	}

	@Override
	public DataParameter<ArcaneWolfElementalType> createKey(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public ArcaneWolfElementalType copyValue(ArcaneWolfElementalType value) {
		return value;
	}
}