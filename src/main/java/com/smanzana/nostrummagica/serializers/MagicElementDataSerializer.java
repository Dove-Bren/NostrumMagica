package com.smanzana.nostrummagica.serializers;

import com.smanzana.nostrummagica.spells.EMagicElement;

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
		buf.writeEnumValue(value);
	}

	@Override
	public EMagicElement read(PacketBuffer buf)  {
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
