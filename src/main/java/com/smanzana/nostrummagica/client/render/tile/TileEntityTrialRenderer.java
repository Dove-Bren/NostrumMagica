package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.TrialBlockTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class TileEntityTrialRenderer extends TileEntityRenderer<TrialBlockTileEntity> {

	public TileEntityTrialRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	@Override
	public void render(TrialBlockTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		
		// Get the model from the tile entity
		EMagicElement element = tileEntityIn.getElement();
		SpellComponentIcon icon = SpellComponentIcon.get(element);
		ResourceLocation textLoc = icon.getModelLocation();
		float rot = 2.0f * (float)((double) tileEntityIn.getWorld().getGameTime() / 2.5);
		float scale = tileEntityIn.getScale();
		
		// Before this was disabling lighting...
		final IVertexBuilder buffer = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(textLoc));
		
		// Recompute lighting for block above
		combinedLightIn = WorldRenderer.getCombinedLight(tileEntityIn.getWorld(), tileEntityIn.getPos().up());
		
		matrixStackIn.push();
		matrixStackIn.translate(.5, 1.25, .5);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rot));
		matrixStackIn.scale(scale, scale, scale);
		matrixStackIn.translate(0, .5, 0); // x: [-.5, .5] y: [0, 1]
		RenderFuncs.renderSpaceQuad(matrixStackIn, buffer, .5f, combinedLightIn, combinedOverlayIn, 1f, 1f, 1f, 1f);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90f));
		RenderFuncs.renderSpaceQuad(matrixStackIn, buffer, .5f, combinedLightIn, combinedOverlayIn, 1f, 1f, 1f, 1f);
		matrixStackIn.pop();
	}
}
