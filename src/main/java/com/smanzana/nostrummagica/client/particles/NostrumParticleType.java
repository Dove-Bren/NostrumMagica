package com.smanzana.nostrummagica.client.particles;

import com.mojang.serialization.Codec;

import net.minecraft.particles.ParticleType;

public class NostrumParticleType extends ParticleType<NostrumParticleData> {

	public NostrumParticleType(String registryName) {
		this(registryName, false);
	}
	
	public NostrumParticleType(String registryName, boolean allowHiding) {
		super(allowHiding, NostrumParticleData.DESERIALIZER);
		this.setRegistryName(registryName);
	}

	@Override
	public Codec<NostrumParticleData> func_230522_e_() {
		return NostrumParticleData.CODEC;
	}

}
