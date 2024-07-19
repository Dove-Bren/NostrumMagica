package com.smanzana.nostrummagica.client.gui.infoscreen;

import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.text.StringTextComponent;

public abstract class ISubScreenButton extends AbstractButton {

	protected final InfoScreen screen;
	
	public ISubScreenButton(InfoScreen screen, int x, int y) {
		super(x, y, 200, 20, StringTextComponent.EMPTY);
		this.screen = screen;
	}

	public abstract void onPress();
	
}
