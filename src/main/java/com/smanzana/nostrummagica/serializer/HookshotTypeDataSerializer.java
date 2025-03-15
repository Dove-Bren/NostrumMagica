package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.item.equipment.HookshotItem.HookshotType;

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
		buf.writeEnum(value);
	}

	@Override
	public HookshotType read(PacketBuffer buf)  {
		return buf.readEnum(HookshotType.class);
	}

	@Override
	public DataParameter<HookshotType> createAccessor(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public HookshotType copy(HookshotType value) {
		return value;
	}
}
