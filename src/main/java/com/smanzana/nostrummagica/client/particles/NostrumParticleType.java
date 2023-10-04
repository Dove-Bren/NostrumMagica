package com.smanzana.nostrummagica.client.particles;

import net.minecraft.particles.ParticleType;

public class NostrumParticleType extends ParticleType<NostrumParticleData> {

	public NostrumParticleType(String registryName) {
		this(registryName, false);
	}
	
	public NostrumParticleType(String registryName, boolean allowHiding) {
		super(allowHiding, NostrumParticleData.DESERIALIZER);
		this.setRegistryName(registryName);
	}

}
