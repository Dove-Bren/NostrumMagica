package com.smanzana.nostrummagica.serializer;

import com.smanzana.petcommand.api.pet.PetInfo;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class PetJobSerializer implements EntityDataSerializer<PetInfo.PetAction> {
	
	private static PetJobSerializer instance = new PetJobSerializer();
	public static PetJobSerializer GetInstance() {
		return instance;
	}
	
	private PetJobSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, PetInfo.PetAction value) {
		buf.writeEnum(value);
	}

	@Override
	public PetInfo.PetAction read(FriendlyByteBuf buf)  {
		return buf.readEnum(PetInfo.PetAction.class);
	}

	@Override
	public EntityDataAccessor<PetInfo.PetAction> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public PetInfo.PetAction copy(PetInfo.PetAction value) {
		return value;
	}
}