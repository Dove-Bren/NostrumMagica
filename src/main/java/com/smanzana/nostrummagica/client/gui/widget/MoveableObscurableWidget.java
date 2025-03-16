package com.smanzana.nostrummagica.client.gui.widget;

import net.minecraft.network.chat.Component;

public class MoveableObscurableWidget extends ObscurableWidget implements IMoveableWidget {

	protected final int startX;
	protected final int startY;
	
	public MoveableObscurableWidget(int x, int y, int width, int height, Component label) {
		super(x, y, width, height, label);
		this.startX = x;
		this.startY = y;
	}

	@Override
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void offset(int x, int y) {
		this.setPosition(this.x + x, this.y + y);
	}

	@Override
	public void resetPosition() {
		setPosition(getStartingX(), getStartingY());
	}

	@Override
	public int getStartingX() {
		return startX;
	}

	@Override
	public int getStartingY() {
		return startY;
	}

}
