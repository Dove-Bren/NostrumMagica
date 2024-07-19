package com.smanzana.nostrummagica.client.gui.widget;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class LabeledWidget extends FixedWidget {
	
	public static interface ILabel {
		public Rectangle2d render(MatrixStack matrixStackIn, int x, int y, float partialTicks, int color);
	}
	
	public static interface IValue {
		public Rectangle2d render(MatrixStack matrixStackIn, int x, int y, float partialTicks, int color);
	}
	
	protected final Screen parent;
	protected final ILabel label;
	protected final IValue value;
	
	protected @Nullable List<ITextComponent> tooltip;
	protected int colorLabel = 0xFFAAAAAA;
	protected int colorValue = 0xFFE4E5D5;
	protected float scale = 1f;
	
	public LabeledWidget(Screen parent, ILabel label, IValue value, int x, int y, int width, int height) {
		super(x, y, width, height, StringTextComponent.EMPTY);
		this.parent = parent;
		this.label = label;
		this.value = value;
	}
	
	public LabeledWidget tooltip(List<ITextComponent> tooltip) {
		this.tooltip = tooltip;
		return this;
	}
	
	public LabeledWidget tooltip(ITextComponent tooltip) {
		return tooltip(Lists.newArrayList(tooltip));
	}
	
	public LabeledWidget color(int labelColor, int valueColor) {
		this.colorLabel = labelColor;
		this.colorValue = valueColor;
		return this;
	}
	
	public LabeledWidget color(int valueColor) {
		return color(this.colorLabel, valueColor);
	}
	
	public LabeledWidget scale(float scale) {
		this.scale = scale;
		return this;
	}
	
	protected Rectangle2d renderLabel(MatrixStack matrixStackIn, int x, int y, float partialTicks) {
		return this.label.render(matrixStackIn, x, y, partialTicks, this.colorLabel);
	}
	
	protected Rectangle2d renderValue(MatrixStack matrixStackIn, int x, int y, float partialTicks) {
		return this.value.render(matrixStackIn, x, y, partialTicks, this.colorValue);
	}
	
	@Override
	public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		matrixStackIn.push();
		matrixStackIn.translate(this.x, this.y, 0);
		matrixStackIn.scale(scale, scale, 1f);
		final Rectangle2d labelArea = renderLabel(matrixStackIn, 0, 0, partialTicks);
		final Rectangle2d valueArea = renderValue(matrixStackIn, labelArea.getWidth(), 0, partialTicks);
		matrixStackIn.pop();
		
		// Recalc hover to be actual rendered text length. value can change so have to keep redoing this...
		final int actingWidth = (int) ((valueArea.getX() + valueArea.getWidth()) * scale); // expect value to return a rect offset by label starting x etc.
		final int actingHeight = (int) (Math.max(labelArea.getY() + labelArea.getHeight(), valueArea.getY() + valueArea.getHeight()) * scale);
		this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + actingWidth && mouseY < this.y + actingHeight;
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
	
	public static class TextLabel implements ILabel {
		
		private final ITextComponent label;
		
		public TextLabel(ITextComponent label) {
			this.label = label;
		}
		
		@Override
		public Rectangle2d render(MatrixStack matrixStackIn, int x, int y, float partialTicks, int color) {
			final Minecraft mc = Minecraft.getInstance();
			final FontRenderer font = mc.fontRenderer;
			final int len = font.getStringPropertyWidth(label);
			
			font.func_243246_a(matrixStackIn, label, 0, 0, color);
			
			return new Rectangle2d(x, y, len, font.FONT_HEIGHT);
		}
	}
	
	public static class StringLabel extends TextLabel {
		public StringLabel(String label) {
			super(new StringTextComponent(label));
		}
	}
	
	public static class SpellIconLabel implements ILabel {
		
		private final SpellIcon icon;
		private final int width;
		private final int height;
		
		public SpellIconLabel(SpellIcon icon, int width, int height) {
			this.icon = icon;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public Rectangle2d render(MatrixStack matrixStackIn, int x, int y, float partialTicks, int color) {
			final float[] colors = ColorUtil.ARGBToColor(color);
			this.icon.render(Minecraft.getInstance(), matrixStackIn, x, y, width, height, colors[0], colors[1], colors[2], colors[3]);
			return new Rectangle2d(x, y, width, height);
		}
	}
	
	public static class ComponentIconLabel implements ILabel {
		
		private final SpellComponentIcon icon;
		private final int width;
		private final int height;
		
		public ComponentIconLabel(SpellComponentIcon icon, int width, int height) {
			this.icon = icon;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public Rectangle2d render(MatrixStack matrixStackIn, int x, int y, float partialTicks, int color) {
			final float[] colors = ColorUtil.ARGBToColor(color);
			this.icon.draw(matrixStackIn, x, y, width, height, colors[0], colors[1], colors[2], colors[3]);
			return new Rectangle2d(x, y, width, height);
		}
	}
	
	public static class TextValue implements IValue {
		
		private final Supplier<String> valueGetter;
		
		public TextValue(Supplier<String> value) {
			this.valueGetter = value;
		}
		
		@Override
		public Rectangle2d render(MatrixStack matrixStackIn, int x, int y, float partialTicks, int color) {
			final Minecraft mc = Minecraft.getInstance();
			final FontRenderer font = mc.fontRenderer;
			final String value = valueGetter.get();
			final int len = font.getStringWidth(value);
			
			font.drawString(matrixStackIn, value, x, y, color);
			
			return new Rectangle2d(x, y, len, font.FONT_HEIGHT);
		}
	}
}
