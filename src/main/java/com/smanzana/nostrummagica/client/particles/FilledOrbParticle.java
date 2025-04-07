package com.smanzana.nostrummagica.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class FilledOrbParticle extends TextureSheetParticle {
	
	protected final float maxAlpha;
	//protected Vec3 targetPos; // Absolute position to move to (if targetEntity == null) or offset from entity to go to
	//protected Entity targetEntity;
	//protected boolean dieOnTarget;
	protected final SpriteSet sprite;
	
	public FilledOrbParticle(ClientLevel worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime, float gravity,
			SpriteSet sprite) {
		super(worldIn, x, y, z, 0, 0, 0);
		
		this.rCol = red;
		this.gCol = green;
		this.bCol = blue;
		//this.alpha = 0; this.maxAlpha = alpha;
		this.alpha = alpha;this.maxAlpha = alpha;
		this.sprite = sprite;
		
		this.lifetime = lifetime;
		this.gravity = gravity;
//		this.targetEntity = targetEntity;
//		this.targetPos = targetPos;
//		this.dieOnTarget = dieOnTarget;
		
		this.setSpriteFromAge(sprite);
		
	}
	
	@Override
	public void tick() {
		super.tick();
		setSpriteFromAge(this.sprite);
	}
	
	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

}
