package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumPortal.NostrumPortalTileEntityBase;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class TileEntityPortalRenderer extends TileEntityRenderer<NostrumPortalTileEntityBase> {

	public static final ResourceLocation TEX_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/block/portal.png");
	
	public TileEntityPortalRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	@Override
	public void render(NostrumPortalTileEntityBase tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		
		// Want to rotate to camera but only around Y. Before th is was a atan between z and x..
		// I THINK now it's using the active render info's look vector and chopping out the Y, and then
		// doing the same thing.
		final ActiveRenderInfo renderInfo = this.renderDispatcher.renderInfo;
		Vector3d posOffset = Vector3d.copyCentered(tileEntityIn.getPos()).subtract(renderInfo.getProjectedView());
		float rotY = (float) (Math.atan2(posOffset.getZ(), posOffset.getX()) / (2 * Math.PI));
		rotY *= -360f;
		rotY += 180f;
		rotY += 90;
		//final float rotY = 0f;
		
		final double time = (double)tileEntityIn.getWorld().getGameTime() + partialTicks;
		final float rotAngle = (float) (((time / 20.0) % tileEntityIn.getRotationPeriod()) / tileEntityIn.getRotationPeriod());
		final float[] color = ColorUtil.ARGBToColor(tileEntityIn.getColor());
		color[3] = tileEntityIn.getOpacity();
		
		final int points = 25;
		final float horizontalRadius = .6f;
		final float verticalRadius = 1.2f;
		
		final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.NOSTRUM_PORTAL);
		
		matrixStackIn.push();
		matrixStackIn.translate(.5, 1.2, .5);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rotY));
		RenderFuncs.drawEllipse(horizontalRadius, verticalRadius, points, rotAngle, matrixStackIn, buffer, combinedLightIn, color[0], color[1], color[2], color[3]);
		matrixStackIn.pop();
	}
}
