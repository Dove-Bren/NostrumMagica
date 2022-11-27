package com.smanzana.nostrummagica.client.effects;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifier;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientEffectHellBurn extends ClientEffect {

	protected final Entity entity;
	
	public ClientEffectHellBurn(Entity entity, int duration) {
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
	
	protected void spawnFireEffect() {
		final double angleRad = Math.PI * 2 * NostrumMagica.rand.nextFloat();
		final Vec3d offset = new Vec3d(Math.cos(angleRad) * .5, .25, Math.sin(angleRad) * .5);
		entity.world.spawnParticle(EnumParticleTypes.FLAME,
				entity.posX + offset.x,
				entity.posY + entity.height + offset.y,
				entity.posZ + offset.z,
				-offset.x * .1,
				-offset.y * .1,
				-offset.z * .1,
				new int[0]
				);
	}
	
	protected void spawnPoisonEffect() {
		final double angleRad = Math.PI * 2 * NostrumMagica.rand.nextFloat();
		final Vec3d offset = new Vec3d(Math.cos(angleRad) * .5, .25, Math.sin(angleRad) * .5);
		entity.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
				entity.posX + offset.x,
				entity.posY + entity.height + offset.y,
				entity.posZ + offset.z,
				0, 0, 0,
				new int[0]
				);
	}
	
	protected void spawnMagmaEffect() {
		final double angleRad = Math.PI * 2 * NostrumMagica.rand.nextFloat();
		final Vec3d dir = new Vec3d(Math.cos(angleRad) * .5, .25, Math.sin(angleRad) * .5);
		entity.world.spawnParticle(EnumParticleTypes.LAVA,
				entity.posX,
				entity.posY + entity.height - .1,
				entity.posZ,
				dir.x,
				dir.y,
				dir.z,
				new int[0]
				);
	}
	
	@Override
	protected void drawForm(ClientEffectRenderDetail detail, Minecraft mc, float progress, float partialTicks) {
		if (!this.modifiers.isEmpty())
			for (ClientEffectModifier mod : modifiers) {
				mod.apply(detail, progress, partialTicks);
			}
		
		if (!this.entity.isDead) {
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
