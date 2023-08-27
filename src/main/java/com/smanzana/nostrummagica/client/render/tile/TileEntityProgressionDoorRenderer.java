package com.smanzana.nostrummagica.client.render.tile;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.ProgressionDoorTileEntity;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TileEntityProgressionDoorRenderer extends TileEntitySpecialRenderer<ProgressionDoorTileEntity> {

	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(ProgressionDoorTileEntity.class,
				new TileEntityProgressionDoorRenderer());
	}
	
	private static final ResourceLocation TEX_GEM_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/gui/brass.png");
	private static final ResourceLocation TEX_PLATE_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/blocks/ceramic_generic.png");
	
	public TileEntityProgressionDoorRenderer() {
		
	}
	
	@Override
	public void render(ProgressionDoorTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		
		double time = (double)te.getWorld().getTotalWorldTime() + partialTicks;
		INostrumMagic attr = NostrumMagica.getMagicWrapper(Minecraft.getInstance().player);
		
		GlStateManager.pushMatrix();
		GlStateManager.translatef(x + .5, y + 1.2, z + .5);
		
		// Render centered on bottom-center of door, not TE (in case they're different)
		{
			BlockPos pos = te.getPos();
			BlockPos targ = te.getBottomCenterPos();
			GlStateManager.translatef(targ.getX() - pos.getX(), targ.getY() - pos.getY(), targ.getZ() - pos.getZ());
		}
		
		float rotY = te.getFace().getOpposite().getHorizontalAngle();
		
		GlStateManager.rotatef((float) rotY, 0, -1, 0);
		
		
		BufferBuilder wr = Tessellator.getInstance().getBuffer();
		
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableLighting();
		GlStateManager.enableAlphaTest();
		
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		
		// Draw lock symbol
		{
			GlStateManager.pushMatrix();
			
			
			final double horizontalRadius = .2;
			final double verticalRadius = .4;
			final int points = 4;
			final double depth = .2;
			final double spinRate = 60.0;

			GlStateManager.translatef(0, 0.25, -.3);
			GlStateManager.rotatef((float) (360.0 * (time % spinRate) / spinRate),
					0, 1, 0);
			
			if (te.meetsRequirements(Minecraft.getInstance().player, null))
				GlStateManager.color4f(0f, 1f, 1f, .8f);
			else
				GlStateManager.color4f(1f, .3f, .6f, .8f);
			
			Minecraft.getInstance().getTextureManager().bindTexture(TEX_GEM_LOC);
			
			wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
			wr.pos(0, 0, -depth).tex(.5, .5).endVertex();
			for (int i = points; i >= 0; i--) {
				double angle = (2*Math.PI) * ((double) i / (double) points);
				double vx = Math.cos(angle) * horizontalRadius;
				double vy = Math.sin(angle) * verticalRadius;
				
				double u = (vx + (horizontalRadius)) / (horizontalRadius * 2);
				double v = (vy + (verticalRadius)) / (verticalRadius * 2);
				wr.pos(vx, vy, 0).tex(u, v).endVertex();
			}
			Tessellator.getInstance().draw();
			
			wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
			wr.pos(0, 0, depth).tex(.5, .5).endVertex();
			for (int i = 0; i <= points; i++) {
				double angle = (2*Math.PI) * ((double) i / (double) points);
				double vx = Math.cos(angle) * horizontalRadius;
				double vy = Math.sin(angle) * verticalRadius;
				
				double u = (vx + (horizontalRadius)) / (horizontalRadius * 2);
				double v = (vy + (verticalRadius)) / (verticalRadius * 2);
				wr.pos(vx, vy, 0).tex(u, v).endVertex();
			}
			Tessellator.getInstance().draw();
			
			
			GlStateManager.popMatrix();
		}
		
		// Draw requirement icons
		if (!te.getRequiredComponents().isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0, 0, -.2);
			
			final float angleDiff = (float) (Math.PI/(float)te.getRequiredComponents().size());
			float angle = (float) (Math.PI + angleDiff/2);
			for (SpellComponentWrapper comp : te.getRequiredComponents()) {
				boolean has = false;
				SpellComponentIcon icon;
				if (comp.isTrigger()) {
					icon = SpellComponentIcon.get(comp.getTrigger());
					has = attr != null && attr.getTriggers().contains(comp.getTrigger());
				} else if (comp.isShape()) {
					icon = SpellComponentIcon.get(comp.getShape());
					has = attr != null && attr.getShapes().contains(comp.getShape());
				} else if (comp.isAlteration()) {
					icon = SpellComponentIcon.get(comp.getAlteration());
					Boolean known = attr == null ? null : attr.getAlterations().get(comp.getAlteration());
					has = known != null && known;
				} else {
					icon = SpellComponentIcon.get(comp.getElement());
					Boolean known = attr == null ? null : attr.getKnownElements().get(comp.getElement());
					has = known != null && known;
				}
				
				if (icon != null && icon.getModelLocation() != null) {
					// Draw background
					Minecraft.getInstance().getTextureManager().bindTexture(TEX_PLATE_LOC);
					if (has)
						GlStateManager.color4f(.4f, .4f, .4f, .4f);
					else
						GlStateManager.color4f(.8f, .6f, .6f, .8f);
					
					wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
					
					final double wiggleTicks = 100;
					final double imageHalfLength = .15;
					final double plateHalfLength = imageHalfLength * 2;
					final double radius = .75;
					
					double effAngle = angle + (.04f * Math.cos(2 * Math.PI * (time % wiggleTicks) / wiggleTicks));
					double vx = Math.cos(effAngle) * radius;
					double vy = Math.sin(effAngle) * radius;
					
					// +z
					wr.pos(vx, vy + plateHalfLength, 0.0005).tex(0.0, 1.0).normal(0, 0, -1).endVertex();
					wr.pos(vx + plateHalfLength, vy, 0.0005).tex(1.0, 1.0).normal(0, 0, -1).endVertex();
					wr.pos(vx, vy - plateHalfLength, 0.0005).tex(1.0, 0.0).normal(0, 0, -1).endVertex();
					wr.pos(vx - plateHalfLength, vy, 0.0005).tex(0.0, 0.0).normal(0, 0, -1).endVertex();
					
					//wr.finishDrawing();
					Tessellator.getInstance().draw();
					
					// Draw icon
					Minecraft.getInstance().getTextureManager().bindTexture(icon.getModelLocation());
					if (has)
						GlStateManager.color4f(1, 1, 1, .2f);
					else
						GlStateManager.color4f(1, 1, 1, .8f);
					
					wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
					
					// +z
					wr.pos(vx + imageHalfLength, vy - imageHalfLength, 0.0).tex(0.0, 1.0).normal(0, 0, -1).endVertex();
					wr.pos(vx - imageHalfLength, vy - imageHalfLength, 0.0).tex(1.0, 1.0).normal(0, 0, -1).endVertex();
					wr.pos(vx - imageHalfLength, vy + imageHalfLength, 0.0).tex(1.0, 0.0).normal(0, 0, -1).endVertex();
					wr.pos(vx + imageHalfLength, vy + imageHalfLength, 0.0).tex(0.0, 0.0).normal(0, 0, -1).endVertex();
					
					//wr.finishDrawing();
					Tessellator.getInstance().draw();
				}
				angle += angleDiff;
			}
			
			GlStateManager.popMatrix();
		}
		
		// Draw required level
		if (te.getRequiredLevel() > 0) {
			GlStateManager.pushMatrix();
			double drawZ = -.5;
			
			GlStateManager.translatef(0, 1, drawZ);
			final float VANILLA_FONT_SCALE = 0.010416667f;
			
			GlStateManager.scalef(-VANILLA_FONT_SCALE * 2, -VANILLA_FONT_SCALE * 2, VANILLA_FONT_SCALE * 2);
			
			String val = "Level: " + te.getRequiredLevel();
			
			int color = 0xFFFFFFFF;
			if (attr != null && attr.getLevel() >= te.getRequiredLevel()) {
				color = 0x50A0FFA0;
			}
			
			this.getFontRenderer().drawString(val, this.getFontRenderer().getStringWidth(val) / -2, 0, color);
			GlStateManager.popMatrix();
		}
		
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
		
	}
	
}
