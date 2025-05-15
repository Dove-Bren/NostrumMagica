package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifier;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.ArcaneWolfElementalType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEffectEldrichBlast extends ClientEffect {

	protected final Entity entity;
	
	public ClientEffectEldrichBlast(Entity entity, int duration) {
		super(Vec3.ZERO, null, duration);
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
		final Vec3 offset = new Vec3(Math.cos(angleRad) * .5, .25 + .1 * Math.sin(angleRad), Math.sin(angleRad) * .5);
		entity.level.addParticle(ParticleTypes.PORTAL,
				entity.getX() + offset.x,
				entity.getY() + entity.getBbHeight() + offset.y,
				entity.getZ() + offset.z,
				0, -.1, 0
				);
	}
	
	protected void spawnActiveEffect() {
		//int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, 
		//Vector3d velocity, Vector3d velocityJitter
		NostrumParticles.LIGHTNING_STATIC.spawn(entity.level, new SpawnParams(
				30, entity.getX(), entity.getY() + (entity.getBbHeight() / 2), entity.getZ(), entity.getBbHeight()/2, 30, 5,
				Vec3.ZERO, null
				).color(ArcaneWolfElementalType.ELDRICH.getColor()));
	}
	
	@Override
	protected void drawForm(PoseStack matrixStackIn, ClientEffectRenderDetail detail, Minecraft mc, MultiBufferSource buffersIn, float progress, float partialTicks) {
		if (!this.modifiers.isEmpty())
			for (ClientEffectModifier mod : modifiers) {
				mod.apply(matrixStackIn, detail, progress, partialTicks);
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
