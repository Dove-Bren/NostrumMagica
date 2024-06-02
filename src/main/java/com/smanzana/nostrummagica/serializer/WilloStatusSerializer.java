package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.entity.EntityWillo.WilloStatus;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public final class WilloStatusSerializer implements IDataSerializer<WilloStatus> {
	
	public static WilloStatusSerializer instance = new WilloStatusSerializer();
	
	private WilloStatusSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, WilloStatus value) {
		buf.writeEnumValue(value);
	}

	@Override
	public WilloStatus read(PacketBuffer buf)  {
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