package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;
import com.smanzana.nostrummagica.tile.SymbolTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class TileEntitySymbolRenderer extends TileEntityRenderer<SymbolTileEntity> {

	public TileEntitySymbolRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	@Override
	public void render(SymbolTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		
		// Get the model from the tile entity
		SpellComponentWrapper comp = tileEntityIn.getComponent();
		SpellComponentIcon icon;
		if (comp.isShape())
			icon = SpellComponentIcon.get(comp.getShape());
		else if (comp.isAlteration())
			icon = SpellComponentIcon.get(comp.getAlteration());
		else
			icon = SpellComponentIcon.get(comp.getElement());
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
