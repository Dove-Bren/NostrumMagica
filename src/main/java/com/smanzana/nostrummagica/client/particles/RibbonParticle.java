package com.smanzana.nostrummagica.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonEmitter;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonEmitter.ISegmentSpawner;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;

// Particle wrapper for a single ribbon emitter
public abstract class RibbonParticle extends Particle {
	
	protected final int fixedRandom;
	protected final RibbonEmitter<RibbonParticle> emitter;
	
	public RibbonParticle(ClientLevel worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime, ISegmentSpawner<RibbonParticle> spawner) {
		super(worldIn, x, y, z, 0, 0, 0);
		
		this.lifetime = lifetime;
		this.fixedRandom = NostrumMagica.rand.nextInt();
		this.emitter = new RibbonEmitter<>(worldIn, x, y, z, red, green, blue, alpha, lifetime, fixedRandom, spawner, this);
		this.xd = 0;
		this.yd = 0;
		this.zd = 0;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.removed) {
			this.emitter.disable();
		}
		
		this.emitter.tick(this.x, this.y, this.z, this.xo, this.yo, this.zo);
	}
	
	@Override
	public boolean isAlive() {
		return super.isAlive() || this.emitter.isAlive();
	}
	
	@Override
	public boolean shouldCull() {
		return false;
	}
	
	@Override
	public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
		this.emitter.render(null, buffer, camera, partialTicks);
	}
}
