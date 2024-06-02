package com.smanzana.nostrummagica.serializer;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

/**
 * Doesn't support null Floats. Serializes them as 0.
 * @author Skyler
 *
 */
public class FloatArraySerializer implements IDataSerializer<Float[]> {

	public static final FloatArraySerializer instance = new FloatArraySerializer();
	
	private FloatArraySerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, Float[] value) {
		buf.writeInt(value.length);
		for (Float f : value) {
			final float fv = (f == null) ? 0 : f.floatValue();
			buf.writeFloat(fv);
		}
	}

	@Override
	public Float[] read(PacketBuffer buf)  {
		int len = buf.readInt();
		Float[] array = new Float[len];
		for (int i = 0; i < len; i++) {
			array[i] = buf.readFloat();
		}
		return array;
	}

	@Override
	public DataParameter<Float[]> createKey(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public Float[] copyValue(Float[] value) {
		return value.clone();
	}
	
}
