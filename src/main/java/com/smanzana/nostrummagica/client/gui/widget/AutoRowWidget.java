package com.smanzana.nostrummagica.client.gui.widget;

import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;

public class AutoRowWidget<T extends ObscurableChildWidget> extends AutoLayoutParentWidget<T> {
	
	public AutoRowWidget(int x, int y, int width, int height, Component title) {
		super(x, y, width, height, title);
	}
	
	@Override
	protected Vec3i getChildOffset(T child, int index, int itemSpacing) {
		int absoluteX = 0;
		for (int i = 0; i < index; i++) {
			absoluteX += this.children.get(i).getWidth();
			if (absoluteX != 0) {
				absoluteX += this.itemSpacing;
			}
		}
		return new Vec3i(absoluteX, 0, 0);
	}
	
	@Override
	protected void autoSizeChild(T child, int effectiveWidth, int effectiveHeight) {
		child.setHeight(effectiveHeight);
	}
	
	protected final int getTotalChildWidth() {
		int sum = 0;
		for (T child : children) {
			if (sum != 0) {
				sum += this.itemSpacing;
			}
			sum += child.getWidth();
		}
		return sum;
	}

	@Override
	protected int getScrollPixelsTotal() {
		return Math.max(0, this.getTotalChildWidth() - this.getWidth());
	}

	@Override
	protected int getAverageChildLength() {
		return this.children.isEmpty() ? 0 : (getTotalChildWidth() / this.children.size());
	}

}
