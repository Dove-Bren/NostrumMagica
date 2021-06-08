package com.smanzana.nostrummagica.client.particles;

import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface INostrumParticleFactory<T extends Particle> extends IParticleFactory {

	public T createParticle(World world, SpawnParams params);
	
	@Override
	default public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
		return createParticle(worldIn, new SpawnParams(
				1,
				xCoordIn, yCoordIn, zCoordIn, .5,
				60, 10,
				new Vec3d(xSpeedIn, ySpeedIn, zSpeedIn), false));
	};
	
}
