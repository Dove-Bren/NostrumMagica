package com.smanzana.nostrummagica.serializer;


import java.util.Optional;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.core.particles.ParticleOptions;

public class OptionalParticleDataSerializer implements EntityDataSerializer<Optional<ParticleOptions>> {

	public static final OptionalParticleDataSerializer instance = new OptionalParticleDataSerializer();
	
	private OptionalParticleDataSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, Optional<ParticleOptions> value) {
		buf.writeBoolean(value.isPresent());
		if (value.isPresent()) {
			EntityDataSerializers.PARTICLE.write(buf, value.get());
		}
	}

	@Override
	public Optional<ParticleOptions> read(FriendlyByteBuf buf)  {
		if (buf.readBoolean()) {
			return Optional.of(EntityDataSerializers.PARTICLE.read(buf));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public EntityDataAccessor<Optional<ParticleOptions>> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public Optional<ParticleOptions> copy(Optional<ParticleOptions> value) {
		return Optional.ofNullable(value.orElse(null));
	}
}
