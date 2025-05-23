package com.smanzana.nostrummagica.client.particles;

import java.util.Random;

import javax.annotation.Nullable;

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
import com.smanzana.nostrummagica.client.particles.ParticleTargetMotion.MotionUpdate;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// Like what the ender dragon does when it dies but in a particle
public class LightExplosionParticle extends Particle implements IMotionParticle<LightExplosionParticle> {

	protected final int beamCount;
	protected ParticleTargetMotion motion;
	
	protected final int fixedRandom;
	protected float maxAlpha;
	protected float size;
	
	public LightExplosionParticle(ClientLevel worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime, int beamCount) {
		super(worldIn, x, y, z, 0, 0, 0);
		
		this.beamCount = beamCount;
		this.rCol = red;
		this.gCol = green;
		this.bCol = blue;
		this.alpha = 0f;
		this.maxAlpha = alpha;
		this.lifetime = lifetime;
		this.fixedRandom = NostrumMagica.rand.nextInt();
		this.motion = new ParticleTargetMotion(fixedRandom, .2f);
		
		this.size = 2f;
		
		this.hasPhysics = false;
	}
	
	@Override
	public ParticleTargetMotion getMotion() {
		return this.motion;
	}
	
	@Override
	public LightExplosionParticle setPosition(double x, double y, double z) {
		this.setPos(x, y, z);
		return this;
	}
	
	@Override
	public LightExplosionParticle setGravityStrength(float strength) {
		gravity = strength;
		return this;
	}
	
	@Override
	public LightExplosionParticle setMotion(double xVelocity, double yVelocity, double zVelocity) {
		this.xd = xVelocity;
		this.yd = yVelocity;
		this.zd = zVelocity;
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
	
	public static final LightExplosionParticle MakeParticle(ClientLevel world, SpriteSet spites, SpawnParams params) {
		final double spawnX = params.spawnX + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
		final double spawnY = params.spawnY + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
		final double spawnZ = params.spawnZ + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
		final float[] colors = (params.color == null
				? new float[] {.2f, .4f, 1f, .3f}
				: ColorUtil.ARGBToColor(params.color));
		final int lifetime = params.lifetime + (params.lifetimeJitter > 0 ? NostrumMagica.rand.nextInt(params.lifetimeJitter) : 0);
		
		LightExplosionParticle particle = new LightExplosionParticle(world, spawnX, spawnY, spawnZ, colors[0], colors[1], colors[2], colors[3], lifetime, params.count);
		particle.setFromParams(params, world::getEntity);
		
		return particle;
	}
}
