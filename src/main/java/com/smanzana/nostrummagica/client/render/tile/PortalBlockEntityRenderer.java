package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.PortalBlock.NostrumPortalTileEntityBase;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class PortalBlockEntityRenderer extends BlockEntityRendererBase<NostrumPortalTileEntityBase> {

	public static final ResourceLocation TEX_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/block/portal.png");
	
	public PortalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}
	
	@Override
	public void render(NostrumPortalTileEntityBase tileEntityIn, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		
		// Want to rotate to camera but only around Y. Before th is was a atan between z and x..
		// I THINK now it's using the active render info's look vector and chopping out the Y, and then
		// doing the same thing.
		final Camera renderInfo = this.context.getBlockEntityRenderDispatcher().camera;
		Vec3 posOffset = Vec3.atCenterOf(tileEntityIn.getBlockPos()).subtract(renderInfo.getPosition());
		float rotY = (float) (Math.atan2(posOffset.z(), posOffset.x()) / (2 * Math.PI));
		rotY *= -360f;
		rotY += 180f;
		rotY += 90;
		//final float rotY = 0f;
		
		final double time = (double)tileEntityIn.getLevel().getGameTime() + partialTicks;
		final float rotAngle = (float) (((time / 20.0) % tileEntityIn.getRotationPeriod()) / tileEntityIn.getRotationPeriod());
		final float[] color = ColorUtil.ARGBToColor(tileEntityIn.getColor());
		color[3] = tileEntityIn.getOpacity();
		
		final int points = 25;
		final float horizontalRadius = .6f;
		final float verticalRadius = 1.2f;
		
		final VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.NOSTRUM_PORTAL);
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(.5, 1.2, .5);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rotY));
		RenderFuncs.drawEllipse(horizontalRadius, verticalRadius, points, rotAngle, matrixStackIn, buffer, combinedLightIn, color[0], color[1], color[2], color[3]);
		matrixStackIn.popPose();
	}
}
