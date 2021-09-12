package com.smanzana.nostrummagica.client.render;

import java.util.EnumMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.blocks.tiles.CandleTileEntity;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TileEntityCandleRenderer extends TileEntitySpecialRenderer<CandleTileEntity> {

	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(CandleTileEntity.class,
				new TileEntityCandleRenderer());
	}
	
	private Map<ReagentType, ItemStack> itemCache;
	
	public TileEntityCandleRenderer() {
		itemCache = new EnumMap<>(ReagentType.class);
		for (ReagentType type : ReagentType.values()) {
			itemCache.put(type, ReagentItem.instance().getReagent(type, 1));
		}
	}
	
	@Override
	public void render(CandleTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		ItemStack item = itemCache.get(te.getType());
		
		float rot = 2.0f * (Minecraft.getSystemTime() / 50 + partialTicks);
		float scale = .75f;
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + .5, y + 1.25, z + .5);
		GlStateManager.rotate(rot, 0, 1f, 0);
		
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		
		Minecraft.getMinecraft().getRenderItem()
			.renderItem(item, TransformType.GROUND);
		
		GlStateManager.popMatrix();
		
	}
	
}
