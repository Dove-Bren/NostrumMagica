package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Effect made up of multiple copies of another effect
 * @author Skyler
 *
 */
public class ClientEffectBeam extends ClientEffect {

	private static class BeamForm implements ClientEffectForm {

		private Vector3d end;
		
		public BeamForm(Vector3d end) {
			this.end = end;
		}
		
		@Override
		public void draw(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int color) {
			final int detailCount = Minecraft.isFancyGraphicsEnabled()
					? 20
					: 3;
			final float alphaMax = .15f;
			Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
//			GlStateManager.disableBlend();
//			GlStateManager.disableAlphaTest();
//			GlStateManager.enableBlend();
//			GlStateManager.enableAlphaTest();
//			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//			GlStateManager.enableTexture();
//			GlStateManager.disableTexture(); // !!!!!!!
			for (int i = 0; i < detailCount; i++) {
				float frac = (float) i / (float) detailCount;
				float alpha = alphaMax;
				float radius = (frac * frac * frac);
				
				float red = (float) ((color >> 16) & 255) / 255f;
				float blue = (float) ((color >> 0) & 255) / 255f;
				float green = (float) ((color >> 8) & 255) / 255f;
				alpha *= (float) ((color >> 24) & 255) / 255f;
				
				BlockRendererDispatcher renderer = Minecraft.getInstance().getBlockRendererDispatcher();
				
				IBakedModel model = renderer.getBlockModelShapes().getModelManager()
						.getModel(new ModelResourceLocation(
						NostrumMagica.MODID + ":effects/cyl", "normal"));
				matrixStackIn.push();
				
				//GlStateManager.translatef(mc.thePlayer.getPosX(), mc.thePlayer.getPosY(), mc.thePlayer.getPosZ());
				// Model is length 1 and points y+
				float len = (float) end.length();
				Vector3d norm = end.normalize();
				double xzdist = Math.min(1.0, Math.max(-1.0, Math.sqrt(norm.x * norm.x + norm.z * norm.z)));
				float angle = (float) (Math.asin(xzdist) / (2.0 * Math.PI));
				float x = (float) (Math.asin(norm.x) / (2.0 * Math.PI));
				float z = (float) (Math.asin(norm.z) / (2.0 * Math.PI));
				matrixStackIn.rotate(new Vector3f(z, 0, -x).rotationDegrees((norm.y > 0 ? 1 : -1f) * 360f * angle));
				if (norm.y < 0) {
					matrixStackIn.rotate(new Vector3f(1, 0, 1).rotationDegrees(180f));
				}
				matrixStackIn.scale(.2f * radius, len, .2f * radius);

				// Model is centered on 0. Shift
				matrixStackIn.translate(0f, .5f, 0f);
				
				// Avoid z fighting by shifting up just a tiiiiiiny but
				matrixStackIn.translate(0f, (float) -i / 10000f, 0f);

				int newcolor =   ((int) (red * 255) << 16)
						| ((int) (green * 255) << 8)
						| ((int) (blue * 255) << 0)
						| ((int) (alpha * 255) << 24);
				
				final int light = ClientEffectForm.InferLightmap(matrixStackIn, mc);
				ClientEffectForm.drawModel(matrixStackIn, model, newcolor, light);
				
				matrixStackIn.pop();
				
			}
//			GlStateManager.enableTexture();
//			GlStateManager.disableBlend();
		}
		
	}
	
	public ClientEffectBeam(Vector3d origin, Vector3d end, int ticks) {
		super(origin, new BeamForm(end.subtract(origin)), ticks);
	}
	
	public ClientEffectBeam(Vector3d origin, Vector3d end, long ms) {
		super(origin, new BeamForm(end.subtract(origin)), ms);
	}
	
}
