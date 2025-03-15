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
		buf.writeEnum(value);
	}

	@Override
	public DragonBodyPartType read(PacketBuffer buf)  {
		return buf.readEnum(DragonBodyPartType.class);
	}

	@Override
	public DataParameter<DragonBodyPartType> createAccessor(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public DragonBodyPartType copy(DragonBodyPartType value) {
		return value;
	}
}