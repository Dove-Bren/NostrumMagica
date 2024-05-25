package com.smanzana.nostrummagica.client.gui.widget;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class LabeledTextWidget extends FixedWidget {
	
	protected final Screen parent;
	protected final ITextComponent label;
	protected final Supplier<String> valueGetter;
	
	protected @Nullable List<ITextComponent> tooltip;
	protected int colorLabel = 0xFFAAAAAA;
	protected int colorValue = 0xFFE4E5D5;
	protected float scale = 1f;
	
	public LabeledTextWidget(Screen parent, ITextComponent label, Supplier<String> value, int x, int y, int width, int height) {
		super(x, y, width, height, StringTextComponent.EMPTY);
		this.parent = parent;
		this.label = label;
		this.valueGetter = value;
	}
	
	public LabeledTextWidget(Screen parent, String label, Supplier<String> value, int x, int y, int width, int height) {
		this(parent, new StringTextComponent(label), value, x, y, width, height);
	}
	
	public LabeledTextWidget tooltip(List<ITextComponent> tooltip) {
		this.tooltip = tooltip;
		return this;
	}
	
	public LabeledTextWidget tooltip(ITextComponent tooltip) {
		return tooltip(Lists.newArrayList(tooltip));
	}
	
	public LabeledTextWidget color(int labelColor, int valueColor) {
		this.colorLabel = labelColor;
		this.colorValue = valueColor;
		return this;
	}
	
	public LabeledTextWidget color(int valueColor) {
		return color(this.colorLabel, valueColor);
	}
	
	public LabeledTextWidget scale(float scale) {
		this.scale = scale;
		return this;
	}
	
	@Override
	public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		Minecraft mc = parent.getMinecraft();
		final FontRenderer font = mc.fontRenderer;
		
		final int labelWidth = font.getStringPropertyWidth(label);
		final String value = valueGetter.get();
		
		matrixStackIn.push();
		matrixStackIn.translate(this.x, this.y, 0);
		matrixStackIn.scale(scale, scale, 1f);
		font.func_243246_a(matrixStackIn, label, 0, 0, colorLabel);
		font.drawString(matrixStackIn, value, labelWidth, 0, colorValue);
		matrixStackIn.pop();
		
		// Recalc hover to be actual rendered text length. value can change so have to keep redoing this...
		final int actingWidth = (int) ((labelWidth + font.getStringWidth(value)) * scale);
		final int actingHeight = (int) (font.FONT_HEIGHT * scale);
		this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + actingWidth && mouseY < this.y + actingHeight;
		
		// Rely on screen we're part of to call renderToolTip
//		if (this.isHovered()) {
//			renderToolTip(matrixStackIn, mouseX, mouseY);
//		}
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
