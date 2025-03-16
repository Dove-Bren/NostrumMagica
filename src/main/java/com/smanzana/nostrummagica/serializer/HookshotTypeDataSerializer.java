package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.item.equipment.HookshotItem.HookshotType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public class HookshotTypeDataSerializer implements EntityDataSerializer<HookshotType> {
	
	public static final HookshotTypeDataSerializer instance = new HookshotTypeDataSerializer();
	
	private HookshotTypeDataSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, HookshotType value) {
		buf.writeEnum(value);
	}

	@Override
	public HookshotType read(FriendlyByteBuf buf)  {
		return buf.readEnum(HookshotType.class);
	}

	@Override
	public EntityDataAccessor<HookshotType> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public HookshotType copy(HookshotType value) {
		return value;
	}
}
