package com.smanzana.nostrummagica.client.particles;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetMotion.MotionUpdate;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.phys.Vec3;

public class GlowOrbParticle extends TextureSheetParticle implements IMotionParticle<GlowOrbParticle> {
	
	protected final float maxAlpha;
	private final float fixedRandom; // Only generated when needed, hopefully. -1 means not generated
	protected final ParticleTargetMotion motion;
	
	protected final SpriteSet sprites;
	
	public GlowOrbParticle(ClientLevel worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime, SpriteSet sprites) {
		super(worldIn, x, y, z, 0, 0, 0);
		
		this.rCol = red;
		this.gCol = green;
		this.bCol = blue;
		this.alpha = 0f;
		this.maxAlpha = alpha;
		this.lifetime = lifetime;
		this.fixedRandom = NostrumMagica.rand.nextFloat();
		this.motion = new ParticleTargetMotion(this.fixedRandom, .2f);
		
		
		this.quadSize = .1f;
		this.sprites = sprites;
		this.setSpriteFromAge(sprites);
	}

	@Override
	public GlowOrbParticle setGravityStrength(float strength) {
		gravity = strength;
		return this;
	}
	
	@Override
	public GlowOrbParticle setMotion(double xVelocity, double yVelocity, double zVelocity) {
		this.xd = xVelocity;
		this.yd = yVelocity;
		this.zd = zVelocity;
		return this;
	}

	@Override
	public ParticleTargetMotion getMotion() {
		return motion;
	}

	@Override
	public GlowOrbParticle setPosition(double x, double y, double z) {
		this.setPos(x, y, z);
		return this;
	}
	
	protected float fixedRandom() {
		return this.fixedRandom;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return GlowOrbRenderType;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		this.setSpriteFromAge(sprites);
		
		if (this.age < 20) {
			// fade in in first second
			this.alpha = ((float) age / 20f);
		} else if (this.age >= this.lifetime - 20f) {
			// Fade out in last second
			this.alpha = ((float) (lifetime - age) / 20f);
		} else {
			this.alpha = 1f;
		}
		
		this.alpha *= maxAlpha;
		
		if (this.getMotion().shouldUpdate()) {
			final @Nullable MotionUpdate update = this.getMotion().update(new Vec3(x, y, z), new Vec3(xd, yd, zd));
			if (update == null) {
				this.remove();
			} else {
				ParticleTargetMotion.ApplyUpdate(update, this);
			}
		}
		// else let super.tick do normal particle motion
	}
	
	@Override
	public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
		//BatchRenderParticle.RenderQuad(matrixStackIn, buffer, this, renderInfo, partialTicks, .1f);
		//BatchRenderParticle.RenderQuad(matrixStackIn, buffer, this, renderInfo, partialTicks, .05f);
		this.quadSize = .1f;
		super.render(buffer, camera, partialTicks);
//		this.quadSize = .05f;
//		super.render(buffer, camera, partialTicks);
	}
	
	protected static ParticleRenderType GlowOrbRenderType = new ParticleRenderType() {

		@SuppressWarnings("deprecation")
		@Override
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.depthMask(false);
			RenderSystem.enableDepthTest();
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
			//RenderSystem.alphaFunc(GL11.GL_GREATER, 0);
			//RenderSystem.disableLighting();
			// Texture set up by batch renderer but would need to be here if this were a real particlerendertype
			RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
		}

		@Override
		public void end(Tesselator tessellator) {
			tessellator.end();
			RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		}
		
	};
	
	public static final GlowOrbParticle MakeParticle(ClientLevel world, SpriteSet sprites, SpawnParams params) {
		GlowOrbParticle particle = null;
		for (int i = 0; i < params.count; i++) {
			if (particle != null) {
				Minecraft mc = Minecraft.getInstance();
				mc.particleEngine.add(particle);
			}
			
			final double spawnX = params.spawnX + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final double spawnY = params.spawnY + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final double spawnZ = params.spawnZ + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
			final float[] colors = (params.color == null
					? new float[] {.2f, .4f, 1f, .3f}
					: ColorUtil.ARGBToColor(params.color));
			final int lifetime = params.lifetime + (params.lifetimeJitter > 0 ? NostrumMagica.rand.nextInt(params.lifetimeJitter) : 0);
			particle = new GlowOrbParticle(world, spawnX, spawnY, spawnZ, colors[0], colors[1], colors[2], colors[3], lifetime, sprites);
			particle.setFromParams(params, world::getEntity);
		}
		return particle;
	}
}
