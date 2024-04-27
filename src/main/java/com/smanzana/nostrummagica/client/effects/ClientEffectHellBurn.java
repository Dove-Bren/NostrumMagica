package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifier;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEffectHellBurn extends ClientEffect {

	protected final Entity entity;
	
	public ClientEffectHellBurn(Entity entity, int duration) {
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
	
	protected void spawnFireEffect() {
		final double angleRad = Math.PI * 2 * NostrumMagica.rand.nextFloat();
		final Vector3d offset = new Vector3d(Math.cos(angleRad) * .5, .25, Math.sin(angleRad) * .5);
		entity.world.addParticle(ParticleTypes.FLAME,
				entity.getPosX() + offset.x,
				entity.getPosY() + entity.getHeight() + offset.y,
				entity.getPosZ() + offset.z,
				-offset.x * .1,
				-offset.y * .1,
				-offset.z * .1
				);
	}
	
	protected void spawnPoisonEffect() {
		final double angleRad = Math.PI * 2 * NostrumMagica.rand.nextFloat();
		final Vector3d offset = new Vector3d(Math.cos(angleRad) * .5, .25, Math.sin(angleRad) * .5);
		entity.world.addParticle(ParticleTypes.CRIT,
				entity.getPosX() + offset.x,
				entity.getPosY() + entity.getHeight() + offset.y,
				entity.getPosZ() + offset.z,
				0, 0, 0
				);
	}
	
	protected void spawnMagmaEffect() {
		final double angleRad = Math.PI * 2 * NostrumMagica.rand.nextFloat();
		final Vector3d dir = new Vector3d(Math.cos(angleRad) * .5, .25, Math.sin(angleRad) * .5);
		entity.world.addParticle(ParticleTypes.LAVA,
				entity.getPosX(),
				entity.getPosY() + entity.getHeight() - .1,
				entity.getPosZ(),
				dir.x,
				dir.y,
				dir.z
				);
	}
	
	@Override
	protected void drawForm(MatrixStack matrixStackIn, ClientEffectRenderDetail detail, Minecraft mc, float progress, float partialTicks) {
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
