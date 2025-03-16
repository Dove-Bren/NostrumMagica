package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.entity.WilloEntity.WilloStatus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class WilloStatusSerializer implements EntityDataSerializer<WilloStatus> {
	
	public static WilloStatusSerializer instance = new WilloStatusSerializer();
	
	private WilloStatusSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, WilloStatus value) {
		buf.writeEnum(value);
	}

	@Override
	public WilloStatus read(FriendlyByteBuf buf)  {
		return buf.readEnum(WilloStatus.class);
	}

	@Override
	public EntityDataAccessor<WilloStatus> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public WilloStatus copy(WilloStatus value) {
		return value;
	}
}