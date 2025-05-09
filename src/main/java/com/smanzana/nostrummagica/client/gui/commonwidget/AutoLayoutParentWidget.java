package com.smanzana.nostrummagica.client.gui.commonwidget;

import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;

public abstract class AutoLayoutParentWidget<W extends AutoLayoutParentWidget<W, T>, T extends ObscurableChildWidget<?>> extends ParentWidget<W, T> implements ScrollbarWidget.IScrollbarListener {

	protected @Nullable ScrollbarWidget scrollbar;
	protected float curScroll;
	protected int itemSpacing;
	protected int itemMargin;
	
	protected boolean autoSizeChildren;
	
	public AutoLayoutParentWidget(int x, int y, int width, int height, Component title) {
		super(x, y, width, height, title);
	}
	
	public AutoLayoutParentWidget<W, T> setScrollbar(ScrollbarWidget scrollbar) {
		this.scrollbar = scrollbar;
		this.updateScrollbar();
		return self();
	}
	
	public AutoLayoutParentWidget<W, T> setSpacing(int pixels) {
		this.itemSpacing = pixels;
		this.resetLayout();
		return self();
	}
	
	public AutoLayoutParentWidget<W, T> setMargin(int pixels) {
		this.itemMargin = pixels;
		this.resetLayout();
		return self();
	}
	
	/**
	 * When true, automatically set child widget width to fill the horizontal length of the list.
	 * Height is not adjusted to allow each component to specify their own size.
	 * @return
	 */
	public AutoLayoutParentWidget<W, T> setAutoSizeChildren() {
		this.autoSizeChildren = true;
		this.resetLayout();
		return self();
	}
	
	@Override
	public void addChild(T child) {
		super.addChild(child);
		handleCountChange(); // May shift around children if scroll changes
		setupChildLayout(child, this.children.size() - 1); // optimization -- don't need to reset other children
	}
	
	@Override
	public void clearChildren() {
		super.clearChildren();
		resetLayout();
	}

	@Override
	public void addChildren(Collection<T> children) {
		super.addChildren(children);
		resetLayout();
	}
	
	public void recalculateLayout() {
		this.resetLayout();
	}

	@Override
	public void handleScroll(float scroll) {
		this.curScroll = scroll;
		setupLayout();
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double dx) {
		if (super.mouseScrolled(mouseX, mouseY, dx)) { // child consumed
			return true;
		}
		if (!active) {
			return false;
		}
		
		if (this.scrollbar != null) {
			this.scrollbar.mouseScrolled(mouseX, mouseY, dx);
		}
		
		return false;
	}
	
	@Override
	protected Rect2i getBounds() {
		return new Rect2i(x + itemMargin, y + itemMargin, this.width - (itemMargin * 2), this.height - (itemMargin * 2));
	}
	
	@Override
	protected void updateChildPosition(T child, Rect2i bounds) {
		// parent snaps them to our position, but we want to insert offsets
		final int idx = this.children.indexOf(child);
		if (idx >= 0) {
			this.setupChildLayout(child, idx);
		}
	}
	
	protected abstract Vec3i getChildOffset(T child, int index, int itemSpacing);
	
	protected abstract void autoSizeChild(T child, int effectiveWidth, int effectiveHeight);
	
	protected void setupChildLayout(T child, int index) {
		// Ultimately, calculate where the child should be and set their position.
		final Rect2i bounds = this.getBounds();
		final int scrollPixels = getScrollPixels(curScroll); // how many pixels to offset
		final Vec3i offset = getChildOffset(child, index, this.itemSpacing);
		
		child.setBounds(bounds);
		child.snapToParent(x + itemMargin + offset.getX(), y + itemMargin + offset.getY() - scrollPixels);
		if (autoSizeChildren) {
			this.autoSizeChild(child, this.width - (itemMargin * 2), this.height - (itemMargin * 2));
		}
	}
	
	protected void setupLayout() {
		for (int i = 0; i < children.size(); i++) {
			setupChildLayout(children.get(i), i);
		}
	}
	
	/**
	 * Recalculate the layout of the list and all children in it.
	 * Useful when lots changes all at once, and it's easier to recalculate everything
	 * then to try and incrementally inform the list about a change
	 */
	protected void resetLayout() {
		handleCountChange();
		setupLayout();
	}
	
	/**
	 * Called when the number of items in the list has changed.
	 * Scrolling, for example, has to be adjusted when this happens
	 */
	protected void handleCountChange() {
		updateScrollbar();
	}
	
	protected void updateScrollbar() {
		if (this.scrollbar != null) {
			// Make sure scrollbar has good interval values for us
			final int scrollAmt = getScrollPixelsTotal();
			if (scrollAmt <= 0) {
				scrollbar.setEnabled(false);
			} else {
				scrollbar.setEnabled(true);
				
				// Set rate to match an average interval
				final float avgScroll = (float) getAverageChildLength() / (float) scrollAmt;
				scrollbar.setScrollRate(avgScroll);
			}
		}
	}
	
	protected final int getScrollPixels(float scrollProg) {
		return Math.round(getScrollPixelsTotal() * scrollProg);
	}
	
	/**
	 * Get how many pixels we can't render because of our dimensions. This should be how much extra height or width we need.
	 * @return
	 */
	protected abstract int getScrollPixelsTotal();
	
	/**
	 * Return the average size of a child in the 'scrolling' dimension. AKA average height for vertical lists.
	 * @return
	 */
	protected abstract int getAverageChildLength();
}
