package com.smanzana.nostrummagica.client.particles;

import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;

// Note: doesn't extend vanilla interface to remain client/server compatible
@FunctionalInterface
public interface INostrumParticleFactory<T extends Particle> {

	public T createParticle(ClientLevel world, SpriteSet sprites, SpawnParams params);
	
}
