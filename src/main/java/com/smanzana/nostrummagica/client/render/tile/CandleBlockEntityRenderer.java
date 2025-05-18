package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.tile.CandleTileEntity;
import com.smanzana.nostrummagica.util.NonNullEnumMap;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;

public class CandleBlockEntityRenderer extends BlockEntityRendererBase<CandleTileEntity> {

	private NonNullEnumMap<ReagentType, ItemStack> itemCache;
	
	public CandleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		itemCache = new NonNullEnumMap<>(ReagentType.class, ItemStack.EMPTY);
		for (ReagentType type : ReagentType.values()) {
			itemCache.put(type, ReagentItem.CreateStack(type, 1));
		}
	}
	
	@Override
	public void render(CandleTileEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

		final ItemStack item = itemCache.get(tileEntityIn.getReagentType());
		
		final float rot = 360f * (float) ((double)(tileEntityIn.getLevel().getGameTime() % 200) / 200.0); // Copied into ClientEffectRitual
		//float rot = 2.0f * (System.currentTimeMillis() / 50 + partialTicks);
		final float scale = .75f;
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(.5, 1.25, .5);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rot));
		matrixStackIn.scale(scale, scale, scale);
		RenderFuncs.RenderWorldItem(item, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		matrixStackIn.popPose();
		
	}
}
