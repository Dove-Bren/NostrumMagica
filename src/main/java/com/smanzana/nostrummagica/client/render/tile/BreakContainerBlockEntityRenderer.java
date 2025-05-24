package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.tile.BreakContainerTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.model.data.EmptyModelData;

public class BreakContainerBlockEntityRenderer extends BlockEntityRendererBase<BreakContainerTileEntity> {

	public BreakContainerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}
	
	@Override
	public void render(BreakContainerTileEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

		final float rot = 360f * (float) ((double)(tileEntityIn.getLevel().getGameTime() % 200) / 200.0); // Copied into ClientEffectRitual
		final float scale = .5f;
		final float yoffset = (float) (.025f * (Math.sin(Math.PI * 2 * (tileEntityIn.getLevel().getGameTime() % 80) / 80.0)));
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(.5, .35 + yoffset, .5);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rot));
		matrixStackIn.scale(scale, scale, scale);
		
		if (tileEntityIn.isChestMode()) {
			renderChest(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		} else {
			final ItemStack item = tileEntityIn.getFirstHeldItem();
			if (!item.isEmpty()) {
				renderItem(item, tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
			}
				
		}
		
		matrixStackIn.popPose();
	}
	
	protected void renderChest(BreakContainerTileEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// ChestRenderer is not easy to hook into and call...
		matrixStackIn.pushPose();
		matrixStackIn.translate(-.5, 0, -.5);
		this.context.getBlockRenderDispatcher().renderSingleBlock(Blocks.CHEST.defaultBlockState(), matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
		matrixStackIn.popPose();
	}
	
	protected void renderItem(ItemStack stack, BreakContainerTileEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		RenderFuncs.RenderWorldItem(stack, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
	}
}
