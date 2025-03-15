package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.entity.WilloEntity.WilloStatus;

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
		buf.writeEnum(value);
	}

	@Override
	public WilloStatus read(PacketBuffer buf)  {
		return buf.readEnum(WilloStatus.class);
	}

	@Override
	public DataParameter<WilloStatus> createAccessor(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public WilloStatus copy(WilloStatus value) {
		return value;
	}
}