package com.smanzana.nostrummagica.client.gui.book;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.FontRenderer;

public class LinedTextPage implements IBookPage {
	
	private List<String> text;
	
	public LinedTextPage(List<String> text) {
		this.text = text;
	}
	
	public LinedTextPage(String ... text) {
		this.text = Lists.newArrayList(text);
	}

	@Override
	public void draw(BookScreen parent, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {
		for (String line : text) {
			fonter.drawString(line, xoffset, yoffset, 0x000000);
			yoffset += fonter.FONT_HEIGHT + 3;
		}
	}

	@Override
	public void overlay(BookScreen parent, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
		; //nothing to do
	}
	
}
