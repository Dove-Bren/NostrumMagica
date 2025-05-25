package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.client.render.BeamRenderer;
import com.smanzana.nostrummagica.tile.LaserBlockEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class LaserBlockEntityRenderer extends BlockEntityRendererBase<LaserBlockEntity> {

	public LaserBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}
	
	@Override
	public void render(LaserBlockEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		
		final float length = tileEntityIn.getLaserAnimLength(partialTicks);
		if (length <= 0f) {
			return;
		}
		
		final Direction direction = tileEntityIn.getDirection();
		final Vector3f step = direction.step();
		final Vec3 end = new Vec3(step).scale(length);
		//final Vec3 fadeEnd = end.add(new Vec3(direction.step()));
		final int color = tileEntityIn.getElement().getColor();
		
		//RenderSystem.disableCull();
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(.5, .5, .5); // center of TE
		matrixStackIn.translate(-step.x() * .5f, -step.y() * .5f, -step.z() * .5f); // far edge of block
		
		BeamRenderer.renderToBuffer(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, RenderFuncs.ARGBFade(color, .6f), Vec3.ZERO, end, .35f, tileEntityIn.getLevel().getGameTime() + partialTicks);
		//BeamRenderer.renderToBuffer(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, RenderFuncs.ARGBFade(color, .2f), end, fadeEnd, .35f, tileEntityIn.getLevel().getGameTime() + partialTicks);
		
		//RenderSystem.enableCull();
		
		matrixStackIn.popPose();
	}
	
	@Override
	public boolean shouldRenderOffScreen(LaserBlockEntity entity) {
		return true;
	}
	
}
