package com.smanzana.nostrummagica.client.render.tile;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.LockedChest;
import com.smanzana.nostrummagica.tiles.LockedChestEntity;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public class TileEntityLockedChestRenderer extends TileEntityRenderer<LockedChestEntity> {
	
	private static final ResourceLocation TEXT_LOCK_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/models/block/lock_plate.png");
	private static final ResourceLocation TEXT_CHAINLINK_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/models/block/chain_link.png");

	public TileEntityLockedChestRenderer() {
		
	}
	
	protected void renderLock(LockedChestEntity te, double ticks) {
		final Direction direction = te.getBlockState().get(LockedChest.FACING);
		float rot = direction.getHorizontalAngle() + 90f;
		
		final double glowPeriod = 60;
		final double glowProg = ((ticks % glowPeriod) / glowPeriod); 
		final float glow = .15f * (float) Math.sin(glowProg * 2 * Math.PI);

		GlStateManager.disableCull();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1f, 0f, 0f, .75f + glow);
		
		GlStateManager.pushMatrix();
		GlStateManager.rotatef(rot, 0, -1, 0); // Rotate arm that's centered in the block
		GlStateManager.translated(10.5, 0, 0); // distance from center of block (so it's outside it)
		GlStateManager.rotatef(-90f, 0, 1, 0); // Rotate so it's facing away from block instead of perpendicular
		GlStateManager.translated(-3, -3, 0); // center
		
		// Wiggle a bit
		final double xWigglePeriod = 160;
		final double xWiggleProg = ((ticks % xWigglePeriod) / xWigglePeriod);
		final double xWiggle = .5 * Math.sin(xWiggleProg * Math.PI * 2);
		final double yWigglePeriod = 200;
		final double yWiggleProg = ((ticks % yWigglePeriod) / yWigglePeriod);
		final double yWiggle = .5 * Math.sin(yWiggleProg * Math.PI * 2);
		GlStateManager.translated(xWiggle, yWiggle, 0); // center
		
		this.bindTexture(TEXT_LOCK_LOC);
		RenderFuncs.drawScaledCustomSizeModalRect(0, 0, 0, 0, 16, 16, 6, 6, 16, 16);
		GlStateManager.popMatrix();
		GlStateManager.enableCull();
	}
	
	protected void renderChains(LockedChestEntity te, double ticks) {
		final Direction direction = te.getBlockState().get(LockedChest.FACING);
		float rot = direction.getHorizontalAngle();
		
		final double armLen = 10;
		final int points = 8;
		final double linkWidth = 3;
		
		final double minorWigglePeriod = 140;
		final double minorWiggleProg = 1 - ((ticks % minorWigglePeriod) / minorWigglePeriod);
		final float minorWiggle = (float) (1.5 * Math.sin(2 * Math.PI * minorWiggleProg));
		
		final double majorWigglePeriod = 140;
		final double majorWiggleProg = 1 - ((ticks % majorWigglePeriod) / majorWigglePeriod);
		final float majorWiggle = (float) (2 * Math.sin(2 * Math.PI * majorWiggleProg));
		
		this.bindTexture(TEXT_CHAINLINK_LOC);
		GlStateManager.pushMatrix();
		GlStateManager.rotatef(rot, 0, -1, 0); // Rotate to match block's orientation
		
		GlStateManager.pushMatrix();
		GlStateManager.rotatef(-115f - majorWiggle, 0, 0, 1);
		renderChain(te, ticks, armLen, points, linkWidth);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotatef(-30f - minorWiggle, 0, 0, 1);
		renderChain(te, ticks, armLen, points, linkWidth);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotatef(30f + minorWiggle, 0, 0, 1); // Is this faster than pushing and popping matrix?
		renderChain(te, ticks, armLen, points, linkWidth);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotatef(115f + majorWiggle, 0, 0, 1); // Is this faster than pushing and popping matrix?
		renderChain(te, ticks, armLen, points, linkWidth);
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}
	
	protected void renderChain(LockedChestEntity te, double ticks, double armLen, int points, double linkWidth) {
		// Two ribbons perpendicular to each other offset by half a v on texture
			
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		
		BufferBuilder wr = Tessellator.getInstance().getBuffer();
		
		final double colorPeriod = 140;
		final double colorProg = 1 - ((ticks % colorPeriod) / colorPeriod); 
		
		{
			wr.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);
			
			for (int i = 0; i <= points; i++) {
				final double prog = ((double) i / (double)points);
				final double px = 0;
				final double py = armLen * Math.sin(prog * Math.PI); // half circle
				final double pz = armLen * Math.cos(prog * Math.PI); //half circle
				final double colorDist = Math.min(Math.abs(colorProg - prog), 1 - Math.abs(colorProg - prog));
				final float alpha = Math.max(.2f, 1f - 4f *(float) colorDist);
				
				final double v = (i % 2 == 0 ? 0 : 1);
				
				wr.pos(px - (linkWidth / 2), py, pz).tex(0, v).color(1f, 1f, 1f, alpha).endVertex();
				wr.pos(px + (linkWidth / 2), py, pz).tex(1, v).color(1f, 1f, 1f, alpha).endVertex();
			}
			
			Tessellator.getInstance().draw();
		}
		
		{
			wr.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);
			
			for (int i = 0; i < points; i++) {
				final double prog = (((double) i + .5) / (double)points);
				final double px = 0;
				final double py = armLen * Math.sin(prog * Math.PI); // half circle
				final double pz = armLen * Math.cos(prog * Math.PI); //half circle
				final double colorDist = Math.min(Math.abs(colorProg - prog), 1 - Math.abs(colorProg - prog));
				final float alpha = Math.max(.2f, 1f - 4f *(float) colorDist);
				
				final double v = (i % 2 == 0 ? 0 : 1);
				final double offY = (linkWidth/2) * Math.sin(prog * Math.PI);
				final double offZ = (linkWidth/2) * Math.cos(prog * Math.PI);
				
				wr.pos(px, py - offY, pz - offZ).tex(0, v).color(1f, 1f, 1f, alpha).endVertex();
				wr.pos(px, py + offY, pz + offZ).tex(1, v).color(1f, 1f, 1f, alpha).endVertex();
			}
			
			Tessellator.getInstance().draw();
		}
		
		GlStateManager.enableCull();
	}
	
	@Override
	public void render(LockedChestEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		
		final double ticks = te.getWorld().getGameTime() + partialTicks;
		
		GlStateManager.pushMatrix();
		GlStateManager.translated(x + .5, y + .5, z + .5);
		GlStateManager.scaled(1.0/16.0, 1.0/16.0, 1.0/16.0); // Down to block scale, where 1 block is 16 units/pixels
		
		// Draw chain links
		this.renderChains(te, ticks);
		
		// Draw lock icon
		this.renderLock(te, ticks);


		GlStateManager.popMatrix();
		
	}
	
}
