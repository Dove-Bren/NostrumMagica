package com.smanzana.nostrummagica.client.gui.commonwidget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ChildButtonWidget<T extends ChildButtonWidget<T>> extends ObscurableChildWidget<T> {
	
	// Mirror of Button.OnPress
	public static interface OnPress {
		 void onPress(ChildButtonWidget<?> button);
	}
	
	protected final Screen parent;
	
	protected final OnPress onPress;
	
	public ChildButtonWidget(Screen parent, int x, int y, int width, int height, Component label, OnPress onPress) {
		super(x, y, width, height, label);
		this.parent = parent;
		this.onPress = onPress;
	}
	
	protected void renderButtonIcon(PoseStack matrixStackIn, int iconX, int iconY, int iconWidth, int iconHeight, float partialTicks) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
		blit(matrixStackIn, iconX, iconY, iconWidth, iconHeight, 2, 68, (width - 2), Math.min(height - 2, 14), 256, 256);
	}
	
	@Override
	public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		//super.renderButton(matrixStackIn, mouseX, mouseY, partialTicks);
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		final float sat = this.active ? 1f : .3f;
		RenderSystem.setShaderColor(sat, sat, sat, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		{
//			this.blit(matrixStackIn, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
//			this.blit(matrixStackIn, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
			// gonna just render outside border, and then grainy texture. Will scale if button is too large?
			fill(matrixStackIn, x, y, x + width, y + height, this.active && this.isHoveredOrFocused() ? 0xFFCCCCCC : 0xFF000000);
			
			renderButtonIcon(matrixStackIn, x + 1, y + 1, width - 2, height - 2, partialTicks);
			
			// highlights
			fill(matrixStackIn, x + 1, y + 1, x + (width - 1), y + 2, 0x40FFFFFF);
			fill(matrixStackIn, x + 1, y + 1, x + 2, y + (height - 1), 0x40FFFFFF);
			fill(matrixStackIn, x + 1, y + (height - 2), x + (width - 1), y + (height - 1), 0x40000000);
			fill(matrixStackIn, x + (width - 2), y + 1, x + (width - 1), y + (height - 1), 0x40000000);
			
		}
		this.renderBg(matrixStackIn, minecraft, mouseX, mouseY);
		int j = getFGColor();
		
		{
			// possibly scale down if button is small
			matrixStackIn.pushPose();
			matrixStackIn.translate(this.x + this.width / 2, this.y + this.height / 2, 0);
			if (font.lineHeight > this.height - 4) {
				final float scale = (float)(this.height - 4) / (float) (font.lineHeight);
				matrixStackIn.scale(scale, scale, 1f);
			}
			drawCenteredString(matrixStackIn, font, this.getMessage(), 0, -(font.lineHeight / 2), j | Mth.ceil(this.alpha * 255.0F) << 24);
			matrixStackIn.popPose();
		}
	}
	
	@Override
	protected boolean isValidClickButton(int button) {
		return super.isValidClickButton(button); // no click consumption
	}
	
	@Override
	public void onClick(double mouseX, double mouseY) {
		this.onPress.onPress(this);
	}
}
