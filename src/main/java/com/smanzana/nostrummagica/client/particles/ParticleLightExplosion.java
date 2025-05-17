package com.smanzana.nostrummagica.client.particles;

import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

// Like what the ender dragon does when it dies but in a particle
public class ParticleLightExplosion extends Particle {

	protected final int beamCount;
	protected TargetLocation target;
	protected Vec3 targetOffset;
	
	protected final int fixedRandom;
	protected float maxAlpha;
	protected float size;
	
	public ParticleLightExplosion(ClientLevel worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime, int beamCount) {
		super(worldIn, x, y, z, 0, 0, 0);
		
		this.beamCount = beamCount;
		this.rCol = red;
		this.gCol = green;
		this.bCol = blue;
		this.alpha = 0f;
		this.maxAlpha = alpha;
		this.lifetime = lifetime;
		this.fixedRandom = NostrumMagica.rand.nextInt();
		
		this.size = 2f;
		
		this.hasPhysics = false;
	}
	
	public ParticleLightExplosion setGravity(boolean gravity) {
		return setGravityStrength(gravity ? .01f : 0);
	}
	
	public ParticleLightExplosion setGravityStrength(float strength) {
		gravity = strength;
		return this;
	}
	
	public ParticleLightExplosion setMotion(Vec3 motion) {
		return this.setMotion(motion.x, motion.y, motion.z);
	}
	
	public ParticleLightExplosion setMotion(double xVelocity, double yVelocity, double zVelocity) {
		return this.setMotion(xVelocity, yVelocity, zVelocity, 0, 0, 0);
	}
	
	public ParticleLightExplosion setMotion(Vec3 motion, Vec3 jitter) {
		return this.setMotion(motion.x, motion.y, motion.z, jitter.x, jitter.y, jitter.z);
	}
	
	public ParticleLightExplosion setMotion(double xVelocity, double yVelocity, double zVelocity,
			double xJitter, double yJitter, double zJitter) {
		this.xd = xVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * xJitter; // +- jitter
		this.yd = yVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * yJitter;
		this.zd = zVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * zJitter;
		return this;
	}
	
	public ParticleLightExplosion setTarget(Entity ent) {
		this.target = new TargetLocation(ent, true);
		if (this.targetOffset == null && ent != null) {
			final double wRad = ent.getBbWidth() * 2; // double width
			final double hRad = ent.getBbHeight();
			this.targetOffset = new Vec3(wRad * (NostrumMagica.rand.nextDouble() - .5),
					hRad * (NostrumMagica.rand.nextDouble() - .5),
					wRad * (NostrumMagica.rand.nextDouble() - .5));
		}
		return this;
	}
	
	public ParticleLightExplosion setTarget(Vec3 targetPos) {
		this.target = new TargetLocation(targetPos);
		return this;
	}
	
	public ParticleLightExplosion setTargetOffset(Vec3 offset) {
		this.targetOffset = offset;
		return this;
	}
	
	@Override
	public ParticleRenderType getRenderType() {
		return LightExplosionRenderType.INSTANCE;
	}
	
	protected float getAlpha() {
		// Like ender dragon, fade out at end and begin. Don't bother fading individual rays.
		if (this.lifetime < 40) {
			return maxAlpha;
		} else if (this.age < 20) {
			return maxAlpha * ((float)this.age / 20f);
		} else if (this.age > this.lifetime - 20) {
			return maxAlpha * ((float)(this.lifetime - this.age) / 20f);
		} else {
			return maxAlpha;
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		this.alpha = getAlpha();
	}
	
	@Override
	public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
		Vec3 cameraPos = camera.getPosition();
		float offX = (float)(Mth.lerp((double)partialTicks, this.xo, this.x) - cameraPos.x());
		float offY = (float)(Mth.lerp((double)partialTicks, this.yo, this.y) - cameraPos.y());
		float offZ = (float)(Mth.lerp((double)partialTicks, this.zo, this.z) - cameraPos.z());
		
		final float totalAge = this.age + partialTicks;
		final float spinPeriod = 4 * 20;
		
		PoseStack stack = new PoseStack();
		stack.translate(offX, offY, offZ);
		stack.mulPose(Vector3f.YP.rotation(Mth.PI * 2 * ((totalAge % spinPeriod) / spinPeriod)));
		final Random random = new Random(this.fixedRandom);
		for (int i = 0; i < getRayCount((totalAge) / this.lifetime); i++) {
			stack.pushPose();
			stack.mulPose(Quaternion.fromXYZ(
					random.nextFloat() * 2 * Mth.PI,
					random.nextFloat() * 2 * Mth.PI,
					random.nextFloat() * 2 * Mth.PI
					)
			);
			final float rayLen = (this.size/2f) * (.75f + .25f * random.nextFloat());
			this.renderRay(stack, buffer, partialTicks, this.alpha, rayLen);
			
			stack.popPose();
		}
	}
	
