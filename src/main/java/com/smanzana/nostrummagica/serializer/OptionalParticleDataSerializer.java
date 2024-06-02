package com.smanzana.nostrummagica.serializer;


import java.util.Optional;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.particles.IParticleData;

public class OptionalParticleDataSerializer implements IDataSerializer<Optional<IParticleData>> {

	public static final OptionalParticleDataSerializer instance = new OptionalParticleDataSerializer();
	
	private OptionalParticleDataSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, Optional<IParticleData> value) {
		buf.writeBoolean(value.isPresent());
		if (value.isPresent()) {
			DataSerializers.PARTICLE_DATA.write(buf, value.get());
		}
	}

	@Override
	public Optional<IParticleData> read(PacketBuffer buf)  {
		if (buf.readBoolean()) {
			return Optional.of(DataSerializers.PARTICLE_DATA.read(buf));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public DataParameter<Optional<IParticleData>> createKey(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public Optional<IParticleData> copyValue(Optional<IParticleData> value) {
		return Optional.ofNullable(value.orElse(null));
	}
}
