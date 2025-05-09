package com.smanzana.nostrummagica.client.gui.widget;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.gui.commonwidget.LabeledWidget.IValue;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.renderer.Rect2i;

public class ComponentIconValue implements IValue {
	
	protected final Supplier<SpellComponentIcon> icon;
	protected final int width;
	protected final int height;
	
	public ComponentIconValue(Supplier<SpellComponentIcon> icon, int width, int height) {
		this.icon = icon;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public Rect2i render(PoseStack matrixStackIn, int x, int y, float partialTicks, int color, Rect2i labelArea) {
		final var icon = this.icon.get();
		if (icon != null) {
			final float[] colors = ColorUtil.ARGBToColor(color);
			icon.draw(matrixStackIn, x, y, width, height, colors[0], colors[1], colors[2], colors[3]);
		}
		
		return new Rect2i(x, y, width, height);
	}
}