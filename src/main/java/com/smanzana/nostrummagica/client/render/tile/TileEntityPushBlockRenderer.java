package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.autodungeons.util.ColorUtil;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.PushBlockTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityPushBlockRenderer extends BlockEntityRendererBase<PushBlockTileEntity> {

	private static final ResourceLocation TEX = NostrumMagica.Loc("textures/entity/cursed_glass.png");
	
	public TileEntityPushBlockRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}
	
	protected void translateToAnim(PushBlockTileEntity te, PoseStack matrixStackIn, float partialTicks) {
		if (te.isAnimating()) {
			final float prog = te.getAnimationProgress(partialTicks);
			if (prog < 1f) {
				final Direction direction = te.getAnimDirection();
				matrixStackIn.translate(
						direction.getStepX() * (1f-prog),
						direction.getStepY() * (1f-prog),
						direction.getStepZ() * (1f-prog)
						);
			}
		}
	}
	
	protected void renderBlock(PushBlockTileEntity te, PoseStack matrixStackIn, float partialTicks, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// this.context.getBlockRenderDispatcher().renderSingleBlock(te.getBlockState(), matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
		// Can't do this, because block is set to be invisible so it renders nothing.
		// Instead, grab model and render manually
		// RenderFuncs.RenderBlockState(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		final BlockState state = te.getBlockState();
		{
			final VertexConsumer buffer = bufferIn.getBuffer(ItemBlockRenderTypes.getRenderType(state, false));
			BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
			BakedModel ibakedmodel = dispatcher.getBlockModel(state);
			RenderFuncs.RenderModel(matrixStackIn, buffer, ibakedmodel, combinedLightIn, combinedOverlayIn, 1f, 1f, 1f, 1f);
		}
	}
	
	protected void renderDecal(PushBlockTileEntity te, PoseStack matrixStackIn, float partialTicks, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, float red, float green, float blue, float alpha) {
		matrixStackIn.pushPose();
		matrixStackIn.scale(1.01f, 1.01f, 1.01f);
		
		VertexConsumer buffer = bufferIn.getBuffer(RenderType.entityCutout(TEX));
		RenderFuncs.drawUnitCube(matrixStackIn, buffer, combinedLightIn, OverlayTexture.NO_OVERLAY, red * .6f, green * .6f, blue * .6f, alpha);
		matrixStackIn.popPose();
	}
	
	@Override
	public void render(PushBlockTileEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		
		matrixStackIn.pushPose();
		translateToAnim(tileEntityIn, matrixStackIn, partialTicks);
		
		renderBlock(tileEntityIn, matrixStackIn, partialTicks, bufferIn, combinedLightIn, combinedOverlayIn);
		
		if (tileEntityIn.getElement() != null) {
			matrixStackIn.translate(.5, .5, .5);
			final float[] colors = ColorUtil.ARGBToColor(tileEntityIn.getElement().getColor());
			renderDecal(tileEntityIn, matrixStackIn, partialTicks, bufferIn, combinedLightIn, combinedOverlayIn, colors[0], colors[1], colors[2], colors[3]);
		}
		
		matrixStackIn.popPose();
		
		
		
//		final Minecraft mc = Minecraft.getInstance();
//		final double time = (double)tileEntityIn.getLevel().getGameTime() + partialTicks;
//		
//		matrixStackIn.pushPose();
//		
//		// Render centered on bottom-center of door, not TE (in case they're different)
//		{
//			BlockPos pos = tileEntityIn.getBlockPos();
//			BoundingBox bounds = tileEntityIn.getDoorBounds();
//			Vec3 centerPos = new Vec3(
//					bounds.minX() + (float) (bounds.maxX() + 1 - bounds.minX()) / 2,
//					bounds.minY(),
//					bounds.minZ() + (float) (bounds.maxZ() + 1 - bounds.minZ()) / 2
//					);
//			matrixStackIn.translate(centerPos.x() - pos.getX(), centerPos.y() - pos.getY(), centerPos.z() - pos.getZ());
//		}
//		
//		final float rotY = tileEntityIn.getFace().getOpposite().toYRot();
//		matrixStackIn.mulPose(Vector3f.YN.rotationDegrees(rotY));
//		
//		this.renderChains(tileEntityIn, time, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
//		
//		this.renderLock(tileEntityIn, time, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
//		
//		// Draw lock info
//		if (mc.player.isCreative())
//		{
//			final String lockStr;
//			boolean matches = false;
//			if (tileEntityIn.hasWorldKey()) {
//				WorldKey key = tileEntityIn.getWorldKey();
//				lockStr = mc.player.isShiftKeyDown() ? key.toString() : key.toString().substring(0, 8);
//				
//				final ItemStack held = mc.player.getMainHandItem();
//				if ((held.getItem() instanceof WorldKeyItem && key.equals(((WorldKeyItem) held.getItem()).getKey(held)))) {
//					matches = true;
//				}
//			} else {
//				lockStr = "No lock info found";
//			}
//			
//			final double drawZ = -.5;
//			final float VANILLA_FONT_SCALE = 0.010416667f;
//			final int color = (matches)
//					? 0x50A0FFA0
//					: 0xFFFFFFFF;
//			
//			matrixStackIn.pushPose();
//			matrixStackIn.translate(0, 1, drawZ);
//			matrixStackIn.scale(-VANILLA_FONT_SCALE * 2, -VANILLA_FONT_SCALE * 2, VANILLA_FONT_SCALE * 2);
//			
//			Font fonter = context.getBlockEntityRenderDispatcher().font;
//			fonter.drawInBatch(lockStr, fonter.width(lockStr) / -2, 0, color, false, matrixStackIn.last().pose(), bufferIn, false, 0x0, combinedLightIn);
//			matrixStackIn.popPose();
//		}
//		
//		matrixStackIn.popPose();
	}
}
