package com.smanzana.nostrummagica.serializers;

import java.io.IOException;

import com.google.common.base.Optional;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;

public class OptionalMagicElementDataSerializer implements DataSerializer<Optional<EMagicElement>> {

	public static final OptionalMagicElementDataSerializer instance = new OptionalMagicElementDataSerializer();
	
	private OptionalMagicElementDataSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, Optional<EMagicElement> value) {
		buf.writeBoolean(value.isPresent());
		if (value.isPresent()) {
			buf.writeEnumValue(value.get());
		}
	}

	@Override
	public Optional<EMagicElement> read(PacketBuffer buf) throws IOException {
		if (buf.readBoolean()) {
			return Optional.of(buf.readEnumValue(EMagicElement.class));
		} else {
			return Optional.absent();
		}
	}

	@Override
	public DataParameter<Optional<EMagicElement>> createKey(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public Optional<EMagicElement> copyValue(Optional<EMagicElement> value) {
		return Optional.fromNullable(value.orNull());
	}
}