	protected int getRayCount(float ageProgress) {
		// we want all rays out by 75% of lifetime.
		// We want at least 2 rays out immediately.
		if (this.beamCount <= 2) {
			return this.beamCount;
		}
		
		final int gradualBeams = this.beamCount - 2;
		return (int) Math.floor(gradualBeams * Math.min(1f, ageProgress / .75f)); 
	}
	
	protected void renderRay(PoseStack matrixStack, VertexConsumer buffer, float partialTicks, float alpha, float length) {
		final Matrix4f transform = matrixStack.last().pose();
		//final Matrix3f normal = matrixStack.last().normal();
		
		final float triAngle = 20f; // degrees
		// We always do triangles with the center angle being [triAngle]. So some trig to find out coords
		
		// imagine the triangle we want to render, and then split it in half from the origin vertex. It's a small right triangle with [triAngle]/2
		// as the angle our origin is at. So we have that one angle + the one side length. So tan(angle) = X / length. X = length * tan(angle)
		final float offset = (float) (length * Math.tan((triAngle / 2) * (Math.PI / 180f)));
		
		buffer.vertex(transform, 0, 0, 0).color(rCol, gCol, bCol, alpha).endVertex();
		buffer.vertex(transform, offset, length, 0).color(rCol, gCol, bCol, 0f).endVertex();
		buffer.vertex(transform, -offset, length, 0).color(rCol, gCol, bCol, 0f).endVertex();
	}
	
	protected static class LightExplosionRenderType implements ParticleRenderType {
		public static final LightExplosionRenderType INSTANCE = new LightExplosionRenderType();
		
		private LightExplosionRenderType() {
			
		}

		@Override
		public void begin(BufferBuilder buffer, TextureManager textureManager) {
			//RenderSystem.depthMask(true);
			RenderSystem.disableDepthTest();
			RenderSystem.setShader(GameRenderer::getRendertypeLightningShader);
			RenderSystem.enableBlend();
			//RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.disableCull();
			buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
		}

		@Override
		public void end(Tesselator tesselator) {
			tesselator.end();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableCull();
		}
		
		@Override
		public String toString() {
			return "NostrumMagica::LightExplosion";
		}
	}
	
	public static final ParticleLightExplosion MakeParticle(ClientLevel world, SpriteSet spites, SpawnParams params) {
		final double spawnX = params.spawnX + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
		final double spawnY = params.spawnY + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
		final double spawnZ = params.spawnZ + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
		final float[] colors = (params.color == null
				? new float[] {.2f, .4f, 1f, .3f}
				: ColorUtil.ARGBToColor(params.color));
		final int lifetime = params.lifetime + (params.lifetimeJitter > 0 ? NostrumMagica.rand.nextInt(params.lifetimeJitter) : 0);
		
		ParticleLightExplosion particle = new ParticleLightExplosion(world, spawnX, spawnY, spawnZ, colors[0], colors[1], colors[2], colors[3], lifetime, params.count);
		if (params.targetEntID != null) {
			particle.setTarget(world.getEntity(params.targetEntID));
		}
		if (params.targetPos != null) {
			particle.setTarget(params.targetPos);
		}
		if (params.velocity != null) {
			particle.setMotion(params.velocity, params.velocityJitter == null ? Vec3.ZERO : params.velocityJitter);
		}
		if (params.gravityStrength != 0f) {
			particle.setGravityStrength(params.gravityStrength);
		}
		
		return particle;
	}
}
