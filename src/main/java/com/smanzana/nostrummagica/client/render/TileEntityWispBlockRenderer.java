package com.smanzana.nostrummagica.client.render;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.integration.aetheria.blocks.WispBlock.WispBlockTileEntity;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TileEntityWispBlockRenderer extends TileEntitySpecialRenderer<WispBlockTileEntity> {

	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(WispBlockTileEntity.class,
				new TileEntityWispBlockRenderer());
	}
	
	//private static final ResourceLocation MODEL_LOC = new ResourceLocation(NostrumMagica.MODID, "block/crystal.obj");
	private static final ResourceLocation BASE_TEX_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/blocks/stone_generic1.png");
	private static final ResourceLocation PLATFORM_TEX_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/blocks/ceramic_generic.png");
	private static final ResourceLocation GEM_TEX_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/models/crystal_blank.png");
	
	//private IBakedModel model;
	
	public TileEntityWispBlockRenderer() {
		
	}
	
	// 0,0 is bottom point
	protected void renderPlatform(Tessellator tessellator, VertexBuffer buffer) {
		// Top
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
		
		buffer.pos(-.5, 1, -.5).tex(0,0).normal(-.6682f, 0.3272f, -.6682f).endVertex();
		buffer.pos(-.5, 1, .5).tex(0,1).normal(-.6682f, 0.3272f, .6682f).endVertex();
		buffer.pos(.5, 1, .5).tex(1, 1).normal(.6682f, 0.3272f, .6682f).endVertex();
		buffer.pos(.5, 1, -.5).tex(1,0).normal(.6682f, 0.3272f, -.6682f).endVertex();
		
		tessellator.draw();
		
		// Edges
		buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
		buffer.pos(0, 0, 0).tex(.5,1).normal(0, 1, 0).endVertex();
		buffer.pos(-.5, 1, -.5).tex(0,0).normal(-.6682f, -0.3272f, -.6682f).endVertex();
		buffer.pos(.5, 1, -.5).tex(1,0).normal(.6682f, -0.3272f, -.6682f).endVertex(); // first
		
		buffer.pos(0, 0, 0).tex(0, .5).normal(0, 1, 0).endVertex();
		buffer.pos(.5, 1, -.5).tex(1,0).normal(.6682f, -0.3272f, -.6682f).endVertex();
		buffer.pos(.5, 1, .5).tex(1,1).normal(.6682f, -0.3272f, .6682f).endVertex(); // second
		
		buffer.pos(0, 0, 0).tex(.5,0).normal(0, 1, 0).endVertex();
		buffer.pos(.5, 1, .5).tex(1,1).normal(.6682f, -0.3272f, .6682f).endVertex();
		buffer.pos(-.5, 1, .5).tex(0,1).normal(-.6682f, -0.3272f, .6682f).endVertex(); // third
		
		buffer.pos(0, 0, 0).tex(1,.5).normal(0, 1, 0).endVertex();
		buffer.pos(-.5, 1, .5).tex(0,1).normal(-.6682f, -0.3272f, .6682f).endVertex();
		buffer.pos(-.5, 1, -.5).tex(0,0).normal(-.6682f, -0.3272f, -.6682f).endVertex(); // fourth
		
		tessellator.draw();
	}
	
	// Origin at center of bottom
	protected void renderBase(Tessellator tessellator, VertexBuffer buffer) {
		
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
		
		// Top
		buffer.pos(-.5, .2, -.5).tex(0,0).normal(-.5773f, .5773f, -.5773f).endVertex();
		buffer.pos(-.5, .2, .5).tex(0,1).normal(-.5773f, .5773f, .5773f).endVertex();
		buffer.pos(.5, .2, .5).tex(1,1).normal(.5773f, .5773f, .5773f).endVertex();
		buffer.pos(.5, .2, -.5).tex(1,0).normal(.5773f, .5773f, -.5773f).endVertex();
		
		// North
		buffer.pos(.5, .2, -.5).tex(1,0).normal(.5773f, .5773f, -.5773f).endVertex();
		buffer.pos(.5, 0, -.5).tex(1,.2).normal(.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(-.5, 0, -.5).tex(0,.2).normal(-.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(-.5, .2, -.5).tex(0,0).normal(-.5773f, .5773f, -.5773f).endVertex();
		
		// East
		buffer.pos(.5, .2, .5).tex(1,1).normal(.5773f, .5773f, .5773f).endVertex();
		buffer.pos(.5, 0, .5).tex(.8,1).normal(.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(.5, 0, -.5).tex(.8,0).normal(.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(.5, .2, -.5).tex(1,0).normal(.5773f, .5773f, -.5773f).endVertex();
		
		// South
		buffer.pos(-.5, .2, .5).tex(0,1).normal(-.5773f, .5773f, .5773f).endVertex();
		buffer.pos(-.5, 0, .5).tex(0,.8).normal(-.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(.5, 0, .5).tex(1,.8).normal(.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(.5, .2, .5).tex(1,1).normal(.5773f, .5773f, .5773f).endVertex();
		
		// West
		buffer.pos(-.5, .2, -.5).tex(0,0).normal(-.5773f, .5773f, -.5773f).endVertex();
		buffer.pos(-.5, 0, -.5).tex(.2,0).normal(-.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(-.5, 0, .5).tex(.2,1).normal(-.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(-.5, .2, .5).tex(0,1).normal(-.5773f, .5773f, .5773f).endVertex();
		
		// Bottom
		buffer.pos(-.5, 0, -.5).tex(1,0).normal(-.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(.5, 0, -.5).tex(0,0).normal(.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(.5, 0, .5).tex(0,1).normal(.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(-.5, 0, .5).tex(1,1).normal(-.5773f, -.5773f, .5773f).endVertex();
		
		tessellator.draw();
	}
	
	protected void renderGem(Tessellator tessellator, VertexBuffer buffer, boolean outline) {
		if (!outline) {
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
		} else {
			buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
		}
		
		// Top
		buffer.pos(-.5, 1, -.5).tex(0,0).normal(-.5773f, .5773f, -.5773f).endVertex();
		buffer.pos(-.5, 1, .5).tex(0,1).normal(-.5773f, .5773f, .5773f).endVertex();
		buffer.pos(.5, 1, .5).tex(1,1).normal(.5773f, .5773f, .5773f).endVertex();
		buffer.pos(.5, 1, -.5).tex(1,0).normal(.5773f, .5773f, -.5773f).endVertex();
		
		if (outline) {
			buffer.pos(-.5, 1, -.5).tex(0,0).normal(-.5773f, .5773f, -.5773f).endVertex();
			tessellator.draw();
			buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
		}
		
		// North
		buffer.pos(.5, 1, -.5).tex(1,0).normal(.5773f, .5773f, -.5773f).endVertex();
		buffer.pos(.5, 0, -.5).tex(1,1).normal(.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(-.5, 0, -.5).tex(0,1).normal(-.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(-.5, 1, -.5).tex(0,0).normal(-.5773f, .5773f, -.5773f).endVertex();
		
		if (outline) {
			tessellator.draw();
			buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
		}
		
		// East
		buffer.pos(.5, 1, .5).tex(1,1).normal(.5773f, .5773f, .5773f).endVertex();
		buffer.pos(.5, 0, .5).tex(0,1).normal(.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(.5, 0, -.5).tex(0,0).normal(.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(.5, 1, -.5).tex(1,0).normal(.5773f, .5773f, -.5773f).endVertex();
		
		if (outline) {
			tessellator.draw();
			buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
		}
		
		// South
		buffer.pos(-.5, 1, .5).tex(0,1).normal(-.5773f, .5773f, .5773f).endVertex();
		buffer.pos(-.5, 0, .5).tex(0,0).normal(-.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(.5, 0, .5).tex(1,0).normal(.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(.5, 1, .5).tex(1,1).normal(.5773f, .5773f, .5773f).endVertex();
		
		if (outline) {
			tessellator.draw();
			buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
		}
		
		// West
		buffer.pos(-.5, 1, -.5).tex(0,0).normal(-.5773f, .5773f, -.5773f).endVertex();
		buffer.pos(-.5, 0, -.5).tex(1,0).normal(-.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(-.5, 0, .5).tex(1,1).normal(-.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(-.5, 1, .5).tex(0,1).normal(-.5773f, .5773f, .5773f).endVertex();
		
		if (outline) {
			tessellator.draw();
			buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
		}
		
		// Bottom
		buffer.pos(-.5, 0, -.5).tex(1,0).normal(-.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(.5, 0, -.5).tex(0,0).normal(.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(.5, 0, .5).tex(0,1).normal(.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(-.5, 0, .5).tex(1,1).normal(-.5773f, -.5773f, .5773f).endVertex();
		
		tessellator.draw();
	}
	
	protected void renderAetherDebug(WispBlockTileEntity te) {
//		final int aether = te.getHandler().getAether(null);
//		final int maxAether = te.getHandler().getMaxAether(null);
//		final String str = aether + " / " + maxAether;
//		final FontRenderer fonter = Minecraft.getMinecraft().fontRendererObj;
//		
//		GlStateManager.pushMatrix();
//		
//		GlStateManager.rotate(180, 1, 0, 0);
//		// Make billboard
//		GlStateManager.color(1f, 1f, 1f, 1f);
//		GlStateManager.disableBlend();
//		GlStateManager.disableLighting();
//		fonter.drawString(str, -(fonter.getStringWidth(str) / 2), 0, 0xFF000000);
//		GlStateManager.popMatrix();
	}
	
	@Override
	public void renderTileEntityAt(WispBlockTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		
		final float shortPeriod = 3f;
		final float longPeriod = 30f;
		// float progress = (te.ticks + partialTicks % period) / period;
		final double shortPeriodMS = shortPeriod * 1000;
		final double longPeriodMS = longPeriod * 1000;
		float progressShort = (float) ((System.currentTimeMillis() % shortPeriodMS) / shortPeriodMS);
		float progShortOffset = (float) Math.sin(progressShort * Math.PI * 2);
		float progressLong = (float) ((System.currentTimeMillis() % longPeriodMS) / longPeriodMS);
		//float progLongOffset = (float) Math.sin(progressLong * Math.PI * 2);
		
		// Figure out color and fetch scroll
		int baseColor = 0xFFFFFFFF;
		int spellIcon = -1;
		ItemStack scroll = te.getScroll();
		if (scroll != null) {
			Spell spell = SpellScroll.getSpell(scroll);
			if (spell != null) {
				baseColor = spell.getPrimaryElement().getColor();
				spellIcon = spell.getIconIndex();
			}
			
		}
		ItemStack reagents = te.getReagent();
		
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();
		GlStateManager.pushAttrib();
		GlStateManager.enableBlend();
		
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.enableLighting();
		GlStateManager.disableRescaleNormal();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + .5, y, z + .5);
		
		// Base
		Minecraft.getMinecraft().getTextureManager().bindTexture(BASE_TEX_LOC);
		GlStateManager.color(.99f, .99f, .99f, 1f);
		GlStateManager.pushMatrix();
		renderBase(tessellator, buffer);
		GlStateManager.popMatrix();
		
		double platOffset = progShortOffset * .01;
		// Platform
		Minecraft.getMinecraft().getTextureManager().bindTexture(PLATFORM_TEX_LOC);
		GlStateManager.color(.99f, .99f, .99f, 1f);
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, .8 + platOffset, 0);
		GlStateManager.scale(.5, .5, .5);
		GlStateManager.rotate(360f * progressLong, 0, 1, 0);
		renderPlatform(tessellator, buffer);
		GlStateManager.popMatrix();
		
		// Scroll
		if (scroll != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 1.31 + platOffset, 0);
			GlStateManager.rotate(360f * progressLong, 0, 1, 0);
			GlStateManager.translate(0, 0, .1);
			GlStateManager.rotate(90f, 1, 0, 0);
			
			GlStateManager.scale(.25f, .25f, .25f);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.disableLighting();
			GlStateManager.enableAlpha();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			
			Minecraft.getMinecraft().getRenderItem()
				.renderItem(scroll, TransformType.GROUND);
			GlStateManager.popMatrix();
		}
		
		// Reagent
		if (reagents != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 1.31 + platOffset, 0);
			GlStateManager.rotate(360f * progressLong, 0, 1, 0);
			GlStateManager.translate(0, 0, -.15);
			GlStateManager.rotate(90f, 1, 0, 0);
			
			GlStateManager.scale(.25f, .25f, .25f);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.disableLighting();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			
			Minecraft.getMinecraft().getRenderItem()
				.renderItem(reagents, TransformType.GROUND);
			GlStateManager.popMatrix();
		}
		
		// Gem effect
		if (scroll != null || reagents != null) {
			
			// Draw spell icon
			if (scroll != null) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 2 + platOffset, 0);
				GlStateManager.scale(.5, .5, .5);
				GlStateManager.rotate(90f + (float) (360.0 * (Math.atan2(z, x) / (2 * Math.PI))), 0, -1, 0);
				GlStateManager.translate(.5, 0, 0);
				GlStateManager.rotate(180, 0, 0, 1);
				// Make billboard
				GlStateManager.color(1f, 1f, 1f, .4f);
				GlStateManager.disableCull();
				SpellIcon.get(spellIcon).render(Minecraft.getMinecraft(), 0, 0, 1, 1);
				GlStateManager.enableCull();
				GlStateManager.popMatrix();
			}
			
			// Draw rotating 'gem' effect
			GlStateManager.disableColorMaterial();
			//GlStateManager.enableLighting();
			final int count = 16;
			for (int i = 0; i < count; i++) {
				final float voffset = reagents != null && scroll != null ? (float) (Math.sin((((float) i / ((float) count / 2f)) + progressShort) * Math.PI * 2) * .05) : 0f;
				//final float rotoffset = reagents != null && scroll != null ? (360f * progressLong) : 0f;
				final float rotoffset = 360f * progressLong;
				int color = baseColor;
				//double voffset = progShortOffset * .01;
				
				// draw white if aether levels are low.
				if ((float) i / (float) count
						>= (float) te.getHandler().getAether(null) / (float) te.getHandler().getMaxAether(null)) {
					color = 0xFFFFFFFF;
				}
				
				GlStateManager.pushMatrix();
				Minecraft.getMinecraft().getTextureManager().bindTexture(GEM_TEX_LOC);
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA,
						SourceFactor.ONE, DestFactor.ZERO);
				GlStateManager.color(
						(float) ((color >> 16) & 0xFF) / 256f,
						(float) ((color >> 8) & 0xFF) / 256f,
						(float) ((color >> 0) & 0xFF) / 256f,
						.8f);
				GlStateManager.translate(0, .8, 0);
				GlStateManager.rotate(rotoffset + (360f * ((float) i / (float) count)), 0, -1, 0);
				GlStateManager.translate(0, voffset, .3);
				GlStateManager.scale(.05, .05, .05);
				
				renderGem(tessellator, buffer, false);
				GlStateManager.color(0f, 0f, 0f, 1f);
				GlStateManager.glLineWidth(2f);
				renderGem(tessellator, buffer, true);
				GlStateManager.color(1f, 1f, 1f, 1f);
				GlStateManager.popMatrix();
				
				// Single bouncing ring
				/*
				GlStateManager.pushMatrix();
				Minecraft.getMinecraft().getTextureManager().bindTexture(GEM_TEX_LOC);
				GlStateManager.enableBlend();
				GlStateManager.color(
						(float) ((color >> 16) & 0xFF) / 256f,
						(float) ((color >> 8) & 0xFF) / 256f,
						(float) ((color >> 0) & 0xFF) / 256f,
						.4f);
				GlStateManager.translate(0, .8, 0);
				GlStateManager.rotate(360f * progressShort, 0, 1, 0);
				GlStateManager.rotate(360f * ((float) i / (float) count), 0, -1, 0);
				GlStateManager.translate(0, voffset, .3);
				GlStateManager.scale(.05, .05, .05);
				
				renderGem(tessellator, buffer);
				GlStateManager.color(1f, 1f, 1f, 1f);
				GlStateManager.popMatrix();
				 */
			}
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 2.5, 0);
		GlStateManager.scale(.05, .05, .05);
		GlStateManager.rotate(90f + (float) (360.0 * (Math.atan2(z, x) / (2 * Math.PI))), 0, -1, 0);
		renderAetherDebug(te);
		GlStateManager.popMatrix();
		
		
		GlStateManager.color(1f, 1f, 1f, 1f);
		GlStateManager.popMatrix();
		GlStateManager.popAttrib();
		GlStateManager.disableBlend();
	}
	
}
