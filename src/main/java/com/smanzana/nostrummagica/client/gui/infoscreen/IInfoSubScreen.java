package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.Collection;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.Minecraft;

public interface IInfoSubScreen {

	public void draw(INostrumMagic attr, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY);
	
	public Collection<ISubScreenButton> getButtons();
	
}
