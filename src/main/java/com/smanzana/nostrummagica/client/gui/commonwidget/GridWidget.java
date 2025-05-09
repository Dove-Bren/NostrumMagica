package com.smanzana.nostrummagica.client.gui.commonwidget;

import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;

public class GridWidget<T extends ObscurableChildWidget<?>> extends AutoLayoutParentWidget<GridWidget<T>, T> {

	public GridWidget(int x, int y, int width, int height, Component title) {
		super(x, y, width, height, title);
	}
	
	@Override
	public AutoLayoutParentWidget<GridWidget<T>, T> setAutoSizeChildren() {
		throw new UnsupportedOperationException("Can't auto resize grid children");
	}
	
	@Override
	protected Vec3i getChildOffset(T childIgnored, int index, int itemSpacing) {
		int rowX = 0;
		int rowY = 0;
		int rowHeight = 0;
		
		// Iterate all children up to the specified index, with each being after eachother horizontally
		// until they don't fit, and then continued in the next row and so on
		
		for (int i = 0; i < index; i++) {
			final T child = children.get(i);
			final int itemWidth = child.getWidth();
			final int itemHeight = child.getHeight();
			if (rowX + itemWidth > this.width) {
				// spill over. Either move this one to next, or if it's the only thing put it anyways
				if (rowX == 0) {
					; // simulate 'placing' it by not adjusting current rowX or rowY
				} else {
					// Reset row count and repeat this index to evaluate this item in next row
					rowX = 0;
					rowY += rowHeight + itemSpacing;
					rowHeight = 0;
				}
			} else {
				;
			}
			
			rowX += itemWidth + itemSpacing;
			rowHeight = Math.max(rowHeight, itemHeight);
		}
		
		if (childIgnored != null && rowX + childIgnored.getWidth() > this.width) {
			// spill over. Either move this one to next, or if it's the only thing put it anyways
			if (rowX == 0) {
				; // simulate 'placing' it by not adjusting current rowX or rowY
			} else {
				// Reset row count and repeat this index to evaluate this item in next row
				rowX = 0;
				rowY += rowHeight + itemSpacing;
				rowHeight = 0;
			}
		}
		
		return new Vec3i(rowX, rowY, 0);
	}
	
	@Override
	protected void autoSizeChild(T child, int effectiveWidth, int effectiveHeight) {
		;
	}
	
	@Override
	protected final int getScrollPixelsTotal() {
		return Math.max(0, this.getTotalChildHeight() - this.getHeight());
	}
	
	protected final int getTotalChildHeight() {
		if (children.isEmpty()) {
			return 0;
		}
		// Get last item's offset, and then use that + its height
		// Can be wrong if an item in its row is larger height than our last item's height
		Vec3i lastOffset = getChildOffset(null, this.children.size() - 1, this.itemSpacing);
		return lastOffset.getY() + this.children.get(this.children.size() - 1).getHeight()
				+ (this.itemMargin * 2);
	}
	
	@Override
	protected int getAverageChildLength() {
		// this is used to calculate vertical scroll intervals, so give it average row height
		// .... which is? Could actually calculate, but will abuse hte fact that every time I use this
		// it's with uniformly-sized objects
		return this.children.isEmpty() ? 0 : this.children.get(0).getHeight() + this.itemSpacing;
	}

}
