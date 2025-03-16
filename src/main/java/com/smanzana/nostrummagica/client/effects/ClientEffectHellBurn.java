package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifier;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEffectHellBurn extends ClientEffect {

	protected final Entity entity;
	
	public ClientEffectHellBurn(Entity entity, int duration) {
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
	
	protected void spawnFireEffect() {
		final double angleRad = Math.PI * 2 * NostrumMagica.rand.nextFloat();
		final Vec3 offset = new Vec3(Math.cos(angleRad) * .5, .25, Math.sin(angleRad) * .5);
		entity.level.addParticle(ParticleTypes.FLAME,
				entity.getX() + offset.x,
				entity.getY() + entity.getBbHeight() + offset.y,
				entity.getZ() + offset.z,
				-offset.x * .1,
				-offset.y * .1,
				-offset.z * .1
				);
	}
	
	protected void spawnPoisonEffect() {
		final double angleRad = Math.PI * 2 * NostrumMagica.rand.nextFloat();
		final Vec3 offset = new Vec3(Math.cos(angleRad) * .5, .25, Math.sin(angleRad) * .5);
		entity.level.addParticle(ParticleTypes.CRIT,
				entity.getX() + offset.x,
				entity.getY() + entity.getBbHeight() + offset.y,
				entity.getZ() + offset.z,
				0, 0, 0
				);
	}
	
	protected void spawnMagmaEffect() {
		final double angleRad = Math.PI * 2 * NostrumMagica.rand.nextFloat();
		final Vec3 dir = new Vec3(Math.cos(angleRad) * .5, .25, Math.sin(angleRad) * .5);
		entity.level.addParticle(ParticleTypes.LAVA,
				entity.getX(),
				entity.getY() + entity.getBbHeight() - .1,
				entity.getZ(),
				dir.x,
				dir.y,
				dir.z
				);
	}
	
	@Override
	protected void drawForm(PoseStack matrixStackIn, ClientEffectRenderDetail detail, Minecraft mc, float progress, float partialTicks) {
		if (!this.modifiers.isEmpty())
			for (ClientEffectModifier mod : modifiers) {
				mod.apply(matrixStackIn, detail, progress, partialTicks);
			}
		
		if (this.entity.isAlive()) {
			if (this.existedTicks % 10 == 0) {
				spawnFireEffect();
			}
			if (this.existedTicks % 5 == 0) {
				spawnMagmaEffect();
			}
			if (this.existedTicks % 5 == 0) {
				spawnPoisonEffect();
			}
		}
	}

}
