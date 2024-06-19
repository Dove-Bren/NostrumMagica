package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.entity.dragon.RedDragonEntity.DragonBodyPartType;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public final class RedDragonBodyPartTypeSerializer implements IDataSerializer<DragonBodyPartType> {
	
	public static RedDragonBodyPartTypeSerializer instance = new RedDragonBodyPartTypeSerializer();
	
	private RedDragonBodyPartTypeSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, DragonBodyPartType value) {
		buf.writeEnumValue(value);
	}

	@Override
	public DragonBodyPartType read(PacketBuffer buf)  {
		return buf.readEnumValue(DragonBodyPartType.class);
	}

	@Override
	public DataParameter<DragonBodyPartType> createKey(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public DragonBodyPartType copyValue(DragonBodyPartType value) {
		return value;
	}
}