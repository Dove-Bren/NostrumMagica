package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.block.dungeon.LockedDoorBlock;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.tile.DungeonDoorTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3f;

public class TileEntityDungeonDoorRenderer extends TileEntityLockedDoorRenderer<DungeonDoorTileEntity> {

	public TileEntityDungeonDoorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	protected void renderLock(DungeonDoorTileEntity tileEntityIn, double ticks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		final MutableBoundingBox bounds = tileEntityIn.getDoorBounds();
		final float yDiff = bounds.maxY + 1 - bounds.minY;
		
		final float width = .75f;
		final float height = .75f;
		
		final float glow;
		if (tileEntityIn.getBlockState().get(LockedDoorBlock.UNLOCKABLE)) {
			final double glowPeriod = 20;
			final double glowProg = ((ticks % glowPeriod) / glowPeriod);
			glow = .15f * (float) Math.sin(glowProg * 2 * Math.PI);
		} else {
			final double glowPeriod = 60;
			final double glowProg = ((ticks % glowPeriod) / glowPeriod); 
			glow = .5f + (.15f * (float) Math.sin(glowProg * 2 * Math.PI));
		}
		
		final int colorRGB = tileEntityIn.getColor().getColorValue();
		final float red = ((float) ((colorRGB >> 16) & 0xFF) / 255f);
		final float green = ((float) ((colorRGB >> 8) & 0xFF) / 255f);
		final float blue = ((float) ((colorRGB >> 0) & 0xFF) / 255f);
		
		IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.LOCKEDCHEST_LOCK);
		
		matrixStackIn.push();
		matrixStackIn.translate(0, yDiff/2, -.3f);
		matrixStackIn.translate(-width/2, -height/2, 0); // center
		
		// Wiggle a bit
		matrixStackIn.scale(width, height, 1f);
		RenderFuncs.drawScaledCustomSizeModalRect(matrixStackIn, buffer, 0, 0, 0, 0, 16, 16, 1, 1, 16, 16, red, green, blue, .25f + glow);
		
		final ResourceLocation keyIcon = tileEntityIn.isLarge() ? TileEntityDungeonKeyChestRenderer.ICON_SILVER_KEY : TileEntityDungeonKeyChestRenderer.ICON_COPPER_KEY;
		buffer = bufferIn.getBuffer(RenderType.getEntityCutout(keyIcon));
		matrixStackIn.translate(.75f, .75f, -.0005f);
		matrixStackIn.scale(.5f, .5f, 1f);
		matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180f));
		RenderFuncs.drawScaledCustomSizeModalRect(matrixStackIn, buffer, 0, 0, 0, 0, 16, 16, 1, 1, 16, 16, 1f, 1f, 1f, .25f + glow);
		//RenderFuncs.drawUnitCube(matrixStackIn, buffer, combinedLightIn, combinedOverlayIn, .6f, .6f, .6f, .25f + glow);
		
		matrixStackIn.pop();
	}
}
