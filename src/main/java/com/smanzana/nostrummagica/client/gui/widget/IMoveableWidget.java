package com.smanzana.nostrummagica.client.gui.widget;

public interface IMoveableWidget {

	public void setPosition(int x, int y);
	
	public void offset(int x, int y);
	
	public void resetPosition();
	
	public int getStartingX();
	
	public int getStartingY();
	
}
