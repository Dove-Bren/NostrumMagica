package com.smanzana.nostrummagica.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.inventory.tooltip.ConfigurableHintTooltip;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class ConfigurableHintTooltipComponent extends AbsoluteTooltipComponent {

	private static final ResourceLocation GUI_ICONS = new ResourceLocation(NostrumMagica.MODID, "textures/gui/icons.png");
	private static final int ICON_WIDTH = 8;
	private static final int ICON_HEIGHT = ICON_WIDTH;
	
	public ConfigurableHintTooltipComponent(ConfigurableHintTooltip tooltip) {
		super();
	}
	
	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getWidth(Font p_169952_) {
		return 0;
	}
	
	@Override
	public void renderImage(Font font, int x, int y, PoseStack matrixStackIn, ItemRenderer itemRenderer, int z, TextureManager textureManager) {
		// I think in 1.18 this loses its textureManager param?
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, 0, z + 500);
		
		RenderSystem.enableBlend();
		RenderSystem.setShaderTexture(0, GUI_ICONS);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, mouseX - 7, mouseY + tooltipHeight, 160, 32, 32, 32, ICON_WIDTH, ICON_HEIGHT, 256, 256);
		matrixStackIn.popPose();
		
	}
}
