package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.tiles.CandleTileEntity;
import com.smanzana.nostrummagica.utils.NonNullEnumMap;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;

public class TileEntityCandleRenderer extends TileEntityRenderer<CandleTileEntity> {

	private NonNullEnumMap<ReagentType, ItemStack> itemCache;
	
	public TileEntityCandleRenderer() {
		itemCache = new NonNullEnumMap<>(ReagentType.class, ItemStack.EMPTY);
		for (ReagentType type : ReagentType.values()) {
			itemCache.put(type, ReagentItem.CreateStack(type, 1));
		}
	}
	
	@Override
	public void render(CandleTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {

		ItemStack item = itemCache.get(te.getReagentType());
		
		float rot = 2.0f * (System.currentTimeMillis() / 50 + partialTicks);
		float scale = .75f;
		
		GlStateManager.pushMatrix();
		GlStateManager.translated(x + .5, y + 1.25, z + .5);
		GlStateManager.rotatef(rot, 0, 1f, 0);
		GlStateManager.scalef(scale, scale, scale);
		
		RenderFuncs.renderItemStandard(item);
		RenderHelper.disableStandardItemLighting();
		
		GlStateManager.popMatrix();
		
	}
	
}
