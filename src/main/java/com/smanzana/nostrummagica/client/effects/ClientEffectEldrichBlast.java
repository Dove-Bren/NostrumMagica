package com.smanzana.nostrummagica.client.effects;

import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifier;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.ArcaneWolfElementalType;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientEffectEldrichBlast extends ClientEffect {

	protected final Entity entity;
	
	public ClientEffectEldrichBlast(Entity entity, int duration) {
		super(Vec3d.ZERO, null, duration);
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
		final Vec3d offset = new Vec3d(Math.cos(angleRad) * .5, .25 + .1 * Math.sin(angleRad), Math.sin(angleRad) * .5);
		entity.world.spawnParticle(EnumParticleTypes.PORTAL,
				entity.posX + offset.x,
				entity.posY + entity.height + offset.y,
				entity.posZ + offset.z,
				0, -.1, 0,
				new int[0]
				);
	}
	
	protected void spawnActiveEffect() {
		//int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, 
		//Vec3d velocity, Vec3d velocityJitter
		NostrumParticles.LIGHTNING_STATIC.spawn(entity.world, new SpawnParams(
				30, entity.posX, entity.posY + (entity.height / 2), entity.posZ, entity.height/2, 30, 5,
				Vec3d.ZERO, null
				).color(ArcaneWolfElementalType.ELDRICH.getColor()));
	}
	
	@Override
	protected void drawForm(ClientEffectRenderDetail detail, Minecraft mc, float progress, float partialTicks) {
		if (!this.modifiers.isEmpty())
			for (ClientEffectModifier mod : modifiers) {
				mod.apply(detail, progress, partialTicks);
			}
		
		if (!this.entity.isDead) {
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
