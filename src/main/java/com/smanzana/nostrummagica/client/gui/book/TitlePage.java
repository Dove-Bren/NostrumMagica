package com.smanzana.nostrummagica.client.gui.book;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;

/**
 * Plain text page, except the text is centered and put down a bit
 * and can be linked to via a table of contents
 * @author Skyler
 *
 */
public class TitlePage implements IBookPage {
	
	private String text;
	private boolean index;
	private int hoffset = -1;
	
	public TitlePage(String text, boolean index) {
		this.text = text;
		this.index = index;
	}

	@Override
	public void draw(BookScreen parent, PoseStack matrixStackIn, Font fonter, int xoffset, int yoffset, int width, int height) {
			if (hoffset == -1) {
				int w = fonter.width(this.text);
				hoffset = w/2; 
			}
			fonter.draw(matrixStackIn, text, xoffset + ( (width / 2) - hoffset ), yoffset + 20, 0xFF106020);
	}

	@Override
	public void overlay(BookScreen parent, PoseStack matrixStackIn, Font fonter, int mouseX, int mouseY, int trueX, int trueY) {
		;
	}
	
	public String getTitle() {
		return text;
	}
	
	public boolean shouldIndex() {
		return index;
	}
	
}
