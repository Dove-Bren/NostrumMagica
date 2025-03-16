package com.smanzana.nostrummagica.serializer;

import java.util.Optional;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public class OptionalMagicElementDataSerializer implements EntityDataSerializer<Optional<EMagicElement>> {

	public static final OptionalMagicElementDataSerializer instance = new OptionalMagicElementDataSerializer();
	
	private OptionalMagicElementDataSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, Optional<EMagicElement> value) {
		buf.writeBoolean(value.isPresent());
		if (value.isPresent()) {
			buf.writeEnum(value.get());
		}
	}

	@Override
	public Optional<EMagicElement> read(FriendlyByteBuf buf)  {
		if (buf.readBoolean()) {
			return Optional.of(buf.readEnum(EMagicElement.class));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public EntityDataAccessor<Optional<EMagicElement>> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public Optional<EMagicElement> copy(Optional<EMagicElement> value) {
		return Optional.ofNullable(value.orElse(null));
	}
}
