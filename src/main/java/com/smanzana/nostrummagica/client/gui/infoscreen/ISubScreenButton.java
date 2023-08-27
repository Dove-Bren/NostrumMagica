package com.smanzana.nostrummagica.client.gui.infoscreen;

import net.minecraft.client.gui.widget.button.AbstractButton;

public abstract class ISubScreenButton extends AbstractButton {

	protected final InfoScreen screen;
	
	public ISubScreenButton(InfoScreen screen, int x, int y) {
		super(x, y, 200, 20, "");
		this.screen = screen;
	}

	public abstract void onPress();
	
}
