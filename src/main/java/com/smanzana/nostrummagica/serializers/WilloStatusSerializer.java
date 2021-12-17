package com.smanzana.nostrummagica.serializers;

import java.io.IOException;

import com.smanzana.nostrummagica.entity.EntityWillo.WilloStatus;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;

public final class WilloStatusSerializer implements DataSerializer<WilloStatus> {
	
	public static WilloStatusSerializer instance = new WilloStatusSerializer();
	
	private WilloStatusSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, WilloStatus value) {
		buf.writeEnumValue(value);
	}

	@Override
	public WilloStatus read(PacketBuffer buf) throws IOException {
		return buf.readEnumValue(WilloStatus.class);
	}

	@Override
	public DataParameter<WilloStatus> createKey(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public WilloStatus copyValue(WilloStatus value) {
		return value;
	}
}