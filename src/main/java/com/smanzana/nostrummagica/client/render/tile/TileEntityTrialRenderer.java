package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.TrialBlockTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TileEntityTrialRenderer extends BlockEntityRendererBase<TrialBlockTileEntity> {

	public TileEntityTrialRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	@Override
	public void render(TrialBlockTileEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		
		// Get the model from the tile entity
		EMagicElement element = tileEntityIn.getElement();
		SpellComponentIcon icon = SpellComponentIcon.get(element);
		ResourceLocation textLoc = icon.getModelLocation();
		float rot = 2.0f * (float)((double) tileEntityIn.getLevel().getGameTime() / 2.5);
		float scale = tileEntityIn.getScale();
		
		// Before this was disabling lighting...
		final VertexConsumer buffer = bufferIn.getBuffer(RenderType.entityCutoutNoCull(textLoc));
		
		// Recompute lighting for block above
		combinedLightIn = LevelRenderer.getLightColor(tileEntityIn.getLevel(), tileEntityIn.getBlockPos().above());
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(.5, 1.25, .5);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rot));
		matrixStackIn.scale(scale, scale, scale);
		matrixStackIn.translate(0, .5, 0); // x: [-.5, .5] y: [0, 1]
		RenderFuncs.renderSpaceQuad(matrixStackIn, buffer, .5f, combinedLightIn, combinedOverlayIn, 1f, 1f, 1f, 1f);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90f));
		RenderFuncs.renderSpaceQuad(matrixStackIn, buffer, .5f, combinedLightIn, combinedOverlayIn, 1f, 1f, 1f, 1f);
		matrixStackIn.popPose();
	}
}
