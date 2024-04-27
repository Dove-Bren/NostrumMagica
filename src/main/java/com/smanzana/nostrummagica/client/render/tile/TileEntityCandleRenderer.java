package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.tiles.CandleTileEntity;
import com.smanzana.nostrummagica.utils.NonNullEnumMap;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

public class TileEntityCandleRenderer extends TileEntityRenderer<CandleTileEntity> {

	private NonNullEnumMap<ReagentType, ItemStack> itemCache;
	
	public TileEntityCandleRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
		
		itemCache = new NonNullEnumMap<>(ReagentType.class, ItemStack.EMPTY);
		for (ReagentType type : ReagentType.values()) {
			itemCache.put(type, ReagentItem.CreateStack(type, 1));
		}
	}
	
	@Override
	public void render(CandleTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

		final ItemStack item = itemCache.get(tileEntityIn.getReagentType());
		
		final float rot = 360f * (float) ((double)(tileEntityIn.getWorld().getGameTime() % 200) / 200.0); // Copied into ClientEffectRitual
		//float rot = 2.0f * (System.currentTimeMillis() / 50 + partialTicks);
		final float scale = .75f;
		
		matrixStackIn.push();
		matrixStackIn.translate(.5, 1.25, .5);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rot));
		matrixStackIn.scale(scale, scale, scale);
		RenderFuncs.renderItemStandard(item, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		matrixStackIn.pop();
		
	}
}
