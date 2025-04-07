package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Effect made up of multiple copies of another effect
 * @author Skyler
 *
 */
public class ClientEffectMajorSphere extends ClientEffect {

	private static class MajorSphereForm implements ClientEffectForm {
		
		private static BakedModel MODEL_CLOUDY;
		private static BakedModel MODEL_SCALY;
		private static ResourceLocation LOC_CLOUDY = new ResourceLocation(NostrumMagica.MODID, "effect/orb_cloudy");
		private static ResourceLocation LOC_SCALY = new ResourceLocation(NostrumMagica.MODID, "effect/orb_scaled");

		private float scale;
		private boolean cloudy;
		
		public MajorSphereForm(float scale, boolean cloudy) {
			this.scale = scale;
			this.cloudy = cloudy;
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void draw(PoseStack matrixStackIn, Minecraft mc, float partialTicks, int color) {
			
			final BakedModel model;
			if (cloudy) {
				if (MODEL_CLOUDY == null) {
					MODEL_CLOUDY = mc.getBlockRenderer().getBlockModelShaper().getModelManager().getModel(LOC_CLOUDY);
				}
				
				model = MODEL_CLOUDY;
			} else {
				if (MODEL_SCALY == null) {
					MODEL_SCALY = mc.getBlockRenderer().getBlockModelShaper().getModelManager().getModel(LOC_SCALY);
				}
				
				model = MODEL_SCALY;
			}
			
//			GlStateManager.disableBlend();
//			GlStateManager.enableBlend();
//			GlStateManager.disableAlphaTest();
//			GlStateManager.enableAlphaTest();
//			GlStateManager.disableTexture();
//			GlStateManager.enableTexture();
//			GlStateManager.color4f(0f, 0f, 0f, 0f);
//			GlStateManager.color4f(1f, 1f, 1f, 1f);
			RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
			RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
			final int light = ClientEffectForm.InferLightmap(matrixStackIn, mc);
			
			// outside
			matrixStackIn.pushPose();
			matrixStackIn.scale(scale * 2, scale * 2, scale * 2); // input scale is 'blocks radius' vs model is default to .5 blocks.
			ClientEffectForm.drawModel(matrixStackIn, model, color, light);
			matrixStackIn.popPose();
			
			// inside
			matrixStackIn.pushPose();
			matrixStackIn.scale(scale * -2, scale * -2, scale * -2); // input scale is 'blocks radius' vs model is default to .5 blocks.
			ClientEffectForm.drawModel(matrixStackIn, model, color, light);
			matrixStackIn.popPose();
			
//			GlStateManager.disableBlend();
			
		}
		
	}
	
	// Scale in 'blocks' radius
	public ClientEffectMajorSphere(Vec3 origin, float scale, boolean cloudy, int ticks) {
		super(origin, new MajorSphereForm(scale, cloudy), ticks);
	}
	
	public ClientEffectMajorSphere(Vec3 origin, float scale, boolean cloudy, long ms) {
		super(origin, new MajorSphereForm(scale, cloudy), ms);
	}
	
}
