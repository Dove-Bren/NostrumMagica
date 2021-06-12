package com.smanzana.nostrummagica.client.particles;

import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import net.minecraft.world.World;

public interface INostrumParticleFactory<T> {

	public T createParticle(World world, SpawnParams params);
}
