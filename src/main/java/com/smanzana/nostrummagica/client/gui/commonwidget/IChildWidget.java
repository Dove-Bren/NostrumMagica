package com.smanzana.nostrummagica.client.gui.commonwidget;

/**
 * Widget specifically parented to another widget, with its location being relative to its parent.
 */
public interface IChildWidget extends IMoveableWidget {
	
	public int getParentOffsetX();
	
	public int getParentOffsetY();
	
	public default void setAbsolutePosition(int x, int y) {
		this.setPosition(x, y);
	}
	
	public default void snapToParent(int parentX, int parentY) {
		this.setAbsolutePosition(getParentOffsetX() + parentX, getParentOffsetY() + parentY);
	}
	
	public default <T extends ParentWidget<?, ?>> void setParent(T parent) {
		;
	}

}
