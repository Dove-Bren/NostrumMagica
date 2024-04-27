package com.smanzana.nostrummagica.client.render.tile;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.LockedChest;
import com.smanzana.nostrummagica.items.WorldKeyItem;
import com.smanzana.nostrummagica.tiles.LockedChestEntity;
import com.smanzana.nostrummagica.utils.RenderFuncs;
import com.smanzana.nostrummagica.world.NostrumKeyRegistry.NostrumWorldKey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class TileEntityLockedChestRenderer extends TileEntityRenderer<LockedChestEntity> {
	
	private static final ResourceLocation TEXT_LOCK_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/models/block/lock_plate.png");
	private static final ResourceLocation TEXT_CHAINLINK_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/models/block/chain_link.png");

	public TileEntityLockedChestRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	protected void renderLock(LockedChestEntity te, double ticks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		final Direction direction = te.getBlockState().get(LockedChest.FACING);
		float rot = direction.getHorizontalAngle() + 90f;
		
		final float glow;
		if (te.getBlockState().get(LockedChest.UNLOCKABLE)) {
			final double glowPeriod = 20;
			final double glowProg = ((ticks % glowPeriod) / glowPeriod);
			glow = .15f * (float) Math.sin(glowProg * 2 * Math.PI);
		} else {
			final double glowPeriod = 60;
			final double glowProg = ((ticks % glowPeriod) / glowPeriod); 
			glow = .5f + (.15f * (float) Math.sin(glowProg * 2 * Math.PI));
		}
		
		final int colorRGB = te.getColor().getColorValue();
		final float red = ((float) ((colorRGB >> 16) & 0xFF) / 255f);
		final float green = ((float) ((colorRGB >> 8) & 0xFF) / 255f);
		final float blue = ((float) ((colorRGB >> 0) & 0xFF) / 255f);
		

		final double xWigglePeriod = 160;
		final double xWiggleProg = ((ticks % xWigglePeriod) / xWigglePeriod);
		final double xWiggle = .5 * Math.sin(xWiggleProg * Math.PI * 2);
		final double yWigglePeriod = 200;
		final double yWiggleProg = ((ticks % yWigglePeriod) / yWigglePeriod);
		final double yWiggle = .5 * Math.sin(yWiggleProg * Math.PI * 2);

//		GlStateManager.disableCull();
//		GlStateManager.disableLighting();
//		GlStateManager.enableBlend();
//		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
//		GlStateManager.color4f(red, green, blue, .25f + glow);
		
		matrixStackIn.push();
		matrixStackIn.rotate(Vector3f.YN.rotationDegrees(rot)); // Rotate arm that's centered in the block
		matrixStackIn.translate(10.5, 0, 0); // distance from center of block (so it's outside it)
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90f)); // Rotate so it's facing away from block instead of perpendicular
		matrixStackIn.translate(-3, -3, 0); // center
		
		// Wiggle a bit
		matrixStackIn.translate(xWiggle, yWiggle, 0); // center
		
		this.bindTexture(TEXT_LOCK_LOC);
		RenderFuncs.drawScaledCustomSizeModalRect(0, 0, 0, 0, 16, 16, 6, 6, 16, 16);
		
		matrixStackIn.pop();
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
	public void render(LockedChestEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		
		final double ticks = tileEntityIn.getWorld().getGameTime() + partialTicks;
		final Minecraft mc = Minecraft.getInstance();
		
		GlStateManager.pushMatrix();
		GlStateManager.translated(x + .5, y + .5, z + .5);
		GlStateManager.scaled(1.0/16.0, 1.0/16.0, 1.0/16.0); // Down to block scale, where 1 block is 16 units/pixels
		
		// Draw chain links
		this.renderChains(tileEntityIn, ticks);
		
		// Draw lock icon
		this.renderLock(tileEntityIn, ticks);

		
		// Draw lock info
		if (mc.player.isCreative())
		{
			final String lockStr;
			boolean matches = false;
			if (tileEntityIn.hasWorldKey()) {
				NostrumWorldKey key = tileEntityIn.getWorldKey();
				lockStr = mc.player.isSneaking() ? key.toString() : key.toString().substring(0, 8);
				
				final ItemStack held = mc.player.getHeldItemMainhand();
				if ((held.getItem() instanceof WorldKeyItem && key.equals(((WorldKeyItem) held.getItem()).getKey(held)))) {
					matches = true;
				}
			} else {
				lockStr = "No lock info found";
			}
			
			GlStateManager.scaled(8, 8, 8);
			ActiveRenderInfo renderInfo = mc.gameRenderer.getActiveRenderInfo();
			float viewerYaw = renderInfo.getYaw();
			float viewerPitch = renderInfo.getPitch();
			float yOffset = 1.4f;
			int i = 0;
			
			if (matches) {
				final double matchWigglePeriod = 20;
				final double matchWiggleProg = 1 - ((ticks % matchWigglePeriod) / matchWigglePeriod);
				yOffset += (float) (.05 * Math.sin(2 * Math.PI * matchWiggleProg));
			}
			
			GameRenderer.drawNameplate(mc.fontRenderer, lockStr, (float)0, (float)0 + yOffset, (float)0, i, viewerYaw, viewerPitch, false);
		}

		GlStateManager.popMatrix();
		
	}
}
