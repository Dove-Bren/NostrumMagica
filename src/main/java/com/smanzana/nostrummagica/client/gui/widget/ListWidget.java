package com.smanzana.nostrummagica.client.gui.widget;

import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;

public class ListWidget<T extends ObscurableChildWidget> extends AutoLayoutParentWidget<T> {

	public ListWidget(int x, int y, int width, int height, Component title) {
		super(x, y, width, height, title);
	}
	
	@Override
	protected Vec3i getChildOffset(T child, int index, int itemSpacing) {
		int absoluteY = 0;
		for (int i = 0; i < index; i++) {
			absoluteY += this.children.get(i).getHeight();
			if (absoluteY != 0) {
				absoluteY += itemSpacing;
			}
		}
		
		return new Vec3i(0, absoluteY, 0);
	}
	
	@Override
	protected void autoSizeChild(T child, int effectiveWidth, int effectiveHeight) {
		child.setWidth(effectiveWidth);
	}
	
	@Override
	protected final int getScrollPixelsTotal() {
		return Math.max(0, this.getTotalChildHeight() - this.getHeight());
	}
	
	protected final int getTotalChildHeight() {
		int sum = 0;
		for (T child : children) {
			if (sum != 0) {
				sum += itemSpacing;
			}
			sum += child.getHeight();
		}
		return sum;
	}
	
	@Override
	protected int getAverageChildLength() {
		return this.children.isEmpty() ? 0 : (this.getTotalChildHeight() / this.children.size()); 
	}

}
