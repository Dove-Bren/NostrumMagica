package com.smanzana.nostrummagica.serializers;

import java.io.IOException;

import com.smanzana.nostrummagica.items.HookshotItem.HookshotType;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;

public class HookshotTypeDataSerializer implements DataSerializer<HookshotType> {
	
	public static final HookshotTypeDataSerializer instance = new HookshotTypeDataSerializer();
	
	private HookshotTypeDataSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, HookshotType value) {
		buf.writeEnumValue(value);
	}

	@Override
	public HookshotType read(PacketBuffer buf) throws IOException {
		return buf.readEnumValue(HookshotType.class);
	}

	@Override
	public DataParameter<HookshotType> createKey(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public HookshotType copyValue(HookshotType value) {
		return value;
	}
}
