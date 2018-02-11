package com.smanzana.nostrummagica.client.render;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.blocks.AltarBlock.AltarTileEntity;

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
	public void renderTileEntityAt(AltarTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {

		ItemStack item = te.getItem();
		if (item == null)
			return;
		
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
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		
		Minecraft.getMinecraft().getRenderItem()
			.renderItem(item, TransformType.GROUND);
		
		GlStateManager.popMatrix();
		
	}
	
}
