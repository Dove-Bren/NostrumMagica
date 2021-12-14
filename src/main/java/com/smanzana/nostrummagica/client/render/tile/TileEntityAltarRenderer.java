package com.smanzana.nostrummagica.client.render.tile;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.blocks.tiles.AltarTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TileEntityAltarRenderer extends TileEntitySpecialRenderer<AltarTileEntity> {

	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(AltarTileEntity.class,
				new TileEntityAltarRenderer());
	}
	
	public TileEntityAltarRenderer() {
		
	}
	
	@Override
	public void render(AltarTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		ItemStack item = te.getItem();
		if (item.isEmpty() || te.isHidingItem())
			return;
		
		float rot = (float) (2.0 * ((double) Minecraft.getSystemTime() / 50.0)); // Copied into ClientEffectRitual
		float scale = .75f;
		float yoffset = (float) (.1f * (-.5f + Math.sin(((double) Minecraft.getSystemTime()) / 1000.0)));
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + .5, y + 1.25 + yoffset, z + .5);
		GlStateManager.rotate(rot, 0, 1f, 0);
		
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		
		Minecraft.getMinecraft().getRenderItem()
			.renderItem(item, TransformType.GROUND);
		
		GlStateManager.popMatrix();
		
	}
	
}
