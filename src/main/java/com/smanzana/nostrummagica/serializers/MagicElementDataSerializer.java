package com.smanzana.nostrummagica.serializers;

import java.io.IOException;

import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;

public class MagicElementDataSerializer implements DataSerializer<EMagicElement> {

	public static final MagicElementDataSerializer instance = new MagicElementDataSerializer();
	
	private MagicElementDataSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, EMagicElement value) {
		buf.writeEnumValue(value);
	}

	@Override
	public EMagicElement read(PacketBuffer buf) throws IOException {
		return buf.readEnumValue(EMagicElement.class);
	}

	@Override
	public DataParameter<EMagicElement> createKey(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public EMagicElement copyValue(EMagicElement value) {
		return value;
	}
}
