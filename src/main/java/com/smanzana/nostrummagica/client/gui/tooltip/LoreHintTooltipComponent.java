package com.smanzana.nostrummagica.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.inventory.tooltip.LoreHintTooltip;
import com.smanzana.nostrummagica.inventory.tooltip.LoreHintTooltip.LoreLevel;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class LoreHintTooltipComponent extends AbsoluteTooltipComponent {

	private static final ResourceLocation GUI_ICONS = new ResourceLocation(NostrumMagica.MODID, "textures/gui/icons.png");
	private static final int ICON_WIDTH = 8;
	private static final int ICON_HEIGHT = ICON_WIDTH;
	
	protected final LoreHintTooltip.LoreLevel level;
	
	public LoreHintTooltipComponent(LoreHintTooltip tooltip) {
		super();
		
		this.level = tooltip.level;
	}
	
	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getWidth(Font p_169952_) {
		return 0;
	}
	
	protected void renderBasicIcon(PoseStack matrixStackIn) {
		RenderSystem.setShaderTexture(0, GUI_ICONS);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 192, 0, 32, 32, ICON_WIDTH, ICON_HEIGHT, 256, 256);
	}
	
	protected void renderFullIcon(PoseStack matrixStackIn) {
		RenderSystem.setShaderTexture(0, GUI_ICONS);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 160, 0, 32, 32, ICON_WIDTH, ICON_HEIGHT, 256, 256);
	}
	
	@Override
	public void renderImage(Font font, int x, int y, PoseStack matrixStackIn, ItemRenderer itemRenderer, int z) {
		// I think in 1.18 this loses its textureManager param?
		matrixStackIn.pushPose();
		matrixStackIn.translate(mouseX + tooltipWidth - 4, mouseY + tooltipHeight - 6, z);
		RenderFuncs.RenderGUIItem(new ItemStack(NostrumItems.spellScroll), matrixStackIn);
		
		if (level == LoreLevel.BASIC) {
			matrixStackIn.pushPose();
			matrixStackIn.translate(8, 8, 600); // items render z+100
			renderBasicIcon(matrixStackIn);
			matrixStackIn.popPose();
		} else if (level == LoreLevel.FULL) {
			matrixStackIn.pushPose();
			matrixStackIn.translate(8, 8, 600); // items render z+100
			renderFullIcon(matrixStackIn);
			matrixStackIn.popPose();
		}
		matrixStackIn.popPose();
		
	}
}
