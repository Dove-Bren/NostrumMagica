package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TileEntityAltarRenderer extends TileEntityRenderer<AltarTileEntity> {

	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(AltarTileEntity.class,
				new TileEntityAltarRenderer());
	}
	
	public TileEntityAltarRenderer() {
		
	}
	
	@Override
	public void render(AltarTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {

		ItemStack item = te.getItem();
		if (item.isEmpty() || te.isHidingItem())
			return;
		
		float rot = (float) (2.0 * ((double) System.currentTimeMillis() / 50.0)); // Copied into ClientEffectRitual
		float scale = .75f;
		float yoffset = (float) (.1f * (-.5f + Math.sin(((double) System.currentTimeMillis()) / 1000.0)));
		
		GlStateManager.pushMatrix();
		GlStateManager.translated(x + .5, y + 1.25 + yoffset, z + .5);
		GlStateManager.rotatef(rot, 0, 1f, 0);
		GlStateManager.scalef(scale, scale, scale);
		
//		GlStateManager.enableBlend();
//		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		GlStateManager.disableLighting();
//		GlStateManager.enableAlphaTest();
//		GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
//		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
//		
//		Minecraft.getInstance().getItemRenderer()
//			.ItemRenderer(item, TransformType.GROUND);
		
		RenderFuncs.renderItemStandard(item);
		RenderHelper.disableStandardItemLighting();
		
		GlStateManager.popMatrix();
		
	}
	
}
