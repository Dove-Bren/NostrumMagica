package com.smanzana.nostrummagica.serializer;

import com.smanzana.petcommand.api.pet.PetInfo;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public final class PetJobSerializer implements IDataSerializer<PetInfo.PetAction> {
	
	private static PetJobSerializer instance = new PetJobSerializer();
	public static PetJobSerializer GetInstance() {
		return instance;
	}
	
	private PetJobSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, PetInfo.PetAction value) {
		buf.writeEnum(value);
	}

	@Override
	public PetInfo.PetAction read(PacketBuffer buf)  {
		return buf.readEnum(PetInfo.PetAction.class);
	}

	@Override
	public DataParameter<PetInfo.PetAction> createAccessor(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public PetInfo.PetAction copy(PetInfo.PetAction value) {
		return value;
	}
}