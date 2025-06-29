package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.autodungeons.client.render.BlueprintRenderer;
import com.smanzana.autodungeons.world.blueprints.Blueprint;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.TemplateStamperBlockEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TemplateStamperBlockEntityRenderer extends BlockEntityRendererBase<TemplateStamperBlockEntity> {

	public TemplateStamperBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}
	
	@Override
	public void render(TemplateStamperBlockEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		
		final Player player = NostrumMagica.Proxy.getPlayer();
		
		if (player == null || !player.isCreative()) {
				return;
		}
		
		final BlockPos pos = tileEntityIn.getBlockPos();
		final double distSqr = player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		matrixStackIn.pushPose();
		
		this.renderBlock(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, 1f);
		if (tileEntityIn.getBlueprint() != null && distSqr < 36) {
			this.renderTemplate(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, 1f);
			this.renderBounds(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, 1f);
		}
		
		matrixStackIn.popPose();
	}
	
	protected void renderBlock(TemplateStamperBlockEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, float alpha) {
		final BlockState state = tileEntityIn.getBlockState();
		final VertexConsumer buffer = bufferIn.getBuffer(ItemBlockRenderTypes.getRenderType(state, false));
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		BakedModel ibakedmodel = dispatcher.getBlockModel(state);
		RenderFuncs.RenderModel(matrixStackIn, buffer, ibakedmodel, combinedLightIn, combinedOverlayIn, 1f, 1f, 1f, 1f);
	}
	
	protected void renderBounds(TemplateStamperBlockEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, float alpha) {
		final BlueprintLocation offset = tileEntityIn.getOffset();
		final BlockPos pos = offset.getPos();
		final Blueprint blueprint = tileEntityIn.getBlueprint();
		final BlockPos dims = blueprint.getAdjustedDimensions(offset.getFacing());
		
		// I think I need to get the adjusted offset and offset from there...
		final BlockPos bpOffset = blueprint.getAdjustedOffset(offset.getFacing()); // how many
		// bounds are -bpOffset to (dims-bpOffset) I think?
		// range is -bpOffset + ((dims-bpOffset) / 2)
		final BlockPos adjustedCenter = new BlockPos(
					((-bpOffset.getX() + (dims.getX()-1)) / 2),
					((-bpOffset.getY() + (dims.getY()-1)) / 2),
					((-bpOffset.getZ() + (dims.getZ()-1)) / 2)
				);
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		RenderFuncs.drawUnitCubeOutline(matrixStackIn, bufferIn.getBuffer(RenderType.lines()), combinedLightIn, combinedOverlayIn, .4f, 1f, 1f, 1f);
		matrixStackIn.translate(adjustedCenter.getX(), adjustedCenter.getY(), adjustedCenter.getZ());
		matrixStackIn.popPose();
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(pos.getX(), pos.getY(), pos.getZ());
		matrixStackIn.translate(-bpOffset.getX(), -bpOffset.getY(), -bpOffset.getZ());
		matrixStackIn.translate(dims.getX()/2.0, dims.getY()/2.0, dims.getZ()/2.0);
		matrixStackIn.scale(dims.getX(), dims.getY(), dims.getZ());
		RenderFuncs.drawUnitCubeOutline(matrixStackIn, bufferIn.getBuffer(RenderType.lines()), combinedLightIn, combinedOverlayIn, 1f, 1f, 1f, 1f);
		
		matrixStackIn.popPose();
	}
	
	protected void renderTemplate(TemplateStamperBlockEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, float alpha) {
		final Blueprint blueprint = tileEntityIn.getBlueprint();
		final BlueprintLocation loc = tileEntityIn.getOffset();
		
		matrixStackIn.pushPose();
		BlueprintRenderer.RenderBlueprintPreview(matrixStackIn, bufferIn, Vec3.atLowerCornerOf(loc.getPos()), blueprint.getPreview(), loc.getFacing());
		matrixStackIn.popPose();
	}
}
