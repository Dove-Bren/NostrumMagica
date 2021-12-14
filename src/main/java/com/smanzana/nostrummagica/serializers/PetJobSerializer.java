package com.smanzana.nostrummagica.serializers;

import java.io.IOException;

import com.smanzana.nostrummagica.pet.PetInfo;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;

public final class PetJobSerializer implements DataSerializer<PetInfo.PetAction> {
	
	public static PetJobSerializer instance = new PetJobSerializer();
	
	private PetJobSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, PetInfo.PetAction value) {
		buf.writeEnumValue(value);
	}

	@Override
	public PetInfo.PetAction read(PacketBuffer buf) throws IOException {
		return buf.readEnumValue(PetInfo.PetAction.class);
	}

	@Override
	public DataParameter<PetInfo.PetAction> createKey(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public PetInfo.PetAction copyValue(PetInfo.PetAction value) {
		return value;
	}
}