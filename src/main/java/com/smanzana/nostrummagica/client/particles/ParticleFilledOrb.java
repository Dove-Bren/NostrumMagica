package com.smanzana.nostrummagica.client.particles;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class ParticleFilledOrb extends BatchRenderParticle {
	
	private static final ResourceLocation TEX_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/effects/filled_orb.png");
	
	protected final float maxAlpha;
	protected Vector3d targetPos; // Absolute position to move to (if targetEntity == null) or offset from entity to go to
	protected Entity targetEntity;
	protected boolean dieOnTarget;
	
	public ParticleFilledOrb(ClientWorld worldIn, double x, double y, double z, float red, float green, float blue, float alpha, int lifetime) {
		super(worldIn, x, y, z, 0, 0, 0);
		
		particleRed = red;
		particleGreen = green;
		particleBlue = blue;
		particleAlpha = 0f;
		this.maxAlpha = alpha;
		maxAge = lifetime;
	}
	
	public ParticleFilledOrb setGravity(boolean gravity) {
		return setGravityStrength(gravity ? .01f : 0);
	}
	
	public ParticleFilledOrb setGravityStrength(float strength) {
		particleGravity = strength;
		return this;
	}
	
	public ParticleFilledOrb setMotion(Vector3d motion) {
		return this.setMotion(motion.x, motion.y, motion.z);
	}
	
	public ParticleFilledOrb setMotion(double xVelocity, double yVelocity, double zVelocity) {
		return this.setMotion(xVelocity, yVelocity, zVelocity, 0, 0, 0);
	}
	
	public ParticleFilledOrb setMotion(Vector3d motion, Vector3d jitter) {
		return this.setMotion(motion.x, motion.y, motion.z, jitter.x, jitter.y, jitter.z);
	}
	
	public ParticleFilledOrb setMotion(double xVelocity, double yVelocity, double zVelocity,
			double xJitter, double yJitter, double zJitter) {
		this.motionX = xVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * xJitter; // +- jitter
		this.motionY = yVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * yJitter;
		this.motionZ = zVelocity + (NostrumMagica.rand.nextDouble() * 2 - 1) * zJitter;
		return this;
	}
	
	public ParticleFilledOrb setTarget(Entity ent) {
		targetEntity = ent;
		if (this.targetPos == null && ent != null) {
			final double wRad = ent.getWidth() * 2; // double width
			final double hRad = ent.getHeight();
			this.targetPos = new Vector3d(wRad * (NostrumMagica.rand.nextDouble() - .5),
					hRad * (NostrumMagica.rand.nextDouble() - .5),
					wRad * (NostrumMagica.rand.nextDouble() - .5));
		}
		return this;
	}
	
	public ParticleFilledOrb setTarget(Vector3d targetPos) {
		this.targetPos = targetPos;
		return this;
	}
	
	public void dieOnTarget(boolean die) {
		this.dieOnTarget = die;
	}
	
	@Override
	public ResourceLocation getTexture() {
		return TEX_LOC;
	}

	@Override
	public void setupBatchedRender() {
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.alphaFunc(GL11.GL_GREATER, 0);
		RenderSystem.disableLighting();
		// Texture set up by batch renderer but would need to be here if this were a real particlerendertype
		
	}
	
	@Override
	public void teardownBatchedRender() {
		RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F); // idk where this was copied from
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
	}
	
	@Override
	public int hashCode() {
		return 29 * 419 + 5119;
	}

	@Override
	public int compareTo(BatchRenderParticle o) {
		return hashCode() - o.hashCode();
	}

	@Override
	public void renderBatched(MatrixStack matrixStackIn, IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
		BatchRenderParticle.RenderQuad(matrixStackIn, buffer, this, renderInfo, partialTicks, .05f);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.age < 20) {
			// fade in in first second
			this.particleAlpha = ((float) age / 20f);
		} else if (this.age >= this.maxAge - 20f) {
			// Fade out in last second
			this.particleAlpha = ((float) (maxAge - age) / 20f);
		} else {
			this.particleAlpha = 1f;
		}
		
		this.particleAlpha *= maxAlpha;
		
		if (targetEntity != null) {
			if (targetEntity.isAlive()) {
				final float period = 20f;
				Vector3d offset = targetPos == null ? new Vector3d(0,0,0) : targetPos.rotateYaw((float) (Math.PI * 2 * ((float) age % period) / period));
				Vector3d curVelocity = new Vector3d(motionX, motionY, motionZ);
				Vector3d posDelta = targetEntity.getPositionVec()
						.add(offset.x, offset.y + targetEntity.getHeight()/2, offset.z)
						.subtract(posX, posY, posZ);
				Vector3d idealVelocity = posDelta.normalize().scale(.3);
				this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
			}
			// Else just do nothing
		} else if (targetPos != null) {
			Vector3d curVelocity = new Vector3d(motionX, motionY, motionZ);
			Vector3d posDelta = targetPos.subtract(posX, posY, posZ);
			Vector3d idealVelocity = posDelta.normalize().scale(.3);
			this.setMotion(curVelocity.scale(.8).add(idealVelocity.scale(.2)));
		}
	}
	
	public static final class Factory implements INostrumParticleFactory<ParticleFilledOrb> {

		@Override
		public ParticleFilledOrb createParticle(ClientWorld world, SpawnParams params) {
			ParticleFilledOrb particle = null;
			for (int i = 0; i < params.count; i++) {
				final double spawnX = params.spawnX + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
				final double spawnY = params.spawnY + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
				final double spawnZ = params.spawnZ + (NostrumMagica.rand.nextDouble() * 2 - 1) * params.spawnJitterRadius;
				final float[] colors = (params.color == null
						? new float[] {.2f, .4f, 1f, .3f}
						: ColorUtil.ARGBToColor(params.color));
				final int lifetime = params.lifetime + (params.lifetimeJitter > 0 ? NostrumMagica.rand.nextInt(params.lifetimeJitter) : 0);
				particle = new ParticleFilledOrb(world, spawnX, spawnY, spawnZ, colors[0], colors[1], colors[2], colors[3], lifetime);
				
				if (params.targetEntID != null) {
					particle.setTarget(world.getEntityByID(params.targetEntID));
				}
				if (params.targetPos != null) {
					particle.setTarget(params.targetPos);
				}
				if (params.velocity != null) {
					particle.setMotion(params.velocity, params.velocityJitter == null ? Vector3d.ZERO : params.velocityJitter);
				}
				if (params.gravityStrength != 0f) {
					particle.setGravityStrength(params.gravityStrength);
				}
				particle.dieOnTarget(params.dieOnTarget);
				Minecraft mc = Minecraft.getInstance();
				mc.particles.addEffect(particle);
			}
			return particle;
		}
		
	}

}
