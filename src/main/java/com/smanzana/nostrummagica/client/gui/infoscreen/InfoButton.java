package com.smanzana.nostrummagica.client.gui.infoscreen;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.gui.GuiButton;

public abstract class InfoButton extends GuiButton {

	protected static final int BUTTON_WIDTH = 18;
	
	public InfoButton(int buttonId, int x, int y) {
		super(buttonId, x, y, BUTTON_WIDTH, BUTTON_WIDTH, "");
	}

	public abstract IInfoSubScreen getScreen(INostrumMagic attr);
	
}
