package com.smanzana.nostrummagica.client.gui.widget;

import net.minecraft.network.chat.Component;

public class ObscurableChildWidget extends MoveableObscurableWidget implements IChildWidget {

	protected int parentOffsetX;
	protected int parentOffsetY;
	
	public ObscurableChildWidget(int x, int y, int width, int height, Component label) {
		super(x, y, width, height, label);
		this.setParentOffset(x, y);
	}
	
	public ObscurableChildWidget setParentOffset(int x, int y) {
		this.parentOffsetX = x;
		this.parentOffsetY = y;
		return this;
	}

	@Override
	public int getParentOffsetX() {
		return parentOffsetX;
	}

	@Override
	public int getParentOffsetY() {
		return parentOffsetY;
	}

}
