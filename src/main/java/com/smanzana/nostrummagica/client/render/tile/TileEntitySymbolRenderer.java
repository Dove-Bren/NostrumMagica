package com.smanzana.nostrummagica.client.render.tile;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.tiles.SymbolTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class TileEntitySymbolRenderer extends TileEntityRenderer<SymbolTileEntity> {

	public TileEntitySymbolRenderer() {
		
	}
	
	@Override
	public void render(SymbolTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		
		// Get the model from the tile entity
		SpellComponentWrapper comp = te.getComponent();
		SpellComponentIcon icon;
		if (comp.isTrigger())
			icon = SpellComponentIcon.get(comp.getTrigger());
		else if (comp.isShape())
			icon = SpellComponentIcon.get(comp.getShape());
		else if (comp.isAlteration())
			icon = SpellComponentIcon.get(comp.getAlteration());
		else
			icon = SpellComponentIcon.get(comp.getElement());
		ResourceLocation textLoc = icon.getModelLocation();
		float rot = 2.0f * (float)((double) te.getWorld().getGameTime() / 2.5);
		float scale = te.getScale();
		BufferBuilder wr = Tessellator.getInstance().getBuffer();
		
		Minecraft.getInstance().getTextureManager().bindTexture(textLoc);
		GlStateManager.pushMatrix();
		GlStateManager.translated(x + .5, y + 1.25, z + .5);
		GlStateManager.rotatef(rot, 0, 10, 0);
		
		GlStateManager.scalef(scale, scale, scale);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableLighting();
		GlStateManager.enableAlphaTest();
		GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
		
		double min = -.5;
		double max = .5;
		
		// +z
		wr.pos(max, 0.0, 0.0).tex(0.0, 1.0).normal(0, 0, -1).endVertex();
		wr.pos(min, 0.0, 0.0).tex(1.0, 1.0).normal(0, 0, -1).endVertex();
		wr.pos(min, 1.0, 0.0).tex(1.0, 0.0).normal(0, 0, -1).endVertex();
		wr.pos(max, 1.0, 0.0).tex(0.0, 0.0).normal(0, 0, -1).endVertex();
		
		// -z
		wr.pos(min, 0.0, 0.0).tex(1.0, 1.0).normal(0, 0, 1).endVertex();
        wr.pos(max, 0.0, 0.0).tex(0.0, 1.0).normal(0, 0, 1).endVertex();
        wr.pos(max, 1.0, 0.0).tex(0.0, 0.0).normal(0, 0, 1).endVertex();
        wr.pos(min, 1.0, 0.0).tex(1.0, 0.0).normal(0, 0, 1).endVertex();
        
    	// -x
    	wr.pos(0.0, 0.0, min).tex(0.0, 1.0).normal(-1, 0, 0).endVertex();
    	wr.pos(0.0, 0.0, max).tex(1.0, 1.0).normal(-1, 0, 0).endVertex();
    	wr.pos(0.0, 1.0, max).tex(1.0, 0.0).normal(-1, 0, 0).endVertex();
    	wr.pos(0.0, 1.0, min).tex(0.0, 0.0).normal(-1, 0, 0).endVertex();
		
		// +x
		wr.pos(0.0, 0.0, max).tex(1.0, 1.0).normal(1, 0, 0).endVertex();
        wr.pos(0.0, 0.0, min).tex(0.0, 1.0).normal(1, 0, 0).endVertex();
        wr.pos(0.0, 1.0, min).tex(0.0, 0.0).normal(1, 0, 0).endVertex();
        wr.pos(0.0, 1.0, max).tex(1.0, 0.0).normal(1, 0, 0).endVertex();
		
		//wr.finishDrawing();
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
		
	}
	
}
