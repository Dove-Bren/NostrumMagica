package com.smanzana.nostrummagica.client.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.effects.ClientEffectForm;
import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuser.AetherInfuserTileEntity;
import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuser.AetherInfuserTileEntity.EffectSpark;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TileEntityAetherInfuserRenderer extends TileEntitySpecialRenderer<AetherInfuserTileEntity> {

	public static final float ORB_RADIUS = 2f;
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(AetherInfuserTileEntity.class,
				new TileEntityAetherInfuserRenderer());
	}
	
	private static final ModelResourceLocation ORB_MODEL_LOC = new ModelResourceLocation(new ResourceLocation(NostrumMagica.MODID, "effects/orb_pure"), "normal");
	private static IBakedModel MODEL_ORB;
	private static final ResourceLocation SPARK_TEX_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/effects/glow_orb.png");
	
	private List<EffectSpark> sparks;
	
	public TileEntityAetherInfuserRenderer() {
		sparks = new ArrayList<>();
	}
	
	private void renderOrb(Tessellator tessellator, VertexBuffer buffer, float opacity, boolean outside) {
		
		final float mult = 2 * ORB_RADIUS * (outside ? 1 : -1);
		final int color = 0x0033BB88 | ((int) (opacity * 255f) << 24);
		
		if (MODEL_ORB == null) {
			MODEL_ORB = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getModel(ORB_MODEL_LOC);
		}
		
		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.enableAlpha();
		GlStateManager.disableTexture2D();
		GlStateManager.enableTexture2D();
		GlStateManager.color(0f, 0f, 0f, 0f);
		GlStateManager.color(1f, 1f, 1f, 1f);
		GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0.0f);
		GlStateManager.disableLighting();
		GlStateManager.enableCull();
		GlStateManager.depthMask(false);
		
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		
		// outside
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, ORB_RADIUS - .5f, 0);
		GlStateManager.scale(mult, mult, mult);
		ClientEffectForm.drawModel(MODEL_ORB, color);

		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GlStateManager.popMatrix();
	}
	
	private void renderSpark(Tessellator tessellator, VertexBuffer buffer, Vec3d camera, int ticks, float partialTicks, EffectSpark spark) {
		
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
//		camera = camera.addVector(offsetX, offsetY, offsetZ);
//		double rotY = (Math.atan2(camera.zCoord, camera.xCoord) / (2 * Math.PI));
//		final double hDist = Math.sqrt(Math.pow(camera.xCoord, 2) + Math.pow(camera.zCoord, 2)); 
//		double rotX = (Math.atan2(camera.yCoord, hDist) / (2 * Math.PI));
//		
//		rotY *= -360f;
//		rotX *= 360f;
//		
//		rotY += 180f;
//		rotY += 90;
		// Rotation available already on ActiveRenderInfo object
		final float rX = ActiveRenderInfo.getRotationX();
		final float rXZ = ActiveRenderInfo.getRotationXZ();
		final float rZ = ActiveRenderInfo.getRotationZ();
		final float rYZ = ActiveRenderInfo.getRotationYZ();
		final float rXY = ActiveRenderInfo.getRotationXY();
		
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
		
		//GlStateManager.translate(offsetX, offsetY, offsetZ);
		//GlStateManager.rotate((float)rotY, 0, 1, 0);
		//GlStateManager.rotate((float)rotX, 1, 0, 0);
		//GlStateManager.scale(scale, scale, scale);
		
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
	public void renderTileEntityAt(AetherInfuserTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		
		final float ORB_PERIOD = 200f;
		
		final int ticks = te.getEffectTicks();
		final float allTicks = ticks + partialTicks;
		final float t = (allTicks % ORB_PERIOD) / ORB_PERIOD;
		
		te.getSparks(sparks);
		
		// Calculate opacity for orb. Probably should add glow.
		// 0f to .4f
		final float maxOrbOpacity = .075f;
		final float orbOpacity = maxOrbOpacity * (.75f + .25f * (float)Math.sin(t * 2 * Math.PI)) * te.getChargePerc();
		Vec3d trueCamPos = ActiveRenderInfo.getPosition();
		Vec3d camOffset = new Vec3d((x + .5) - trueCamPos.xCoord, (y + 1) - trueCamPos.yCoord, (z + .5) - trueCamPos.zCoord);
		
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();
		
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.disableLighting();
		GlStateManager.enableLighting();
		//GlStateManager.disableRescaleNormal();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + .5, y + 1, z + .5);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		renderOrb(tessellator, buffer, orbOpacity, false);
		renderOrb(tessellator, buffer, orbOpacity, true);
		
		//GlStateManager.clearDepth(100000);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(SPARK_TEX_LOC);
		GlStateManager.alphaFunc(516, 0);
		GlStateManager.color(1f, 1f, 1f, .75f);
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		for (EffectSpark spark : sparks) {
			renderSpark(tessellator, buffer, camOffset, ticks, partialTicks, spark);
		}
		tessellator.draw();
		GlStateManager.depthMask(true);
		
//		Minecraft.getMinecraft().getTextureManager().bindTexture(SPARK_TEX_LOC);
//		renderSpark(tessellator, buffer, camOffset, ticks, partialTicks, null);
		
		
		GlStateManager.popMatrix();
	}
}
