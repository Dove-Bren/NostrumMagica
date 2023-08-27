package com.smanzana.nostrummagica.client.effects;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

/**
 * Effect made up of multiple copies of another effect
 * @author Skyler
 *
 */
public class ClientEffectMajorSphere extends ClientEffect {

	private static class MajorSphereForm implements ClientEffectForm {
		
		private static IBakedModel MODEL_CLOUDY;
		private static IBakedModel MODEL_SCALY;
		private static ModelResourceLocation LOC_CLOUDY = new ModelResourceLocation(new ResourceLocation(NostrumMagica.MODID, "effects/orb_cloudy"), "normal");
		private static ModelResourceLocation LOC_SCALY = new ModelResourceLocation(new ResourceLocation(NostrumMagica.MODID, "effects/orb_scaled"), "normal");

		private float scale;
		private boolean cloudy;
		
		public MajorSphereForm(float scale, boolean cloudy) {
			this.scale = scale;
			this.cloudy = cloudy;
		}
		
		@Override
		public void draw(Minecraft mc, float partialTicks, int color) {
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
			
			GlStateManager.disableBlend();
			GlStateManager.enableBlend();
			GlStateManager.disableAlphaTest();
			GlStateManager.enableAlphaTest();
			GlStateManager.disableTexture();
			GlStateManager.enableTexture();
			GlStateManager.color4f(0f, 0f, 0f, 0f);
			GlStateManager.color4f(1f, 1f, 1f, 1f);
			Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
			
			// outside
			GlStateManager.pushMatrix();
			GlStateManager.scalef(scale * 2, scale * 2, scale * 2); // input scale is 'blocks radius' vs model is default to .5 blocks.
			ClientEffectForm.drawModel(model, color);
			GlStateManager.popMatrix();
			
			// inside
			GlStateManager.pushMatrix();
			GlStateManager.scalef(scale * -2, scale * -2, scale * -2); // input scale is 'blocks radius' vs model is default to .5 blocks.
			ClientEffectForm.drawModel(model, color);
			GlStateManager.popMatrix();
			
			GlStateManager.disableBlend();
			
		}
		
	}
	
	// Scale in 'blocks' radius
	public ClientEffectMajorSphere(Vec3d origin, float scale, boolean cloudy, int ticks) {
		super(origin, new MajorSphereForm(scale, cloudy), ticks);
	}
	
	public ClientEffectMajorSphere(Vec3d origin, float scale, boolean cloudy, long ms) {
		super(origin, new MajorSphereForm(scale, cloudy), ms);
	}
	
}
