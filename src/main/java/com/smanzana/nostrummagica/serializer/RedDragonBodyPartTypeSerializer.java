package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.entity.boss.reddragon.RedDragonEntity.DragonBodyPartType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class RedDragonBodyPartTypeSerializer implements EntityDataSerializer<DragonBodyPartType> {
	
	public static RedDragonBodyPartTypeSerializer instance = new RedDragonBodyPartTypeSerializer();
	
	private RedDragonBodyPartTypeSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, DragonBodyPartType value) {
		buf.writeEnum(value);
	}

	@Override
	public DragonBodyPartType read(FriendlyByteBuf buf)  {
		return buf.readEnum(DragonBodyPartType.class);
	}

	@Override
	public EntityDataAccessor<DragonBodyPartType> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public DragonBodyPartType copy(DragonBodyPartType value) {
		return value;
	}
}