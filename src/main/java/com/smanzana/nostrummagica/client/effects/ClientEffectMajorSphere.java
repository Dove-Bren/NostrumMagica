package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Effect made up of multiple copies of another effect
 * @author Skyler
 *
 */
public class ClientEffectMajorSphere extends ClientEffect {

	private static class MajorSphereForm implements ClientEffectForm {
		
		private static IBakedModel MODEL_CLOUDY;
		private static IBakedModel MODEL_SCALY;
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
		public void draw(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int color) {
			
			final IBakedModel model;
			if (cloudy) {
				if (MODEL_CLOUDY == null) {
					MODEL_CLOUDY = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(LOC_CLOUDY);
				}
				
				model = MODEL_CLOUDY;
			} else {
				if (MODEL_SCALY == null) {
					MODEL_SCALY = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(LOC_SCALY);
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
			Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
			final int light = ClientEffectForm.InferLightmap(matrixStackIn, mc);
			
			// outside
			matrixStackIn.push();
			matrixStackIn.scale(scale * 2, scale * 2, scale * 2); // input scale is 'blocks radius' vs model is default to .5 blocks.
			ClientEffectForm.drawModel(matrixStackIn, model, color, light);
			matrixStackIn.pop();
			
			// inside
			matrixStackIn.push();
			matrixStackIn.scale(scale * -2, scale * -2, scale * -2); // input scale is 'blocks radius' vs model is default to .5 blocks.
			ClientEffectForm.drawModel(matrixStackIn, model, color, light);
			matrixStackIn.pop();
			
//			GlStateManager.disableBlend();
			
		}
		
	}
	
	// Scale in 'blocks' radius
	public ClientEffectMajorSphere(Vector3d origin, float scale, boolean cloudy, int ticks) {
		super(origin, new MajorSphereForm(scale, cloudy), ticks);
	}
	
	public ClientEffectMajorSphere(Vector3d origin, float scale, boolean cloudy, long ms) {
		super(origin, new MajorSphereForm(scale, cloudy), ms);
	}
	
}
