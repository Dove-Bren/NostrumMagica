package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.tile.AltarTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;

public class TileEntityAltarRenderer extends BlockEntityRendererBase<AltarTileEntity> {

	public TileEntityAltarRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}
	
	@Override
	public void render(AltarTileEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

		final ItemStack item = tileEntityIn.getItem();
		if (item.isEmpty() || tileEntityIn.isHidingItem())
			return;
		
		final float rot = 360f * (float) ((double)(tileEntityIn.getLevel().getGameTime() % 200) / 200.0); // Copied into ClientEffectRitual
		final float scale = .75f;
		final float yoffset = (float) (.1f * (-.5f + Math.sin(((double) System.currentTimeMillis()) / 1000.0)));
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(.5, 1.25 + yoffset, .5);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rot));
		matrixStackIn.scale(scale, scale, scale);
		
		RenderFuncs.RenderWorldItem(item, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		
		matrixStackIn.popPose();
	}
}
