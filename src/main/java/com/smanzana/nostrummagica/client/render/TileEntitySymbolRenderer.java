package com.smanzana.nostrummagica.client.render;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.blocks.SymbolBlock.SymbolTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TileEntitySymbolRenderer extends TileEntitySpecialRenderer<SymbolTileEntity> {

	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(SymbolTileEntity.class,
				new TileEntitySymbolRenderer());
	}
	
	public TileEntitySymbolRenderer() {
		
	}
	
	@Override
	public void renderTileEntityAt(SymbolTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		
		// Get the model from the tile entity
		ResourceLocation textLoc = te.getSymbolModel();
		float rot = te.getRotation();
		VertexBuffer wr = Tessellator.getInstance().getBuffer();
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(textLoc);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + .5, y + .5, z + .5);
		GlStateManager.rotate(360.0f * rot, 0, 10, 0);
		
		GlStateManager.scale(5.0, 5.0, 5.0);
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
		
		double min = -.5;
		double max = .5;
		
		// +z
		wr.pos(max, 0.0, 0.0).tex(0.0, 1.0).normal(0, 0, -1).endVertex();
		wr.pos(min, 0.0, 0.0).tex(1.0, 1.0).normal(0, 0, -1).endVertex();
		wr.pos(min, 1.0, 0.0).tex(1.0, 0.0).normal(0, 0, -1).endVertex();
		wr.pos(max, 1.0, 0.0).tex(0.0, 0.0).normal(0, 0, -1).endVertex();
		
		// -z
		wr.pos(min, 0.0, 0.0).tex(0.0, 1.0).normal(0, 0, 1).endVertex();
        wr.pos(max, 0.0, 0.0).tex(1.0, 1.0).normal(0, 0, 1).endVertex();
        wr.pos(max, 1.0, 0.0).tex(1.0, 0.0).normal(0, 0, 1).endVertex();
        wr.pos(min, 1.0, 0.0).tex(0.0, 0.0).normal(0, 0, 1).endVertex();
		
		//wr.finishDrawing();
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
		
	}
	
}
