package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.tiles.NostrumObeliskEntity;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Vector3f;

public class TileEntityObeliskRenderer extends TileEntityRenderer<NostrumObeliskEntity> {

	public TileEntityObeliskRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	@Override
	public void render(NostrumObeliskEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

		if (tileEntityIn.isMaster())
			return;
		
		final BlockState state = tileEntityIn.getBlockState();
		final long time = System.currentTimeMillis();
		float rotY = (float) (time % 3000) / 3000f;
		float rotX = (float) (time % 5000) / 5000f;
		
		
		rotY *= 360f;
		rotX *= 360f;
		
		matrixStackIn.push();
		matrixStackIn.translate(.5, .5, .5);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rotY));
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(rotX));
		RenderFuncs.RenderBlockState(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn); // Used to fetch custom model and render itself
		matrixStackIn.pop();
	}
}
