package com.smanzana.nostrummagica.client.gui.book;

import net.minecraft.client.gui.FontRenderer;

public class PlainTextPage implements IBookPage {
	
	private String text;
	
	public PlainTextPage(String text) {
		this.text = text;
	}

	@Override
	public void draw(BookScreen parent, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {
		fonter.drawSplitString(text, xoffset, yoffset, width, 0x000000);
	}

	@Override
	public void overlay(BookScreen parent, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
		; //nothing to do
	}
	
}
