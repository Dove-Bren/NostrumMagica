package com.smanzana.nostrummagica.client.gui.commonwidget;

public interface IMoveableWidget {

	public void setPosition(int x, int y);
	
	public void offset(int x, int y);
	
	public default void offsetFromStart(int x, int y) {
		setPosition(getStartingX() + x, getStartingY() + y);
	}
	
	public void resetPosition();
	
	public int getStartingX();
	
	public int getStartingY();
	
}
