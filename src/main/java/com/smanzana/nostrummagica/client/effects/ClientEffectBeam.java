package com.smanzana.nostrummagica.client.effects;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.math.Vec3d;

/**
 * Effect made up of multiple copies of another effect
 * @author Skyler
 *
 */
public class ClientEffectBeam extends ClientEffect {

	private static class BeamForm implements ClientEffectForm {

		private Vec3d end;
		
		public BeamForm(Vec3d end) {
			this.end = end;
		}
		
		@Override
		public void draw(Minecraft mc, float partialTicks, int color) {
			final int detailCount = Minecraft.isFancyGraphicsEnabled()
					? 20
					: 3;
			final float alphaMax = .15f;
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.disableTexture2D();
			for (int i = 0; i < detailCount; i++) {
				float frac = (float) i / (float) detailCount;
				float alpha = alphaMax;
				float radius = (frac * frac * frac);
				
				float red = (float) ((color >> 16) & 255) / 255f;
				float blue = (float) ((color >> 0) & 255) / 255f;
				float green = (float) ((color >> 8) & 255) / 255f;
				alpha *= (float) ((color >> 24) & 255) / 255f;
				
				BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
				
				IBakedModel model = renderer.getBlockModelShapes().getModelManager()
						.getModel(new ModelResourceLocation(
						NostrumMagica.MODID + ":effects/cyl", "normal"));
				GlStateManager.pushMatrix();
				GlStateManager.pushAttrib();
				
				
				// Model is length 1 and points y+
				double len = end.lengthVector();
				Vec3d norm = end.normalize();
				double xzdist = Math.min(1.0, Math.max(-1.0, Math.sqrt(norm.xCoord * norm.xCoord + norm.zCoord * norm.zCoord)));
				float angle = (float) (Math.asin(xzdist) / (2.0 * Math.PI));
				float x = (float) (Math.asin(norm.xCoord) / (2.0 * Math.PI));
				float z = (float) (Math.asin(norm.zCoord) / (2.0 * Math.PI));
				GlStateManager.rotate((norm.yCoord > 0 ? 1 : -1f) * 360f * angle, z, 0, -x);
				if (norm.yCoord < 0) {
					GlStateManager.rotate(180f, 1, 0, 1);
				}
				GlStateManager.scale(.2 * radius, len, .2 * radius);

				// Model is centered on 0. Shift
				GlStateManager.translate(0f, .5f, 0f);
				
				// Avoid z fighting by shifting up just a tiiiiiiny but
				GlStateManager.translate(0f, (float) -i / 10000f, 0f);

				int newcolor =   ((int) (red * 255) << 16)
						| ((int) (green * 255) << 8)
						| ((int) (blue * 255) << 0)
						| ((int) (alpha * 255) << 24);
				
				ClientEffectForm.drawModel(model, newcolor);
				
				//GlStateManager.color(.5f, 0f, 0f, .5f); // WHY DOESNT THIS WORK??
				//draw(model.getQuads(null, null, 0), color);
				GlStateManager.popAttrib();
				GlStateManager.popMatrix();
				
			}
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
		}
		
	}
	
	public ClientEffectBeam(Vec3d origin, Vec3d end, int ticks) {
		super(origin, new BeamForm(end), ticks);
	}
	
	public ClientEffectBeam(Vec3d origin, Vec3d end, long ms) {
		super(origin, new BeamForm(end), ms);
	}
	
}
