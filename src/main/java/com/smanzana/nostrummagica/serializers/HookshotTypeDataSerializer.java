package com.smanzana.nostrummagica.serializers;

import com.smanzana.nostrummagica.items.HookshotItem.HookshotType;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public class HookshotTypeDataSerializer implements IDataSerializer<HookshotType> {
	
	public static final HookshotTypeDataSerializer instance = new HookshotTypeDataSerializer();
	
	private HookshotTypeDataSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, HookshotType value) {
		buf.writeEnumValue(value);
	}

	@Override
	public HookshotType read(PacketBuffer buf)  {
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
