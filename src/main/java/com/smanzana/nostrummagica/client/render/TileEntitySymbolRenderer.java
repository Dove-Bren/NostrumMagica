package com.smanzana.nostrummagica.client.render;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.blocks.SymbolBlock.SymbolTileEntity;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

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
		SpellComponentWrapper comp = te.getComponent();
		SpellIcon icon;
		if (comp.isTrigger())
			icon = SpellIcon.get(comp.getTrigger());
		else if (comp.isShape())
			icon = SpellIcon.get(comp.getShape());
		else if (comp.isTrigger())
			icon = SpellIcon.get(comp.getTrigger());
		else
			icon = SpellIcon.get(comp.getElement());
		ResourceLocation textLoc = icon.getModelLocation();
		float rot = 2.0f * (Minecraft.getSystemTime() / 50 + partialTicks);
		float scale = te.getScale();
		VertexBuffer wr = Tessellator.getInstance().getBuffer();
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(textLoc);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + .5, y + 1.25, z + .5);
		GlStateManager.rotate(rot, 0, 10, 0);
		
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
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
