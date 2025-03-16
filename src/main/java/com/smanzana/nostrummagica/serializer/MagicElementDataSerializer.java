package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public class MagicElementDataSerializer implements EntityDataSerializer<EMagicElement> {

	public static final MagicElementDataSerializer instance = new MagicElementDataSerializer();
	
	private MagicElementDataSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, EMagicElement value) {
		buf.writeEnum(value);
	}

	@Override
	public EMagicElement read(FriendlyByteBuf buf)  {
		return buf.readEnum(EMagicElement.class);
	}

	@Override
	public EntityDataAccessor<EMagicElement> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public EMagicElement copy(EMagicElement value) {
		return value;
	}
}
