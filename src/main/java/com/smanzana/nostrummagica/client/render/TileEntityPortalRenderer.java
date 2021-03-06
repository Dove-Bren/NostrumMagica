package com.smanzana.nostrummagica.client.render;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumPortal.NostrumPortalTileEntityBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TileEntityPortalRenderer extends TileEntitySpecialRenderer<NostrumPortalTileEntityBase> {

	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(NostrumPortalTileEntityBase.class,
				new TileEntityPortalRenderer());
	}
	
	private static final ResourceLocation TEX_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/blocks/portal.png");
	
	public TileEntityPortalRenderer() {
		
	}
	
	@Override
	public void renderTileEntityAt(NostrumPortalTileEntityBase te, double x, double y, double z, float partialTicks, int destroyStage) {
		double rotY = (Math.atan2(z+.5, x+.5) / (2 * Math.PI));
		
		rotY *= -360f;
		rotY += 180f;
		
		double time = (double)te.getWorld().getTotalWorldTime() + partialTicks;
		rotY += 90;
		
		GlStateManager.pushMatrix();
		
		GlStateManager.translate(x + .5, y + 1.2, z + .5);
		GlStateManager.rotate((float)rotY, 0, 1, 0);
		
		
		VertexBuffer wr = Tessellator.getInstance().getBuffer();
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEX_LOC);
		
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.disableCull();
		int color = te.getColor();
		GlStateManager.color(
				((color >> 16) & 255) / 255f,
				((color >> 8) & 255) / 255f,
				(color & 255) / 255f,
				te.getOpacity());
		
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX_NORMAL);
		
		double rotAngle = (2*Math.PI) * (((time / 20.0) % te.getRotationPeriod()) / te.getRotationPeriod());
		
		int points = 25;
		double horizontalRadius = .6;
		double verticalRadius = 1.2;
		for (int i = 0; i < points; i++) {
			double angle = (2*Math.PI) * ((double) i / (double) points);
			double vx = Math.cos(angle) * horizontalRadius;
			double vy = Math.sin(angle) * verticalRadius;
			
			double aheadAngle = angle + rotAngle;
			double ux = Math.cos(aheadAngle) * horizontalRadius;
			double uy = Math.sin(aheadAngle) * verticalRadius;
			double u = (ux + (horizontalRadius)) / (horizontalRadius * 2);
			double v = (uy + (verticalRadius)) / (verticalRadius * 2);
			wr.pos(vx, vy, 0.0).tex(u, v).normal(0, 0, -1).endVertex();
		}
		
		Tessellator.getInstance().draw();
		
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
	}
	
}
