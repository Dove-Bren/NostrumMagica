package com.smanzana.nostrummagica.client.render.tile;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuserTileEntity;
import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuserTileEntity.EffectSpark;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class TileEntityAetherInfuserRenderer extends TileEntityRenderer<AetherInfuserTileEntity> {

	public static final float ORB_RADIUS = 2f;
	
	//private static final ModelResourceLocation ORB_MODEL_LOC = new ModelResourceLocation(new ResourceLocation(NostrumMagica.MODID, "effects/orb_pure"), "normal");
	//private static IBakedModel MODEL_ORB;
	private static final ResourceLocation SPARK_TEX_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/effects/glow_orb.png");
	
	private List<EffectSpark> sparks;
	
	public TileEntityAetherInfuserRenderer() {
		sparks = new ArrayList<>();
	}
	
	private void renderOrb(Tessellator tessellator, BufferBuilder buffer, float opacity, boolean outside) {
		
		final float mult = 2 * ORB_RADIUS * (outside ? 1 : -1);
		
		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.enableAlphaTest();
		GlStateManager.disableTexture();
		GlStateManager.enableTexture();
		GlStateManager.color4f(0f, 0f, 0f, 0f);
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		//GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);
		GlStateManager.disableLighting();
		GlStateManager.enableCull();
		GlStateManager.depthMask(false);
		
		
		//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240); TODO this?
		
		// outside
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, ORB_RADIUS - .5f, 0);
		GlStateManager.scalef(mult, mult, mult);
//		
//		{
//			final int color = 0x0033BB88 | (((int) (opacity * 255f) << 24) & 0xFF000000);
//			//if (MODEL_ORB == null) {
//				//MODEL_ORB = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(ORB_MODEL_LOC);
//				IBakedModel MODEL_ORB = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(new ModelResourceLocation(new ResourceLocation(NostrumMagica.MODID, "effects/orb_pure"), "normal"));
//			//}
//			
//			ClientEffectForm.drawModel(MODEL_ORB, color);
//		}
		
		{
			//ClientEffectForm.drawModel(Model, color);
			// ARGB
			// final int color = 0x0033BB88 | ((int) (opacity * 255f) << 24);
			final float red = .2f;
			final float green = .73f;
			final float blue = .53f;
			final float alpha = opacity;
			
			//GlStateManager.disableCull();
			//GlStateManager.disableDepth();
			GlStateManager.alphaFunc(516, 0);
			Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation(NostrumMagica.MODID, "textures/effects/slate.png"));
			final int rows = 10;
			final int cols = 10;
			final double radius = 1;
			for (int i = 1; i <= rows; i++) {
				final double yRad0 = Math.PI * (-0.5f + (float) (i - 1) / (float) rows);
				final double y0 = Math.sin(yRad0);
				final double yR0 = Math.cos(yRad0);
				final double yRad1 = Math.PI * (-0.5f + (float) (i) / (float) rows);
				final double y1 = Math.sin(yRad1);
				final double yR1 = Math.cos(yRad1);

				buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
				for (int j = 0; j <= cols; j++) {
					final double xRad = Math.PI * 2 * (double) ((float) (j-1) / (float) cols);
					final double x = Math.cos(xRad);
					final double z = Math.sin(xRad);
					
					buffer.pos(radius * x * yR0, radius * y0, radius * z * yR0).tex(0, 0).color(red, green, blue, alpha)
						.normal((float) (x * yR0), (float) (y0), (float) (z * yR0)).endVertex();
					buffer.pos(radius * x * yR1, radius * y1, radius * z * yR1).tex(1, 1).color(red, green, blue, alpha)
						.normal((float) (x * yR1), (float) (y1), (float) (z * yR1)).endVertex();
				}
				tessellator.draw();
				
			}
		}

		GlStateManager.enableDepthTest();
		GlStateManager.depthMask(true);
		GlStateManager.popMatrix();
	}
	
	private void renderSpark(Tessellator tessellator, BufferBuilder buffer, Vec3d camera, int ticks, float partialTicks, EffectSpark spark) {
		
		// Translation
		final float pitch = spark.getPitch(ticks, partialTicks);
		final float yaw = spark.getYaw(ticks, partialTicks);
		final double pitchRad = 2 * Math.PI * pitch;
		final double yawRad = 2 * Math.PI * yaw;
		final float offsetX = (float) (ORB_RADIUS * Math.sin(pitchRad) * Math.cos(yawRad));
		final float offsetZ = (float) (ORB_RADIUS * Math.sin(pitchRad) * Math.sin(yawRad));
		
		// Y offset so that lowest is at -.5, not -radius
		final float offsetY = (float) (ORB_RADIUS * -Math.cos(pitchRad)) + ORB_RADIUS -.5f;
		
		// Rotation
//		camera = camera.add(offsetX, offsetY, offsetZ);
//		double rotY = (Math.atan2(camera.zCoord, camera.xCoord) / (2 * Math.PI));
//		final double hDist = Math.sqrt(Math.pow(camera.xCoord, 2) + Math.pow(camera.zCoord, 2)); 
//		double rotX = (Math.atan2(camera.yCoord, hDist) / (2 * Math.PI));
//		
//		rotY *= -360f;
//		rotX *= 360f;
//		
//		rotY += 180f;
//		rotY += 90;
		
		final Minecraft mc = Minecraft.getInstance();
		final ActiveRenderInfo renderInfo = mc.gameRenderer.getActiveRenderInfo();
		final float rX = MathHelper.cos(renderInfo.getYaw() * ((float)Math.PI / 180F));
		final float rXZ = rX * MathHelper.sin(renderInfo.getPitch() * ((float)Math.PI / 180F));
		final float rZ = MathHelper.cos(renderInfo.getPitch() * ((float)Math.PI / 180F));
		final float rYZ = MathHelper.sin(renderInfo.getYaw() * ((float)Math.PI / 180F));
		final float rXY = -rYZ * MathHelper.sin(renderInfo.getPitch() * ((float)Math.PI / 180F));
		
		// Size
		final float scale = .2f * spark.yawStart;
		final float radius = scale;
		final float smallRadius = scale / 2f;
		
		// Color
		final float brightness = spark.getBrightness(ticks, partialTicks);
		final float red = .24f;
		final float green = .8f;
		final float blue = .93f;
		final float alphaInner = .01f + .2f * brightness;
		final float alphaOuter = .01f + .2f * brightness;
		
		//GlStateManager.translatef(offsetX, offsetY, offsetZ);
		//GlStateManager.rotatef((float)rotY, 0, 1, 0);
		//GlStateManager.rotatef((float)rotX, 1, 0, 0);
		//GlStateManager.scalef(scale, scale, scale);
		
		buffer.pos(offsetX - (rX * radius) - (rYZ * radius), offsetY - (rXZ * radius), offsetZ - (rZ * radius) - (rXY * radius))
			.tex(0, 0).color(red, green, blue, alphaOuter).normal(0, 0, 1).endVertex();
		buffer.pos(offsetX - (rX * radius) + (rYZ * radius), offsetY + (rXZ * radius), offsetZ - (rZ * radius) + (rXY * radius))
			.tex(0, 1).color(red, green, blue, alphaOuter).normal(0, 0, 1).endVertex();
		buffer.pos(offsetX + (rX * radius) + (rYZ * radius), offsetY + (rXZ * radius), offsetZ + (rZ * radius) + (rXY * radius))
			.tex(1, 1).color(red, green, blue, alphaOuter).normal(0, 0, 1).endVertex();
		buffer.pos(offsetX + (rX * radius) - (rYZ * radius), offsetY - (rXZ * radius), offsetZ + (rZ * radius) - (rXY * radius))
			.tex(1, 0).color(red, green, blue, alphaOuter).normal(0, 0, 1).endVertex();
		
		buffer.pos(offsetX - (rX * smallRadius) - (rYZ * smallRadius), offsetY - (rXZ * smallRadius), offsetZ - (rZ * smallRadius) - (rXY * smallRadius))
			.tex(0, 0).color(red, green, blue, alphaInner).normal(0, 0, 1).endVertex();
		buffer.pos(offsetX - (rX * smallRadius) + (rYZ * smallRadius), offsetY + (rXZ * smallRadius), offsetZ - (rZ * smallRadius) + (rXY * smallRadius))
			.tex(0, 1).color(red, green, blue, alphaInner).normal(0, 0, 1).endVertex();
		buffer.pos(offsetX + (rX * smallRadius) + (rYZ * smallRadius), offsetY + (rXZ * smallRadius), offsetZ + (rZ * smallRadius) + (rXY * smallRadius))
			.tex(1, 1).color(red, green, blue, alphaInner).normal(0, 0, 1).endVertex();
		buffer.pos(offsetX + (rX * smallRadius) - (rYZ * smallRadius), offsetY - (rXZ * smallRadius), offsetZ + (rZ * smallRadius) - (rXY * smallRadius))
			.tex(1, 0).color(red, green, blue, alphaInner).normal(0, 0, 1).endVertex();
		
//		buffer.pos(-.25, .25, 0.01).tex(0, 0).color(red, green, blue, alphaInner).normal(0, 0, 1).endVertex();
//		buffer.pos(-.25, -.25, 0.01).tex(0, 1).color(red, green, blue, alphaInner).normal(0, 0, 1).endVertex();
//		buffer.pos(.25, -.25, 0.01).tex(1, 1).color(red, green, blue, alphaInner).normal(0, 0, 1).endVertex();
//		buffer.pos(.25, .25, 0.01).tex(1, 0).color(red, green, blue, alphaInner).normal(0, 0, 1).endVertex();
		
	}
	
	@Override
	public void render(AetherInfuserTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		
		final float ORB_PERIOD = 200f;
		
		final int ticks = te.getEffectTicks();
		final float allTicks = ticks + partialTicks;
		final float t = (allTicks % ORB_PERIOD) / ORB_PERIOD;
		
		te.getSparks(sparks);
		
		// Calculate opacity for orb. Probably should add glow.
		// 0f to .4f
		final float maxOrbOpacity = .075f;
		final float orbOpacity = maxOrbOpacity * (.75f + .25f * (float)Math.sin(t * 2 * Math.PI)) * te.getChargePerc();
		Minecraft mc = Minecraft.getInstance();
		Vec3d trueCamPos = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
		Vec3d camOffset = new Vec3d((x + .5) - trueCamPos.x, (y + 1) - trueCamPos.y, (z + .5) - trueCamPos.z);
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlphaTest();
		GlStateManager.disableLighting();
		GlStateManager.enableLighting();
		//GlStateManager.disableRescaleNormal();
		
		GlStateManager.pushMatrix();
		GlStateManager.translated(x + .5, y + 1, z + .5);
		
		Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		renderOrb(tessellator, buffer, orbOpacity, false);
		renderOrb(tessellator, buffer, orbOpacity, true);
		
		//GlStateManager.clearDepth(100000);
		
		Minecraft.getInstance().getTextureManager().bindTexture(SPARK_TEX_LOC);
		GlStateManager.alphaFunc(516, 0);
		GlStateManager.color4f(1f, 1f, 1f, .75f);
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);
		//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		for (EffectSpark spark : sparks) {
			renderSpark(tessellator, buffer, camOffset, ticks, partialTicks, spark);
		}
		tessellator.draw();
		GlStateManager.depthMask(true);
		
//		Minecraft.getInstance().getTextureManager().bindTexture(SPARK_TEX_LOC);
//		renderSpark(tessellator, buffer, camOffset, ticks, partialTicks, null);
		
		
		GlStateManager.popMatrix();
	}
}
