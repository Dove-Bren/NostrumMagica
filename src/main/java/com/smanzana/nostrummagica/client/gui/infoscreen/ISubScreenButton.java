package com.smanzana.nostrummagica.client.gui.infoscreen;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.gui.GuiButton;

public abstract class ISubScreenButton extends GuiButton {

	public ISubScreenButton(int buttonId, int x, int y) {
		super(buttonId, x, y, "");
	}

	public abstract void onClick(INostrumMagic attr);
	
}
