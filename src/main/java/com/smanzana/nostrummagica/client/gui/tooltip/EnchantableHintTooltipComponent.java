package com.smanzana.nostrummagica.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.inventory.tooltip.EnchantableHintTooltip;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;

public class EnchantableHintTooltipComponent extends AbsoluteTooltipComponent {

	private static final ResourceLocation GUI_ICONS = new ResourceLocation(NostrumMagica.MODID, "textures/gui/icons.png");
	private static final int ICON_WIDTH = 12;
	private static final int ICON_HEIGHT = ICON_WIDTH;
	
	public EnchantableHintTooltipComponent(EnchantableHintTooltip tooltip) {
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
	public void renderImage(Font font, int x, int y, PoseStack matrixStackIn, ItemRenderer itemRenderer, int z) {
		// I think in 1.18 this loses its textureManager param?
		
		RenderSystem.enableBlend();
		RenderSystem.setShaderTexture(0, GUI_ICONS);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, mouseX + tooltipWidth - 2, mouseY - 10, 192, 32, 32, 32, ICON_WIDTH, ICON_HEIGHT, 256, 256);
		
	}
}
