package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelBaked;
import com.smanzana.nostrummagica.entity.EntityEnderRodBall;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class RenderEnderRodBall extends EntityRenderer<EntityEnderRodBall> {
	
	private static final ResourceLocation BALL_MODEL = new ResourceLocation(NostrumMagica.MODID, "entity/koid");
	
	protected ModelBaked<EntityEnderRodBall> ballOrb;

	public RenderEnderRodBall(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
		this.ballOrb = new ModelBaked<>(RenderType::getEntityTranslucent, BALL_MODEL);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ResourceLocation getEntityTexture(EntityEnderRodBall entity) {
		return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	}
	
	@Override
	public void render(EntityEnderRodBall entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		// Render orb three times with different alphas and sizes to do a glow effect.
		final float time = entityIn.ticksExisted + partialTicks;
		//134, 80, 185
		final float red = .525f;
		final float green = .314f;
		final float blue = .725f;
		final IVertexBuilder buffer = bufferIn.getBuffer(this.ballOrb.getRenderType(getEntityTexture(entityIn)));
		
		matrixStackIn.push();
		
		// All three orbs rotate at the same rate and have the same offset
		final float offset = entityIn.getHeight() / 2;
		final float rotY = time / (20f * 3.0f);
		final float rotX = time / (20f * 10f);
		
		// Inner two orbs grow at a slower rate than the glow orb
		float frac = time / (20 * 1.5f);
		float scale = 1.3f + .1f * (float) (Math.sin(Math.PI * 2 * frac));
		
		matrixStackIn.translate(0, offset, 0);
		
		// Center orb is perfectly opaque
		matrixStackIn.push();
		matrixStackIn.scale(scale, scale, scale);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rotY));
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(rotX));
		ballOrb.render(matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, red, green, blue, 1f);
		matrixStackIn.pop();
		
		// Inner glow orb is slightly larger but pulses at same rate and is transparent
		scale += .2f;
		matrixStackIn.push();
		matrixStackIn.scale(scale, scale, scale);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rotY));
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(rotX));
		ballOrb.render(matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, red, green, blue, .2f);
		matrixStackIn.pop();
		
		// Outer glow changes at a different rate and is much lighter
		frac = time / (20f * .6f);
        scale += .2f * (1 - (frac % 1f));
		matrixStackIn.push();
		matrixStackIn.scale(scale, scale, scale);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rotY));
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(rotX));
		ballOrb.render(matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, red, green, blue, .05f);
		matrixStackIn.pop();
		
		matrixStackIn.pop();
		
//        GlStateManager.rotatef(entity.rotationPitch, 1, 0, 0);
	}
}
