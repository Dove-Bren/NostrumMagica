package com.smanzana.nostrummagica.client.gui.book;

import net.minecraft.client.gui.FontRenderer;

public interface IBookPage {

	public void draw(BookScreen parent, FontRenderer fonter, int xoffset, int yoffset, int width, int height);

	public void overlay(BookScreen parent, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY);
	
}
