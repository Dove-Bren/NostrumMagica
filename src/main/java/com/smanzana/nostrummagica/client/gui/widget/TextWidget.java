package com.smanzana.nostrummagica.client.gui.widget;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class TextWidget extends FixedWidget {
	
	protected final Screen parent;
	protected final ITextComponent text;
	
	protected @Nullable List<ITextComponent> tooltip;
	protected int color = 0xFFDDDDDD;
	protected float scale = 1f;
	protected boolean centered = false;
	
	public TextWidget(Screen parent, ITextComponent text, int x, int y, int width, int height) {
		super(x, y, width, height, StringTextComponent.EMPTY);
		this.parent = parent;
		this.text = text;
	}
	
	public TextWidget tooltip(List<ITextComponent> tooltip) {
		this.tooltip = tooltip;
		return this;
	}
	
	public TextWidget tooltip(ITextComponent tooltip) {
		return tooltip(Lists.newArrayList(tooltip));
	}
	
	public TextWidget color(int color) {
		this.color = color;
		return this;
	}
	
	public TextWidget scale(float scale) {
		this.scale = scale;
		return this;
	}
	
	public TextWidget center() {
		this.centered = true;
		return this;
	}
	
	@Override
	public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		final Minecraft mc = this.parent.getMinecraft();
		final FontRenderer font = mc.fontRenderer;
		
		matrixStackIn.push();
		matrixStackIn.translate(this.x, this.y, 0);
		matrixStackIn.scale(scale, scale, 1f);
		final int textWidth = font.func_243245_a(this.text.func_241878_f());
		font.func_243248_b(matrixStackIn, this.text, centered ? -(textWidth/2) : 0, 0, color);
		matrixStackIn.pop();
		
		final int actingWidth = (int) (textWidth * scale);
		final int actingHeight = (int) (font.FONT_HEIGHT * scale);
		final int actingX = centered ? (this.x - (textWidth/2)) : this.x;
		this.isHovered = mouseX >= actingX && mouseY >= this.y && mouseX < actingX + actingWidth && mouseY < this.y + actingHeight;
	}
	
	@Override
	public void renderToolTip(MatrixStack matrixStackIn, int mouseX, int mouseY) {
		if (this.isHovered() && this.tooltip != null) {
			matrixStackIn.push();
			matrixStackIn.translate(0, 0, 100);
			parent.func_243308_b(matrixStackIn, tooltip, mouseX, mouseY);
			matrixStackIn.pop();
		}
	}
}
