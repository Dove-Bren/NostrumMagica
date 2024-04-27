package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.tiles.ManaArmorerTileEntity;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Vector3f;

public class TileEntityManaArmorerRenderer extends TileEntityRenderer<ManaArmorerTileEntity> {

	public TileEntityManaArmorerRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	@Override
	public void render(ManaArmorerTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		final BlockState state = NostrumBlocks.manaArmorerBlock.getDefaultState();
		
		final double ticks = tileEntityIn.getTicksExisted() + partialTicks;
		
		// Up/down bobble has period of 3 seconds
		final double vPeriod = 3 * 20;
		final double vMag = .025;
		
		final double vProg = (ticks % vPeriod) / vPeriod;
		final double vAmt = Math.sin(vProg * Math.PI * 2) * vMag;
		
		// Rotate around y axis starting by not rotating until mana starts going
		final float rProg = tileEntityIn.getRenderRotation(partialTicks) * 360f;
		
		matrixStackIn.push();
		matrixStackIn.translate(.5, 0, .5);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rProg));
		matrixStackIn.translate(-.5, 0, -.5);
		
		matrixStackIn.translate(0, vAmt, 0);
		
		RenderFuncs.RenderBlockState(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		
		matrixStackIn.pop();
	}
	
}
