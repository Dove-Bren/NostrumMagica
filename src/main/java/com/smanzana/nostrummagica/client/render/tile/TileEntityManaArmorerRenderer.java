package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.tile.ManaArmorerTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import com.mojang.math.Vector3f;

public class TileEntityManaArmorerRenderer extends BlockEntityRenderer<ManaArmorerTileEntity> {

	public TileEntityManaArmorerRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	@Override
	public void render(ManaArmorerTileEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		final BlockState state = NostrumBlocks.manaArmorerBlock.defaultBlockState();
		
		final double ticks = tileEntityIn.getTicksExisted() + partialTicks;
		
		// Up/down bobble has period of 3 seconds
		final double vPeriod = 3 * 20;
		final double vMag = .025;
		
		final double vProg = (ticks % vPeriod) / vPeriod;
		final double vAmt = Math.sin(vProg * Math.PI * 2) * vMag;
		
		// Rotate around y axis starting by not rotating until mana starts going
		final float rProg = tileEntityIn.getRenderRotation(partialTicks) * 360f;
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(.5, 0, .5);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rProg));
		matrixStackIn.translate(-.5, 0, -.5);
		
		matrixStackIn.translate(0, vAmt, 0);
		
		// Can't do this, because block is set to be invisible so it renders nothing.
		// Instead, grab model and render manually
		//RenderFuncs.RenderBlockState(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		{
			final VertexConsumer buffer = bufferIn.getBuffer(ItemBlockRenderTypes.getRenderType(state, false));
			BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
			BakedModel ibakedmodel = dispatcher.getBlockModel(state);
			RenderFuncs.RenderModel(matrixStackIn, buffer, ibakedmodel, combinedLightIn, combinedOverlayIn, 1f, 1f, 1f, 1f);
		}
		
		matrixStackIn.popPose();
	}
	
}
