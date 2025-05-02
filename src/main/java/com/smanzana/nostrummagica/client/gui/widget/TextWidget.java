package com.smanzana.nostrummagica.client.gui.widget;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;

public class TextWidget extends ObscurableChildWidget {
	
	protected final Screen parent;
	protected final Component text;
	
	protected @Nullable ITooltip tooltip;
	protected int color = 0xFFDDDDDD;
	protected float scale = 1f;
	protected boolean centerHorizontal = false;
	protected boolean centerVertical = false;
	protected boolean centerInBounds = false;
	protected boolean truncate = false;
	
	public TextWidget(Screen parent, Component text, int x, int y, int width, int height) {
		super(x, y, width, height, TextComponent.EMPTY);
		this.parent = parent;
		this.text = text;
	}
	
	public TextWidget tooltip(ITooltip tooltip) {
		this.tooltip = tooltip;
		return this;
	}
	
	public TextWidget color(int color) {
		this.color = color;
		return this;
	}
	
	public TextWidget scale(float scale) {
		this.scale = scale;
		return this;
	}
	
	public TextWidget centerHorizontal() {
		this.centerHorizontal = true;
		return this;
	}
	
	public TextWidget centerVertical() {
		this.centerVertical = true;
		return this;
	}
	
	/**
	 * When turned out, centerHorizontal/vertical center in this widget's width/height
	 * instead of at its x/y
	 * @return
	 */
	public TextWidget centerInBounds() {
		this.centerInBounds = true;
		return this;
	}
	
	public TextWidget truncate() {
		this.truncate = true;
		return this;
	}
	
	private int getOffsetX(float scale, int textWidth) {
		if (this.centerHorizontal) {
			if (this.centerInBounds) {
				return Math.round(((this.width/scale) - textWidth) / 2f);
			} else {
				return -textWidth / 2;
			}
		}
		return 0;
	}
	
	private int getOffsetY(float scale, int textHeight) {
		if (this.centerVertical) {
			if (this.centerInBounds) {
				return Math.round(((this.height/scale) - textHeight) / 2f);
			} else {
				return -textHeight / 2;
			}
		}
		return 0;
	}
	
	@Override
	public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		final Minecraft mc = this.parent.getMinecraft();
		final Font font = mc.font;
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(this.x, this.y, 0);
		matrixStackIn.scale(scale, scale, 1f);
		final FormattedCharSequence textToDraw;
		if (truncate) {
			textToDraw = Language.getInstance().getVisualOrder(font.substrByWidth(this.text, (int) (this.width / scale)));
		} else {
			textToDraw = this.text.getVisualOrderText();
		}
		final int textWidth = font.width(textToDraw);
		final int offsetX = this.getOffsetX(scale, textWidth);
		final int offsetY = this.getOffsetY(scale, font.lineHeight);
		font.draw(matrixStackIn, textToDraw, offsetX, offsetY, color);
		matrixStackIn.popPose();
		
		final int actingWidth = (int) (textWidth * scale);
		final int actingHeight = (int) (font.lineHeight * scale);
		final int actingX = x + offsetX;
		final int actingY = y + offsetY;
		this.isHovered = mouseX >= actingX && mouseY >= actingY && mouseX < actingX + actingWidth && mouseY < actingY + actingHeight;
	}
	
	@Override
	public void renderToolTip(PoseStack matrixStackIn, int mouseX, int mouseY) {
		if (this.isHoveredOrFocused() && this.tooltip != null) {
			Tooltip.RenderTooltip(this.tooltip, parent, matrixStackIn, mouseX, mouseY);
		}
	}
	
	@Override
	protected boolean isValidClickButton(int button) {
		return false; // no click consumption
	}
}
