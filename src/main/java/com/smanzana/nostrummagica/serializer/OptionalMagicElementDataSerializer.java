package com.smanzana.nostrummagica.serializer;

import java.util.Optional;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public class OptionalMagicElementDataSerializer implements IDataSerializer<Optional<EMagicElement>> {

	public static final OptionalMagicElementDataSerializer instance = new OptionalMagicElementDataSerializer();
	
	private OptionalMagicElementDataSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, Optional<EMagicElement> value) {
		buf.writeBoolean(value.isPresent());
		if (value.isPresent()) {
			buf.writeEnum(value.get());
		}
	}

	@Override
	public Optional<EMagicElement> read(PacketBuffer buf)  {
		if (buf.readBoolean()) {
			return Optional.of(buf.readEnum(EMagicElement.class));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public DataParameter<Optional<EMagicElement>> createAccessor(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public Optional<EMagicElement> copy(Optional<EMagicElement> value) {
		return Optional.ofNullable(value.orElse(null));
	}
}
