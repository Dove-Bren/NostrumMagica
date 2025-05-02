package com.smanzana.nostrummagica.client.gui.widget;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class LabeledWidget extends ObscurableChildWidget {
	
	public static interface ILabel {
		public Rect2i render(PoseStack matrixStackIn, int x, int y, float partialTicks, int color);
	}
	
	public static interface IValue {
		public Rect2i render(PoseStack matrixStackIn, int x, int y, float partialTicks, int color, Rect2i labelArea);
	}
	
	protected final Screen parent;
	protected final ILabel label;
	protected final IValue value;
	
	protected @Nullable Supplier<List<Component>> tooltip;
	protected int colorLabel = 0xFFAAAAAA;
	protected int colorValue = 0xFFE4E5D5;
	protected float scale = 1f;
	
	public LabeledWidget(Screen parent, ILabel label, IValue value, int x, int y, int width, int height) {
		super(x, y, width, height, TextComponent.EMPTY);
		this.parent = parent;
		this.label = label;
		this.value = value;
	}
	
	public LabeledWidget tooltip(List<Component> tooltip) {
		this.tooltip = () -> tooltip;
		return this;
	}
	
	public LabeledWidget tooltip(Component tooltip) {
		return tooltip(Lists.newArrayList(tooltip));
	}
	
	public LabeledWidget tooltip(Supplier<List<Component>> tooltip) {
		this.tooltip = tooltip;
		return this;
	}
	
	public LabeledWidget tooltip(Supplier<Component> tooltip, int dummy) {
		return tooltip(() -> Lists.newArrayList(tooltip.get()));
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
	
	protected Rect2i renderLabel(PoseStack matrixStackIn, int x, int y, float partialTicks) {
		return this.label.render(matrixStackIn, x, y, partialTicks, this.colorLabel);
	}
	
	protected Rect2i renderValue(PoseStack matrixStackIn, int x, int y, float partialTicks, Rect2i labelArea) {
		return this.value.render(matrixStackIn, x, y, partialTicks, this.colorValue, labelArea);
	}
	
	@Override
	public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		matrixStackIn.pushPose();
		matrixStackIn.translate(this.x, this.y, 0);
		matrixStackIn.scale(scale, scale, 1f);
		final Rect2i labelArea = renderLabel(matrixStackIn, 0, 0, partialTicks);
		final Rect2i valueArea = renderValue(matrixStackIn, labelArea.getWidth(), 0, partialTicks, labelArea);
		matrixStackIn.popPose();
		
		// Recalc hover to be actual rendered text length. value can change so have to keep redoing this...
		final int actingWidth = (int) ((valueArea.getX() + valueArea.getWidth()) * scale); // expect value to return a rect offset by label starting x etc.
		final int actingHeight = (int) (Math.max(labelArea.getY() + labelArea.getHeight(), valueArea.getY() + valueArea.getHeight()) * scale);
		this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + actingWidth && mouseY < this.y + actingHeight;
	}
	
	@Override
	public void renderToolTip(PoseStack matrixStackIn, int mouseX, int mouseY) {
		if (this.isHoveredOrFocused() && this.tooltip != null) {
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, 0, 100);
			parent.renderComponentTooltip(matrixStackIn, tooltip.get(), mouseX, mouseY);
			matrixStackIn.popPose();
		}
	}
	
	@Override
	protected boolean isValidClickButton(int button) {
		return false; // no click consumption
	}
	
	public static class TextLabel implements ILabel {
		
		private final Component label;
		
		public TextLabel(Component label) {
			this.label = label;
		}
		
		protected Component getLabel() {
			return label;
		}
		
		@Override
		public Rect2i render(PoseStack matrixStackIn, int x, int y, float partialTicks, int color) {
			final Component label = getLabel();
			
			final Minecraft mc = Minecraft.getInstance();
			final Font font = mc.font;
			final int len = font.width(label);
			
			font.drawShadow(matrixStackIn, label, 0, 0, color);
			
			return new Rect2i(x, y, len, font.lineHeight);
		}
	}
	
	public static class StringLabel extends TextLabel {
		public StringLabel(String label) {
			super(new TextComponent(label));
		}
	}
	
	public static class SpellIconLabel implements ILabel {
		
		protected final SpellIcon icon;
		protected final int width;
		protected final int height;
		
		public SpellIconLabel(SpellIcon icon, int width, int height) {
			this.icon = icon;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public Rect2i render(PoseStack matrixStackIn, int x, int y, float partialTicks, int color) {
			final float[] colors = ColorUtil.ARGBToColor(color);
			this.icon.render(Minecraft.getInstance(), matrixStackIn, x, y, width, height, colors[0], colors[1], colors[2], colors[3]);
			return new Rect2i(x, y, width, height);
		}
	}
	
	public static class ComponentIconLabel implements ILabel {
		
		protected final SpellComponentIcon icon;
		protected final int width;
		protected final int height;
		
		public ComponentIconLabel(SpellComponentIcon icon, int width, int height) {
			this.icon = icon;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public Rect2i render(PoseStack matrixStackIn, int x, int y, float partialTicks, int color) {
			final float[] colors = ColorUtil.ARGBToColor(color);
			this.icon.draw(matrixStackIn, x, y, width, height, colors[0], colors[1], colors[2], colors[3]);
			return new Rect2i(x, y, width, height);
		}
	}
	
	public static class TextValue implements IValue {
		
		private final Supplier<String> valueGetter;
		
		public TextValue(Supplier<String> value) {
			this.valueGetter = value;
		}
		
		@Override
		public Rect2i render(PoseStack matrixStackIn, int x, int y, float partialTicks, int color, Rect2i labelArea) {
			final Minecraft mc = Minecraft.getInstance();
			final Font font = mc.font;
			final String value = valueGetter.get();
			final int len = font.width(value);
			
			// Try to center vertically with the label
			final int labelCenter = (labelArea.getY() + (labelArea.getHeight() / 2));
			final int yAdjust = labelCenter - (font.lineHeight / 2);
			
			font.draw(matrixStackIn, value, x, yAdjust, color);
			
			return new Rect2i(x, yAdjust, len, font.lineHeight);
		}
	}
}
