package com.smanzana.nostrummagica.serializers;

import java.util.Optional;
import com.smanzana.nostrummagica.spells.EMagicElement;

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
			buf.writeEnumValue(value.get());
		}
	}

	@Override
	public Optional<EMagicElement> read(PacketBuffer buf)  {
		if (buf.readBoolean()) {
			return Optional.of(buf.readEnumValue(EMagicElement.class));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public DataParameter<Optional<EMagicElement>> createKey(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public Optional<EMagicElement> copyValue(Optional<EMagicElement> value) {
		return Optional.ofNullable(value.orElse(null));
	}
}
