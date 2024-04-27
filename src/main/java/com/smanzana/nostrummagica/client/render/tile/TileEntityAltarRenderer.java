package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

public class TileEntityAltarRenderer extends TileEntityRenderer<AltarTileEntity> {

	public TileEntityAltarRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	@Override
	public void render(AltarTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

		final ItemStack item = tileEntityIn.getItem();
		if (item.isEmpty() || tileEntityIn.isHidingItem())
			return;
		
		final float rot = 360f * (float) ((double)(tileEntityIn.getWorld().getGameTime() % 200) / 200.0); // Copied into ClientEffectRitual
		final float scale = .75f;
		final float yoffset = (float) (.1f * (-.5f + Math.sin(((double) System.currentTimeMillis()) / 1000.0)));
		
		matrixStackIn.push();
		matrixStackIn.translate(0, 1.25 + yoffset, 0);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rot));
		matrixStackIn.scale(scale, scale, scale);
		
		RenderFuncs.renderItemStandard(item, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		
		matrixStackIn.pop();
	}
}
