package com.smanzana.nostrummagica.client.particles;

import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

// Note: doesn't extend vanilla interface to remain client/server compatible
public interface INostrumParticleFactory<T extends Particle> {

	public T createParticle(World world, SpawnParams params);
	
}
