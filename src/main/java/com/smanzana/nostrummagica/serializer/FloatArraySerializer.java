package com.smanzana.nostrummagica.serializer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

/**
 * Doesn't support null Floats. Serializes them as 0.
 * @author Skyler
 *
 */
public class FloatArraySerializer implements EntityDataSerializer<Float[]> {

	public static final FloatArraySerializer instance = new FloatArraySerializer();
	
	private FloatArraySerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, Float[] value) {
		buf.writeInt(value.length);
		for (Float f : value) {
			final float fv = (f == null) ? 0 : f.floatValue();
			buf.writeFloat(fv);
		}
	}

	@Override
	public Float[] read(FriendlyByteBuf buf)  {
		int len = buf.readInt();
		Float[] array = new Float[len];
		for (int i = 0; i < len; i++) {
			array[i] = buf.readFloat();
		}
		return array;
	}

	@Override
	public EntityDataAccessor<Float[]> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public Float[] copy(Float[] value) {
		return value.clone();
	}
	
}
