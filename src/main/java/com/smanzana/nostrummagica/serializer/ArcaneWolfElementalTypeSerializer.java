package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.ArcaneWolfElementalType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class ArcaneWolfElementalTypeSerializer implements EntityDataSerializer<ArcaneWolfElementalType> {
	
	public static ArcaneWolfElementalTypeSerializer instance = new ArcaneWolfElementalTypeSerializer();
	
	private ArcaneWolfElementalTypeSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, ArcaneWolfElementalType value) {
		buf.writeEnum(value);
	}

	@Override
	public ArcaneWolfElementalType read(FriendlyByteBuf buf) {
		return buf.readEnum(ArcaneWolfElementalType.class);
	}

	@Override
	public EntityDataAccessor<ArcaneWolfElementalType> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public ArcaneWolfElementalType copy(ArcaneWolfElementalType value) {
		return value;
	}
}