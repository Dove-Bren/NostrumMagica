package com.smanzana.nostrummagica.client.effects;

import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifier;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.ArcaneWolfElementalType;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEffectEldrichBlast extends ClientEffect {

	protected final Entity entity;
	
	public ClientEffectEldrichBlast(Entity entity, int duration) {
		super(Vector3d.ZERO, null, duration);
		this.entity = entity;
	}
	
	@Override
	public void onStart() {
		;
	}
	
	@Override
	public void onEnd() {
		;
	}
	
	protected void spawnPassiveEffect(int ticksExisted, float partialTicks) {
		final float ticks = ticksExisted + partialTicks;
		final float period = 2 * 20;
		final double angleRad = Math.PI * 2 * ((ticks % period) / period);
		final Vector3d offset = new Vector3d(Math.cos(angleRad) * .5, .25 + .1 * Math.sin(angleRad), Math.sin(angleRad) * .5);
		entity.world.addParticle(ParticleTypes.PORTAL,
				entity.getPosX() + offset.x,
				entity.getPosY() + entity.getHeight() + offset.y,
				entity.getPosZ() + offset.z,
				0, -.1, 0
				);
	}
	
	protected void spawnActiveEffect() {
		//int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, 
		//Vector3d velocity, Vector3d velocityJitter
		NostrumParticles.LIGHTNING_STATIC.spawn(entity.world, new SpawnParams(
				30, entity.getPosX(), entity.getPosY() + (entity.getHeight() / 2), entity.getPosZ(), entity.getHeight()/2, 30, 5,
				Vector3d.ZERO, null
				).color(ArcaneWolfElementalType.ELDRICH.getColor()));
	}
	
	@Override
	protected void drawForm(ClientEffectRenderDetail detail, Minecraft mc, float progress, float partialTicks) {
		if (!this.modifiers.isEmpty())
			for (ClientEffectModifier mod : modifiers) {
				mod.apply(detail, progress, partialTicks);
			}
		
		if (this.entity.isAlive()) {
			final int ticksLeft = this.durationTicks - existedTicks;
			if (ticksLeft % 30 == 0) {
				spawnActiveEffect();
			}
			if (ticksLeft % 1 == 0) {
				spawnPassiveEffect(existedTicks, partialTicks);
			}
		}
	}

}
