package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public class MagicElementDataSerializer implements IDataSerializer<EMagicElement> {

	public static final MagicElementDataSerializer instance = new MagicElementDataSerializer();
	
	private MagicElementDataSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, EMagicElement value) {
		buf.writeEnum(value);
	}

	@Override
	public EMagicElement read(PacketBuffer buf)  {
		return buf.readEnum(EMagicElement.class);
	}

	@Override
	public DataParameter<EMagicElement> createAccessor(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public EMagicElement copy(EMagicElement value) {
		return value;
	}
}
