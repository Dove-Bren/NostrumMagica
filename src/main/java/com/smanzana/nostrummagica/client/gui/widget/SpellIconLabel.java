package com.smanzana.nostrummagica.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.client.gui.commonwidget.LabeledWidget.ILabel;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;

public class SpellIconLabel implements ILabel {
	
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