package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.dungeon.LockedChest;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.item.WorldKeyItem;
import com.smanzana.nostrummagica.tile.LockedChestEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.world.NostrumKeyRegistry.NostrumWorldKey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class TileEntityLockedChestRenderer extends TileEntityRenderer<LockedChestEntity> {
	
	public static final ResourceLocation TEXT_LOCK_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/models/block/lock_plate.png");
	public static final ResourceLocation TEXT_CHAINLINK_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/models/block/chain_link.png");

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

		final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.LOCKEDCHEST_LOCK);
		
		matrixStackIn.push();
		matrixStackIn.rotate(Vector3f.YN.rotationDegrees(rot)); // Rotate arm that's centered in the block
		matrixStackIn.translate(11.5, 0, 0); // distance from center of block (so it's outside it)
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90f)); // Rotate so it's facing away from block instead of perpendicular
		matrixStackIn.translate(-3, -3, 0); // center
		
		// Wiggle a bit
		matrixStackIn.translate(xWiggle, yWiggle, 0); // center
		RenderFuncs.drawScaledCustomSizeModalRect(matrixStackIn, buffer, 0, 0, 0, 0, 16, 16, 6, 6, 16, 16, red, green, blue, .25f + glow);
		
		matrixStackIn.pop();
	}
	
	protected void renderChains(LockedChestEntity te, double ticks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		final Direction direction = te.getBlockState().get(LockedChest.FACING);
		float rot = direction.getHorizontalAngle();
		
		final float armLen = 10;
		final int points = 8;
		final float linkWidth = 3;
		
		final double minorWigglePeriod = 140;
		final double minorWiggleProg = 1 - ((ticks % minorWigglePeriod) / minorWigglePeriod);
		final float minorWiggle = (float) (1.5 * Math.sin(2 * Math.PI * minorWiggleProg));
		
		final double majorWigglePeriod = 140;
		final double majorWiggleProg = 1 - ((ticks % majorWigglePeriod) / majorWigglePeriod);
		final float majorWiggle = (float) (2 * Math.sin(2 * Math.PI * majorWiggleProg));
		
		final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.LOCKEDCHEST_CHAIN);
		
		matrixStackIn.push();
		matrixStackIn.rotate(Vector3f.YN.rotationDegrees(rot)); // Rotate to match block's orientation
		
		matrixStackIn.push();
		matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-115f - majorWiggle));
		renderChain(matrixStackIn, buffer, packedLightIn, ticks, armLen, points, linkWidth);
		matrixStackIn.pop();

		matrixStackIn.push();
		matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-30f - minorWiggle));
		renderChain(matrixStackIn, buffer, packedLightIn, ticks, armLen, points, linkWidth);
		matrixStackIn.pop();

		matrixStackIn.push();
		matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(30f + minorWiggle)); // Is this faster than pushing and popping matrix?
		renderChain(matrixStackIn, buffer, packedLightIn, ticks, armLen, points, linkWidth);
		matrixStackIn.pop();

		matrixStackIn.push();
		matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(115f + majorWiggle)); // Is this faster than pushing and popping matrix?
		renderChain(matrixStackIn, buffer, packedLightIn, ticks, armLen, points, linkWidth);
		matrixStackIn.pop();

		matrixStackIn.pop();
	}
	
	public static void renderChain(MatrixStack matrixStackIn, IVertexBuilder buffer, int packedLightIn, double ticks, float armLen, int points, float linkWidth) {
		// Two ribbons perpendicular to each other offset by half a v on texture
			
		final float colorPeriod = 140;
		final float colorProg = (float) (1 - ((ticks % colorPeriod) / colorPeriod));
		final Matrix4f transform = matrixStackIn.getLast().getMatrix();
		//final Matrix3f normal = matrixStackIn.getLast().getNormal();
		
		{
			for (int i = 0; i < points; i++) {
				final float prog1 = ((float) i / (float)points);
				final float px1 = 0;
				final float py1 = (float) (armLen * Math.sin(prog1 * Math.PI)); // half circle
				final float pz1 = (float) (armLen * Math.cos(prog1 * Math.PI)); //half circle
				final float colorDist1 = Math.min(Math.abs(colorProg - prog1), 1 - Math.abs(colorProg - prog1));
				final float alpha1 = Math.max(.2f, 1f - 4f * colorDist1);
				final float v1 = (i % 2 == 0 ? 0 : 1);
				final float offY1 = (float) ((linkWidth/2) * Math.sin(prog1 * Math.PI));
				final float offZ1 = (float) ((linkWidth/2) * Math.cos(prog1 * Math.PI));
				
				final float prog2 = ((float) (i+1) / (float)points);
				final float px2 = 0;
				final float py2 = (float) (armLen * Math.sin(prog2 * Math.PI)); // half circle
				final float pz2 = (float) (armLen * Math.cos(prog2 * Math.PI)); //half circle
				final float colorDist2 = Math.min(Math.abs(colorProg - prog2), 1 - Math.abs(colorProg - prog2));
				final float alpha2 = Math.max(.2f, 1f - 4f * colorDist2);
				final float v2 = ((i+1) % 2 == 0 ? 0 : 1);
				final float offY2 = (float) ((linkWidth/2) * Math.sin(prog2 * Math.PI));
				final float offZ2 = (float) ((linkWidth/2) * Math.cos(prog2 * Math.PI));
				
				buffer.pos(transform, px1 - (linkWidth / 2), py1, pz1).color(1f, 1f, 1f, alpha1).tex(0, v1).lightmap(packedLightIn).endVertex();
				buffer.pos(transform, px1 + (linkWidth / 2), py1, pz1).color(1f, 1f, 1f, alpha1).tex(1, v1).lightmap(packedLightIn).endVertex();
				buffer.pos(transform, px2 + (linkWidth / 2), py2, pz2).color(1f, 1f, 1f, alpha2).tex(1, v2).lightmap(packedLightIn).endVertex();
				buffer.pos(transform, px2 - (linkWidth / 2), py2, pz2).color(1f, 1f, 1f, alpha2).tex(0, v2).lightmap(packedLightIn).endVertex();
				
				// Cross quad
				buffer.pos(transform, px1, py1 - offY1, pz1 - offZ1).color(1f, 1f, 1f, alpha1).tex(0, v1).lightmap(packedLightIn).endVertex();
				buffer.pos(transform, px1, py1 + offY1, pz1 + offZ1).color(1f, 1f, 1f, alpha1).tex(1, v1).lightmap(packedLightIn).endVertex();
				buffer.pos(transform, px2, py2 + offY2, pz2 + offZ2).color(1f, 1f, 1f, alpha2).tex(1, v2).lightmap(packedLightIn).endVertex();
				buffer.pos(transform, px2, py2 - offY2, pz2 - offZ2).color(1f, 1f, 1f, alpha2).tex(0, v2).lightmap(packedLightIn).endVertex();
			}
		}
	}
	
	@Override
	public void render(LockedChestEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		
		final double ticks = tileEntityIn.getWorld().getGameTime() + partialTicks;
		final Minecraft mc = Minecraft.getInstance();
		
		matrixStackIn.push();
		matrixStackIn.translate(.5, .5, .5);
		matrixStackIn.scale(1.0f/16.0f, 1.0f/16.0f, 1.0f/16.0f); // Down to block scale, where 1 block is 16 units/pixels
		
		// Draw chain links
		this.renderChains(tileEntityIn, ticks, matrixStackIn, bufferIn, combinedLightIn);
		
		// Draw lock icon
		this.renderLock(tileEntityIn, ticks, matrixStackIn, bufferIn, combinedLightIn);

		
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
			
			matrixStackIn.scale(8, 8, 8);
			ActiveRenderInfo renderInfo = mc.gameRenderer.getActiveRenderInfo();
			float yOffset = 1.4f;
			
			if (matches) {
				final double matchWigglePeriod = 20;
				final double matchWiggleProg = 1 - ((ticks % matchWigglePeriod) / matchWigglePeriod);
				yOffset += (float) (.05 * Math.sin(2 * Math.PI * matchWiggleProg));
			}
			
			RenderFuncs.drawNameplate(matrixStackIn, bufferIn, lockStr, mc.fontRenderer, combinedLightIn, yOffset, false, renderInfo);
			//GameRenderer.drawNameplate(mc.fontRenderer, lockStr, (float)0, (float)0 + yOffset, (float)0, i, viewerYaw, viewerPitch, false);
		}

		matrixStackIn.pop();
		
	}
}
