package com.smanzana.nostrummagica.client.particles;

import com.mojang.serialization.Codec;

import net.minecraft.core.particles.ParticleType;

public class NostrumParticleType extends ParticleType<NostrumParticleData> {

	public NostrumParticleType(String registryName) {
		this(registryName, false);
	}
	
	public NostrumParticleType(String registryName, boolean allowHiding) {
		super(allowHiding, NostrumParticleData.DESERIALIZER);
		this.setRegistryName(registryName);
	}

	@Override
	public Codec<NostrumParticleData> codec() {
		return NostrumParticleData.CODEC;
	}

}
