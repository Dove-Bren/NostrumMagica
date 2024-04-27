package com.smanzana.nostrummagica.client.gui.book;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FontRenderer;

/**
 * Plain text page, except the text is centered and put down a bit
 * and can be linked to via a table of contents
 * @author Skyler
 *
 */
public class TitlePage implements IBookPage {
	
	private ImagePage image;
	private String text;
	private boolean index;
	private int hoffset = -1;
	
	public TitlePage(String text, boolean index) {
		this(text, null, index);
	}
	
	public TitlePage(String text, ImagePage image, boolean index) {
		this.text = text;
		this.index = index;
		this.image = image;
	}

	@Override
	public void draw(BookScreen parent, MatrixStack matrixStackIn, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {
		if (image == null) {
			if (hoffset == -1) {
				int w = fonter.getStringWidth(this.text);
				hoffset = w/2; 
			}
			fonter.drawString(matrixStackIn, text, xoffset + ( (width / 2) - hoffset ), yoffset + 20, 0xFF106020);
		} else {
			image.draw(parent, matrixStackIn, fonter, xoffset, yoffset, width, height);
		}
	}

	@Override
	public void overlay(BookScreen parent, MatrixStack matrixStackIn, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
		if (image != null)
			image.overlay(parent, matrixStackIn, fonter, mouseX, mouseY, trueX, trueY);
	}
	
	public String getTitle() {
		return text;
	}
	
	public boolean shouldIndex() {
		return index;
	}
	
}
